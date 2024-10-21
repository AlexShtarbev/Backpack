package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public record BackpackApplicationConfigHikariRecord(
        int maximumPoolSize,
        int maxLifetime,
        String poolName
) {
}
