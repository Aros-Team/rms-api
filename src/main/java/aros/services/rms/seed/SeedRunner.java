/* (C) 2026 */

package aros.services.rms.seed;

import aros.services.rms.core.image.domain.ImageSize;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** Writes a deterministic seed dataset into an existing, migrated schema. */
final class SeedRunner {

  private static final int BATCH_SIZE = 2_000;
  private static final String LOCK_NAME = "rms_seed";

  private static final Color[] IMAGE_PALETTE = {
    new Color(196, 96, 58),
    new Color(58, 122, 104),
    new Color(180, 132, 42),
    new Color(112, 84, 150),
    new Color(52, 104, 148),
    new Color(168, 70, 90),
  };

  private final Connection connection;
  private final LocalDate anchor;
  private final LocalDateTime now;
  private final Path uploadsDir;

  private final Map<Integer, Long> tableIds = new LinkedHashMap<>();
  private final Map<Integer, Integer> productAreas = new LinkedHashMap<>();
  private final Map<Integer, List<RecipeLine>> productRecipes = new LinkedHashMap<>();
  private final Map<Integer, List<RecipeLine>> optionRecipes = new LinkedHashMap<>();
  private final Map<Integer, List<OptionSpec>> productOptions = new LinkedHashMap<>();
  private final Map<Integer, Integer> optionGroupByOption = new LinkedHashMap<>();
  private final Map<Integer, List<AdditionSpec>> comboAdditions = new LinkedHashMap<>();
  private final Map<Integer, List<QuestionSpec>> comboQuestions = new LinkedHashMap<>();
  private final Map<Long, BigDecimal> variantUnitCost = new LinkedHashMap<>();
  private final Map<Long, Long> variantSupply = new LinkedHashMap<>();
  private final Map<Long, Integer> supplyCategory = new LinkedHashMap<>();
  private final Map<Long, List<Long>> supplierVariants = new LinkedHashMap<>();
  private int bodegaLocation;
  private int cocinaLocation;
  private int cuartoFrioLocation;
  private int barLocation;

  SeedRunner(Connection connection, LocalDate anchor, LocalDateTime now, Path uploadsDir) {
    this.connection = connection;
    this.anchor = anchor;
    this.now = now;
    this.uploadsDir = uploadsDir;
  }

