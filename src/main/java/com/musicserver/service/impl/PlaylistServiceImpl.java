package com.musicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Playlist;
import com.musicserver.entity.Music;
import com.musicserver.entity.PlaylistMusicRelation;
import com.musicserver.mapper.PlaylistMapper;
import com.musicserver.mapper.MusicMapper;
import com.musicserver.mapper.PlaylistMusicRelationMapper;
import com.musicserver.service.PlaylistService;
import com.musicserver.common.exception.BusinessException;
import com.musicserver.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 播放列表服务实现类
 * <p>
 * 实现播放列表相关的业务逻辑处理
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistMapper playlistMapper;
    private final MusicMapper musicMapper;
    private final PlaylistMusicRelationMapper playlistMusicRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Playlist createPlaylist(Playlist playlist) {
        log.info("创建播放列表: name={}, userId={}", playlist.getName(), playlist.getUserId());

        // 设置默认值
        playlist.setMusicCount(0);
        playlist.setPlayCount(0L);
        playlist.setCollectCount(0L);
        playlist.setStatus(1); // 正常状态

        int result = playlistMapper.insert(playlist);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        log.info("播放列表创建成功: playlistId={}", playlist.getId());
        return playlist;
    }

    @Override
    @Cacheable(value = "playlistCache", key = "#id")
    public Playlist findById(Long id) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null || playlist.getStatus() == 0) {
            throw new BusinessException(ResultCode.PLAYLIST_NOT_FOUND);
        }
        return playlist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "playlistCache", key = "#playlist.id")
    public Playlist updatePlaylist(Playlist playlist) {
        log.info("更新播放列表信息: playlistId={}", playlist.getId());

        Playlist existingPlaylist = playlistMapper.selectById(playlist.getId());
        if (existingPlaylist == null) {
            throw new BusinessException(ResultCode.PLAYLIST_NOT_FOUND);
        }

        int result = playlistMapper.updateById(playlist);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }

        log.info("播放列表信息更新成功: playlistId={}", playlist.getId());
        return playlistMapper.selectById(playlist.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "playlistCache", key = "#id")
    public boolean deletePlaylist(Long id, Long userId) {
        log.info("删除播放列表: playlistId={}, userId={}", id, userId);

        // 验证权限
        if (!hasPermission(id, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 删除播放列表中的所有音乐关联
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, id);
        playlistMusicRelationMapper.delete(wrapper);

        // 软删除播放列表
        Playlist playlist = new Playlist();
        playlist.setId(id);
        playlist.setStatus(0); // 删除状态

        int result = playlistMapper.updateById(playlist);
        log.info("播放列表删除成功: playlistId={}", id);
        return result > 0;
    }

    @Override
    public IPage<Playlist> getUserPlaylists(Long userId, Page<Playlist> page) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getUserId, userId);
        wrapper.eq(Playlist::getStatus, 1);
        wrapper.orderByDesc(Playlist::getCreatedTime);
        return playlistMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Playlist> getPublicPlaylists(Page<Playlist> page, String keyword) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getIsPublic, 1); // 公开播放列表
        wrapper.eq(Playlist::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Playlist::getName, keyword)
                    .or().like(Playlist::getDescription, keyword));
        }

        wrapper.orderByDesc(Playlist::getPlayCount);
        return playlistMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMusicToPlaylist(Long playlistId, Long musicId, Long userId) {
        log.info("添加音乐到播放列表: playlistId={}, musicId={}, userId={}", playlistId, musicId, userId);

        // 验证权限
        if (!hasPermission(playlistId, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查音乐是否存在
        Music music = musicMapper.selectById(musicId);
        if (music == null || music.getStatus() == 0) {
            throw new BusinessException(ResultCode.MUSIC_NOT_FOUND);
        }

        // 检查是否已存在
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        wrapper.eq(PlaylistMusicRelation::getMusicId, musicId);
        if (playlistMusicRelationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.MUSIC_ALREADY_IN_PLAYLIST);
        }

        // 获取下一个排序位置
        LambdaQueryWrapper<PlaylistMusicRelation> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        orderWrapper.orderByDesc(PlaylistMusicRelation::getSortOrder);
        orderWrapper.last("LIMIT 1");
        PlaylistMusicRelation lastRelation = playlistMusicRelationMapper.selectOne(orderWrapper);
        int nextOrder = lastRelation != null ? lastRelation.getSortOrder() + 1 : 1;

        // 添加关联记录
        PlaylistMusicRelation relation = new PlaylistMusicRelation();
        relation.setPlaylistId(playlistId);
        relation.setMusicId(musicId);
        relation.setSortOrder(nextOrder);
        int result = playlistMusicRelationMapper.insert(relation);

        if (result > 0) {
            // 更新播放列表音乐数量
            updatePlaylistMusicCount(playlistId);
        }

        log.info("音乐添加到播放列表成功: playlistId={}, musicId={}", playlistId, musicId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMusicFromPlaylist(Long playlistId, Long musicId, Long userId) {
        log.info("从播放列表移除音乐: playlistId={}, musicId={}, userId={}", playlistId, musicId, userId);

        // 验证权限
        if (!hasPermission(playlistId, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 删除关联记录
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        wrapper.eq(PlaylistMusicRelation::getMusicId, musicId);
        int result = playlistMusicRelationMapper.delete(wrapper);

        if (result > 0) {
            // 更新播放列表音乐数量
            updatePlaylistMusicCount(playlistId);
        }

        log.info("音乐从播放列表移除成功: playlistId={}, musicId={}", playlistId, musicId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAddMusicToPlaylist(Long playlistId, List<Long> musicIds, Long userId) {
        log.info("批量添加音乐到播放列表: playlistId={}, count={}, userId={}", playlistId, musicIds.size(), userId);

        // 验证权限
        if (!hasPermission(playlistId, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        int successCount = 0;
        for (Long musicId : musicIds) {
            try {
                if (addMusicToPlaylist(playlistId, musicId, userId)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("添加音乐到播放列表失败: musicId={}, error={}", musicId, e.getMessage());
            }
        }

        log.info("批量添加音乐完成: total={}, success={}", musicIds.size(), successCount);
        return successCount;
    }

    @Override
    public IPage<Music> getPlaylistMusic(Long playlistId, Page<Music> page) {
        // 这里需要连接查询，获取播放列表中的音乐
        // 简化实现：直接查询关联记录，然后获取音乐信息
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        wrapper.orderByAsc(PlaylistMusicRelation::getSortOrder);

        List<PlaylistMusicRelation> relations = playlistMusicRelationMapper.selectList(wrapper);
        List<Long> musicIds = relations.stream()
                .map(PlaylistMusicRelation::getMusicId)
                .collect(Collectors.toList());

        if (musicIds.isEmpty()) {
            return new Page<>();
        }

        LambdaQueryWrapper<Music> musicWrapper = new LambdaQueryWrapper<>();
        musicWrapper.in(Music::getId, musicIds);
        musicWrapper.eq(Music::getStatus, 1);

        return musicMapper.selectPage(page, musicWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reorderPlaylistMusic(Long playlistId, Long musicId, Integer newOrder, Long userId) {
        log.info("调整播放列表音乐顺序: playlistId={}, musicId={}, newOrder={}", playlistId, musicId, newOrder);

        // 验证权限
        if (!hasPermission(playlistId, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 更新排序
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        wrapper.eq(PlaylistMusicRelation::getMusicId, musicId);

        PlaylistMusicRelation relation = playlistMusicRelationMapper.selectOne(wrapper);
        if (relation != null) {
            relation.setSortOrder(newOrder);
            int result = playlistMusicRelationMapper.updateById(relation);
            return result > 0;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPlaylist(Long playlistId, Long userId) {
        log.info("收藏播放列表: playlistId={}, userId={}", playlistId, userId);

        // 检查是否已收藏
        if (isUserCollected(playlistId, userId)) {
            return false;
        }

        // 增加收藏次数
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist != null) {
            playlist.setCollectCount(playlist.getCollectCount() + 1);
            playlistMapper.updateById(playlist);
        }

        // TODO: 添加用户收藏播放列表的记录表

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectPlaylist(Long playlistId, Long userId) {
        log.info("取消收藏播放列表: playlistId={}, userId={}", playlistId, userId);

        // 减少收藏次数
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist != null && playlist.getCollectCount() > 0) {
            playlist.setCollectCount(playlist.getCollectCount() - 1);
            playlistMapper.updateById(playlist);
        }

        // TODO: 删除用户收藏播放列表的记录

        return true;
    }

    @Override
    public boolean isUserCollected(Long playlistId, Long userId) {
        // TODO: 实现用户收藏播放列表的检查逻辑
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementPlayCount(Long playlistId, Long userId) {
        log.info("增加播放列表播放次数: playlistId={}, userId={}", playlistId, userId);

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist != null) {
            playlist.setPlayCount(playlist.getPlayCount() + 1);
            playlistMapper.updateById(playlist);
        }

        return true;
    }

    @Override
    public IPage<Playlist> searchPlaylists(String keyword, Page<Playlist> page) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getStatus, 1);
        wrapper.eq(Playlist::getIsPublic, 1); // 只搜索公开播放列表

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Playlist::getName, keyword)
                    .or().like(Playlist::getDescription, keyword));
        }

        wrapper.orderByDesc(Playlist::getPlayCount);
        return playlistMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Playlist> getHotPlaylists(Integer limit) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getStatus, 1);
        wrapper.eq(Playlist::getIsPublic, 1);
        wrapper.orderByDesc(Playlist::getPlayCount);
        wrapper.last("LIMIT " + (limit != null ? limit : 20));
        return playlistMapper.selectList(wrapper);
    }

    @Override
    public List<Playlist> getRecommendedPlaylists(Long userId, Integer limit) {
        // 简化的推荐算法：推荐热门播放列表
        List<Playlist> result = getHotPlaylists(limit);
        log.info("为用户推荐播放列表: userId={}, count={}", userId, result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Playlist copyPlaylist(Long sourcePlaylistId, Long userId, String newName) {
        log.info("复制播放列表: sourceId={}, userId={}, newName={}", sourcePlaylistId, userId, newName);

        Playlist sourcePlaylist = playlistMapper.selectById(sourcePlaylistId);
        if (sourcePlaylist == null) {
            throw new BusinessException(ResultCode.PLAYLIST_NOT_FOUND);
        }

        // 创建新播放列表
        Playlist newPlaylist = new Playlist();
        newPlaylist.setName(newName);
        newPlaylist.setDescription("复制自: " + sourcePlaylist.getName());
        newPlaylist.setUserId(userId);
        newPlaylist.setIsPublic(0); // 默认私有

        createPlaylist(newPlaylist);

        // 复制音乐列表
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, sourcePlaylistId);
        wrapper.orderByAsc(PlaylistMusicRelation::getSortOrder);

        List<PlaylistMusicRelation> relations = playlistMusicRelationMapper.selectList(wrapper);
        for (PlaylistMusicRelation relation : relations) {
            addMusicToPlaylist(newPlaylist.getId(), relation.getMusicId(), userId);
        }

        log.info("播放列表复制成功: newPlaylistId={}", newPlaylist.getId());
        return newPlaylist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearPlaylist(Long playlistId, Long userId) {
        log.info("清空播放列表: playlistId={}, userId={}", playlistId, userId);

        // 验证权限
        if (!hasPermission(playlistId, userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 删除所有音乐关联
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        int result = playlistMusicRelationMapper.delete(wrapper);

        // 更新播放列表音乐数量
        updatePlaylistMusicCount(playlistId);

        log.info("播放列表清空成功: playlistId={}, removed={}", playlistId, result);
        return true;
    }

    @Override
    public boolean hasPermission(Long playlistId, Long userId) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        return playlist != null && playlist.getUserId().equals(userId);
    }

    /**
     * 更新播放列表的音乐数量
     *
     * @param playlistId 播放列表ID
     */
    private void updatePlaylistMusicCount(Long playlistId) {
        LambdaQueryWrapper<PlaylistMusicRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusicRelation::getPlaylistId, playlistId);
        long count = playlistMusicRelationMapper.selectCount(wrapper);

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);
        playlist.setMusicCount((int) count);
        playlistMapper.updateById(playlist);
    }
}