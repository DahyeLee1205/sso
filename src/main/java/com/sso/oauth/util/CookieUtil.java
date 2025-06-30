package com.sso.oauth.util;

import com.sso.common.entity.dto.ResultDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@Slf4j
public class CookieUtil {
    public static void addTokenCookies(HttpServletResponse response, ResultDto resultDto){
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", resultDto.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", resultDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        log.info("CookieUtil - addTokenCookies addHeader finished.");
    }
}
