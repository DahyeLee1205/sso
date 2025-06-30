package com.sso.oauth.service;

import com.sso.common.entity.dto.ResultDto;
import com.sso.oauth.entity.User;
import com.sso.oauth.repository.OAuth2Repository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service("JwtServiceImpl")
public class JwtServiceImpl implements JwtService {

    private final OAuth2Repository OAuth2Repository;
    private static final String LOG_NAME = "JwtServiceImpl";
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ACCESS_EXP = 15 * 60 * 1000L;
    private static final long REFRESH_EXP = 7 * 24 * 60 * 60 * 1000L;

    public JwtServiceImpl(com.sso.oauth.repository.OAuth2Repository oAuth2Repository) {
        OAuth2Repository = oAuth2Repository;
    }


    @Override
    public String createAccessToken(String userId) {
        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                .signWith(key)
                .compact();
    }

    @Override
    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject("RefreshToken")
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .signWith(key)
                .compact();
    }

    @Override
    public ResultDto reissueAccessToken(String userId, String refreshToken) {

        ResultDto resultDto = null;
        User findUser = OAuth2Repository.findByUserId(userId);
        if(findUser == null){
            log.info(LOG_NAME + " getUserInfo - user info is null.");
            resultDto = this.resultCannotFindUser();
            return resultDto;
        }

        // 기존 refreshToken 일치 여부
        if(!findUser.getRefreshToken().equals(refreshToken)){
            log.info(LOG_NAME + " reissueAccessToken - Token mismatch.");
            resultDto = this.resultFail("Token mismatch.");
            return resultDto;
        }

        if(this.isExpiredRefreshToken(findUser)){
            log.info(LOG_NAME + " isExpiredRefreshToken - RefreshToken has Expired.");
            resultDto = this.resultFail("RefreshToken has Expired.");
            return resultDto;
        }

        String accessToken = this.createAccessToken(findUser.getUserId());
        return ResultDto.builder()
                .resultCode("000")
                .resultMsg("AccessToken 재발급")
                .accessToken(accessToken)
                .build();
    }

    private boolean isExpiredRefreshToken(User userInfo){
        long validDays = 7;
        return userInfo.getTokenCreateDate().plusDays(validDays).isBefore(LocalDateTime.now());
    }

    private ResultDto resultSuccess(){
        ResultDto resultDto = ResultDto.builder()
                .resultCode("000")
                .resultMsg("success")
                .build();
        return resultDto;
    }

    private ResultDto resultFail(String errMsg){
        ResultDto resultDto = ResultDto.builder()
                .resultCode("999")
                .resultMsg(StringUtils.hasText(errMsg) ? errMsg : "failed.")
                .build();
        return resultDto;
    }

    private ResultDto resultCannotFindUser(){
        ResultDto resultDto = ResultDto.builder()
                .resultCode("300")
                .resultMsg("cannot find userInfo.")
                .build();
        return resultDto;
    }
}
