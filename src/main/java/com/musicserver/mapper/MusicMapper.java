package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 音乐数据访问接口
 * 
 * 提供音乐相关的数据库操作，包括查询、统计、推荐等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface MusicMapper extends BaseMapper<Music> {

    /**
     * 分页查询音乐（带关联信息）
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param artistId 艺术家ID
     * @return 分页结果
     */
    @Select("""
        <script>
        SELECT m.*, a.name as artist_name, al.name as album_name, mc.name as category_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        LEFT JOIN music_categories mc ON m.category_id = mc.id
        WHERE m.status = 1
        <if test="keyword != null and keyword != ''">
            AND (m.title LIKE CONCAT('%', #{keyword}, '%') OR a.name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="categoryId != null">
            AND m.category_id = #{categoryId}
        </if>
        <if test="artistId != null">
            AND m.artist_id = #{artistId}
        </if>
        ORDER BY m.play_count DESC, m.created_time DESC
        </script>
        """)
    IPage<Music> selectMusicWithDetails(Page<Music> page, 
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId,
                                       @Param("artistId") Long artistId);

    /**
     * 查询热门音乐
     * 
     * @param limit 数量限制
     * @return 热门音乐列表
     */
    @Select("""
        SELECT m.*, a.name as artist_name, al.name as album_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        WHERE m.status = 1
        ORDER BY m.play_count DESC, m.like_count DESC
        LIMIT #{limit}
        """)
    List<Music> selectHotMusic(@Param("limit") int limit);

    /**
     * 查询最新音乐
     * 
     * @param limit 数量限制
     * @return 最新音乐列表
     */
    @Select("""
        SELECT m.*, a.name as artist_name, al.name as album_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        WHERE m.status = 1
        ORDER BY m.created_time DESC
        LIMIT #{limit}
        """)
    List<Music> selectLatestMusic(@Param("limit") int limit);

    /**
     * 根据分类查询推荐音乐
     * 
     * @param categoryIds 分类ID列表
     * @param excludeMusicIds 排除的音乐ID列表
     * @param limit 数量限制
     * @return 推荐音乐列表
     */
    @Select("""
        <script>
        SELECT m.*, a.name as artist_name, al.name as album_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        WHERE m.status = 1
        <if test="categoryIds != null and categoryIds.size() > 0">
            AND m.category_id IN
            <foreach collection="categoryIds" item="id" open="(" close=")" separator=",">
                #{id}
            </foreach>
        </if>
        <if test="excludeMusicIds != null and excludeMusicIds.size() > 0">
            AND m.id NOT IN
            <foreach collection="excludeMusicIds" item="id" open="(" close=")" separator=",">
                #{id}
            </foreach>
        </if>
        ORDER BY RAND()
        LIMIT #{limit}
        </script>
        """)
    List<Music> selectRecommendMusic(@Param("categoryIds") List<Long> categoryIds,
                                   @Param("excludeMusicIds") List<Long> excludeMusicIds,
                                   @Param("limit") int limit);

    /**
     * 更新音乐播放次数
     * 
     * @param musicId 音乐ID
     * @return 更新行数
     */
    @Update("UPDATE music SET play_count = play_count + 1, updated_time = NOW() WHERE id = #{musicId}")
    int incrementPlayCount(@Param("musicId") Long musicId);

    /**
     * 更新音乐点赞次数
     * 
     * @param musicId 音乐ID
     * @param increment 增量（可为负数）
     * @return 更新行数
     */
    @Update("UPDATE music SET like_count = like_count + #{increment}, updated_time = NOW() WHERE id = #{musicId}")
    int updateLikeCount(@Param("musicId") Long musicId, @Param("increment") int increment);

    /**
     * 更新音乐收藏次数
     * 
     * @param musicId 音乐ID
     * @param increment 增量（可为负数）
     * @return 更新行数
     */
    @Update("UPDATE music SET collect_count = collect_count + #{increment}, updated_time = NOW() WHERE id = #{musicId}")
    int updateCollectCount(@Param("musicId") Long musicId, @Param("increment") int increment);

    /**
     * 根据艺术家ID查询音乐
     * 
     * @param artistId 艺术家ID
     * @param limit 数量限制
     * @return 音乐列表
     */
    @Select("""
        SELECT m.*, a.name as artist_name, al.name as album_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        LEFT JOIN albums al ON m.album_id = al.id
        WHERE m.artist_id = #{artistId} AND m.status = 1
        ORDER BY m.play_count DESC
        LIMIT #{limit}
        """)
    List<Music> selectByArtistId(@Param("artistId") Long artistId, @Param("limit") int limit);

    /**
     * 根据专辑ID查询音乐
     * 
     * @param albumId 专辑ID
     * @return 音乐列表
     */
    @Select("""
        SELECT m.*, a.name as artist_name
        FROM music m
        LEFT JOIN artists a ON m.artist_id = a.id
        WHERE m.album_id = #{albumId} AND m.status = 1
        ORDER BY m.track_number ASC, m.created_time ASC
        """)
    List<Music> selectByAlbumId(@Param("albumId") Long albumId);

    /**
     * 获取音乐统计信息
     * 
     * @return 统计数据
     */
    @Select("""
        SELECT 
            COUNT(*) as total_count,
            SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as active_count,
            SUM(CASE WHEN DATE(created_time) = CURDATE() THEN 1 ELSE 0 END) as today_new_count,
            SUM(play_count) as total_play_count,
            AVG(duration) as avg_duration
        FROM music
        """)
    java.util.Map<String, Object> getMusicStatistics();
}