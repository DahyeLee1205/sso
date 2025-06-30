package com.sso.common.entity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ResultDto {
    private String resultCode;
    private String resultMsg;
    private String accessToken;
    private String refreshToken;

    @Builder
    public ResultDto(String resultCode, String resultMsg, String accessToken, String refreshToken) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
