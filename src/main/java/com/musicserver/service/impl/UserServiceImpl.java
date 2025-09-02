package com.musicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.User;
import com.musicserver.entity.UserProfile;
import com.musicserver.mapper.UserMapper;
import com.musicserver.mapper.UserProfileMapper;
import com.musicserver.service.UserService;
import com.musicserver.common.exception.BusinessException;
import com.musicserver.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * <p>
 * 实现用户相关的业务逻辑处理
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(User user) {
        log.info("用户注册: username={}, email={}", user.getUsername(), user.getEmail());

        // 检查用户名是否已存在
        if (!isUsernameAvailable(user.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        if (!isEmailAvailable(user.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        user.setStatus(1); // 正常状态
        user.setLevel(1); // 初始等级
        user.setExperience(0L); // 初始经验值

        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        // 创建用户配置
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getId());
        userProfile.setAutoPlay(1);
        userProfile.setQualityPreference(1);
        userProfile.setPlaybackMode(1);
        userProfile.setVolume(80);
        userProfile.setPrivacyLevel(1);
        userProfileMapper.insert(userProfile);

        log.info("用户注册成功: userId={}", user.getId());
        return user;
    }

    @Override
    public User login(String username, String password) {
        log.info("用户登录验证: username={}", username);

        // 根据用户名或邮箱查找用户
        User user = findByUsernameOrEmail(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        log.info("用户登录验证成功: userId={}", user.getId());
        return user;
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User findByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        log.info("更新用户信息: userId={}", user.getId());

        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户名是否被其他用户使用
        if (StringUtils.hasText(user.getUsername()) &&
                !user.getUsername().equals(existingUser.getUsername())) {
            if (!isUsernameAvailable(user.getUsername())) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
            }
        }

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(user.getEmail()) &&
                !user.getEmail().equals(existingUser.getEmail())) {
            if (!isEmailAvailable(user.getEmail())) {
                throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
            }
        }

        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        log.info("用户信息更新成功: userId={}", user.getId());
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("更新用户密码: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        int result = userMapper.updateById(user);

        log.info("用户密码更新成功: userId={}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(Long userId, String reason) {
        log.info("禁用用户: userId={}, reason={}", userId, reason);

        User user = new User();
        user.setId(userId);
        user.setStatus(0); // 禁用状态

        int result = userMapper.updateById(user);
        log.info("用户禁用成功: userId={}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(Long userId) {
        log.info("启用用户: userId={}", userId);

        User user = new User();
        user.setId(userId);
        user.setStatus(1); // 正常状态

        int result = userMapper.updateById(user);
        log.info("用户启用成功: userId={}", userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        log.info("删除用户: userId={}", userId);

        // 软删除 - 设置状态为删除
        User user = new User();
        user.setId(userId);
        user.setStatus(-1); // 删除状态

        int result = userMapper.updateById(user);
        log.info("用户删除成功: userId={}", userId);
        return result > 0;
    }

    @Override
    public IPage<User> getUserList(Page<User> page, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreatedTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public UserProfile getUserProfile(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        return userProfileMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfile updateUserProfile(UserProfile userProfile) {
        log.info("更新用户配置: userId={}", userProfile.getUserId());

        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userProfile.getUserId());
        UserProfile existingProfile = userProfileMapper.selectOne(wrapper);

        int result;
        if (existingProfile != null) {
            userProfile.setId(existingProfile.getId());
            result = userProfileMapper.updateById(userProfile);
        } else {
            result = userProfileMapper.insert(userProfile);
        }

        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        log.info("用户配置更新成功: userId={}", userProfile.getUserId());
        return userProfile;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectCount(wrapper) == 0;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return userMapper.selectCount(wrapper) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginInfo(Long userId, String ipAddress) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userMapper.updateById(user);
    }

    @Override
    public User getUserLevel(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserExperience(Long userId, Long experience) {
        log.info("增加用户经验值: userId={}, experience={}", userId, experience);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 增加经验值
        long newExperience = user.getExperience() + experience;

        // 计算新等级（简单的等级计算规则）
        int newLevel = calculateLevel(newExperience);

        user.setExperience(newExperience);
        user.setLevel(newLevel);

        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public List<User> getUsersByLevel(Integer level) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getLevel, level);
        wrapper.eq(User::getStatus, 1); // 只查询正常状态的用户
        wrapper.orderByDesc(User::getExperience);
        return userMapper.selectList(wrapper);
    }

    /**
     * 根据用户名或邮箱查找用户
     *
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户信息
     */
    private User findByUsernameOrEmail(String usernameOrEmail) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUsername, usernameOrEmail)
                .or().eq(User::getEmail, usernameOrEmail));
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据经验值计算用户等级
     *
     * @param experience 经验值
     * @return 用户等级
     */
    private int calculateLevel(long experience) {
        // 简单的等级计算规则：每1000经验值升1级
        return (int) (experience / 1000) + 1;
    }
}