package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(value = "backpack")
public record BackpackApplicationConfigRecord(
        @NestedConfigurationProperty BackpackApplicationConfigDownloadsRecord downloads,
        @NestedConfigurationProperty BackpackApplicationConfigDatasourceRecord datasource) {}
