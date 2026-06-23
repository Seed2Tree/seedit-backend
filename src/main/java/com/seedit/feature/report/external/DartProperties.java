package com.seedit.feature.report.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="app.dart")
public record DartProperties (
        String apiKey,
        String baseUrl
){}
