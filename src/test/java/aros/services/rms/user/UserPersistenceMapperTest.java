package aros.services.rms.user;

import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.infraestructure.area.persistence.AreaType;
import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.area.persistence.jpa.AreaMapper;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import aros.services.rms.infraestructure.user.persistence.jpa.UserPersistenceMapper;
import aros.services.rms.infraestructure.user.persistence.jpa.UserPersistenceMapperImpl;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Unit tests for the UserPersistenceMapper. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserPersistenceMapperImpl.class, AreaMapper.class})
public class UserPersistenceMapperTest {

  @Autowired
  private UserPersistenceMapper userMapper = Mappers.getMapper(UserPersistenceMapper.class);

  @Test
  public void convertEntityToUserWithAreas() {
    Area area1 = new Area();
    area1.setId(1L);
    area1.setName("Cocina");
    area1.setEnabled(true);
    area1.setType(AreaType.KITCHEN);

    UserEntity entity = new UserEntity();
    entity.setId(10L);
    entity.setName("Brahian");
    entity.setEmail("example@test.com");
    entity.setDocument("123");
    entity.setPassword("123");
    entity.setAssignedAreas(List.of(area1));

    UserWithAreas mapped = userMapper.toUserWithAreas(entity);

    Assertions.assertNotNull(mapped);
    Assertions.assertNotNull(mapped.assignedAreas());
    Assertions.assertEquals(1, mapped.assignedAreas().size());
    Assertions.assertEquals("Cocina", mapped.assignedAreas().getFirst().getName());
    Assertions.assertEquals(1L, mapped.assignedAreas().getFirst().getId());
    Assertions.assertTrue(
        mapped.assignedAreas().getFirst() instanceof aros.services.rms.core.area.domain.Area);
  }

  @Test
  @WithMockUser(username = "Juan")
  public void proof() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Assertions.assertEquals("Juan", auth.getName());
    Assertions.assertTrue(auth.isAuthenticated());
    Assertions.assertFalse("anonymousUser".equals(auth.getPrincipal()));
  }
}
