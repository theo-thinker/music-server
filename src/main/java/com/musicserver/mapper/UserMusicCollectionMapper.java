package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicserver.entity.UserMusicCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户音乐收藏数据访问接口
 * 
 * 提供用户收藏音乐的数据库操作
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface UserMusicCollectionMapper extends BaseMapper<UserMusicCollection> {

    /**
     * 查询用户收藏的音乐
     * 
     * @param userId 用户ID
     * @return 收藏的音乐列表
     */
    @Select("""
        SELECT umc.*, m.title, m.artist_id, m.album_cover, m.duration,
               a.name as artist_name, al.name as album_name
        FROM user_music_collections umc
        LEFT JOIN music m ON umc.music_id = m.id
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        WHERE umc.user_id = #{userId}
        ORDER BY umc.created_time DESC
        """)
    List<UserMusicCollection> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否收藏了某首音乐
     * 
     * @param userId 用户ID
     * @param musicId 音乐ID
     * @return 是否收藏
     */
    @Select("SELECT COUNT(*) > 0 FROM user_music_collections WHERE user_id = #{userId} AND music_id = #{musicId}")
    boolean isCollected(@Param("userId") Long userId, @Param("musicId") Long musicId);

    /**
     * 获取用户收藏的音乐ID列表
     * 
     * @param userId 用户ID
     * @return 音乐ID列表
     */
    @Select("SELECT music_id FROM user_music_collections WHERE user_id = #{userId} ORDER BY created_time DESC")
    List<Long> selectMusicIdsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM user_music_collections WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}