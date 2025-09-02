package com.musicserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.User;
import com.musicserver.entity.UserProfile;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 提供用户相关的业务逻辑处理，包括：
 * 1. 用户注册、登录、注销
 * 2. 用户信息查询、更新
 * 3. 用户配置管理
 * 4. 用户状态管理
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册成功的用户信息
     */
    User register(User user);

    /**
     * 用户登录验证
     *
     * @param username 用户名或邮箱
     * @param password 密码
     * @return 验证通过的用户信息
     */
    User login(String username, String password);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户信息
     */
    User findByEmail(String email);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User findById(Long id);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 更新用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @param reason 禁用原因
     * @return 是否成功
     */
    boolean disableUser(Long userId, String reason);

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean enableUser(Long userId);

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 分页查询用户列表
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @param status  用户状态
     * @return 用户分页列表
     */
    IPage<User> getUserList(Page<User> page, String keyword, Integer status);

    /**
     * 获取用户配置信息
     *
     * @param userId 用户ID
     * @return 用户配置
     */
    UserProfile getUserProfile(Long userId);

    /**
     * 更新用户配置
     *
     * @param userProfile 用户配置
     * @return 更新后的配置
     */
    UserProfile updateUserProfile(UserProfile userProfile);

    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @return 是否可用
     */
    boolean isUsernameAvailable(String username);

    /**
     * 检查邮箱是否可用
     *
     * @param email 邮箱地址
     * @return 是否可用
     */
    boolean isEmailAvailable(String email);

    /**
     * 更新用户最后登录信息
     *
     * @param userId    用户ID
     * @param ipAddress IP地址
     */
    void updateLastLoginInfo(Long userId, String ipAddress);

    /**
     * 获取用户等级信息
     *
     * @param userId 用户ID
     * @return 用户等级相关信息
     */
    User getUserLevel(Long userId);

    /**
     * 增加用户经验值
     *
     * @param userId     用户ID
     * @param experience 经验值
     * @return 是否成功
     */
    boolean addUserExperience(Long userId, Long experience);

    /**
     * 根据等级查询用户列表
     *
     * @param level 用户等级
     * @return 用户列表
     */
    List<User> getUsersByLevel(Integer level);
}