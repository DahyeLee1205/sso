package com.sso.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.sso.oauth.config.RestTemplateConfig;
import com.sso.oauth.config.properties.OAuth2ProviderProperties;
import com.sso.oauth.config.properties.OAuth2RegistrationProperties;
import com.sso.oauth.dto.OAuth2AuthInfoDto;
import com.sso.oauth.dto.OAuth2KakaoUserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import com.sso.oauth.dto.OAuth2TokenInfoDto;

import java.util.Map;

@Slf4j
@Service("OAuth2ServiceImpl")
public class OAuth2ServiceImpl implements OAuth2Service {

    private final RestTemplateConfig restTemplateConfig;
    private final OAuth2ProviderProperties oAuthProvider;
    private final OAuth2RegistrationProperties oAuthRegistration;

    public OAuth2ServiceImpl(RestTemplateConfig restTemplateConfig, OAuth2ProviderProperties oAuthProvider, OAuth2RegistrationProperties oAuthRegistration) {
        this.restTemplateConfig = restTemplateConfig;
        this.oAuthProvider = oAuthProvider;
        this.oAuthRegistration = oAuthRegistration;
    }

    private HttpHeaders defaultHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return headers;
    }
    @Override
    public OAuth2KakaoUserInfoDto kakaoLogin(OAuth2AuthInfoDto authInfo) {
        log.debug("[+] 카카오 로그인이 성공하여 리다이렉트 되었습니다.", authInfo);
        log.debug("코드 값 확인 : {}", authInfo.getCode());
        log.debug("에러 값 확인 : {}", authInfo.getErrorCd());
        log.debug("에러 설명 값 확인 : {}", authInfo.getErrMsg());
        log.debug("상태 값 확인 : {}", authInfo.getState());

        if (authInfo.getCode() == null || authInfo.getCode().isEmpty()) {
            log.error("[-] 카카오 로그인 리다이렉션에서 문제가 발생하였습니다.");
            return null;
        }

        // [STEP2] 카카오로 토큰을 요청(접근 토큰, 갱신 토큰)
        OAuth2TokenInfoDto kakaoTokenInfo = this.getKakaoTokenInfo(authInfo.getCode());
        log.debug("토큰 정보 전체를 확인합니다 :: {}", kakaoTokenInfo);


        // [STEP3] 접근 토큰을 기반으로 사용자 정보를 요청
        OAuth2KakaoUserInfoDto userInfo = this.getKakaoUserInfo(kakaoTokenInfo.getAccessToken());
        log.debug("userInfo :: {}", userInfo);
        return userInfo;
    }

    private OAuth2TokenInfoDto getKakaoTokenInfo(String authCode){
        log.debug("[+] getKakaoTokenInfo 함수가 실행 됩니다. :: {}", authCode);

        OAuth2TokenInfoDto resultDto = null;
        ResponseEntity< Map<String, Object>> responseTokenInfo = null;

        // 카카오 토큰 URL 로 전송할 데이터 구성
        MultiValueMap<String, Object> requestParamMap = new LinkedMultiValueMap<>();
        requestParamMap.add("grant_type", "authorization_code");
        requestParamMap.add("client_id", oAuthRegistration.kakao().clientId());
        requestParamMap.add("redirect_uri", oAuthRegistration.kakao().redirectUri());
        requestParamMap.add("code", authCode);
        requestParamMap.add("client_secret", oAuthRegistration.kakao().clientSecret());
        HttpEntity<MultiValueMap<String, Object>> requestMap = new HttpEntity<>(requestParamMap, this.defaultHeader());

        try{
            responseTokenInfo = restTemplateConfig
                    .restTemplate()
                    .exchange(oAuthProvider.kakao().tokenUri(), HttpMethod.POST, requestMap, new ParameterizedTypeReference<>(){
                    });
        }catch (Exception e){
            log.error("getKakaoTokenInfo Exception occurred.", e);
        }

        if(responseTokenInfo != null & responseTokenInfo.getBody() != null && responseTokenInfo.getStatusCode().is2xxSuccessful()){
            Map<String, Object> body = responseTokenInfo.getBody();
            if(body != null){
                resultDto = OAuth2TokenInfoDto.builder()
                .accessToken(body.get("access_token").toString())
                .refreshToken(body.get("refresh_token").toString())
                .tokenType(body.get("token_type").toString())
                .build();
            }
        }else{
            log.error("getKakaoTokenInfo responseTokenInfo is not exists.");
        }
        return resultDto;
    }

    private OAuth2KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        log.debug("[+] getKakaoUserInfo을 수행합니다 :: {}", accessToken);

        ResponseEntity<Map<String, Object>> responseUserInfo = null;
        OAuth2KakaoUserInfoDto resultDto = null;

        // [STEP1] 필수 요청 Header 값 구성 : ContentType, Authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);        // accessToken 추가

        // [STEP2] 요청 파라미터 구성 : 원하는 사용자 정보
        MultiValueMap<String, Object> userInfoParam = new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            userInfoParam.add("property_keys", objectMapper.writeValueAsString(oAuthRegistration.kakao().scope()));   // 불러올 데이터 조회 (리스트 to 문자열 변환)
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<MultiValueMap<String, Object>> userInfoReq = new HttpEntity<>(userInfoParam, headers);

        // [STEP3] 요청 Header, 파라미터를 포함하여 사용자 정보 조회 URL로 요청을 수행합니다.
        try {
            responseUserInfo = restTemplateConfig.restTemplate()
                    .exchange(oAuthProvider.kakao().userInfoUri(), HttpMethod.POST, userInfoReq, new ParameterizedTypeReference<>() {
                    });
            log.debug("결과 값 :: {}", responseUserInfo);

        } catch (Exception e) {
            log.error("[-] 사용자 정보 요청 중에 오류가 발생하였습니다.{}", e.getMessage());
        }

        // [STEP4] 사용자 정보가 존재한다면 값을 불러와서 OAuth2KakaoUserInfoDto 객체로 구성하여 반환
        if (responseUserInfo != null && responseUserInfo.getBody() != null && responseUserInfo.getStatusCode().is2xxSuccessful()) {
            Map<String,Object> body = responseUserInfo.getBody();

            if (body != null) {
                Map<String,Object> kakaoAccount = this.cvtObjectToMap(body.get("kakao_account"));
                Map<String,Object> profile = this.cvtObjectToMap(this.cvtObjectToMap(body.get("kakao_account")).get("profile"));
                resultDto = OAuth2KakaoUserInfoDto.builder()
                        .id(body.get("id").toString())                                      // 사용자 아이디 번호
                        .statusCode(responseUserInfo.getStatusCode().value())               // 상태 코드
                        .email(kakaoAccount.get("email").toString())                        // 이메일
                        .profileImageUrl(profile.get("profile_image_url").toString())
                        .nickname(profile.get("nickname").toString())
                        .build();
                log.debug("최종 구성 결과 :: {}", resultDto);
            }
        }
        return resultDto;
    }

    private Map<String, Object> cvtObjectToMap(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }
}
