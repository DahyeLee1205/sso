package com.sso.oauth.service;

import com.sso.oauth.dto.OAuth2AuthInfoDto;
import com.sso.common.entity.dto.ResultDto;
import org.springframework.stereotype.Service;

@Service("OAuth2Service")
public interface OAuth2Service {
    ResultDto kakaoLogin(OAuth2AuthInfoDto oAuth2AuthInfoDto);
}
