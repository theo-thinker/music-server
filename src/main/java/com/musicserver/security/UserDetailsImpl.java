package com.musicserver.security;

import com.musicserver.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security用户详情实现类
 * 
 * 实现UserDetails接口，封装用户信息和权限
 * 用于Spring Security的认证和授权
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /**
     * 用户实体对象
     */
    private final User user;

    /**
     * 用户权限列表
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * 根据用户实体创建UserDetails对象
     * 
     * @param user 用户实体
     * @return UserDetails实现
     */
    public static UserDetailsImpl create(User user) {
        // 根据用户等级分配角色
        List<GrantedAuthority> authorities = getUserAuthorities(user);
        
        return new UserDetailsImpl(user, authorities);
    }

    /**
     * 根据用户信息获取权限列表
     * 
     * @param user 用户实体
     * @return 权限列表
     */
    private static List<GrantedAuthority> getUserAuthorities(User user) {
        // 基础用户角色
        if (user.getLevel() != null && user.getLevel() >= 10) {
            // 高级用户权限
            return List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_VIP")
            );
        } else if (user.getLevel() != null && user.getLevel() >= 50) {
            // 管理员权限（假设50级以上为管理员）
            return List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_VIP"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        } else {
            // 普通用户权限
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    /**
     * 获取用户权限
     * 
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 获取用户密码
     * 
     * @return 密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 获取用户昵称
     * 
     * @return 用户昵称
     */
    public String getNickname() {
        return user.getNickname();
    }

    /**
     * 获取用户邮箱
     * 
     * @return 用户邮箱
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 账户是否未过期
     * 
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * 
     * @return true-未锁定，false-已锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        // 根据用户状态判断：2-冻结状态视为锁定
        return user.getStatus() != null && !user.getStatus().equals(2);
    }

    /**
     * 凭证（密码）是否未过期
     * 
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否已启用
     * 
     * @return true-已启用，false-已禁用
     */
    @Override
    public boolean isEnabled() {
        // 根据用户状态判断：1-正常状态为启用
        return user.getStatus() != null && user.getStatus().equals(1);
    }

    /**
     * 判断用户是否有指定权限
     * 
     * @param authority 权限字符串
     * @return true-有权限，false-无权限
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * 判断用户是否有指定角色
     * 
     * @param role 角色名称（不包含ROLE_前缀）
     * @return true-有角色，false-无角色
     */
    public boolean hasRole(String role) {
        return hasAuthority("ROLE_" + role);
    }

    /**
     * 判断是否为管理员
     * 
     * @return true-是管理员，false-不是管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 判断是否为VIP用户
     * 
     * @return true-是VIP，false-不是VIP
     */
    public boolean isVip() {
        return hasRole("VIP");
    }
}