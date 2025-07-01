package com.sso.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sso.oauth.config.RestTemplateConfig;
import com.sso.oauth.config.properties.OAuth2ProviderProperties;
import com.sso.oauth.config.properties.OAuth2RegistrationProperties;
import com.sso.common.entity.dto.ResultDto;
import com.sso.oauth.repository.OAuth2Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.sso.oauth.dto.OAuth2AuthInfoDto;
import com.sso.oauth.dto.OAuth2KakaoUserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import com.sso.oauth.dto.OAuth2TokenInfoDto;
import com.sso.oauth.entity.User;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service("OAuth2ServiceImpl")
public class OAuth2ServiceImpl implements OAuth2Service {

    private final RestTemplateConfig restTemplateConfig;
    private final OAuth2ProviderProperties oAuthProvider;
    private final OAuth2RegistrationProperties oAuthRegistration;
    private final OAuth2Repository OAuth2Repository;
    private static final String LOG_NAME = "OAuth2ServiceImpl";

    public OAuth2ServiceImpl(RestTemplateConfig restTemplateConfig, OAuth2ProviderProperties oAuthProvider, OAuth2RegistrationProperties oAuthRegistration, com.sso.oauth.repository.OAuth2Repository oAuth2Repository) {
        this.restTemplateConfig = restTemplateConfig;
        this.oAuthProvider = oAuthProvider;
        this.oAuthRegistration = oAuthRegistration;
        this.OAuth2Repository = oAuth2Repository;
    }

