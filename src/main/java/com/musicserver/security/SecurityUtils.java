package com.musicserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security工具类
 * 
 * 提供当前用户信息获取等安全相关的实用方法
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     * 
     * @return 当前用户ID，如果未登录则抛出异常
     */
    public static Long getCurrentUserId() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }
        throw new RuntimeException("无法获取当前用户ID");
    }

    /**
     * 获取当前登录用户ID（可为空）
     * 
     * @return 当前用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 当前用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前用户详情
     * 
     * @return 当前用户详情，如果未登录则返回null
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    /**
     * 获取当前认证信息
     * 
     * @return 当前认证信息
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称
     * @return 是否具有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> 
                        grantedAuthority.getAuthority().equals("ROLE_" + role) ||
                        grantedAuthority.getAuthority().equals(role));
        }
        return false;
    }

    /**
     * 检查当前用户是否具有指定权限
     * 
     * @param permission 权限名称
     * @return 是否具有指定权限
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> 
                        grantedAuthority.getAuthority().equals(permission));
        }
        return false;
    }

    /**
     * 清除当前用户的安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}