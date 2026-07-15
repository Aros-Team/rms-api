package aros.services.rms.infraestructure.common.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.common.money.domain.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoneyJsonSerializerTest {

  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Money.class, new MoneyJsonSerializer());
    mapper.registerModule(module);
  }

  @Test
  void shouldSerializeMoneyToJsonObject() throws Exception {
    Money money = new Money(new java.math.BigDecimal("1.20"), Currency.getInstance("COP"));
    String json = mapper.writeValueAsString(money);
    assertEquals("{\"amount\":1.20,\"currency\":\"COP\"}", json);
  }

  @Test
  void shouldSerializeNegativeAmount() throws Exception {
    Money money = new Money(new java.math.BigDecimal("-50.00"), Currency.getInstance("USD"));
    String json = mapper.writeValueAsString(money);
    assertEquals("{\"amount\":-50.00,\"currency\":\"USD\"}", json);
  }

  @Test
  void shouldSerializeZeroAmount() throws Exception {
    Money money = Money.zero(Currency.getInstance("COP"));
    String json = mapper.writeValueAsString(money);
    assertEquals("{\"amount\":0.00,\"currency\":\"COP\"}", json);
  }

  @Test
  void shouldSerializeZeroFractionCurrency() throws Exception {
    Currency jpy = Currency.getInstance("JPY");
    Money money = new Money(new java.math.BigDecimal("500"), jpy);
    String json = mapper.writeValueAsString(money);
    assertEquals("{\"amount\":500,\"currency\":\"JPY\"}", json);
  }
}
