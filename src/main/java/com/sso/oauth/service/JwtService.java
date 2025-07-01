package com.sso.oauth.service;

import com.sso.common.entity.dto.ResultDto;

public interface JwtService {
    public String createAccessToken(String userId);
    public String createRefreshToken(String userId);
    public ResultDto reissueAccessToken(String userId, String refreshToken);
}
