package aros.services.rms.infraestructure.common.money;

import aros.services.rms.core.common.money.domain.Money;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/** Jackson serializer for Money value object, emitting { amount, currency }. */
public class MoneyJsonSerializer extends JsonSerializer<Money> {

  @Override
  public void serialize(Money money, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeStartObject();
    gen.writeNumberField("amount", money.amount());
    gen.writeStringField("currency", money.currency().getCurrencyCode());
    gen.writeEndObject();
  }
}
