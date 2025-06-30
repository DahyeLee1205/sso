package com.sso.oauth.repository;

import com.sso.oauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2Repository extends JpaRepository<User, String> {
    User findByUserId(String userId);
}
