
## OAuth2.0

### **1-1. 사용 스택**

SpringBoot + JPA + Gradle + MySQL + JWT


### **1-2. 플로우**

    a.  카카오 사용자 정보 요청
    
    → 로그인 버튼 클릭
    
    → 카카오 인증 서버로 authrization Code 발급
    
     (카카오 인증서버 - 사용자 정보 세션에 존재여부 확인 : 로그인 유도 )
    
    → 카카오로부터 access_token 요청
    
    → 카카오로부터 access_token 으로 사용자 정보 조회
    
    → 전달받은 사용자 정보로 DB 에 있는 사용자 정보 조회
    
     있을 경우 : access_token 세션 쿠키에 저장, refresh_token DB에 저장 
    
     없을 경우 : 사용자 정보 DB 에 저장 후, 재요청 하도록 에러코드 반환



    b. JWT AccessToken 발급
    
    사용자가  access_token 발급요청 (userId, refreshToken)
    
    → 사용자 정보 조회
    
    → 사용자 정보의 refresh_token과 전달 받은 refreshToken 비교
    
    → refreshToken 만료 여부 확인
    
    → accessToken 세션 쿠키에 저장