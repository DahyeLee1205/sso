package com.sso.oauth.dto;

import lombok.*;

@Getter
public class OAuth2AuthInfoDto {
    private String code;    // 인가코드
    private String errCd; // 에러코드
    private String errMsg;  // 에러메시지
    private String state;   // 요청 시 전달한 state 값과 동일

    @Builder
    public OAuth2AuthInfoDto(String code, String errCd, String errMsg, String state){
        this.code = code;
        this.errCd = errCd;
        this.errMsg = errMsg;
        this.state = state;
    }
}