  SeedRunResult reset(boolean runCatalog) {
    try {
      connection.setAutoCommit(false);
      try (Statement statement = connection.createStatement()) {
        statement.execute("SET time_zone = 'America/Bogota'");
        // Bulk load is controlled and internally consistent; FK checks are
        // restored before commit and are far too slow row-by-row for ~375k rows.
        statement.execute("SET foreign_key_checks = 0");
        statement.execute("SET unique_checks = 0");
      }
      if (!acquireLock()) {
        throw new SeedException("Ya hay otra carga de datos en ejecución (GET_LOCK rms_seed)");
      }
      try {
        if (runCatalog) {
          runCatalog();
        }
        deleteSeedOwnedRows();
        loadReferenceData();
        seedProductImages();
        upsertUsers();
        seedWorkerImages();
        seedSuppliers();
        resetFloor();
        resetOpeningHours();
        seedSchedulesAndTimeLogs();
        SeedPlan plan = SeedPlan.complete(anchor, now);
        seedOrdersAndInventory(plan);
        seedPayroll(plan);
        seedSalaryHistory();
        seedSpecialSelectionHistory();
        new SeedSummaries(connection, plan, anchor).write();
        seedAlerts();
        seedCashAndPayments(plan);
        writeSeedMetadata(plan);
        try (Statement statement = connection.createStatement()) {
          statement.execute("SET foreign_key_checks = 1");
          statement.execute("SET unique_checks = 1");
        }
        connection.commit();
        return new SeedRunResult(
            anchor,
            plan.historicalOrdersCount(),
            plan.historicalLinesCount(),
            plan.historicalRevenue(),
            plan.historicalCovers(),
            plan.operationalOrders().size());
      } catch (Exception exception) {
        connection.rollback();
        throw exception;
      } finally {
        try (Statement statement = connection.createStatement()) {
          statement.execute("SET foreign_key_checks = 1");
          statement.execute("SET unique_checks = 1");
        } catch (SQLException ignored) {
          // The connection is closed after reset anyway.
        }
        releaseLock();
      }
    } catch (SQLException exception) {
      throw new SeedException("Falló el reinicio de datos: " + exception.getMessage(), exception);
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException ignored) {
        // Closing the connection is the caller's responsibility.
      }
    }
  }

  private void runCatalog() throws SQLException {
    ScriptUtils.executeSqlScript(
        connection,
        new org.springframework.core.io.support.EncodedResource(
            new ClassPathResource("db/catalog/catalog.sql"), StandardCharsets.UTF_8));
  }

  private boolean acquireLock() throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("SELECT GET_LOCK(?, 60)")) {
      statement.setString(1, LOCK_NAME);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt(1) == 1;
      }
    }
  }

  private void releaseLock() {
    try (PreparedStatement statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
      statement.setString(1, LOCK_NAME);
      statement.executeQuery().close();
    } catch (SQLException ignored) {
      // The lock is released when the session closes anyway.
    }
  }

  private void deleteSeedOwnedRows() throws SQLException {
    for (String table :
        List.of(
            "order_detail_clarifications",
            "order_detail_additions",
            "order_detail_options",
            "order_details",
            "order_preparation_areas",
            "inventory_movements",
            "inventory_stock",
            "time_logs",
            "payroll_events",
            "payroll_settlements",
            "payroll",
            "salary_history",
            "worker_schedule_assignments",
            "schedule_shifts",
            "schedules",
            "monthly_financial_summary",
            "menu_performance_cache",
            "purchase_order_items",
            "purchase_orders",
            "special_selection_history",
            "system_alerts",
            "user_notifications",
            // Payments reference orders and cash_shifts; children first.
            "payment_tenders",
            "payments",
            "cash_movements",
            "cash_shifts",
            "orders")) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("DELETE FROM " + table);
      } catch (SQLException exception) {
        if (!"42S02".equals(exception.getSQLState())) {
          throw exception;
        }
      }
    }
    try (Statement statement = connection.createStatement()) {
      // The seed owns the worker roster: remove every WORKER user (QA included) so a reset
      // ends with exactly the seven people defined in SeedConstants.WORKERS. Admins stay.
      deleteWorkerAccounts(statement);
      // Suppliers are upserted by their fixed seed ids; anything above the seed range is demo
      // data created after the previous reset and would break the golden supplier count.
      statement.executeUpdate("DELETE FROM suppliers WHERE id > " + SeedConstants.SUPPLIERS.size());
    }
  }

  /** Deletes every worker account and the rows that reference it, plus its uploaded images. */
  private void deleteWorkerAccounts(Statement statement) throws SQLException {
    List<Long> workerIds = new ArrayList<>();
    try (ResultSet rows = statement.executeQuery("SELECT id FROM users WHERE role = 'WORKER'")) {
      while (rows.next()) {
        workerIds.add(rows.getLong(1));
      }
    }
    String workerFilter = " WHERE user_id IN (SELECT id FROM users WHERE role = 'WORKER')";
    statement.executeUpdate(
        "DELETE FROM entity_images WHERE entity_type = 'USER' AND entity_id IN "
            + "(SELECT id FROM users WHERE role = 'WORKER')");
    statement.executeUpdate("DELETE FROM user_assigned_areas" + workerFilter);
    statement.executeUpdate("DELETE FROM account_setup_tokens" + workerFilter);
    statement.executeUpdate("DELETE FROM password_reset_tokens" + workerFilter);
    statement.executeUpdate("DELETE FROM users WHERE role = 'WORKER'");
    for (Long workerId : workerIds) {
      deleteDirectory(uploadsDir.resolve("users").resolve(Long.toString(workerId)));
    }
  }

  private void deleteDirectory(Path directory) {
    if (!Files.exists(directory)) {
      return;
    }
    try (var paths = Files.walk(directory)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException ignored) {
                  // A leftover file is harmless; the next seed rewrites the variants.
                }
              });
    } catch (IOException ignored) {
      // Same rationale as above.
    }
  }

  /**
   * Ensures every active product has a usable image.
   *
   * <p>The catalogue seeds names and prices, not media. Without this step a fresh database shows
   * the frontend placeholder for the whole card. Bundled seed photos are used when present,
   * otherwise a deterministic gradient card is rendered, always to the three WebP sizes the API
   * serves. A real upload always wins, so an existing image is never replaced.
   */
  private void seedProductImages() throws SQLException {
    try {
      Files.createDirectories(uploadsDir);
    } catch (IOException exception) {
      throw new SeedException(
          "No se pudo crear el directorio de imágenes: " + uploadsDir, exception);
    }

    String existingSql =
        "SELECT id, storage_key, original_filename FROM entity_images "
            + "WHERE entity_type = 'PRODUCT' AND entity_id = ? ORDER BY id LIMIT 1";
    String insertSql =
        "INSERT INTO entity_images "
            + "(entity_type, entity_id, original_filename, content_type, original_size_bytes, "
            + "storage_key) VALUES ('PRODUCT', ?, ?, ?, ?, ?)";
    String updateSql =
        "UPDATE entity_images SET original_filename = ?, content_type = ?, "
            + "original_size_bytes = ? WHERE id = ?";
    String productSql =
        "SELECT p.id, p.name, c.name AS category_name FROM products p "
            + "LEFT JOIN categories c ON c.id = p.category_id "
            + "WHERE p.active = TRUE ORDER BY p.id";

    try (PreparedStatement existing = connection.prepareStatement(existingSql);
        PreparedStatement insert = connection.prepareStatement(insertSql);
        PreparedStatement update = connection.prepareStatement(updateSql);
        Statement products = connection.createStatement();
        ResultSet rows = products.executeQuery(productSql)) {
      while (rows.next()) {
        long productId = rows.getLong("id");
        String name = rows.getString("name");
        String category = rows.getString("category_name");

        ImageRow row = findImageRow(existing, productId);
        ClassPathResource photo = photoResource(productId);
        boolean hasPhoto = photo.exists();
        boolean seedManaged =
            row != null
                && (row.originalFilename().startsWith("seed-")
                    || (hasPhoto && row.originalFilename().equals(photo.getFilename())));

        if (row != null && !(seedManaged && hasPhoto) && imageFilesExist(row.storageKey())) {
          continue;
        }

        String storageKey = row != null ? row.storageKey() : "products/" + productId + "/seed";
        byte[] photoData = hasPhoto ? readResource(photo) : null;
        long desktopSize = writeProductImages(storageKey, productId, name, category, photoData);
        String filename =
            hasPhoto ? "seed-photo-" + productId + ".jpg" : "seed-" + productId + ".webp";
        String contentType = hasPhoto ? "image/jpeg" : "image/webp";

        if (row == null) {
          insert.setLong(1, productId);
          insert.setString(2, filename);
          insert.setString(3, contentType);
          insert.setLong(4, desktopSize);
          insert.setString(5, storageKey);
          insert.executeUpdate();
        } else if (!filename.equals(row.originalFilename())) {
          update.setString(1, filename);
          update.setString(2, contentType);
          update.setLong(3, desktopSize);
          update.setLong(4, row.id());
          update.executeUpdate();
        }
      }
    }
  }

  /**
   * Attaches the bundled portrait to every seeded worker.
   *
   * <p>The source photos live in {@code seed/worker-images/<id>.jpg} and are written as the same
   * three WebP sizes the upload endpoint produces. A real upload is never replaced.
   */
  private void seedWorkerImages() throws SQLException {
    String existingSql =
        "SELECT id, storage_key, original_filename FROM entity_images "
            + "WHERE entity_type = 'USER' AND entity_id = ? ORDER BY id LIMIT 1";
    String insertSql =
        "INSERT INTO entity_images "
            + "(entity_type, entity_id, original_filename, content_type, original_size_bytes, "
            + "storage_key) VALUES ('USER', ?, ?, ?, ?, ?)";
    String updateSql =
        "UPDATE entity_images SET original_filename = ?, content_type = ?, "
            + "original_size_bytes = ? WHERE id = ?";

    try (PreparedStatement existing = connection.prepareStatement(existingSql);
        PreparedStatement insert = connection.prepareStatement(insertSql);
        PreparedStatement update = connection.prepareStatement(updateSql)) {
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        ClassPathResource photo = workerPhotoResource(worker.id());
        if (!photo.exists()) {
          continue;
        }
        ImageRow row = findImageRow(existing, worker.id());
        boolean seedManaged = row != null && row.originalFilename().startsWith("seed-worker-");
        if (row != null && !seedManaged && imageFilesExist(row.storageKey())) {
          continue;
        }

        String storageKey = row != null ? row.storageKey() : "users/" + worker.id() + "/seed";
        long desktopSize = writeVersions(storageKey, loadImage(readResource(photo)));
        String filename = "seed-worker-" + worker.id() + ".jpg";

        if (row == null) {
          insert.setLong(1, worker.id());
          insert.setString(2, filename);
          insert.setString(3, "image/jpeg");
          insert.setLong(4, desktopSize);
          insert.setString(5, storageKey);
          insert.executeUpdate();
        } else {
          update.setString(1, filename);
          update.setString(2, "image/jpeg");
          update.setLong(3, desktopSize);
          update.setLong(4, row.id());
          update.executeUpdate();
        }
      }
    }
  }

  /** First image row for a product or worker, or {@code null} when it has no image. */
  private ImageRow findImageRow(PreparedStatement query, long entityId) throws SQLException {
    query.setLong(1, entityId);
    try (ResultSet row = query.executeQuery()) {
      if (!row.next()) {
        return null;
      }
      return new ImageRow(
          row.getLong("id"), row.getString("storage_key"), row.getString("original_filename"));
    }
  }

  private static ClassPathResource photoResource(long productId) {
    return new ClassPathResource("seed/product-images/" + productId + ".jpg");
  }

  private static ClassPathResource workerPhotoResource(long workerId) {
    return new ClassPathResource("seed/worker-images/" + workerId + ".jpg");
  }

  private static byte[] readResource(ClassPathResource resource) {
    try (var stream = resource.getInputStream()) {
      return stream.readAllBytes();
    } catch (IOException exception) {
      throw new SeedException(
          "No se pudo leer la imagen de producto incluida: " + resource.getPath(), exception);
    }
  }

  /** Whether all three size variants exist on disk for a storage key. */
  private boolean imageFilesExist(String storageKey) {
    for (ImageSize size : ImageSize.values()) {
      if (!Files.exists(versionPath(storageKey, size))) {
        return false;
      }
    }
    return true;
  }

  /** Stores the mobile, tablet and desktop WebP versions of a product image. */
  private long writeProductImages(
      String storageKey, long productId, String name, String category, byte[] photo) {
    ImmutableImage image =
        photo == null
            ? ImmutableImage.fromAwt(renderProductCard(productId, name, category))
            : loadImage(photo);
    return writeVersions(storageKey, image);
  }

  private static ImmutableImage loadImage(byte[] data) {
    try {
      return ImmutableImage.loader().fromBytes(data);
    } catch (IOException exception) {
      throw new SeedException("No se pudo leer la imagen: " + exception.getMessage(), exception);
    }
  }

  /** Stores the mobile, tablet and desktop WebP versions of an image. */
  private long writeVersions(String storageKey, ImmutableImage image) {
    try {
      for (ImageSize size : ImageSize.values()) {
        ImmutableImage resized = image.scaleToWidth(Math.min(size.maxWidth, image.width));
        byte[] bytes = resized.bytes(WebpWriter.DEFAULT.withQ(size.quality));
        Path path = versionPath(storageKey, size);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
      }
      return Files.size(versionPath(storageKey, ImageSize.DESKTOP));
    } catch (IOException exception) {
      throw new SeedException(
          "No se pudo escribir la imagen " + storageKey + ": " + exception.getMessage(), exception);
    }
  }

  private record ImageRow(long id, String storageKey, String originalFilename) {}

  private Path versionPath(String storageKey, ImageSize size) {
    return uploadsDir.resolve(storageKey).resolve(size.name().toLowerCase(Locale.ROOT) + ".webp");
  }

  /** Draws a branded placeholder card so seeded products never look empty on the menu. */
  private static BufferedImage renderProductCard(long productId, String name, String category) {
    int width = 1200;
    int height = 800;
    Color base = IMAGE_PALETTE[(int) Math.floorMod(productId, IMAGE_PALETTE.length)];

    BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = canvas.createGraphics();
    try {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      graphics.setPaint(new GradientPaint(0, 0, base.brighter(), width, height, base.darker()));
      graphics.fillRect(0, 0, width, height);

      graphics.setColor(new Color(255, 255, 255, 26));
      graphics.fillOval(width - 320, -140, 520, 520);
      graphics.fillOval(-180, height - 260, 420, 420);

      graphics.setColor(new Color(255, 255, 255, 205));
      graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
      String eyebrow =
          category == null || category.isBlank() ? "CARTA" : category.toUpperCase(Locale.ROOT);
      graphics.drawString(eyebrow, 72, 104);

      graphics.setFont(new Font("SansSerif", Font.BOLD, 74));
      FontMetrics metrics = graphics.getFontMetrics();
      List<String> lines = wrapText(name, metrics, width - 144);
      int blockHeight = lines.size() * metrics.getHeight();
      int baseline = (height - blockHeight) / 2 + metrics.getAscent();

      for (String line : lines) {
        int x = (width - metrics.stringWidth(line)) / 2;
        graphics.setColor(new Color(0, 0, 0, 45));
        graphics.drawString(line, x + 3, baseline + 3);
        graphics.setColor(Color.WHITE);
        graphics.drawString(line, x, baseline);
        baseline += metrics.getHeight();
      }

      graphics.setColor(new Color(255, 255, 255, 150));
      graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
      graphics.drawString("RMS", 72, height - 64);
    } finally {
      graphics.dispose();
    }
    return canvas;
  }

  private static List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
    String[] words = text.trim().split("\\s+");
    List<String> lines = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    for (String word : words) {
      String candidate = current.isEmpty() ? word : current + " " + word;
      if (!current.isEmpty() && metrics.stringWidth(candidate) > maxWidth) {
        lines.add(current.toString());
        current = new StringBuilder(word);
      } else {
        current = new StringBuilder(candidate);
      }
    }
    if (!current.isEmpty()) {
      lines.add(current.toString());
    }
    return lines;
  }

  private void loadReferenceData() throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery("SELECT id, name FROM storage_locations")) {
      while (rows.next()) {
        String name = rows.getString("name");
        if ("Bodega".equalsIgnoreCase(name)) {
          bodegaLocation = rows.getInt("id");
        } else if ("Cocina".equalsIgnoreCase(name)) {
          cocinaLocation = rows.getInt("id");
        } else if (name.toLowerCase(java.util.Locale.ROOT).startsWith("cuarto")) {
          cuartoFrioLocation = rows.getInt("id");
        } else if ("Bar".equalsIgnoreCase(name)) {
          barLocation = rows.getInt("id");
        }
      }
    }
    if (bodegaLocation == 0 || cocinaLocation == 0 || cuartoFrioLocation == 0 || barLocation == 0) {
      throw new SeedException("storage_locations is missing Bodega/Cocina/Cuarto Frío/Bar");
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery("SELECT id, table_number FROM tables")) {
      while (rows.next()) {
        tableIds.put(rows.getInt("table_number"), rows.getLong("id"));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery("SELECT id, area_id FROM products WHERE active = TRUE")) {
      while (rows.next()) {
        productAreas.put(rows.getInt("id"), rows.getInt("area_id"));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT product_id, supply_variant_id, required_quantity FROM product_recipes")) {
      while (rows.next()) {
        productRecipes
            .computeIfAbsent(rows.getInt(1), ignored -> new ArrayList<>())
            .add(new RecipeLine(rows.getLong(2), rows.getBigDecimal(3)));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT option_id, supply_variant_id, required_quantity FROM option_recipes")) {
      while (rows.next()) {
        optionRecipes
            .computeIfAbsent(rows.getInt(1), ignored -> new ArrayList<>())
            .add(new RecipeLine(rows.getLong(2), rows.getBigDecimal(3)));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT ppo.product_id, ppo.option_id, ppo.extra_price, po.option_category_id "
                    + "FROM product_product_options ppo "
                    + "JOIN product_options po ON po.id = ppo.option_id")) {
      while (rows.next()) {
        productOptions
            .computeIfAbsent(rows.getInt(1), ignored -> new ArrayList<>())
            .add(new OptionSpec(rows.getInt(2), rows.getBigDecimal(3), rows.getInt(4)));
        optionGroupByOption.put(rows.getInt(2), rows.getInt(4));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT product_id, option_id, extra_price FROM special_selection_additions")) {
      while (rows.next()) {
        comboAdditions
            .computeIfAbsent(rows.getInt(1), ignored -> new ArrayList<>())
            .add(new AdditionSpec(rows.getInt(2), rows.getBigDecimal(3)));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT id, product_id FROM special_selection_questions ORDER BY display_order")) {
      while (rows.next()) {
        comboQuestions
            .computeIfAbsent(rows.getInt(2), ignored -> new ArrayList<>())
            .add(new QuestionSpec(rows.getInt(1)));
      }
    }
    try (Statement statement = connection.createStatement();
        ResultSet rows =
            statement.executeQuery(
                "SELECT v.id, v.unit_cost, v.supply_id, s.supply_category_id "
                    + "FROM supply_variants v JOIN supplies s ON s.id = v.supply_id")) {
      while (rows.next()) {
        variantUnitCost.put(rows.getLong(1), rows.getBigDecimal(2));
        variantSupply.put(rows.getLong(1), rows.getLong(3));
        supplyCategory.put(rows.getLong(3), rows.getInt(4));
      }
    }
    buildSupplierVariants();
  }

  /** Resolves the sorted variant list each supplier delivers, from the catalogue categories. */
  private void buildSupplierVariants() {
    for (SeedConstants.SupplierSpec supplier : SeedConstants.SUPPLIERS) {
      List<Long> variants = new ArrayList<>();
      for (Long variant : variantUnitCost.keySet()) {
        Long supply = variantSupply.get(variant);
        if (supply == null || supplier.excludedSupplyIds().contains(supply)) {
          continue;
        }
        Integer category = supplyCategory.get(supply);
        if (supplier.supplyIds().contains(supply)
            || (category != null && supplier.categoryIds().contains(category))) {
          variants.add(variant);
        }
      }
      variants.sort(Long::compareTo);
      supplierVariants.put(supplier.id(), List.copyOf(variants));
    }
  }

  private void upsertUsers() throws SQLException {
    String sql =
        "INSERT INTO users (id, document, name, email, password, address, phone, job_title, "
            + "role, status, salary, expected_hours_per_month) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) "
            + "ON DUPLICATE KEY UPDATE document=VALUES(document), name=VALUES(name), "
            + "email=VALUES(email), address=VALUES(address), phone=VALUES(phone), "
            + "job_title=VALUES(job_title), role=VALUES(role), status=VALUES(status), "
            + "salary=VALUES(salary), expected_hours_per_month=VALUES(expected_hours_per_month)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        bindUser(
            statement,
            worker.id(),
            worker.name(),
            worker.salary(),
            worker.expectedHoursPerMonth(),
            "WORKER",
            SeedConstants.seedDocument(worker.id()),
            "seed" + worker.id() + "@rms.local",
            worker.jobTitle());
        statement.addBatch();
      }
      bindUser(
          statement,
          1007,
          "Administrador 1",
          null,
          160,
          "ADMIN",
          SeedConstants.seedDocument(1007),
          "admin1@rms.local",
          null);
      statement.addBatch();
      bindUser(
          statement,
          1008,
          "Administrador 2",
          null,
          160,
          "ADMIN",
          SeedConstants.seedDocument(1008),
          "admin2@rms.local",
          null);
      statement.addBatch();
      statement.executeBatch();
    }
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT IGNORE INTO user_assigned_areas (user_id, area_id) VALUES (?,?)")) {
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        for (Integer areaId : worker.areaIds()) {
          statement.setLong(1, worker.id());
          statement.setInt(2, areaId);
          statement.addBatch();
        }
      }
      statement.executeBatch();
    }
  }

  private void seedSuppliers() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO suppliers (id, name, contact, active) VALUES (?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE name=VALUES(name), contact=VALUES(contact), "
                + "active=VALUES(active)")) {
      for (SeedConstants.SupplierSpec supplier : SeedConstants.SUPPLIERS) {
        statement.setLong(1, supplier.id());
        statement.setString(2, supplier.name());
        statement.setString(3, supplier.contact());
        statement.setBoolean(4, supplier.active());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private void bindUser(
      PreparedStatement statement,
      long id,
      String name,
      Integer salary,
      int expectedHours,
      String role,
      String document,
      String email,
      String jobTitle)
      throws SQLException {
    statement.setLong(1, id);
    statement.setString(2, document);
    statement.setString(3, name);
    statement.setString(4, email);
    statement.setNull(5, Types.VARCHAR);
    statement.setString(6, "Calle 10 # 20-" + (id % 100));
    statement.setString(7, String.format(Locale.ROOT, "300%07d", id));
    if (jobTitle == null) {
      statement.setNull(8, Types.VARCHAR);
    } else {
      statement.setString(8, jobTitle);
    }
    statement.setString(9, role);
    statement.setString(10, "ACTIVE");
    if (salary == null) {
      statement.setNull(11, Types.DECIMAL);
    } else {
      statement.setBigDecimal(11, BigDecimal.valueOf(salary));
    }
    statement.setInt(12, expectedHours);
  }

  private void resetFloor() throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("UPDATE tables SET status = 'AVAILABLE'");
      statement.executeUpdate("UPDATE areas SET exclusive = (type = 'KITCHEN')");
    }
  }

  /** Restores the business hours the deterministic schedule is built against. */
  private void resetOpeningHours() throws SQLException {
    String sql =
        "INSERT INTO opening_hours (day_of_week, open_time, close_time, is_closed) "
            + "VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE open_time=VALUES(open_time), "
            + "close_time=VALUES(close_time), is_closed=VALUES(is_closed)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      bindOpeningDay(statement, "MONDAY", null, null, true);
      bindOpeningDay(statement, "TUESDAY", "08:00:00", "22:00:00", false);
      bindOpeningDay(statement, "WEDNESDAY", "08:00:00", "22:00:00", false);
      bindOpeningDay(statement, "THURSDAY", "08:00:00", "22:00:00", false);
      bindOpeningDay(statement, "FRIDAY", "08:00:00", "22:00:00", false);
      bindOpeningDay(statement, "SATURDAY", "08:00:00", "17:00:00", false);
      bindOpeningDay(statement, "SUNDAY", "08:00:00", "17:00:00", false);
      statement.executeBatch();
    }
  }

  private void bindOpeningDay(
      PreparedStatement statement, String day, String open, String close, boolean closed)
      throws SQLException {
    statement.setString(1, day);
    if (open == null) {
      statement.setNull(2, Types.TIME);
      statement.setNull(3, Types.TIME);
    } else {
      statement.setString(2, open);
      statement.setString(3, close);
    }
    statement.setBoolean(4, closed);
    statement.addBatch();
  }

  private void seedSchedulesAndTimeLogs() throws SQLException {
    Map<Long, Map<java.time.DayOfWeek, Long>> shiftIds = new LinkedHashMap<>();
    try (PreparedStatement schedule =
            connection.prepareStatement(
                "INSERT INTO schedules (id, name, description) VALUES (?, ?, ?)");
        PreparedStatement shift =
            connection.prepareStatement(
                "INSERT INTO schedule_shifts (id, schedule_id, day_of_week, start_time, end_time) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
      long shiftId = 1;
      long scheduleId = 1;
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        schedule.setLong(1, scheduleId);
        schedule.setString(2, "Turno de " + worker.name());
        schedule.setString(3, "Turno semanal real del dataset determinista");
        schedule.addBatch();

        Map<java.time.DayOfWeek, Long> byDay = new LinkedHashMap<>();
        for (SeedConstants.ShiftSpec spec : worker.shifts()) {
          shift.setLong(1, shiftId);
          shift.setLong(2, scheduleId);
          shift.setString(3, spec.day().name());
          shift.setTime(4, java.sql.Time.valueOf(spec.start()));
          shift.setTime(5, java.sql.Time.valueOf(spec.start().plusHours(spec.hours())));
          shift.addBatch();
          byDay.put(spec.day(), shiftId);
          shiftId++;
        }
        shiftIds.put(worker.id(), byDay);
        scheduleId++;
      }
      schedule.executeBatch();
      shift.executeBatch();
    }
    try (PreparedStatement assignment =
        connection.prepareStatement(
            "INSERT INTO worker_schedule_assignments (worker_id, schedule_id) VALUES (?, ?)")) {
      for (int index = 0; index < SeedConstants.WORKERS.size(); index++) {
        assignment.setLong(1, SeedConstants.WORKERS.get(index).id());
        assignment.setInt(2, index + 1);
        assignment.addBatch();
      }
      assignment.executeBatch();
    }

    // Attendance follows each worker's real weekly pattern: one IN/OUT pair per scheduled day,
    // linked to the schedule shift that covers it.
    String insertLog =
        "INSERT INTO time_logs (worker_id, timestamp, type, within_shift, related_shift_id) "
            + "VALUES (?,?,?,?,?)";
    try (PreparedStatement log = connection.prepareStatement(insertLog)) {
      int pending = 0;
      LocalDate start = anchor.minusDays(SeedConstants.HISTORY_DAYS);
      for (LocalDate day = start; day.isBefore(anchor); day = day.plusDays(1)) {
        for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
          SeedConstants.ShiftSpec spec = worker.shiftOn(day.getDayOfWeek());
          if (spec == null) {
            continue;
          }
          Long relatedShiftId = shiftIds.get(worker.id()).get(day.getDayOfWeek());
          LocalDateTime in = day.atTime(spec.start());
          log.setLong(1, worker.id());
          log.setString(2, SeedMath.timestamp(in));
          log.setString(3, "IN");
          log.setBoolean(4, true);
          log.setLong(5, relatedShiftId);
          log.addBatch();
          log.setString(2, SeedMath.timestamp(in.plusHours(spec.hours())));
          log.setString(3, "OUT");
          log.setBoolean(4, true);
          log.setLong(5, relatedShiftId);
          log.addBatch();
          pending += 2;
          if (pending >= BATCH_SIZE) {
            log.executeBatch();
            pending = 0;
          }
        }
      }

      // Today's attendance up to the seed instant: a finished shift gets IN/OUT, an in-progress
      // shift only IN (an open session), and a shift that has not started gets nothing.
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        SeedConstants.ShiftSpec spec = worker.shiftOn(anchor.getDayOfWeek());
        if (spec == null) {
          continue;
        }
        LocalDateTime in = anchor.atTime(spec.start());
        if (now.isBefore(in)) {
          continue;
        }
        Long relatedShiftId = shiftIds.get(worker.id()).get(anchor.getDayOfWeek());
        log.setLong(1, worker.id());
        log.setString(2, SeedMath.timestamp(in));
        log.setString(3, "IN");
        log.setBoolean(4, true);
        log.setLong(5, relatedShiftId);
        log.addBatch();
        pending++;
        if (!now.isBefore(in.plusHours(spec.hours()))) {
          log.setString(2, SeedMath.timestamp(in.plusHours(spec.hours())));
          log.setString(3, "OUT");
          log.setBoolean(4, true);
          log.setLong(5, relatedShiftId);
          log.addBatch();
          pending++;
        }
        if (pending >= BATCH_SIZE) {
          log.executeBatch();
          pending = 0;
        }
      }
      log.executeBatch();
    }
  }

  private void seedOrdersAndInventory(SeedPlan plan) throws SQLException {
    OrderWriter writer = new OrderWriter();
    writer.writeAll(plan);
    InventoryWriter inventory = new InventoryWriter(writer.consumption());
    inventory.write();
  }

  private void seedPayroll(SeedPlan plan) throws SQLException {
    YearMonth anchorMonth = YearMonth.from(anchor);
    String payrollSql =
        "INSERT INTO payroll (user_id, period_year, period_month, period_start, period_end, "
            + "base_salary, bonuses, manual_bonuses, deductions, manual_deductions, net_amount, "
            + "paid_amount, hours_worked, status, notes, registered_by) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String eventSql =
        "INSERT INTO payroll_events (user_id, event_date, event_type, quantity, unit_rate, amount, "
            + "notes, created_by) VALUES (?,?,?,?,?,?,?,?)";
    try (PreparedStatement payroll = connection.prepareStatement(payrollSql);
        PreparedStatement event = connection.prepareStatement(eventSql)) {
      for (int back = 5; back >= 0; back--) {
        YearMonth month = anchorMonth.minusMonths(back);
        LocalDate periodStart = month.atDay(1);
        LocalDate periodEnd = month.atEndOfMonth();
        String status = back >= 2 ? "PAID" : (back == 1 ? "APPROVED" : "PENDING");
        int index = 0;
        for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
          BigDecimal hours = workedHours(worker, month);
          boolean attendanceBonus = index < 6;
          BigDecimal base = BigDecimal.valueOf(worker.salary());
          BigDecimal bonus = BigDecimal.valueOf(worker.bonus());
          BigDecimal deduction = BigDecimal.valueOf(worker.deduction());
          // The seed also writes an event row per payroll; the stored totals must
          // already include it so aggregation (manual + events) equals the totals.
          BigDecimal eventBonus = attendanceBonus ? BigDecimal.valueOf(50_000) : BigDecimal.ZERO;
          BigDecimal eventDeduction =
              attendanceBonus ? BigDecimal.ZERO : BigDecimal.valueOf(50_000);
          BigDecimal bonusTotal = bonus.add(eventBonus);
          BigDecimal deductionTotal = deduction.add(eventDeduction);
          BigDecimal net = base.add(bonusTotal).subtract(deductionTotal);
          payroll.setLong(1, worker.id());
          payroll.setInt(2, month.getYear());
          payroll.setInt(3, month.getMonthValue());
          payroll.setDate(4, Date.valueOf(periodStart));
          payroll.setDate(5, Date.valueOf(periodEnd));
          payroll.setBigDecimal(6, base);
          payroll.setBigDecimal(7, bonusTotal);
          payroll.setBigDecimal(8, bonus);
          payroll.setBigDecimal(9, deductionTotal);
          payroll.setBigDecimal(10, deduction);
          payroll.setBigDecimal(11, net);
          payroll.setBigDecimal(12, "PAID".equals(status) ? net : BigDecimal.ZERO);
          payroll.setBigDecimal(13, hours);
          payroll.setString(14, status);
          payroll.setString(15, "Nómina " + month + " (seed v1)");
          payroll.setLong(16, SeedConstants.ADMIN_ID);
          payroll.addBatch();

          event.setLong(1, worker.id());
          event.setDate(2, Date.valueOf(periodEnd));
          event.setString(3, attendanceBonus ? "BONUS_ATTENDANCE" : "DEDUCTION");
          event.setBigDecimal(4, BigDecimal.ONE);
          event.setBigDecimal(5, BigDecimal.valueOf(50_000));
          event.setBigDecimal(
              6, attendanceBonus ? BigDecimal.valueOf(50_000) : BigDecimal.valueOf(-50_000));
          event.setString(7, attendanceBonus ? "Bono de asistencia" : "Descuento de nómina");
          event.setString(8, "seed-v1");
          event.addBatch();
          index++;
        }
      }
      payroll.executeBatch();
      event.executeBatch();
    }
  }

  /** Hours actually logged by a worker in the given month, from the seeded attendance. */
  private BigDecimal workedHours(SeedConstants.WorkerSpec worker, YearMonth month)
      throws SQLException {
    int shiftHours = worker.shifts().getFirst().hours();
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT COUNT(*) FROM time_logs WHERE worker_id = ? AND type = 'OUT' "
                + "AND YEAR(timestamp) = ? AND MONTH(timestamp) = ?")) {
      statement.setLong(1, worker.id());
      statement.setInt(2, month.getYear());
      statement.setInt(3, month.getMonthValue());
      try (ResultSet rows = statement.executeQuery()) {
        rows.next();
        return BigDecimal.valueOf(rows.getLong(1) * shiftHours).setScale(1, RoundingMode.HALF_UP);
      }
    }
  }

  private void seedSalaryHistory() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO salary_history (user_id, old_salary, new_salary, changed_at, reason, "
                + "observations) VALUES (?,?,?,?,?,?)")) {
      LocalDateTime changedAt = windowStart().minusDays(90).atTime(9, 0);
      for (SeedConstants.WorkerSpec worker : SeedConstants.WORKERS) {
        statement.setLong(1, worker.id());
        statement.setBigDecimal(2, BigDecimal.valueOf(worker.salary() - 100_000L));
        statement.setBigDecimal(3, BigDecimal.valueOf(worker.salary()));
        statement.setString(4, SeedMath.timestamp(changedAt));
        statement.setString(5, "Ajuste salarial anual");
        statement.setString(6, "Historial determinista generado por seed v1");
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private void seedSpecialSelectionHistory() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO special_selection_history (product_id, version, change_type, "
                + "snapshot_json, changed_by, changed_at, is_current) VALUES (?,?,?,?,?,?,?)")) {
      for (int product = 22; product <= 24; product++) {
        statement.setInt(1, product);
        statement.setInt(2, 1);
        statement.setString(3, "CREATE");
        statement.setString(
            4,
            "{\"productId\":"
                + product
                + ",\"source\":\"seed-v1\",\"groups\":\"see special_selection_groups\"}");
        statement.setString(5, "seed-v1");
        statement.setString(6, SeedMath.timestamp(windowStart().atTime(8, 0)));
        statement.setBoolean(7, true);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  /**
   * Rebuilds the demo alert rows so the module is never empty after a reset (P2-SEED-14).
   *
   * <p>The four OPEN rows mirror the rule set and severities of golden G5 ({@code
   * FOOD_COST_DEVIATION}, {@code REVPASH_DROP}, {@code SALES_DROP_YOY}, {@code
   * LOW_MARGIN_SKU_SPIKE}); the fifth row stays {@code READ} so the read/dismissed edge case (E15)
   * is covered. Period keys move with the anchor, so a rolling reset never leaves stale alerts.
   */
  private void seedAlerts() throws SQLException {
    YearMonth anchorMonth = YearMonth.from(anchor);
    String previousMonth = anchorMonth.minusMonths(1).toString();
    String currentMonth = anchorMonth.toString();
    String currentWeek = isoWeek(anchor.minusWeeks(1));
    String previousWeek = isoWeek(anchor.minusWeeks(2));
    String sql =
        "INSERT INTO system_alerts (severity, type, metric, baseline, current, deviation_pct, "
            + "period_key, period_bucket, message, detected_at, status, read_at) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      bindAlert(
          statement,
          "RED",
          "FOOD_COST_DEVIATION",
          "food_cost_pct",
          "30.00",
          "32.70",
          "9.00",
          previousMonth,
          "monthly",
          "Food cost is 2.70 pp above the 12-month moving average",
          anchor.minusDays(1).atTime(4, 0),
          "OPEN",
          null);
      bindAlert(
          statement,
          "RED",
          "REVPASH_DROP",
          "revpash",
          "12000.00",
          "9000.00",
          "-25.00",
          currentWeek,
          "weekly",
          "RevPASH dropped 25.00% versus the 4-week moving average",
          anchor.minusDays(2).atTime(4, 0),
          "OPEN",
          null);
      bindAlert(
          statement,
          "YELLOW",
          "SALES_DROP_YOY",
          "sales_orders",
          "100.00",
          "86.00",
          "-14.00",
          currentMonth,
          "monthly",
          "Order volume is 14.00% below the same period last year",
          anchor.minusDays(1).atTime(4, 0),
          "OPEN",
          null);
      bindAlert(
          statement,
          "YELLOW",
          "LOW_MARGIN_SKU_SPIKE",
          "dog_units",
          "50.00",
          "80.00",
          "60.00",
          currentWeek,
          "weekly",
          "Low-margin SKU units grew 60.00% week over week",
          anchor.minusDays(2).atTime(4, 0),
          "OPEN",
          null);
      bindAlert(
          statement,
          "YELLOW",
          "LOW_MARGIN_SKU_SPIKE",
          "dog_units",
          "40.00",
          "60.00",
          "50.00",
          previousWeek,
          "weekly",
          "Low-margin SKU units grew 50.00% week over week",
          anchor.minusDays(9).atTime(4, 0),
          "READ",
          anchor.minusDays(9).atTime(9, 15));
      statement.executeBatch();
    }
  }

  private void bindAlert(
      PreparedStatement statement,
      String severity,
      String type,
      String metric,
      String baseline,
      String current,
      String deviationPct,
      String periodKey,
      String periodBucket,
      String message,
      LocalDateTime detectedAt,
      String status,
      LocalDateTime readAt)
      throws SQLException {
    statement.setString(1, severity);
    statement.setString(2, type);
    statement.setString(3, metric);
    statement.setBigDecimal(4, new BigDecimal(baseline));
    statement.setBigDecimal(5, new BigDecimal(current));
    statement.setBigDecimal(6, new BigDecimal(deviationPct));
    statement.setString(7, periodKey);
    statement.setString(8, periodBucket);
    statement.setString(9, message);
    statement.setString(10, SeedMath.timestamp(detectedAt));
    statement.setString(11, status);
    if (readAt == null) {
      statement.setNull(12, Types.TIMESTAMP);
    } else {
      statement.setString(12, SeedMath.timestamp(readAt));
    }
    statement.addBatch();
  }

  private static String isoWeek(LocalDate day) {
    java.time.temporal.WeekFields fields = java.time.temporal.WeekFields.ISO;
    return String.format(
        Locale.ROOT,
        "%d-W%02d",
        day.get(fields.weekBasedYear()),
        day.get(fields.weekOfWeekBasedYear()));
  }

  /**
   * Seeds 30 days of closed cash shifts with payments plus one open shift (P3-BE-PAY-08).
   *
   * <p>Every historical order in the window is charged exactly once, older orders stay unpaid so
   * the cashier pending board has work, and each shift snapshots {@code expected}, {@code counted}
   * and {@code difference}. Tenders always sum to the payment total; only the last cash tender can
   * overpay, and the change is stored on the payment header.
   */
  private void seedCashAndPayments(SeedPlan plan) throws SQLException {
    LocalDate firstDay = anchor.minusDays(SeedConstants.PAYMENT_HISTORY_DAYS);
    Map<LocalDate, List<PaymentPlan>> paymentsByDay = new TreeMap<>();
    for (SeedPlan.Order order : plan.historicalOrdersBetween(firstDay, anchor)) {
      paymentsByDay
          .computeIfAbsent(order.openTime().toLocalDate(), ignored -> new ArrayList<>())
          .add(planPayment(order));
    }

    String shiftSql =
        "INSERT INTO cash_shifts (status, currency, opened_by, opened_at, closed_by, closed_at, "
            + "opening_float, expected_cash, counted_cash, difference_amount, notes) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    Map<LocalDate, Long> shiftIds = new LinkedHashMap<>();
    try (PreparedStatement shift =
        connection.prepareStatement(shiftSql, Statement.RETURN_GENERATED_KEYS)) {
      for (LocalDate day = firstDay; day.isBefore(anchor); day = day.plusDays(1)) {
        List<PaymentPlan> plans = paymentsByDay.getOrDefault(day, List.of());
        BigDecimal expected = expectedCash(plans);
        BigDecimal adjustment = shiftAdjustment(day);
        BigDecimal counted = expected.add(adjustment);
        long cashier = cashierFor(day);
        shift.setString(1, "CLOSED");
        shift.setString(2, "COP");
        shift.setLong(3, cashier);
        shift.setString(4, SeedMath.timestamp(day.atTime(10, 0)));
        shift.setLong(5, cashier);
        shift.setString(6, SeedMath.timestamp(day.plusDays(1).atTime(1, 0)));
        shift.setBigDecimal(7, SeedConstants.OPENING_FLOAT);
        shift.setBigDecimal(8, expected);
        shift.setBigDecimal(9, counted);
        shift.setBigDecimal(10, counted.subtract(expected));
        shift.setString(11, "Turno cerrado del dataset determinista");
        shift.executeUpdate();
        try (ResultSet keys = shift.getGeneratedKeys()) {
          keys.next();
          shiftIds.put(day, keys.getLong(1));
        }
      }
    }

    String paymentSql =
        "INSERT INTO payments (order_id, shift_id, status, currency, subtotal, discount_amount, "
            + "tip_amount, total_amount, tendered_amount, change_amount, receipt_number, "
            + "cashier_id, idempotency_key, created_at, completed_at) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    String tenderSql =
        "INSERT INTO payment_tenders (payment_id, method, amount, sort_order) VALUES (?,?,?,?)";
    try (PreparedStatement payment =
            connection.prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement tender = connection.prepareStatement(tenderSql)) {
      for (Map.Entry<LocalDate, List<PaymentPlan>> entry : paymentsByDay.entrySet()) {
        LocalDate day = entry.getKey();
        long shiftId = shiftIds.get(day);
        for (PaymentPlan paymentPlan : entry.getValue()) {
          payment.setLong(1, paymentPlan.orderId());
          payment.setLong(2, shiftId);
          payment.setString(3, "COMPLETED");
          payment.setString(4, "COP");
          payment.setBigDecimal(5, paymentPlan.subtotal());
          payment.setBigDecimal(6, BigDecimal.ZERO);
          payment.setBigDecimal(7, paymentPlan.tip());
          payment.setBigDecimal(8, paymentPlan.total());
          payment.setBigDecimal(9, paymentPlan.tendered());
          payment.setBigDecimal(10, paymentPlan.change());
          payment.setString(11, receiptNumber(paymentPlan.orderId(), day));
          payment.setLong(12, cashierFor(day));
          payment.setString(13, "seed-pay-" + paymentPlan.orderId());
          payment.setString(14, SeedMath.timestamp(paymentPlan.completedAt()));
          payment.setString(15, SeedMath.timestamp(paymentPlan.completedAt()));
          payment.executeUpdate();
          long paymentId;
          try (ResultSet keys = payment.getGeneratedKeys()) {
            keys.next();
            paymentId = keys.getLong(1);
          }
          int sortOrder = 0;
          for (TenderPlan tenderPlan : paymentPlan.tenders()) {
            tender.setLong(1, paymentId);
            tender.setString(2, tenderPlan.method());
            tender.setBigDecimal(3, tenderPlan.amount());
            tender.setInt(4, sortOrder++);
            tender.addBatch();
          }
        }
      }
      tender.executeBatch();
    }

    try (PreparedStatement open = connection.prepareStatement(shiftSql)) {
      LocalDateTime openedAt =
          now.toLocalTime().isBefore(LocalTime.of(8, 0))
              ? anchor.atStartOfDay()
              : anchor.atTime(8, 0);
      long cashier = cashierFor(anchor);
      open.setString(1, "OPEN");
      open.setString(2, "COP");
      open.setLong(3, cashier);
      open.setString(4, SeedMath.timestamp(openedAt));
      open.setNull(5, Types.BIGINT);
      open.setNull(6, Types.TIMESTAMP);
      open.setBigDecimal(7, SeedConstants.OPENING_FLOAT);
      open.setNull(8, Types.DECIMAL);
      open.setNull(9, Types.DECIMAL);
      open.setNull(10, Types.DECIMAL);
      open.setString(11, "Turno abierto del dataset determinista");
      open.executeUpdate();
    }
  }

  private PaymentPlan planPayment(SeedPlan.Order order) {
    BigDecimal subtotal =
        order.lines().stream()
            .map(line -> BigDecimal.valueOf(line.unitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    boolean withTip =
        SeedMath.modulo(SeedMath.crc(SeedConstants.SEED_STRING + ":tip:" + order.id()), 100) < 30;
    BigDecimal tip =
        withTip
            ? roundToHundred(subtotal.multiply(new BigDecimal("0.10")))
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = subtotal.add(tip).setScale(2, RoundingMode.HALF_UP);
    long bucket =
        SeedMath.modulo(SeedMath.crc(SeedConstants.SEED_STRING + ":method:" + order.id()), 100);
    List<TenderPlan> tenders = new ArrayList<>();
    BigDecimal tendered;
    BigDecimal change;
    if (bucket < 45) {
      tendered = roundUpToFiveThousand(total);
      change = tendered.subtract(total);
      tenders.add(new TenderPlan("CASH", tendered));
    } else if (bucket < 80) {
      tendered = total;
      change = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
      tenders.add(new TenderPlan("CARD", total));
    } else if (bucket < 90) {
      tendered = total;
      change = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
      tenders.add(new TenderPlan("TRANSFER", total));
    } else {
      BigDecimal cash = roundDownToThousand(total.multiply(new BigDecimal("0.40")));
      if (cash.signum() <= 0 || cash.compareTo(total) >= 0) {
        cash = total;
      }
      tenders.add(new TenderPlan("CASH", cash));
      BigDecimal card = total.subtract(cash);
      if (card.signum() > 0) {
        tenders.add(new TenderPlan("CARD", card));
      }
      tendered = total;
      change = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return new PaymentPlan(
        order.id(),
        subtotal.setScale(2, RoundingMode.HALF_UP),
        tip,
        total,
        tendered,
        change,
        tenders,
        order.closeTime());
  }

  private BigDecimal expectedCash(List<PaymentPlan> plans) {
    BigDecimal cash = BigDecimal.ZERO;
    BigDecimal change = BigDecimal.ZERO;
    for (PaymentPlan plan : plans) {
      for (TenderPlan tender : plan.tenders()) {
        if ("CASH".equals(tender.method())) {
          cash = cash.add(tender.amount());
        }
      }
      change = change.add(plan.change());
    }
    return SeedConstants.OPENING_FLOAT.add(cash).subtract(change).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal shiftAdjustment(LocalDate day) {
    long value = SeedMath.modulo(SeedMath.crc(SeedConstants.SEED_STRING + ":shift:" + day), 3);
    return BigDecimal.valueOf((value - 1) * 1_000L).setScale(2, RoundingMode.HALF_UP);
  }

  private long cashierFor(LocalDate day) {
    return SeedConstants.CASHIER_ONE;
  }

  private static String receiptNumber(long orderId, LocalDate day) {
    return "R-"
        + java.time.format.DateTimeFormatter.BASIC_ISO_DATE.format(day)
        + "-"
        + String.format(Locale.ROOT, "%06d", orderId % 1_000_000L);
  }

  private static BigDecimal roundUpToFiveThousand(BigDecimal value) {
    BigDecimal unit = new BigDecimal("5000");
    return value
        .divide(unit, 0, RoundingMode.CEILING)
        .multiply(unit)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal roundDownToThousand(BigDecimal value) {
    BigDecimal unit = new BigDecimal("1000");
    return value
        .divide(unit, 0, RoundingMode.FLOOR)
        .multiply(unit)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal roundToHundred(BigDecimal value) {
    BigDecimal unit = new BigDecimal("100");
    return value
        .divide(unit, 0, RoundingMode.HALF_UP)
        .multiply(unit)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private record TenderPlan(String method, BigDecimal amount) {}

  private record PaymentPlan(
      long orderId,
      BigDecimal subtotal,
      BigDecimal tip,
      BigDecimal total,
      BigDecimal tendered,
      BigDecimal change,
      List<TenderPlan> tenders,
      LocalDateTime completedAt) {}

  private void writeSeedMetadata(SeedPlan plan) throws SQLException {
    String sql =
        "INSERT INTO system_configuration (`key`, value) VALUES (?,?) "
            + "ON DUPLICATE KEY UPDATE value=VALUES(value)";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      putMetadata(statement, "seed_anchor", anchor.toString());
      putMetadata(statement, "seed_version", SeedConstants.VERSION);
      putMetadata(statement, "seed_dataset", SeedConstants.DATASET);
      putMetadata(statement, "seed_days", Integer.toString(SeedConstants.DAYS));
      putMetadata(statement, "seed_timezone", "America/Bogota");
      putMetadata(statement, "seed_generated_at", anchor.atTime(12, 0).toString());
      putMetadata(statement, "seed_logged_until", SeedMath.timestamp(now));
      putMetadata(
          statement,
          "seed_checksum",
          plan.historicalOrdersCount()
              + "-"
              + plan.historicalLinesCount()
              + "-"
              + plan.historicalRevenue());
      statement.executeBatch();
    }
  }

  private void putMetadata(PreparedStatement statement, String key, String value)
      throws SQLException {
    statement.setString(1, key);
    statement.setString(2, value);
    statement.addBatch();
  }

  private LocalDate windowStart() {
    return anchor.minusDays(SeedConstants.HISTORY_DAYS);
  }

  record SeedRunResult(
      LocalDate anchor,
      int historicalOrders,
      long historicalLines,
      long historicalRevenue,
      long historicalCovers,
      int todayOrders) {}

  private record RecipeLine(long variantId, BigDecimal quantity) {}

  private record PurchaseDraft(
      long supplierId, LocalDate day, boolean received, List<Long> variants) {}

  private record PurchaseLine(long variantId, BigDecimal quantityOrdered, BigDecimal unitPrice) {}

  private record PurchasePlan(
      long supplierId, LocalDate day, boolean received, List<PurchaseLine> lines) {}

  private record OptionSpec(int optionId, BigDecimal extraPrice, int groupId) {}

  private record AdditionSpec(int additionId, BigDecimal extraPrice) {}

  private record QuestionSpec(int questionId) {}

  /** Writes orders, details, options, preparation areas and collects inventory consumption. */
  private final class OrderWriter {
    private final Map<Long, Map<LocalDate, BigDecimal>> consumption = new LinkedHashMap<>();
    private final List<OptionRow> optionRows = new ArrayList<>();
    private final List<AdditionRow> additionRows = new ArrayList<>();
    private final List<ClarificationRow> clarificationRows = new ArrayList<>();
    private PreparedStatement orderStatement;
    private PreparedStatement detailStatement;
    private PreparedStatement optionStatement;
    private PreparedStatement preparationStatement;
    private PreparedStatement additionStatement;
    private PreparedStatement clarificationStatement;
    private PreparedStatement deductionStatement;
    private int orderPending;
    private int detailPending;
    private int optionPending;
    private int preparationPending;
    private int additionPending;
    private int clarificationPending;
    private int deductionPending;

    Map<Long, Map<LocalDate, BigDecimal>> consumption() {
      return consumption;
    }

    void writeAll(SeedPlan plan) throws SQLException {
      prepareStatements();
      try {
        // Pass 1: parents first, in batches, so FK checks always pass.
        for (SeedPlan.Order order : plan.historicalOrders()) {
          writeOrderRow(order);
        }
        for (SeedPlan.Order order : plan.operationalOrders()) {
          writeOrderRow(order);
        }
        flushOrders();

        // Pass 2: details, preparation areas, deductions and deferred option rows.
        for (SeedPlan.Order order : plan.historicalOrders()) {
          writeOrderDetails(order);
        }
        for (SeedPlan.Order order : plan.operationalOrders()) {
          writeOrderDetails(order);
        }
        flushDetails();
        flushDeductions();

        // Pass 3: rows that reference order_details.
        writeOptionRows();
        writeAdditionRows();
        writeClarificationRows();

        try (Statement statement = connection.createStatement()) {
          statement.executeUpdate(
              "UPDATE tables SET status = 'OCCUPIED' WHERE table_number BETWEEN 1 AND 5");
          statement.executeUpdate("UPDATE tables SET status = 'AVAILABLE' WHERE table_number > 5");
        }
      } finally {
        flushDetails();
        flushDeductions();
        closeAll();
      }
    }

    private void prepareStatements() throws SQLException {
      orderStatement =
          connection.prepareStatement(
              "INSERT INTO orders (id, date, status, table_id, party_size, open_time, close_time, "
                  + "customer_phone, customer_fingerprint) VALUES (?,?,?,?,?,?,?,?,?)");
      detailStatement =
          connection.prepareStatement(
              "INSERT INTO order_details (id, order_id, product_id, unit_price, instructions) "
                  + "VALUES (?,?,?,?,?)");
      optionStatement =
          connection.prepareStatement(
              "INSERT INTO order_detail_options (order_detail_id, option_id, extra_price) "
                  + "VALUES (?,?,?)");
      preparationStatement =
          connection.prepareStatement(
              "INSERT IGNORE INTO order_preparation_areas (order_id, area_id) VALUES (?,?)");
      additionStatement =
          connection.prepareStatement(
              "INSERT IGNORE INTO order_detail_additions (order_detail_id, addition_id, "
                  + "extra_price) VALUES (?,?,?)");
      clarificationStatement =
          connection.prepareStatement(
              "INSERT IGNORE INTO order_detail_clarifications (order_detail_id, question_id, "
                  + "answer) VALUES (?,?,?)");
      deductionStatement =
          connection.prepareStatement(
              "INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, "
                  + "to_storage_location_id, quantity, movement_type, reference_order_id, "
                  + "created_at) VALUES (?,?,?,?,?,?,?)");
    }

    private void writeOrderRow(SeedPlan.Order order) throws SQLException {
      orderStatement.setLong(1, order.id());
      orderStatement.setString(2, SeedMath.timestamp(order.openTime()));
      orderStatement.setString(3, order.status());
      Long tableId = tableIds.get(order.tableNumber());
      if (tableId == null) {
        orderStatement.setNull(4, Types.BIGINT);
      } else {
        orderStatement.setLong(4, tableId);
      }
      orderStatement.setInt(5, order.partySize());
      orderStatement.setString(6, SeedMath.timestamp(order.openTime()));
      if (order.closeTime() == null
          || "QUEUE".equals(order.status())
          || "PREPARING".equals(order.status())
          || "READY".equals(order.status())) {
        orderStatement.setNull(7, Types.TIMESTAMP);
      } else {
        orderStatement.setString(7, SeedMath.timestamp(order.closeTime()));
      }
      String phone = SeedMath.customerPhone(order.id());
      if (phone == null) {
        orderStatement.setNull(8, Types.VARCHAR);
        orderStatement.setNull(9, Types.VARCHAR);
      } else {
        orderStatement.setString(8, phone);
        orderStatement.setString(9, SeedMath.customerFingerprint(phone));
      }
      orderStatement.addBatch();
      if (++orderPending >= BATCH_SIZE) {
        orderStatement.executeBatch();
        orderPending = 0;
      }
    }

    private void writeOrderDetails(SeedPlan.Order order) throws SQLException {
      java.util.Set<Integer> areas = new java.util.LinkedHashSet<>();
      for (SeedPlan.Line line : order.lines()) {
        writeDetail(order, line);
        Integer area = productAreas.get(line.productId());
        if (area != null) {
          areas.add(area);
        }
      }
      for (Integer area : areas) {
        preparationStatement.setLong(1, order.id());
        preparationStatement.setInt(2, area);
        preparationStatement.addBatch();
        if (++preparationPending >= BATCH_SIZE) {
          preparationStatement.executeBatch();
          preparationPending = 0;
        }
      }
    }

    private void writeDetail(SeedPlan.Order order, SeedPlan.Line line) throws SQLException {
      detailStatement.setLong(1, line.id());
      detailStatement.setLong(2, order.id());
      detailStatement.setInt(3, line.productId());
      detailStatement.setBigDecimal(4, BigDecimal.valueOf(line.unitPrice()));
      String instruction =
          SeedMath.crc(SeedConstants.SEED_STRING + ":note:" + line.id()) % 12 == 0
              ? "Sin cebolla"
              : (SeedMath.crc(SeedConstants.SEED_STRING + ":note:" + line.id()) % 17 == 0
                  ? "Término medio"
                  : null);
      if (instruction == null) {
        detailStatement.setNull(5, Types.VARCHAR);
      } else {
        detailStatement.setString(5, instruction);
      }
      detailStatement.addBatch();
      if (++detailPending >= BATCH_SIZE) {
        detailStatement.executeBatch();
        detailPending = 0;
      }

      SeedConstants.ProductSpec product = SeedConstants.PRODUCT_BY_ID.get(line.productId());
      if (product.combo()) {
        collectComboRows(line);
      } else {
        collectOptionRows(order, line);
        addProductDeductions(order, line);
      }
    }

    private void collectOptionRows(SeedPlan.Order order, SeedPlan.Line line) throws SQLException {
      List<OptionSpec> options = productOptions.get(line.productId());
      if (options == null || options.isEmpty()) {
        return;
      }
      long hash = SeedMath.crc(SeedConstants.SEED_STRING + ":opt:" + line.id());
      if (hash % 100 >= 25) {
        return;
      }
      OptionSpec selected = options.get((int) (hash % options.size()));
      optionRows.add(new OptionRow(line.id(), selected.optionId(), selected.extraPrice()));
      List<RecipeLine> recipe = optionRecipes.get(selected.optionId());
      if (recipe == null) {
        return;
      }
      for (RecipeLine ingredient : recipe) {
        addConsumption(
            ingredient.variantId(), order.openTime().toLocalDate(), ingredient.quantity());
        addDeduction(order, ingredient.variantId(), ingredient.quantity());
      }
    }

    private void addProductDeductions(SeedPlan.Order order, SeedPlan.Line line) {
      List<RecipeLine> recipe = productRecipes.get(line.productId());
      if (recipe == null) {
        return;
      }
      for (RecipeLine ingredient : recipe) {
        addConsumption(
            ingredient.variantId(), order.openTime().toLocalDate(), ingredient.quantity());
        try {
          addDeduction(order, ingredient.variantId(), ingredient.quantity());
        } catch (SQLException exception) {
          throw new SeedException(exception.getMessage(), exception);
        }
      }
    }

    private void addConsumption(long variantId, LocalDate date, BigDecimal quantity) {
      consumption
          .computeIfAbsent(variantId, ignored -> new LinkedHashMap<>())
          .merge(date, quantity, BigDecimal::add);
    }

    private void addDeduction(SeedPlan.Order order, long variantId, BigDecimal quantity)
        throws SQLException {
      deductionStatement.setLong(1, variantId);
      deductionStatement.setInt(2, cocinaLocation);
      deductionStatement.setNull(3, Types.BIGINT);
      deductionStatement.setBigDecimal(4, quantity);
      deductionStatement.setString(5, "DEDUCTION");
      deductionStatement.setLong(6, order.id());
      deductionStatement.setString(7, SeedMath.timestamp(order.openTime()));
      deductionStatement.addBatch();
      if (++deductionPending >= BATCH_SIZE) {
        deductionStatement.executeBatch();
        deductionPending = 0;
      }
    }

    private void collectComboRows(SeedPlan.Line line) {
      List<AdditionSpec> additions = comboAdditions.get(line.productId());
      if (additions != null && !additions.isEmpty()) {
        long hash = SeedMath.crc(SeedConstants.SEED_STRING + ":add:" + line.id());
        AdditionSpec addition = additions.get((int) (hash % additions.size()));
        additionRows.add(new AdditionRow(line.id(), addition.additionId(), addition.extraPrice()));
      }
      List<QuestionSpec> questions = comboQuestions.get(line.productId());
      if (questions != null) {
        int index = 0;
        for (QuestionSpec question : questions) {
          clarificationRows.add(
              new ClarificationRow(
                  line.id(),
                  question.questionId(),
                  index == 0
                      ? "Ninguna"
                      : (SeedMath.crc(SeedConstants.SEED_STRING + ":ans:" + line.id()) % 2 == 0
                          ? "Sí"
                          : "No")));
          index++;
        }
      }
    }

    private void writeOptionRows() throws SQLException {
      for (OptionRow row : optionRows) {
        optionStatement.setLong(1, row.detailId());
        optionStatement.setInt(2, row.optionId());
        optionStatement.setBigDecimal(3, row.extraPrice());
        optionStatement.addBatch();
        if (++optionPending >= BATCH_SIZE) {
          optionStatement.executeBatch();
          optionPending = 0;
        }
      }
      if (optionPending > 0) {
        optionStatement.executeBatch();
        optionPending = 0;
      }
    }

    private void writeAdditionRows() throws SQLException {
      for (AdditionRow row : additionRows) {
        additionStatement.setLong(1, row.detailId());
        additionStatement.setInt(2, row.additionId());
        additionStatement.setBigDecimal(3, row.extraPrice());
        additionStatement.addBatch();
        if (++additionPending >= BATCH_SIZE) {
          additionStatement.executeBatch();
          additionPending = 0;
        }
      }
      if (additionPending > 0) {
        additionStatement.executeBatch();
        additionPending = 0;
      }
    }

    private void writeClarificationRows() throws SQLException {
      for (ClarificationRow row : clarificationRows) {
        clarificationStatement.setLong(1, row.detailId());
        clarificationStatement.setInt(2, row.questionId());
        clarificationStatement.setString(3, row.answer());
        clarificationStatement.addBatch();
        if (++clarificationPending >= BATCH_SIZE) {
          clarificationStatement.executeBatch();
          clarificationPending = 0;
        }
      }
      if (clarificationPending > 0) {
        clarificationStatement.executeBatch();
        clarificationPending = 0;
      }
    }

    private void flushOrders() throws SQLException {
      if (orderPending > 0) {
        orderStatement.executeBatch();
        orderPending = 0;
      }
    }

    private void flushDetails() throws SQLException {
      if (detailPending > 0) {
        detailStatement.executeBatch();
        detailPending = 0;
      }
      if (preparationPending > 0) {
        preparationStatement.executeBatch();
        preparationPending = 0;
      }
    }

    private void flushDeductions() throws SQLException {
      if (deductionPending > 0) {
        deductionStatement.executeBatch();
        deductionPending = 0;
      }
    }

    private void closeAll() throws SQLException {
      orderStatement.close();
      detailStatement.close();
      optionStatement.close();
      preparationStatement.close();
      additionStatement.close();
      clarificationStatement.close();
      deductionStatement.close();
    }
  }

  private record OptionRow(long detailId, int optionId, BigDecimal extraPrice) {}

  private record AdditionRow(long detailId, int additionId, BigDecimal extraPrice) {}

  private record ClarificationRow(long detailId, int questionId, String answer) {}

  /** Derives purchases, transfers, waste, adjustments and stock from order consumption. */
  private final class InventoryWriter {
    private final Map<Long, Map<LocalDate, BigDecimal>> consumption;
    private final Map<Long, BigDecimal> bodega = new HashMap<>();
    private final Map<Long, BigDecimal> cocina = new HashMap<>();
    private PreparedStatement movement;
    private int pending;

    InventoryWriter(Map<Long, Map<LocalDate, BigDecimal>> consumption) {
      this.consumption = consumption;
    }

    void write() throws SQLException {
      movement =
          connection.prepareStatement(
              "INSERT INTO inventory_movements (supply_variant_id, from_storage_location_id, "
                  + "to_storage_location_id, quantity, movement_type, reference_order_id, "
                  + "created_at, reference_purchase_order_id) VALUES (?,?,?,?,?,?,?,?)");
      try {
        List<PurchasePlan> plans = buildPurchasePlans();
        openingEntries(plans);
        transfers();
        writePurchases(plans);
        waste();
        adjustments();
        flush();
        writeStock();
      } finally {
        movement.close();
      }
    }

    /**
     * Opening stock covers the consumption before a variant's first delivery plus two weeks of
     * demand, so every later replenishment starts from a positive balance. Variants never consumed
     * stay at zero, which keeps the low-stock empty state visible (E11).
     */
    private void openingEntries(List<PurchasePlan> plans) throws SQLException {
      Map<Long, LocalDate> firstPurchase = new HashMap<>();
      Map<Long, BigDecimal> purchased = new HashMap<>();
      for (PurchasePlan plan : plans) {
        if (!plan.received()) {
          continue;
        }
        for (PurchaseLine line : plan.lines()) {
          firstPurchase.merge(
              line.variantId(), plan.day(), (left, right) -> left.isBefore(right) ? left : right);
          purchased.merge(line.variantId(), line.quantityOrdered(), BigDecimal::add);
        }
      }
      LocalDateTime at = windowStart().atTime(6, 0);
      for (Long variant : variantUnitCost.keySet()) {
        BigDecimal total = sumConsumption(variant, windowStart(), anchor.plusDays(1));
        if (total.signum() <= 0) {
          continue;
        }
        LocalDate first = firstPurchase.get(variant);
        BigDecimal beforeFirst =
            first == null ? total : sumConsumption(variant, windowStart(), first);
        BigDecimal unpurchased =
            total.subtract(purchased.getOrDefault(variant, BigDecimal.ZERO)).max(BigDecimal.ZERO);
        BigDecimal safety = sumConsumption(variant, windowStart(), windowStart().plusDays(14));
        BigDecimal opening =
            ceil(beforeFirst.max(unpurchased)).add(ceil(safety)).add(BigDecimal.valueOf(200));
        addMovement(variant, null, bodegaLocation, opening, "ENTRY", null, at, null);
        bodega.merge(variant, opening, BigDecimal::add);
      }
    }

    private void transfers() throws SQLException {
      for (Map.Entry<Long, Map<LocalDate, BigDecimal>> entry : consumption.entrySet()) {
        for (Map.Entry<LocalDate, BigDecimal> day : entry.getValue().entrySet()) {
          BigDecimal quantity = ceil(day.getValue());
          if (quantity.signum() <= 0) {
            continue;
          }
          addMovement(
              entry.getKey(),
              bodegaLocation,
              cocinaLocation,
              quantity,
              "TRANSFER",
              null,
              day.getKey().atTime(8, 0),
              null);
          bodega.merge(entry.getKey(), quantity.negate(), BigDecimal::add);
          // Cocina receives the rounded transfer and gives back the exact recipe consumption
          // recorded as DEDUCTION, so the location ledger reconciles with the movements.
          cocina.merge(entry.getKey(), quantity, BigDecimal::add);
          cocina.merge(entry.getKey(), day.getValue().negate(), BigDecimal::add);
        }
      }
    }

    /**
     * Writes the 60 demo purchase orders and their stock entries (P4-INV-06/07).
     *
     * <p>Two main suppliers deliver roughly weekly; the nine specialty vendors deliver once in the
     * window. Every received order records a {@code PURCHASE} movement in Bodega on the same day
     * and references the purchase order, so stock is never counted twice. The only order dated on
     * the anchor day stays {@code CONFIRMED} (notes token {@code CONFIRMADA}) with zero received
     * quantity and therefore has no movement.
     */
    private void writePurchases(List<PurchasePlan> plans) throws SQLException {
      String orderSql =
          "INSERT INTO purchase_orders (id, supplier_id, registered_by, purchased_at, "
              + "total_amount, status, notes, created_at) VALUES (?,?,?,?,?,?,?,?)";
      try (PreparedStatement order = connection.prepareStatement(orderSql);
          PreparedStatement item =
              connection.prepareStatement(
                  "INSERT INTO purchase_order_items (id, purchase_order_id, supply_variant_id, "
                      + "quantity_ordered, quantity_received, unit_price) VALUES (?,?,?,?,?,?)")) {
        long purchaseOrderId = 1;
        long purchaseItemId = 1;
        for (PurchasePlan plan : plans) {
          final LocalDateTime purchasedAt = plan.day().atTime(7, 30);
          final LocalDateTime receivedAt = plan.day().atTime(10, 30);
          BigDecimal total = BigDecimal.ZERO;
          for (PurchaseLine line : plan.lines()) {
            total = total.add(line.quantityOrdered().multiply(line.unitPrice()));
          }
          order.setLong(1, purchaseOrderId);
          order.setLong(2, plan.supplierId());
          order.setLong(3, SeedConstants.ADMIN_ID);
          order.setString(4, SeedMath.timestamp(purchasedAt));
          order.setBigDecimal(5, total);
          order.setString(
              6,
              plan.received()
                  ? SeedConstants.PURCHASE_ORDER_STATUS_RECEIVED
                  : SeedConstants.PURCHASE_ORDER_STATUS_CONFIRMED);
          order.setString(7, purchaseNotes(plan, receivedAt));
          order.setString(8, SeedMath.timestamp(purchasedAt));
          order.executeUpdate();
          for (PurchaseLine line : plan.lines()) {
            item.setLong(1, purchaseItemId++);
            item.setLong(2, purchaseOrderId);
            item.setLong(3, line.variantId());
            item.setBigDecimal(4, line.quantityOrdered());
            item.setBigDecimal(5, plan.received() ? line.quantityOrdered() : BigDecimal.ZERO);
            item.setBigDecimal(6, line.unitPrice());
            item.addBatch();
            if (plan.received()) {
              addMovement(
                  line.variantId(),
                  null,
                  bodegaLocation,
                  line.quantityOrdered(),
                  "PURCHASE",
                  null,
                  receivedAt,
                  purchaseOrderId);
              bodega.merge(line.variantId(), line.quantityOrdered(), BigDecimal::add);
            }
          }
          item.executeBatch();
          purchaseOrderId++;
        }
      }
    }

    private String purchaseNotes(PurchasePlan plan, LocalDateTime receivedAt) {
      if (plan.received()) {
        String cadence =
            mainSupplier(plan.supplierId()) ? "Compra semanal" : "Compra de reposición";
        return SeedConstants.PURCHASE_NOTES_RECEIVED
            + " · "
            + cadence
            + " · recibida "
            + SeedMath.timestamp(receivedAt);
      }
      LocalDateTime expected = anchor.atTime(16, 0);
      return SeedConstants.PURCHASE_NOTES_CONFIRMED
          + " · Compra programada · entrega prevista "
          + SeedMath.timestamp(expected);
    }

    private List<PurchasePlan> buildPurchasePlans() {
      List<PurchaseDraft> drafts = new ArrayList<>();
      LocalDate start = windowStart();
      for (int week = 0; week < 25; week++) {
        drafts.add(draft(1, start.plusDays(week * 7L), true));
      }
      for (int week = 0; week < 25; week++) {
        drafts.add(draft(2, start.plusDays(3 + week * 7L), true));
      }
      for (int index = 0; index < 9; index++) {
        drafts.add(draft(3 + index, start.plusDays(10 + index * 18L), true));
      }
      drafts.add(draft(1, anchor, false));
      drafts.sort(
          Comparator.comparing(PurchaseDraft::day).thenComparingLong(PurchaseDraft::supplierId));

      Map<Long, List<LocalDate>> purchaseDays = new HashMap<>();
      for (PurchaseDraft draft : drafts) {
        if (!draft.received()) {
          continue;
        }
        for (Long variant : draft.variants()) {
          purchaseDays.computeIfAbsent(variant, ignored -> new ArrayList<>()).add(draft.day());
        }
      }
      for (List<LocalDate> days : purchaseDays.values()) {
        days.sort(LocalDate::compareTo);
      }

      List<PurchasePlan> plans = new ArrayList<>(drafts.size());
      for (PurchaseDraft draft : drafts) {
        List<PurchaseLine> lines = new ArrayList<>();
        for (Long variant : draft.variants()) {
          BigDecimal unitCost = variantUnitCost.getOrDefault(variant, BigDecimal.ONE);
          lines.add(
              new PurchaseLine(variant, purchaseQuantity(draft, variant, purchaseDays), unitCost));
        }
        plans.add(
            new PurchasePlan(
                draft.supplierId(), draft.day(), draft.received(), List.copyOf(lines)));
      }
      return plans;
    }

    private BigDecimal purchaseQuantity(
        PurchaseDraft draft, Long variant, Map<Long, List<LocalDate>> purchaseDays) {
      long span;
      if (!draft.received()) {
        span = 7;
      } else {
        List<LocalDate> days = purchaseDays.getOrDefault(variant, List.of());
        int index = days.indexOf(draft.day());
        LocalDate end =
            index >= 0 && index + 1 < days.size() ? days.get(index + 1) : anchor.plusDays(1);
        span = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(draft.day(), end));
        if (!mainSupplier(draft.supplierId())) {
          // One delivery per specialty vendor: a 30-day basket is realistic; the rest of the
          // window is covered by opening stock instead of a six-month order.
          span = Math.min(span, 30);
        }
      }
      BigDecimal demand =
          sumConsumption(variant, draft.day(), draft.day().plusDays(span))
              .multiply(new BigDecimal("1.03"));
      return ceil(demand).max(BigDecimal.ONE);
    }

    private PurchaseDraft draft(long supplierId, LocalDate day, boolean received) {
      List<Long> variants = supplierVariants.getOrDefault(supplierId, List.of());
      if (variants.isEmpty()) {
        return new PurchaseDraft(supplierId, day, received, List.of());
      }
      List<Long> ordered = new ArrayList<>(variants);
      ordered.sort(
          Comparator.comparingLong(
              variant ->
                  SeedMath.crc(
                      SeedConstants.SEED_STRING
                          + ":po:"
                          + supplierId
                          + ":"
                          + day
                          + ":"
                          + variant)));
      int count =
          Math.min(
              ordered.size(),
              6
                  + SeedMath.modulo(
                      SeedMath.crc(
                          SeedConstants.SEED_STRING + ":po-lines:" + supplierId + ":" + day),
                      7));
      return new PurchaseDraft(supplierId, day, received, List.copyOf(ordered.subList(0, count)));
    }

    private boolean mainSupplier(long supplierId) {
      return supplierId == 1L || supplierId == 2L;
    }

    private BigDecimal sumConsumption(Long variant, LocalDate from, LocalDate toExclusive) {
      BigDecimal total = BigDecimal.ZERO;
      Map<LocalDate, BigDecimal> byDay = consumption.getOrDefault(variant, Map.of());
      for (LocalDate day = from; day.isBefore(toExclusive); day = day.plusDays(1)) {
        total = total.add(byDay.getOrDefault(day, BigDecimal.ZERO));
      }
      return total;
    }

    private BigDecimal ceil(BigDecimal value) {
      return value.setScale(0, RoundingMode.CEILING);
    }

    private void waste() throws SQLException {
      List<Long> top =
          consumption.keySet().stream()
              .sorted(
                  Comparator.comparing(
                          (Long variant) ->
                              consumption.get(variant).values().stream()
                                  .reduce(BigDecimal.ZERO, BigDecimal::add))
                      .reversed())
              .limit(3)
              .toList();
      LocalDate start = windowStart();
      for (int week = 0; week < 26; week++) {
        LocalDate weekStart = start.plusDays(week * 7L);
        for (Long variant : top) {
          BigDecimal quantity =
              weekConsumption(variant, weekStart, 7)
                  .multiply(new BigDecimal("0.02"))
                  .setScale(0, RoundingMode.CEILING);
          if (quantity.signum() <= 0) {
            continue;
          }
          LocalDate wasteDay = weekStart.plusDays(6);
          if (wasteDay.isAfter(anchor)) {
            wasteDay = anchor;
          }
          addMovement(
              variant, bodegaLocation, null, quantity, "WASTE", null, wasteDay.atTime(22, 0), null);
          bodega.merge(variant, quantity.negate(), BigDecimal::add);
        }
      }
    }

    private void adjustments() throws SQLException {
      LocalDate start = windowStart();
      for (int month = 0; month < 6; month++) {
        LocalDate date = start.plusMonths(month).withDayOfMonth(1);
        for (Long variant : variantUnitCost.keySet()) {
          BigDecimal quantity =
              consumption.getOrDefault(variant, Map.of()).values().stream()
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .multiply(new BigDecimal("0.001"))
                  .setScale(0, RoundingMode.CEILING)
                  .add(BigDecimal.ONE);
          addMovement(
              variant, null, bodegaLocation, quantity, "ADJUSTMENT", null, date.atTime(9, 0), null);
          bodega.merge(variant, quantity, BigDecimal::add);
        }
      }
    }

    private BigDecimal weekConsumption(Long variant, LocalDate start, int days) {
      BigDecimal total = BigDecimal.ZERO;
      Map<LocalDate, BigDecimal> byDay = consumption.getOrDefault(variant, Map.of());
      for (int i = 0; i < days; i++) {
        total = total.add(byDay.getOrDefault(start.plusDays(i), BigDecimal.ZERO));
      }
      return total;
    }

    private void addMovement(
        Long variant,
        Integer from,
        Integer to,
        BigDecimal quantity,
        String type,
        Long orderId,
        LocalDateTime at,
        Long purchaseOrderId)
        throws SQLException {
      movement.setLong(1, variant);
      if (from == null) {
        movement.setNull(2, Types.BIGINT);
      } else {
        movement.setInt(2, from);
      }
      if (to == null) {
        movement.setNull(3, Types.BIGINT);
      } else {
        movement.setInt(3, to);
      }
      movement.setBigDecimal(4, quantity);
      movement.setString(5, type);
      if (orderId == null) {
        movement.setNull(6, Types.BIGINT);
      } else {
        movement.setLong(6, orderId);
      }
      movement.setString(7, SeedMath.timestamp(at));
      if (purchaseOrderId == null) {
        movement.setNull(8, Types.BIGINT);
      } else {
        movement.setLong(8, purchaseOrderId);
      }
      movement.addBatch();
      if (++pending >= BATCH_SIZE) {
        movement.executeBatch();
        pending = 0;
      }
    }

    private void flush() throws SQLException {
      if (pending > 0) {
        movement.executeBatch();
        pending = 0;
      }
    }

    private void writeStock() throws SQLException {
      String sql =
          "INSERT INTO inventory_stock (supply_variant_id, storage_location_id, current_quantity) "
              + "VALUES (?,?,?)";
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        for (Long variant : variantUnitCost.keySet()) {
          BigDecimal bodegaQuantity = bodega.getOrDefault(variant, BigDecimal.ZERO);
          BigDecimal cocinaQuantity = cocina.getOrDefault(variant, BigDecimal.ZERO);
          if (bodegaQuantity.signum() < 0 || cocinaQuantity.signum() < 0) {
            throw new SeedException("Stock negativo para la variante " + variant);
          }
          statement.setLong(1, variant);
          statement.setInt(2, bodegaLocation);
          statement.setBigDecimal(3, bodegaQuantity);
          statement.addBatch();
          statement.setLong(1, variant);
          statement.setInt(2, cocinaLocation);
          statement.setBigDecimal(3, cocinaQuantity);
          statement.addBatch();
          statement.setLong(1, variant);
          statement.setInt(2, cuartoFrioLocation);
          statement.setBigDecimal(3, BigDecimal.ZERO);
          statement.addBatch();
          statement.setLong(1, variant);
          statement.setInt(2, barLocation);
          statement.setBigDecimal(3, BigDecimal.ZERO);
          statement.addBatch();
        }
        statement.executeBatch();
      }
    }
  }
}
