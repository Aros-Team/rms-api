package aros.services.rms.infraestructure.category.persistence.jpa;

import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.domain.OptionCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(aros.services.rms.infraestructure.category.persistence.Category entity) {
        if (entity == null) return null;
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public aros.services.rms.infraestructure.category.persistence.Category toEntity(Category domain) {
        if (domain == null) return null;
        return aros.services.rms.infraestructure.category.persistence.Category.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }

    public OptionCategory toOptionCategoryDomain(aros.services.rms.infraestructure.category.persistence.OptionCategory entity) {
        if (entity == null) return null;
        return OptionCategory.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public aros.services.rms.infraestructure.category.persistence.OptionCategory toOptionCategoryEntity(OptionCategory domain) {
        if (domain == null) return null;
        return aros.services.rms.infraestructure.category.persistence.OptionCategory.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }
}