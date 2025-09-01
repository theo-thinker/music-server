package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicserver.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户配置数据访问接口
 * 
 * 提供用户个性化配置的数据库操作
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询用户配置
     * 
     * @param userId 用户ID
     * @return 用户配置信息
     */
    @Select("SELECT * FROM user_profiles WHERE user_id = #{userId}")
    UserProfile selectByUserId(@Param("userId") Long userId);
}