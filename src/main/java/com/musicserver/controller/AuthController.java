package com.musicserver.controller;

import com.musicserver.common.Result;
import com.musicserver.dto.request.UserLoginRequest;
import com.musicserver.dto.request.UserRegisterRequest;
import com.musicserver.dto.response.LoginResponse;
import com.musicserver.dto.response.UserInfoResponse;
import com.musicserver.entity.User;
import com.musicserver.service.UserService;
import com.musicserver.security.JwtTokenProvider;
import com.musicserver.common.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 认证控制器
 * 
 * 处理用户注册、登录、登出等认证相关请求
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证接口")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册
     * 
     * @param request 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<UserInfoResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());
        
        // 验证确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        // 执行注册
        User registeredUser = userService.register(user);
        
        // 构建响应
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(registeredUser, response);
        
        log.info("用户注册成功: userId={}", registeredUser.getId());
        return Result.success(response, "注册成功");
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求参数
     * @param httpRequest HTTP请求对象
     * @return 登录结果，包含访问令牌
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户账号密码登录")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                      HttpServletRequest httpRequest) {
        log.info("用户登录请求: username={}", request.getUsername());
        
        // 验证用户凭据
        User user = userService.login(request.getUsername(), request.getPassword());
        
        // 更新最后登录信息
        String ipAddress = WebUtils.getClientIpAddress(httpRequest);
        userService.updateLastLoginInfo(user.getId(), ipAddress);
        
        // 构建用户权限（简化处理，实际项目中应该从数据库获取用户角色权限）
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        // 生成JWT令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), authorities);
        String refreshToken = null;
        
        // 如果启用了刷新令牌功能
        try {
            refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        } catch (UnsupportedOperationException e) {
            log.debug("刷新令牌功能已禁用");
        }
        
        // 获取令牌过期时间
        long expiresIn = jwtTokenProvider.getRemainingValidityTime(accessToken);
        
        // 构建用户信息响应
        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);
        
        // 构建登录响应
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, expiresIn, userInfo);
        
        log.info("用户登录成功: userId={}", user.getId());
        return Result.success(loginResponse, "登录成功");
    }

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        log.info("刷新令牌请求");
        
        try {
            // 刷新访问令牌
            String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
            
            // 获取用户信息
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 获取令牌过期时间
            long expiresIn = jwtTokenProvider.getRemainingValidityTime(newAccessToken);
            
            // 构建用户信息响应
            UserInfoResponse userInfo = new UserInfoResponse();
            BeanUtils.copyProperties(user, userInfo);
            
            // 构建响应（保持原刷新令牌）
            LoginResponse loginResponse = new LoginResponse(newAccessToken, refreshToken, expiresIn, userInfo);
            
            log.info("令牌刷新成功: userId={}", userId);
            return Result.success(loginResponse, "令牌刷新成功");
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            return Result.error("令牌刷新失败：" + e.getMessage());
        }
    }

    /**
     * 用户登出
     * 
     * @param httpRequest HTTP请求对象
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        log.info("用户登出请求");
        
        // 从请求头获取令牌
        String authHeader = httpRequest.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        if (token != null) {
            // TODO: 将令牌加入黑名单（Redis实现）
            String jti = jwtTokenProvider.getJtiFromToken(token);
            log.info("令牌已加入黑名单: jti={}", jti);
        }
        
        log.info("用户登出成功");
        return Result.success(null, "登出成功");
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否可用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return Result.success(available, available ? "用户名可用" : "用户名已被使用");
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱地址
     * @return 是否可用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否可用")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return Result.success(available, available ? "邮箱可用" : "邮箱已被使用");
    }

    /**
     * 验证令牌有效性
     * 
     * @param httpRequest HTTP请求对象
     * @return 验证结果
     */
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证当前令牌是否有效")
    public Result<UserInfoResponse> validate(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            return Result.error("缺少访问令牌");
        }
        
        // 验证令牌
        JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateTokenSafely(token);
        if (!validationResult.isValid()) {
            return Result.error("令牌无效：" + validationResult.getMessage());
        }
        
        // 获取用户信息
        String username = validationResult.getUsername();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 构建用户信息响应
        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);
        
        return Result.success(userInfo, "令牌有效");
    }
}