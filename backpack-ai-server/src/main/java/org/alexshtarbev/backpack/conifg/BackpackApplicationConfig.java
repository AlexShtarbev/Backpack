package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(value = "backpack")
public record BackpackApplicationConfig(
        @NestedConfigurationProperty BackpackApplicationDownloadsConfig download,
        @NestedConfigurationProperty BackpackApplicationConfigDatasource datasource) {}
