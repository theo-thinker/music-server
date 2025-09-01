package com.musicserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicserver.entity.PlaylistMusicRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 播放列表音乐关联Mapper接口
 * 
 * 提供播放列表音乐关联的数据访问操作
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Mapper
public interface PlaylistMusicRelationMapper extends BaseMapper<PlaylistMusicRelation> {
    
    // 继承BaseMapper，获得基本的CRUD操作
    // 可以在此添加自定义查询方法
}