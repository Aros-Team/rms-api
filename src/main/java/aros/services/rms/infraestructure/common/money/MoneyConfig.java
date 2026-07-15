package aros.services.rms.infraestructure.common.money;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.common.config.AppMoneyProperties;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers Jackson module for Money serialization and config properties. */
@Configuration
@EnableConfigurationProperties(AppMoneyProperties.class)
public class MoneyConfig {

  /** Registers the Money serialization module for Jackson. */
  @Bean
  public SimpleModule moneyJsonSerializerModule() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Money.class, new MoneyJsonSerializer());
    return module;
  }
}
