package com.musicserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义用户详情类
 * 
 * 扩展Spring Security的UserDetails，添加用户ID字段
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class CustomUserDetails extends User {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 构造函数
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param password 密码
     * @param authorities 权限列表
     */
    public CustomUserDetails(Long userId, String username, String password, 
                           Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    /**
     * 构造函数
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param password 密码
     * @param enabled 是否启用
     * @param accountNonExpired 账户是否未过期
     * @param credentialsNonExpired 凭据是否未过期
     * @param accountNonLocked 账户是否未锁定
     * @param authorities 权限列表
     */
    public CustomUserDetails(Long userId, String username, String password, 
                           boolean enabled, boolean accountNonExpired, 
                           boolean credentialsNonExpired, boolean accountNonLocked,
                           Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }

    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }
}