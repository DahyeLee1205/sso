package com.sso.oauth.dto;

import lombok.*;

@Getter
public class OAuth2AuthInfoDto {
    private String code;    // 인가코드
    private String errorCd; // 에러코드
    private String errMsg;  // 에러메시지
    private String state;   // 요청 시 전달한 state 값과 동일

    @Builder
    public OAuth2AuthInfoDto(String code, String errorCd, String errMsg, String state){
        this.code = code;
        this.errorCd = errorCd;
        this.errMsg = errMsg;
        this.state = state;
    }
}
