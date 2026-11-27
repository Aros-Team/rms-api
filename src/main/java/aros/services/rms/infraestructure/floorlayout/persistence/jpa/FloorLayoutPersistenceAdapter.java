/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.port.output.FloorLayoutRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Persistence adapter for the singleton floor layout and its regions. */
@Component
@RequiredArgsConstructor
public class FloorLayoutPersistenceAdapter implements FloorLayoutRepositoryPort {

  private static final long SINGLETON_ID = 1L;

  private final FloorLayoutJpaRepository layoutRepository;
  private final FloorLayoutRegionJpaRepository regionRepository;
  private final FloorTablePlacementJpaRepository tablePlacementRepository;
  private final FloorRegionSegmentsCodec segmentsCodec;

  /** {@inheritDoc} */
  @Override
  public Optional<FloorLayout> find() {
    return layoutRepository.findById(SINGLETON_ID).map(this::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public FloorLayout save(FloorLayout layout) {
    Instant updatedAt = Instant.now();

    FloorLayoutEntity entity =
        layoutRepository.findById(SINGLETON_ID).orElseGet(FloorLayoutEntity::new);
    entity.setId(SINGLETON_ID);
    entity.setColumns(layout.getColumns());
    entity.setRows(layout.getRows());
    entity.setUpdatedAt(updatedAt);
    layoutRepository.saveAndFlush(entity);

    regionRepository.deleteByFloorLayoutId(SINGLETON_ID);
    List<FloorRegionEntity> regions = layout.getRegions().stream().map(this::toEntity).toList();
    regionRepository.saveAllAndFlush(regions);

    return toDomain(entity, regions.stream().map(this::toDomain).toList());
  }

  private FloorLayout toDomain(FloorLayoutEntity layout) {
    List<FloorRegion> regions =
        regionRepository.findByFloorLayoutIdOrderByRegionKeyAsc(layout.getId()).stream()
            .map(this::toDomain)
            .toList();
    return toDomain(layout, regions);
  }

  private FloorLayout toDomain(FloorLayoutEntity layout, List<FloorRegion> regions) {
    return FloorLayout.builder()
        .columns(layout.getColumns())
        .rows(layout.getRows())
        .regions(regions)
        .updatedAt(layout.getUpdatedAt())
        .build();
  }

  private FloorRegion toDomain(FloorRegionEntity entity) {
    return FloorRegion.builder()
        .key(entity.getRegionKey())
        .areaId(entity.getAreaId())
        .segments(segmentsCodec.decode(entity.getSegmentsJson()))
        .build();
  }

  private FloorRegionEntity toEntity(FloorRegion region) {
    return FloorRegionEntity.builder()
        .floorLayoutId(SINGLETON_ID)
        .regionKey(region.getKey())
        .areaId(region.getAreaId())
        .segmentsJson(segmentsCodec.encode(region.getSegments()))
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public List<FloorTablePlacement> findTablePlacements() {
    return tablePlacementRepository.findByFloorLayoutIdOrderByTableIdAsc(SINGLETON_ID).stream()
        .map(this::toDomainPlacement)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public List<FloorTablePlacement> saveTablePlacements(List<FloorTablePlacement> placements) {
    tablePlacementRepository.deleteByFloorLayoutId(SINGLETON_ID);
    List<FloorTablePlacementEntity> entities =
        placements.stream().map(this::toEntityPlacement).collect(Collectors.toList());
    return tablePlacementRepository.saveAllAndFlush(entities).stream()
        .map(this::toDomainPlacement)
        .toList();
  }

  private FloorTablePlacement toDomainPlacement(FloorTablePlacementEntity entity) {
    return FloorTablePlacement.builder()
        .tableId(entity.getTableId())
        .row(entity.getRow())
        .column(entity.getColumn())
        .build();
  }

  private FloorTablePlacementEntity toEntityPlacement(FloorTablePlacement placement) {
    return FloorTablePlacementEntity.builder()
        .floorLayoutId(SINGLETON_ID)
        .tableId(placement.getTableId())
        .row(placement.getRow())
        .column(placement.getColumn())
        .build();
  }
}
