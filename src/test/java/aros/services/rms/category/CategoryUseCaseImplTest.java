/* (C) 2026 */
package aros.services.rms.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.application.service.CategoryService;
import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryUseCaseImplTest {

  @Mock private CategoryRepositoryPort categoryRepositoryPort;
  @Mock private Logger logger;

  private CategoryService categoryUseCase;

  @BeforeEach
  void setUp() {
    categoryUseCase = new CategoryService(categoryRepositoryPort, logger);
  }

  @Test
  void shouldCreateCategorySuccessfully() {
    Category category = Category.builder().name("Entradas").description("Appetizers").build();
    Category saved =
        Category.builder().id(1L).name("Entradas").description("Appetizers").enabled(true).build();

    when(categoryRepositoryPort.save(any(Category.class))).thenReturn(saved);

    Category result = categoryUseCase.create(category);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertTrue(result.isEnabled());
  }

  @Test
  void shouldUpdateCategorySuccessfully() {
    Category existing =
        Category.builder().id(1L).name("Entradas").description("Old").enabled(true).build();
    Category updateData = Category.builder().name("Fuertes").description("Main courses").build();
    Category saved =
        Category.builder().id(1L).name("Fuertes").description("Main courses").enabled(true).build();

    when(categoryRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(categoryRepositoryPort.save(any(Category.class))).thenReturn(saved);

    Category result = categoryUseCase.update(1L, updateData);

    assertEquals("Fuertes", result.getName());
    assertEquals("Main courses", result.getDescription());
  }

  @Test
  void shouldThrowWhenUpdatingNonExistentCategory() {
    when(categoryRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        CategoryNotFoundException.class,
        () -> categoryUseCase.update(99L, Category.builder().name("Test").build()));
  }

  @Test
  void shouldFindAllCategories() {
    when(categoryRepositoryPort.findAll())
        .thenReturn(
            List.of(
                Category.builder().id(1L).name("Entradas").build(),
                Category.builder().id(2L).name("Bebidas").build()));

    List<Category> result = categoryUseCase.findAll();

    assertEquals(2, result.size());
  }

  @Test
  void shouldToggleEnabledStatus() {
    Category existing = Category.builder().id(1L).name("Entradas").enabled(true).build();
    Category saved = Category.builder().id(1L).name("Entradas").enabled(false).build();

    when(categoryRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(categoryRepositoryPort.save(any(Category.class))).thenReturn(saved);

    Category result = categoryUseCase.toggleEnabled(1L);

    assertFalse(result.isEnabled());
  }
}
