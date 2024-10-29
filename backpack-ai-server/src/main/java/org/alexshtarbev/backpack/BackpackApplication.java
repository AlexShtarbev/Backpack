package org.alexshtarbev.backpack;

import org.alexshtarbev.backpack.conifg.BackpackApplicationConfigRecord;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
    exclude = {
      OpenAiAutoConfiguration.class,
      DataSourceAutoConfiguration.class,
      JooqAutoConfiguration.class
    })
@EnableConfigurationProperties({
  BackpackApplicationConfigRecord.class,
  OpenAiConnectionProperties.class
})
public class BackpackApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackpackApplication.class, args);
  }
}
