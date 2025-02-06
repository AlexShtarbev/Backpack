package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties
public record BackpackApplicationConfigDatasource(
    String url,
    String username,
    String password,
    @NestedConfigurationProperty BackpackApplicationConfigHikari hikari) {}
