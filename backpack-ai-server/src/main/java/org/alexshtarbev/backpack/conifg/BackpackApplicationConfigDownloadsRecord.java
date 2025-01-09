package org.alexshtarbev.backpack.conifg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public record BackpackApplicationConfigDownloadsRecord(
        String downloadsDirectory, String cookiesFilePath, int maxParallelDownloads) { }
