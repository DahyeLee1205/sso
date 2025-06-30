package com.sso.oauth.controller;

import com.sso.oauth.util.CookieUtil;
import com.sso.common.entity.dto.ResultDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sso.oauth.dto.OAuth2AuthInfoDto;
import com.sso.oauth.service.OAuth2Service;

@Slf4j
@RestController
@RequestMapping("api/v1/oauth2")
public class OAuth2Controller {
   
    private final OAuth2Service OAuth2Service;
    public OAuth2Controller(OAuth2Service OAuth2Service) {
        this.OAuth2Service = OAuth2Service;
    }
    
    @GetMapping("/kakao")
    public ResponseEntity kakaoLogin(
                @RequestParam(required = false) String code,
                @RequestParam(required = false) String error,
                @RequestParam(required = false) String error_description,
                @RequestParam(required = false) String state,
                HttpServletResponse response
    ){
        log.debug("Kakao Login - code: {}, error: {}, error_description: {}, state: {}", code, error, error_description, state);
        OAuth2AuthInfoDto reqDto = OAuth2AuthInfoDto.builder()
                .code(code)
                .errCd(error)
                .errMsg(error_description)
                .state(state)
                .build();
        ResultDto resultObj = OAuth2Service.kakaoLogin(reqDto);

        if(resultObj.getResultCode().equals("000")){
            CookieUtil.addTokenCookies(response, resultObj);
        }
        return new ResponseEntity<>(resultObj, HttpStatus.OK);
    }

}
