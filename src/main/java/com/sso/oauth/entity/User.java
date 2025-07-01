package com.sso.oauth.entity;

import com.sso.common.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "sso_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
   // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private String userId;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickName;

    @Column(name = "profile")
    private String profile;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_create_date")
    private LocalDateTime tokenCreateDate;

    @Builder
    public User(String userId, String email, String nickName, String profile, String refreshToken) {
        this.userId = userId;
        this.email = email;
        this.nickName = nickName;
        this.profile = profile;
        this.refreshToken = refreshToken;
        this.tokenCreateDate = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
        this.tokenCreateDate = LocalDateTime.now();
    }
}
