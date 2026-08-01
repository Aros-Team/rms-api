/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Round-trip JSON tests for {@link ProductRequest} covering the new {@code optionExtras} field. */
class ProductRequestParsingTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void should_deserialize_optionExtras_when_present() throws Exception {
    String json =
        "{"
            + "\"name\":\"Burger\","
            + "\"basePrice\":12.50,"
            + "\"categoryId\":1,"
            + "\"areaId\":1,"
            + "\"optionIds\":[1,2,3],"
            + "\"optionExtras\":["
            + "  {\"optionId\":4,\"extraPrice\":2500.00},"
            + "  {\"optionId\":5,\"extraPrice\":1500.00}"
            + "]"
            + "}";

    ProductRequest request = mapper.readValue(json, ProductRequest.class);

    assertNotNull(request.optionExtras());
    assertEquals(2, request.optionExtras().size());
    assertEquals(4L, request.optionExtras().get(0).optionId());
    assertEquals(new BigDecimal("2500.00"), request.optionExtras().get(0).extraPrice());
    assertEquals(5L, request.optionExtras().get(1).optionId());
    assertEquals(new BigDecimal("1500.00"), request.optionExtras().get(1).extraPrice());
    assertEquals(Currency.getInstance("COP"), request.optionExtras().get(0).toMoney().currency());
    assertEquals(
        0, new BigDecimal("2500.00").compareTo(request.optionExtras().get(0).toMoney().amount()));
  }

  @Test
  void should_deserialize_when_optionExtras_is_absent() throws Exception {
    String json =
        "{"
            + "\"name\":\"Burger\","
            + "\"basePrice\":12.50,"
            + "\"categoryId\":1,"
            + "\"areaId\":1,"
            + "\"optionIds\":[1,2,3]"
            + "}";

    ProductRequest request = mapper.readValue(json, ProductRequest.class);

    assertNull(request.optionExtras());
  }

  @Test
  void should_deserialize_when_optionExtras_is_empty() throws Exception {
    String json =
        "{"
            + "\"name\":\"Burger\","
            + "\"basePrice\":12.50,"
            + "\"categoryId\":1,"
            + "\"areaId\":1,"
            + "\"optionIds\":[1,2,3],"
            + "\"optionExtras\":[]"
            + "}";

    ProductRequest request = mapper.readValue(json, ProductRequest.class);

    assertTrue(request.optionExtras().isEmpty());
  }

  @Test
  void should_deserialize_when_only_optionExtras_is_present_no_optionIds() throws Exception {
    String json =
        "{"
            + "\"name\":\"Burger\","
            + "\"basePrice\":12.50,"
            + "\"categoryId\":1,"
            + "\"areaId\":1,"
            + "\"optionExtras\":[{\"optionId\":4,\"extraPrice\":2500.00}]"
            + "}";

    ProductRequest request = mapper.readValue(json, ProductRequest.class);

    assertNull(request.optionIds());
    assertNotNull(request.optionExtras());
    assertEquals(1, request.optionExtras().size());
  }

  @Test
  void should_serialize_optionExtras_field_when_present() throws Exception {
    ProductRequest request =
        new ProductRequest(
            "Burger",
            null,
            12.50,
            1L,
            1L,
            List.of(1L, 2L),
            List.of(new OptionExtrasRequest(4L, new BigDecimal("2500.00"))),
            null,
            15);

    String json = mapper.writeValueAsString(request);
    JsonNode root = mapper.readTree(json);

    assertTrue(root.has("optionExtras"));
    assertTrue(root.get("optionExtras").isArray());
    assertEquals(1, root.get("optionExtras").size());
    assertEquals(4L, root.get("optionExtras").get(0).get("optionId").asLong());
    assertEquals(2500.00, root.get("optionExtras").get(0).get("extraPrice").asDouble(), 0.0001);
  }

  @Test
  void should_serialize_with_null_optionExtras() throws Exception {
    ProductRequest request =
        new ProductRequest("Burger", null, 12.50, 1L, 1L, List.of(1L), null, null, 15);

    String json = mapper.writeValueAsString(request);
    JsonNode root = mapper.readTree(json);

    assertTrue(root.has("optionExtras"));
    assertTrue(root.get("optionExtras").isNull());
  }
}
