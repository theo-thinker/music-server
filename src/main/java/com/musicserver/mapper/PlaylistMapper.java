package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 播放列表数据访问接口
 * 
 * 提供播放列表相关的数据库操作
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {

    /**
     * 分页查询公开播放列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @Select("""
        <script>
        SELECT p.*, u.username, u.nickname, u.avatar as user_avatar
        FROM playlists p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.is_public = 1 AND p.status = 1
        <if test="keyword != null and keyword != ''">
            AND p.name LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY p.play_count DESC, p.created_time DESC
        </script>
        """)
    IPage<Playlist> selectPublicPlaylists(Page<Playlist> page, @Param("keyword") String keyword);

    /**
     * 查询用户的播放列表
     * 
     * @param userId 用户ID
     * @param includePrivate 是否包含私有播放列表
     * @return 播放列表
     */
    @Select("""
        <script>
        SELECT * FROM playlists 
        WHERE user_id = #{userId} AND status = 1
        <if test="!includePrivate">
            AND is_public = 1
        </if>
        ORDER BY created_time DESC
        </script>
        """)
    List<Playlist> selectByUserId(@Param("userId") Long userId, @Param("includePrivate") boolean includePrivate);

    /**
     * 查询热门播放列表
     * 
     * @param limit 数量限制
     * @return 热门播放列表
     */
    @Select("""
        SELECT p.*, u.username, u.nickname
        FROM playlists p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.is_public = 1 AND p.status = 1
        ORDER BY p.play_count DESC, p.collect_count DESC
        LIMIT #{limit}
        """)
    List<Playlist> selectHotPlaylists(@Param("limit") int limit);

    /**
     * 更新播放列表播放次数
     * 
     * @param playlistId 播放列表ID
     * @return 更新行数
     */
    @Update("UPDATE playlists SET play_count = play_count + 1, updated_time = NOW() WHERE id = #{playlistId}")
    int incrementPlayCount(@Param("playlistId") Long playlistId);

    /**
     * 更新播放列表收藏次数
     * 
     * @param playlistId 播放列表ID
     * @param increment 增量
     * @return 更新行数
     */
    @Update("UPDATE playlists SET collect_count = collect_count + #{increment}, updated_time = NOW() WHERE id = #{playlistId}")
    int updateCollectCount(@Param("playlistId") Long playlistId, @Param("increment") int increment);

    /**
     * 更新播放列表音乐数量
     * 
     * @param playlistId 播放列表ID
     * @param musicCount 音乐数量
     * @return 更新行数
     */
    @Update("UPDATE playlists SET music_count = #{musicCount}, updated_time = NOW() WHERE id = #{playlistId}")
    int updateMusicCount(@Param("playlistId") Long playlistId, @Param("musicCount") int musicCount);

    /**
     * 查询最近更新的播放列表
     * 
     * @param limit 数量限制
     * @return 播放列表
     */
    @Select("""
        SELECT p.*, u.username, u.nickname
        FROM playlists p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.is_public = 1 AND p.status = 1
        ORDER BY p.updated_time DESC
        LIMIT #{limit}
        """)
    List<Playlist> selectRecentUpdatedPlaylists(@Param("limit") int limit);
}