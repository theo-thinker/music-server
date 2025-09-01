package com.musicserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Music;
import com.musicserver.entity.UserMusicCollection;
import com.musicserver.entity.PlayHistory;
import com.musicserver.mapper.MusicMapper;
import com.musicserver.mapper.UserMusicCollectionMapper;
import com.musicserver.mapper.PlayHistoryMapper;
import com.musicserver.service.MusicService;
import com.musicserver.common.exception.BusinessException;
import com.musicserver.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 音乐服务实现类
 * 
 * 实现音乐相关的业务逻辑处理
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicServiceImpl implements MusicService {

    private final MusicMapper musicMapper;
    private final UserMusicCollectionMapper userMusicCollectionMapper;
    private final PlayHistoryMapper playHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Music addMusic(Music music) {
        log.info("添加音乐: title={}, artistId={}", music.getTitle(), music.getArtistId());
        
        // 设置默认值
        music.setStatus(1); // 正常状态
        music.setPlayCount(0L);
        music.setLikeCount(0L);
        music.setCollectCount(0L);
        
        int result = musicMapper.insert(music);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("音乐添加成功: musicId={}", music.getId());
        return music;
    }

    @Override
    @Cacheable(value = "musicCache", key = "#id")
    public Music findById(Long id) {
        Music music = musicMapper.selectById(id);
        if (music == null || music.getStatus() == 0) {
            throw new BusinessException(ResultCode.MUSIC_NOT_FOUND);
        }
        return music;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "musicCache", key = "#music.id")
    public Music updateMusic(Music music) {
        log.info("更新音乐信息: musicId={}", music.getId());
        
        Music existingMusic = musicMapper.selectById(music.getId());
        if (existingMusic == null) {
            throw new BusinessException(ResultCode.MUSIC_NOT_FOUND);
        }
        
        int result = musicMapper.updateById(music);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("音乐信息更新成功: musicId={}", music.getId());
        return musicMapper.selectById(music.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "musicCache", key = "#id")
    public boolean deleteMusic(Long id) {
        log.info("删除音乐: musicId={}", id);
        
        Music music = new Music();
        music.setId(id);
        music.setStatus(0); // 下架状态
        
        int result = musicMapper.updateById(music);
        log.info("音乐删除成功: musicId={}", id);
        return result > 0;
    }

    @Override
    public IPage<Music> getMusicList(Page<Music> page, String keyword, Long categoryId, Long artistId) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询正常状态的音乐
        wrapper.eq(Music::getStatus, 1);
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Music::getTitle, keyword);
        }
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Music::getCategoryId, categoryId);
        }
        
        // 艺术家筛选
        if (artistId != null) {
            wrapper.eq(Music::getArtistId, artistId);
        }
        
        wrapper.orderByDesc(Music::getCreatedTime);
        return musicMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Music> getMusicByArtist(Long artistId, Page<Music> page) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getArtistId, artistId);
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByDesc(Music::getReleaseDate);
        return musicMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Music> getMusicByAlbum(Long albumId) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getAlbumId, albumId);
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByAsc(Music::getId); // 按专辑顺序排列
        return musicMapper.selectList(wrapper);
    }

    @Override
    public IPage<Music> getMusicByCategory(Long categoryId, Page<Music> page) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getCategoryId, categoryId);
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByDesc(Music::getPlayCount);
        return musicMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Music> searchMusic(String keyword, Page<Music> page) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Music::getTitle, keyword));
        }
        
        wrapper.orderByDesc(Music::getPlayCount);
        return musicMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(value = "hotMusicCache", key = "#limit")
    public List<Music> getHotMusic(Integer limit) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByDesc(Music::getPlayCount);
        wrapper.last("LIMIT " + (limit != null ? limit : 20));
        return musicMapper.selectList(wrapper);
    }

    @Override
    public List<Music> getLatestMusic(Integer limit) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByDesc(Music::getReleaseDate);
        wrapper.last("LIMIT " + (limit != null ? limit : 20));
        return musicMapper.selectList(wrapper);
    }

    @Override
    public List<Music> getRecommendedMusic(Long userId, Integer limit) {
        // 简化的推荐算法：基于用户播放历史推荐相似音乐
        // 实际项目中可以使用更复杂的推荐算法
        List<Music> result = getHotMusic(limit);
        log.info("为用户推荐音乐: userId={}, count={}", userId, result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementPlayCount(Long musicId, Long userId) {
        log.info("增加播放次数: musicId={}, userId={}", musicId, userId);
        
        // 增加音乐播放次数
        Music music = musicMapper.selectById(musicId);
        if (music != null) {
            music.setPlayCount(music.getPlayCount() + 1);
            musicMapper.updateById(music);
        }
        
        // 记录播放历史
        if (userId != null) {
            PlayHistory history = new PlayHistory();
            history.setUserId(userId);
            history.setMusicId(musicId);
            history.setPlayTime(LocalDateTime.now());
            history.setPlayDuration(0); // 实际播放时长需要前端传递
            playHistoryMapper.insert(history);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementLikeCount(Long musicId, Long userId) {
        log.info("点赞音乐: musicId={}, userId={}", musicId, userId);
        
        Music music = musicMapper.selectById(musicId);
        if (music != null) {
            music.setLikeCount(music.getLikeCount() + 1);
            musicMapper.updateById(music);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementLikeCount(Long musicId, Long userId) {
        log.info("取消点赞音乐: musicId={}, userId={}", musicId, userId);
        
        Music music = musicMapper.selectById(musicId);
        if (music != null && music.getLikeCount() > 0) {
            music.setLikeCount(music.getLikeCount() - 1);
            musicMapper.updateById(music);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementCollectCount(Long musicId, Long userId) {
        log.info("收藏音乐: musicId={}, userId={}", musicId, userId);
        
        // 检查是否已收藏
        if (isUserCollected(musicId, userId)) {
            return false;
        }
        
        // 添加收藏记录
        UserMusicCollection collection = new UserMusicCollection();
        collection.setUserId(userId);
        collection.setMusicId(musicId);
        userMusicCollectionMapper.insert(collection);
        
        // 增加音乐收藏次数
        Music music = musicMapper.selectById(musicId);
        if (music != null) {
            music.setCollectCount(music.getCollectCount() + 1);
            musicMapper.updateById(music);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementCollectCount(Long musicId, Long userId) {
        log.info("取消收藏音乐: musicId={}, userId={}", musicId, userId);
        
        // 删除收藏记录
        LambdaQueryWrapper<UserMusicCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMusicCollection::getUserId, userId);
        wrapper.eq(UserMusicCollection::getMusicId, musicId);
        int deleted = userMusicCollectionMapper.delete(wrapper);
        
        if (deleted > 0) {
            // 减少音乐收藏次数
            Music music = musicMapper.selectById(musicId);
            if (music != null && music.getCollectCount() > 0) {
                music.setCollectCount(music.getCollectCount() - 1);
                musicMapper.updateById(music);
            }
        }
        
        return deleted > 0;
    }

    @Override
    public boolean isUserCollected(Long musicId, Long userId) {
        LambdaQueryWrapper<UserMusicCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMusicCollection::getUserId, userId);
        wrapper.eq(UserMusicCollection::getMusicId, musicId);
        return userMusicCollectionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Music getMusicStatistics(Long musicId) {
        return musicMapper.selectById(musicId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchImportMusic(List<Music> musicList) {
        log.info("批量导入音乐: count={}", musicList.size());
        
        int successCount = 0;
        for (Music music : musicList) {
            try {
                addMusic(music);
                successCount++;
            } catch (Exception e) {
                log.error("导入音乐失败: title={}, error={}", music.getTitle(), e.getMessage());
            }
        }
        
        log.info("批量导入音乐完成: total={}, success={}", musicList.size(), successCount);
        return successCount;
    }

    @Override
    public IPage<Music> getMusicByQuality(Integer quality, Page<Music> page) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getQuality, quality);
        wrapper.eq(Music::getStatus, 1);
        wrapper.orderByDesc(Music::getCreatedTime);
        return musicMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Music> getSimilarMusic(Long musicId, Integer limit) {
        // 简化的相似音乐推荐：基于艺术家和分类
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return List.of();
        }
        
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getStatus, 1);
        wrapper.ne(Music::getId, musicId); // 排除自己
        
        // 优先推荐同一艺术家的其他作品
        wrapper.and(w -> w.eq(Music::getArtistId, music.getArtistId())
                .or().eq(Music::getCategoryId, music.getCategoryId()));
        
        wrapper.orderByDesc(Music::getPlayCount);
        wrapper.last("LIMIT " + (limit != null ? limit : 10));
        
        return musicMapper.selectList(wrapper);
    }
}