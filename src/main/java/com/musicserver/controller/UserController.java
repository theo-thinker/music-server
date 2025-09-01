package com.musicserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.common.Result;
import com.musicserver.dto.request.PageRequest;
import com.musicserver.dto.response.PageResponse;
import com.musicserver.dto.response.UserInfoResponse;
import com.musicserver.entity.User;
import com.musicserver.entity.UserProfile;
import com.musicserver.service.UserService;
import com.musicserver.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户管理控制器
 * 
 * 处理用户信息查询、更新等请求
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户信息查询、更新等接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasRole('USER')")
    public Result<UserInfoResponse> getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取当前用户信息: userId={}", currentUserId);
        
        User user = userService.findById(currentUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        
        return Result.success(response);
    }

    /**
     * 更新当前用户信息
     * 
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的基本信息")
    @PreAuthorize("hasRole('USER')")
    public Result<UserInfoResponse> updateCurrentUser(@Valid @RequestBody User user) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("更新当前用户信息: userId={}", currentUserId);
        
        // 设置用户ID，确保只能更新自己的信息
        user.setId(currentUserId);
        
        // 清除敏感字段，防止恶意修改
        user.setPassword(null);
        user.setStatus(null);
        user.setLevel(null);
        user.setExperience(null);
        
        User updatedUser = userService.updateUser(user);
        
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(updatedUser, response);
        
        return Result.success(response, "用户信息更新成功");
    }

    /**
     * 修改密码
     * 
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PutMapping("/me/password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                      @RequestParam String newPassword) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("用户修改密码: userId={}", currentUserId);
        
        boolean success = userService.updatePassword(currentUserId, oldPassword, newPassword);
        if (success) {
            return Result.success(null, "密码修改成功");
        } else {
            return Result.error("密码修改失败");
        }
    }

    /**
     * 获取用户配置
     * 
     * @return 用户配置信息
     */
    @GetMapping("/me/profile")
    @Operation(summary = "获取用户配置", description = "获取当前用户的个性化配置")
    @PreAuthorize("hasRole('USER')")
    public Result<UserProfile> getUserProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取用户配置: userId={}", currentUserId);
        
        UserProfile profile = userService.getUserProfile(currentUserId);
        return Result.success(profile);
    }

    /**
     * 更新用户配置
     * 
     * @param profile 用户配置
     * @return 更新后的配置
     */
    @PutMapping("/me/profile")
    @Operation(summary = "更新用户配置", description = "更新当前用户的个性化配置")
    @PreAuthorize("hasRole('USER')")
    public Result<UserProfile> updateUserProfile(@Valid @RequestBody UserProfile profile) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("更新用户配置: userId={}", currentUserId);
        
        // 设置用户ID，确保只能更新自己的配置
        profile.setUserId(currentUserId);
        
        UserProfile updatedProfile = userService.updateUserProfile(profile);
        return Result.success(updatedProfile, "用户配置更新成功");
    }

    /**
     * 根据ID获取用户信息（公开信息）
     * 
     * @param id 用户ID
     * @return 用户公开信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户公开信息")
    public Result<UserInfoResponse> getUserById(@PathVariable Long id) {
        log.info("获取用户信息: userId={}", id);
        
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        
        // 清除敏感信息
        response.setEmail(null);
        response.setLastLoginTime(null);
        
        return Result.success(response);
    }

    /**
     * 分页查询用户列表（管理员功能）
     * 
     * @param pageRequest 分页请求参数
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表", description = "分页查询用户列表（管理员功能）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResponse<UserInfoResponse>> getUserList(@Valid PageRequest pageRequest) {
        log.info("分页查询用户列表: page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        
        Page<User> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var userPage = userService.getUserList(page, pageRequest.getKeyword(), null);
        
        // 转换为响应DTO
        var responseList = userPage.getRecords().stream()
                .map(user -> {
                    UserInfoResponse response = new UserInfoResponse();
                    BeanUtils.copyProperties(user, response);
                    return response;
                }).toList();
        
        // 构建分页响应
        PageResponse<UserInfoResponse> pageResponse = new PageResponse<>(
                responseList,
                userPage.getTotal(),
                (int) userPage.getSize(),
                (int) userPage.getCurrent()
        );
        
        return Result.success(pageResponse);
    }

    /**
     * 禁用用户（管理员功能）
     * 
     * @param id 用户ID
     * @param reason 禁用原因
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户（管理员功能）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> disableUser(@PathVariable Long id, @RequestParam String reason) {
        log.info("禁用用户: userId={}, reason={}", id, reason);
        
        boolean success = userService.disableUser(id, reason);
        if (success) {
            return Result.success(null, "用户禁用成功");
        } else {
            return Result.error("用户禁用失败");
        }
    }

    /**
     * 启用用户（管理员功能）
     * 
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用用户", description = "启用指定用户（管理员功能）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> enableUser(@PathVariable Long id) {
        log.info("启用用户: userId={}", id);
        
        boolean success = userService.enableUser(id);
        if (success) {
            return Result.success(null, "用户启用成功");
        } else {
            return Result.error("用户启用失败");
        }
    }

    /**
     * 获取用户等级信息
     * 
     * @param id 用户ID
     * @return 用户等级信息
     */
    @GetMapping("/{id}/level")
    @Operation(summary = "获取用户等级", description = "获取用户等级和经验值信息")
    public Result<UserInfoResponse> getUserLevel(@PathVariable Long id) {
        log.info("获取用户等级信息: userId={}", id);
        
        User user = userService.getUserLevel(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setLevel(user.getLevel());
        response.setExperience(user.getExperience());
        
        return Result.success(response);
    }
}