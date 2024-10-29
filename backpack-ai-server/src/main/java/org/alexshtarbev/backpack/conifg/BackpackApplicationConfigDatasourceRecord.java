package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties
public record BackpackApplicationConfigDatasourceRecord(
    String url,
    String username,
    String password,
    @NestedConfigurationProperty BackpackApplicationConfigHikariRecord hikari) {}