    private HttpHeaders defaultHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return headers;
    }

    @Override
    public ResultDto kakaoLogin(OAuth2AuthInfoDto authInfo) {

        log.debug(LOG_NAME + "kakaoLogin OAuth2AuthInfoDto info", authInfo);
        log.debug(LOG_NAME + "code : {}", authInfo.getCode());
        log.debug(LOG_NAME + "errorCd : {}", authInfo.getErrCd());
        log.debug(LOG_NAME + "errMsg : {}", authInfo.getErrMsg());
        log.debug(LOG_NAME + "state : {}", authInfo.getState());

        if (authInfo.getCode() == null || authInfo.getCode().isEmpty()) {
            log.error(LOG_NAME + " [-] 카카오 로그인 리다이렉션에서 문제가 발생하였습니다.");
            return null;
        }

        // [STEP2] 카카오로 토큰을 요청(접근 토큰, 갱신 토큰)
        OAuth2TokenInfoDto kakaoTokenInfo = this.getKakaoTokenInfo(authInfo.getCode());
        log.debug(LOG_NAME + " - getKakaoTokenInfo :: {}", kakaoTokenInfo);

        // [STEP3] 접근 토큰을 기반으로 사용자 정보를 요청
        OAuth2KakaoUserInfoDto userInfo = this.getKakaoUserInfo(kakaoTokenInfo.getAccessToken());
        log.debug(LOG_NAME + " - userInfo :: {}", userInfo);

        // [STEP4] 사용자 정보와 SP의 사용자 정보 비교
        ResultDto userResultDto = this.getUserInfo(userInfo);

        // [STEP5] 사용자 정보 있을 경우 : Token 정보 저장
        if(!userResultDto.getResultCode().equals("000")) {
            // 다시 요청
            return userResultDto;
        }
        ResultDto saveTokenResult = this.saveTokenInfo(userInfo, kakaoTokenInfo);
        if(!saveTokenResult.getResultCode().equals("000")) {
            return saveTokenResult;
        }

        ResultDto resultDto = saveTokenResult.toBuilder()
                .accessToken(kakaoTokenInfo.getAccessToken())
                .refreshToken(kakaoTokenInfo.getRefreshToken())
                .build();

        return resultDto;
    }

    private OAuth2TokenInfoDto getKakaoTokenInfo(String authCode){
        log.info(LOG_NAME + " [+] getKakaoTokenInfo start :: {}", authCode);

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
            log.error(LOG_NAME + " getKakaoTokenInfo Exception occurred.", e);
            throw new RuntimeException(LOG_NAME + " getKakaoTokenInfo Exception occurred");
        }

        if(responseTokenInfo != null & responseTokenInfo.getBody() != null && responseTokenInfo.getStatusCode().is2xxSuccessful()){
            Map<String, Object> body = responseTokenInfo.getBody();
            resultDto = OAuth2TokenInfoDto.builder()
            .accessToken(body.get("access_token").toString())
            .refreshToken(body.get("refresh_token").toString())
            .tokenType(body.get("token_type").toString())
            .build();
        }else if(responseTokenInfo == null || responseTokenInfo.getBody() == null){
            log.error(LOG_NAME + " getKakaoTokenInfo - responseTokenInfo is not exists.");
        }else if(!responseTokenInfo.getStatusCode().is2xxSuccessful()){
            log.error(LOG_NAME + " getKakaoTokenInfo - responseTokenInfo request failed.");
        }
        return resultDto;
    }

    private OAuth2KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        log.debug("[+] getKakaoUserInfo :: {}", accessToken);

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
            log.debug(LOG_NAME + " responseUserInfo :: {}", responseUserInfo);

        } catch (Exception e) {
            log.error(LOG_NAME + " [-] responseUserInfo response failed. {}", e.getMessage());
        }

        // [STEP4] 사용자 정보가 존재한다면 값을 불러와서 OAuth2KakaoUserInfoDto 객체로 구성하여 반환
        if (responseUserInfo != null && responseUserInfo.getBody() != null && responseUserInfo.getStatusCode().is2xxSuccessful()) {
            Map<String,Object> body = responseUserInfo.getBody();

            if (body == null) {
                log.error("getKakaoUserInfo - cannot find user info.");
            }
            Map<String,Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            if(kakaoAccount == null){
                resultDto = OAuth2KakaoUserInfoDto.builder()
                        .id(body.get("id").toString())                                      // 사용자 아이디 번호
                        .statusCode(responseUserInfo.getStatusCode().value())               // 상태 코드
                        .build();
            }else{
                log.info("getKakaoUserInfo - kakaoAccount  :: {}", kakaoAccount);
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                String email = (String) kakaoAccount.get("email");
                String nickname = (String) profile.get("nickname");
                String profileImageUrl = (String) profile.get("profile_image_url");

                log.debug("getKakaoUserInfo - profile in kakaoAccount  :: {}", profile);
                resultDto = OAuth2KakaoUserInfoDto.builder()
                        .id(body.get("id").toString())                                      // 사용자 아이디 번호
                        .statusCode(responseUserInfo.getStatusCode().value())               // 상태 코드
                        .email(email)
                        .profileImageUrl(profileImageUrl)
                        .nickname(nickname)
                        .build();
            }
            log.info("getKakaoUserInfo - OAuth2KakaoUserInfoDto  :: {}", resultDto);
        }
        return resultDto;
    }

    private Map<String, Object> cvtObjectToMap(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(obj, new TypeReference<>() {
        });
    }

    private ResultDto getUserInfo(OAuth2KakaoUserInfoDto userInfo){
        // 사용자정보를 받아서 없으면 : 저장 / 있으면 : 성공
        ResultDto resultDto = null;
        String kakaoUserId = userInfo.getId();
        User user = OAuth2Repository.findByUserId(kakaoUserId);

        if(user == null){
            log.info(LOG_NAME + "getUserInfo - user info is null.");
            this.insertUserInfo(userInfo);
            resultDto = this.resultCannotFindUser();
            return resultDto;
        }else{
            return this.resultSuccess();
        }
    }

    private ResultDto insertUserInfo(OAuth2KakaoUserInfoDto userInfo){
        ResultDto resultDto = null;
        try{
            User saveParams = User.builder()
                    .userId(userInfo.getId())
                    .email(StringUtils.hasText(userInfo.getEmail()) ? userInfo.getEmail() : "")
                    .nickName(StringUtils.hasText(userInfo.getNickname()) ? userInfo.getNickname() : "")
                    .profile(StringUtils.hasText(userInfo.getProfileImageUrl()) ? userInfo.getProfileImageUrl() : "")
                    .build();
            OAuth2Repository.save(saveParams);
            log.info(LOG_NAME + "insertUserInfo saveParams : " + saveParams);
            resultDto = this.resultSuccess();

        }catch (Exception e){
            log.error(LOG_NAME + "insertUserInfo | " + e.getMessage(), e);
            resultDto = this.resultFail(e.getMessage());
        }
        return resultDto;
    }

    private ResultDto saveTokenInfo(OAuth2KakaoUserInfoDto userInfo, OAuth2TokenInfoDto tokenInfo){
        ResultDto resultDto = null;

        // 사용자 조회
        User findUser = OAuth2Repository.findByUserId(userInfo.getId());
        if(findUser == null){
            log.error(LOG_NAME + "saveTokenInfo | cannot find userInfo.");
            String errMsg = "cannot find userInfo";
            return this.resultCannotFindUser();
        }
        // 기존 refreshToken 덮어쓰기
        findUser.updateRefreshToken(tokenInfo.getRefreshToken());
        OAuth2Repository.save(findUser);

        resultDto = this.resultSuccess();
        return resultDto;
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
