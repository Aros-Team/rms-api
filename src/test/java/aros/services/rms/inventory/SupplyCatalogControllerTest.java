/* (C) 2026 */

package aros.services.rms.inventory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.image.port.output.StoragePort;
import aros.services.rms.infraestructure.inventory.persistence.SupplyCategoryEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyEntity;
import aros.services.rms.infraestructure.inventory.persistence.SupplyVariantEntity;
import aros.services.rms.infraestructure.inventory.persistence.UnitOfMeasureEntity;
import aros.services.rms.infraestructure.inventory.persistence.jpa.InventoryStockRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.StorageLocationRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyCategoryRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.SupplyVariantRepository;
import aros.services.rms.infraestructure.inventory.persistence.jpa.UnitOfMeasureRepository;
import aros.services.rms.testsupport.AbstractJwtIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Endpoint tests for Supply Catalog endpoints.
 *
 * <p>E-I-01: shouldReturn201_whenSupplyIsCreatedSuccessfully E-I-02:
 * shouldReturn409_whenSupplyNameIsDuplicated E-I-03: shouldReturn404_whenCategoryNotFound E-I-04:
 * shouldReturn201_whenVariantIsCreatedSuccessfully E-I-05:
 * shouldReturn409_whenVariantCombinationIsDuplicated E-I-06: shouldReturn200_withVariantsAndStock
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
      "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MYSQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "app.admin.email=admin@test.local",
      "app.admin.dummy-email=test@test.local",
      "app.admin.password=TestPassword123!",
      "app.env=development"
    })
class SupplyCatalogControllerTest extends AbstractJwtIntegrationTest {

  @Autowired private WebApplicationContext context;

  @MockitoBean private StoragePort storagePort;

  @MockitoBean private SupplyRepository supplyRepository;
  @MockitoBean private SupplyVariantRepository supplyVariantRepository;
  @MockitoBean private SupplyCategoryRepository supplyCategoryRepository;
  @MockitoBean private InventoryStockRepository inventoryStockRepository;
  @MockitoBean private StorageLocationRepository storageLocationRepository;
  @MockitoBean private UnitOfMeasureRepository unitOfMeasureRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  // ---------------------------------------------------------------------------
  // E-I-01: shouldReturn201_whenSupplyIsCreatedSuccessfully
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenSupplyIsCreatedSuccessfully() throws Exception {
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();

    SupplyEntity saved = SupplyEntity.builder().id(10L).name("Pan").category(category).build();

    when(supplyCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(supplyRepository.findByNameIgnoreCase("Pan")).thenReturn(Optional.empty());
    when(supplyRepository.save(any(SupplyEntity.class))).thenReturn(saved);

    mockMvc
        .perform(
            post("/api/v1/supplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Pan\", \"categoryId\": 1}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Pan"))
        .andExpect(jsonPath("$.categoryId").value(1));
  }

  // ---------------------------------------------------------------------------
  // E-I-02: shouldReturn409_whenSupplyNameIsDuplicated
  // SupplyCatalogController lanza SupplyAlreadyExistsException → 409
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenSupplyNameIsDuplicated() throws Exception {
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();

    SupplyEntity existing = SupplyEntity.builder().id(5L).name("Pan").category(category).build();

    when(supplyCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(supplyRepository.findByNameIgnoreCase("Pan")).thenReturn(Optional.of(existing));

    mockMvc
        .perform(
            post("/api/v1/supplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Pan\", \"categoryId\": 1}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Supply already exists: name=Pan"));
  }

  // ---------------------------------------------------------------------------
  // E-I-03: shouldReturn404_whenCategoryNotFound
  // SupplyCatalogController lanza StorageLocationNotFoundException → 404
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn404_whenCategoryNotFound() throws Exception {
    when(supplyCategoryRepository.findById(9999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/supplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Pan\", \"categoryId\": 9999}"))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.message").value("Storage location not found: Category not found: id=9999"));
  }

  // ---------------------------------------------------------------------------
  // E-I-04: shouldReturn201_whenVariantIsCreatedSuccessfully
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenVariantIsCreatedSuccessfully() throws Exception {
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();

    SupplyEntity supply =
        SupplyEntity.builder().id(3L).name("Carne de Res").category(category).build();

    UnitOfMeasureEntity unit =
        UnitOfMeasureEntity.builder().id(2L).name("Kilogramo").abbreviation("kg").build();

    SupplyVariantEntity saved =
        SupplyVariantEntity.builder()
            .id(7L)
            .supply(supply)
            .unit(unit)
            .quantity(new BigDecimal("0.500"))
            .build();

    when(supplyRepository.findById(3L)).thenReturn(Optional.of(supply));
    when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
    when(supplyVariantRepository.findBySupplyIdAndUnitIdAndQuantity(
            3L, 2L, new BigDecimal("0.500")))
        .thenReturn(Optional.empty());
    when(supplyVariantRepository.save(any(SupplyVariantEntity.class))).thenReturn(saved);
    when(storageLocationRepository.findByName(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/supplies/variants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\": 3, \"unitId\": 2, \"quantity\": 0.500}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.supplyId").value(3))
        .andExpect(jsonPath("$.unitId").value(2))
        .andExpect(jsonPath("$.stockBodega").value(0))
        .andExpect(jsonPath("$.stockCocina").value(0));
  }

  // ---------------------------------------------------------------------------
  // E-I-05: shouldReturn409_whenVariantCombinationIsDuplicated
  // SupplyCatalogController lanza SupplyVariantAlreadyExistsException → 409
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenVariantCombinationIsDuplicated() throws Exception {
    SupplyCategoryEntity category = SupplyCategoryEntity.builder().id(1L).name("Proteínas").build();

    SupplyEntity supply =
        SupplyEntity.builder().id(3L).name("Carne de Res").category(category).build();

    UnitOfMeasureEntity unit =
        UnitOfMeasureEntity.builder().id(2L).name("Kilogramo").abbreviation("kg").build();

    SupplyVariantEntity existing =
        SupplyVariantEntity.builder()
            .id(7L)
            .supply(supply)
            .unit(unit)
            .quantity(new BigDecimal("0.500"))
            .build();

    when(supplyRepository.findById(3L)).thenReturn(Optional.of(supply));
    when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
    when(supplyVariantRepository.findBySupplyIdAndUnitIdAndQuantity(
            3L, 2L, new BigDecimal("0.500")))
        .thenReturn(Optional.of(existing));

    mockMvc
        .perform(
            post("/api/v1/supplies/variants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"supplyId\": 3, \"unitId\": 2, \"quantity\": 0.500}"))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("Supply variant already exists: supplyId=3, unitId=2, quantity=0.500"));
  }

  // ---------------------------------------------------------------------------
  // E-I-06: shouldReturn200_withVariantsAndStock
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_withVariantsAndStock() throws Exception {
    when(supplyVariantRepository.findAll()).thenReturn(List.of());
    when(storageLocationRepository.findByName(any())).thenReturn(Optional.empty());
    when(inventoryStockRepository.findByStorageLocationId(any())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/supplies/variants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
