package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicserver.entity.PlayHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 播放历史数据访问接口
 * 
 * 提供用户播放历史的数据库操作
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    /**
     * 查询用户播放历史
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 播放历史列表
     */
    @Select("""
        SELECT ph.*, m.title, m.artist_id, m.album_cover, m.duration,
               a.name as artist_name
        FROM play_histories ph
        LEFT JOIN music m ON ph.music_id = m.id
        LEFT JOIN artists a ON m.artist_id = a.id
        WHERE ph.user_id = #{userId}
        ORDER BY ph.play_time DESC
        LIMIT #{limit}
        """)
    List<PlayHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询用户最近播放的音乐（去重）
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 音乐ID列表
     */
    @Select("""
        SELECT DISTINCT ph.music_id
        FROM play_histories ph
        WHERE ph.user_id = #{userId}
        ORDER BY MAX(ph.play_time) DESC
        GROUP BY ph.music_id
        LIMIT #{limit}
        """)
    List<Long> selectRecentMusicIds(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询指定时间范围内的播放历史
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 播放历史列表
     */
    @Select("""
        SELECT ph.*, m.title, a.name as artist_name
        FROM play_histories ph
        LEFT JOIN music m ON ph.music_id = m.id
        LEFT JOIN artists a ON m.artist_id = a.id
        WHERE ph.user_id = #{userId}
        AND ph.play_time BETWEEN #{startTime} AND #{endTime}
        ORDER BY ph.play_time DESC
        """)
    List<PlayHistory> selectByTimeRange(@Param("userId") Long userId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户播放次数
     * 
     * @param userId 用户ID
     * @param musicId 音乐ID（可选）
     * @return 播放次数
     */
    @Select("""
        <script>
        SELECT COUNT(*) FROM play_histories 
        WHERE user_id = #{userId}
        <if test="musicId != null">
            AND music_id = #{musicId}
        </if>
        </script>
        """)
    int countUserPlays(@Param("userId") Long userId, @Param("musicId") Long musicId);

    /**
     * 获取用户播放统计
     * 
     * @param userId 用户ID
     * @return 统计数据
     */
    @Select("""
        SELECT 
            COUNT(*) as total_plays,
            COUNT(DISTINCT music_id) as unique_music_count,
            SUM(play_duration) as total_duration,
            MAX(play_time) as last_play_time
        FROM play_histories 
        WHERE user_id = #{userId}
        """)
    java.util.Map<String, Object> getUserPlayStatistics(@Param("userId") Long userId);
}