package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicserver.entity.Artist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 艺术家数据访问接口
 * <p>
 * 提供艺术家相关的数据库操作
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {

    /**
     * 根据名称模糊查询艺术家
     *
     * @param name 艺术家名称
     * @return 艺术家列表
     */
    @Select("""
             SELECT * FROM artists\s
             WHERE name LIKE CONCAT('%', #{name}, '%') AND status = 1
             ORDER BY followers_count DESC
            \s""")
    List<Artist> selectByName(@Param("name") String name);

    /**
     * 查询热门艺术家
     *
     * @param limit 数量限制
     * @return 热门艺术家列表
     */
    @Select("""
             SELECT * FROM artists\s
             WHERE status = 1
             ORDER BY followers_count DESC
             LIMIT #{limit}
            \s""")
    List<Artist> selectHotArtists(@Param("limit") int limit);

    /**
     * 更新艺术家关注者数量
     *
     * @param artistId  艺术家ID
     * @param increment 增量
     * @return 更新行数
     */
    @Update("UPDATE artists SET followers_count = followers_count + #{increment}, updated_time = NOW() WHERE id = #{artistId}")
    int updateFollowersCount(@Param("artistId") Long artistId, @Param("increment") int increment);

    /**
     * 根据国家查询艺术家
     *
     * @param country 国家
     * @return 艺术家列表
     */
    @Select("""
             SELECT * FROM artists\s
             WHERE country = #{country} AND status = 1
             ORDER BY followers_count DESC
            \s""")
    List<Artist> selectByCountry(@Param("country") String country);
}