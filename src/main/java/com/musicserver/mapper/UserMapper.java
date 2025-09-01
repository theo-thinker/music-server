package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问接口
 * 
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 * 同时定义用户相关的自定义查询方法
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户（支持用户名、邮箱、手机号）
     * 
     * @param identifier 用户标识（用户名、邮箱或手机号）
     * @return 用户信息
     */
    @Select("""
        SELECT * FROM users 
        WHERE (username = #{identifier} OR email = #{identifier} OR phone = #{identifier})
        AND status = 1
        """)
    User findByIdentifier(@Param("identifier") String identifier);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);

    /**
     * 更新用户最后登录信息
     * 
     * @param userId 用户ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @return 更新行数
     */
    @Update("""
        UPDATE users 
        SET last_login_time = #{loginTime}, last_login_ip = #{loginIp}, updated_time = NOW()
        WHERE id = #{userId}
        """)
    int updateLastLoginInfo(@Param("userId") Long userId, 
                           @Param("loginTime") LocalDateTime loginTime, 
                           @Param("loginIp") String loginIp);

    /**
     * 增加用户经验值
     * 
     * @param userId 用户ID
     * @param experience 经验值
     * @return 更新行数
     */
    @Update("""
        UPDATE users 
        SET experience = experience + #{experience}, updated_time = NOW()
        WHERE id = #{userId}
        """)
    int addUserExperience(@Param("userId") Long userId, @Param("experience") Long experience);

    /**
     * 分页查询活跃用户
     * 
     * @param page 分页参数
     * @param days 活跃天数
     * @return 分页结果
     */
    @Select("""
        SELECT * FROM users 
        WHERE status = 1 
        AND last_login_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
        ORDER BY last_login_time DESC
        """)
    IPage<User> selectActiveUsers(Page<User> page, @Param("days") int days);

    /**
     * 根据等级范围查询用户
     * 
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 用户列表
     */
    @Select("""
        SELECT * FROM users 
        WHERE status = 1 
        AND level BETWEEN #{minLevel} AND #{maxLevel}
        ORDER BY level DESC, experience DESC
        """)
    List<User> selectUsersByLevelRange(@Param("minLevel") Integer minLevel, 
                                      @Param("maxLevel") Integer maxLevel);

    /**
     * 获取用户统计信息
     * 
     * @return 统计数据Map（包含总数、活跃数、今日新增等）
     */
    @Select("""
        SELECT 
            COUNT(*) as total_count,
            SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as active_count,
            SUM(CASE WHEN DATE(created_time) = CURDATE() THEN 1 ELSE 0 END) as today_new_count,
            SUM(CASE WHEN last_login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as week_active_count
        FROM users
        """)
    java.util.Map<String, Object> getUserStatistics();
}