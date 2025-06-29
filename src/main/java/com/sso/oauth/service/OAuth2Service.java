package com.sso.oauth.service;

import org.springframework.stereotype.Service;
import com.sso.oauth.dto.OAuth2AuthInfoDto;
import com.sso.oauth.dto.OAuth2KakaoUserInfoDto;

@Service("OAuth2Service")
public interface OAuth2Service {
    OAuth2KakaoUserInfoDto kakaoLogin(OAuth2AuthInfoDto oAuth2AuthInfoDto);
}
