package com.teamexp.learnflowapi.global.security.principal;

import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {

    private final String id;
    private final String nickname;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean delFlag;

    public CustomUserPrincipal(User user) {
        this.id = user.getUserId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.delFlag = user.getDelFlag();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isEnabled() {
        return !delFlag; // 논리 삭제된 계정이면 false
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }
}