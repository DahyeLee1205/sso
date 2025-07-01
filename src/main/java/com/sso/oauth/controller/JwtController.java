package com.sso.oauth.controller;

import com.sso.common.entity.dto.ResultDto;
import com.sso.oauth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/token")
public class JwtController {
    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/getAccessToken")
    public ResponseEntity getAccessToken(@RequestBody Map<String, String> requestMap){

        String userId = requestMap.get("userId");
        String refreshToken = requestMap.get("refreshToken");

        ResultDto resultObj = jwtService.reissueAccessToken(userId, refreshToken);
        return new ResponseEntity<>(resultObj, HttpStatus.OK);
    }

}
