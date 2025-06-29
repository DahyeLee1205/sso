const OauthLoginComponent = () => {
    const oAuthLoginHandler = (() => {
        return {
            /**
             * 카카오 로그인
             */
            kakao: () => {
                const kakaoAuthUrl = process.env.REACT_APP_API_OAUTH2_KAKAO_AUTH_URL as string;
                const kakaoClientId = process.env.REACT_APP_API_OAUTH2_KAKAO_CLIENT_ID as string;
                const kakaoRedirectUrl = process.env.REACT_APP_API_OAUTH2_KAKAO_REDIRECT_URL as string;
                window.location.href = `${kakaoAuthUrl}?client_id=${kakaoClientId}&redirect_uri=${kakaoRedirectUrl}&response_type=code;`
            },
        };
    })();

    return (
        <div style={{ width: '100%', textAlign: 'center' }}>
            <div style={{ marginBottom: 100 }}>
                <h1 style={{ textAlign: 'center' }}>OAuth 2.0 Login</h1>
            </div>
            <div
                style={{
                    flex: 1,
                    flexDirection: 'row',
                    justifyContent: 'center',
                    alignItems: 'center',
                }}>
                <div style={{}}>
                    <img
                        style={{ width: 150, height: 40, marginRight: 40 }}
                        src={'assets/icons/kakao_login_medium_narrow.png'}
                        onClick={oAuthLoginHandler.kakao}
                        alt='카카오'
                    />
                </div>
            </div>
        </div>
    );
};

export default OauthLoginComponent;