package com.sso.oauth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider")
public record OAuth2ProviderProperties(ProviderConfig kakao) {
    public record ProviderConfig(
            String authorizationUri,
            String tokenUri,
            String userInfoUri,
            String userNameAttribute
    ){}
}

