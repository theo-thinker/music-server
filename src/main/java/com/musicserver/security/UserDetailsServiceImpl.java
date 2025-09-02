package com.musicserver.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.musicserver.entity.User;
import com.musicserver.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务实现类
 * <p>
 * 实现Spring Security的UserDetailsService接口
 * 从数据库加载用户信息用于认证和授权
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 用户数据访问接口
     */
    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名（可以是用户名、邮箱或手机号）
     * @return UserDetails用户详情对象
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("正在加载用户详情: {}", username);

        try {
            // 构建查询条件：支持用户名、邮箱、手机号登录
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                    .eq("username", username)
                    .or()
                    .eq("email", username)
                    .or()
                    .eq("phone", username)
            );

            // 查询用户
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                log.warn("用户不存在: {}", username);
                throw new UsernameNotFoundException("用户不存在: " + username);
            }

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus().equals(0)) {
                log.warn("用户已被禁用: {}", username);
                throw new UsernameNotFoundException("用户已被禁用: " + username);
            }

            log.debug("成功加载用户详情: userId={}, username={}", user.getId(), user.getUsername());

            // 创建UserDetails对象
            return UserDetailsImpl.create(user);

        } catch (Exception e) {
            log.error("加载用户详情失败: username={}", username, e);
            throw new UsernameNotFoundException("加载用户详情失败: " + username, e);
        }
    }
}