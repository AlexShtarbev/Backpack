package org.alexshtarbev.backpack;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {OpenAiAutoConfiguration.class})
@EnableAutoConfiguration
public class BackpackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackpackApplication.class, args);
    }

}
