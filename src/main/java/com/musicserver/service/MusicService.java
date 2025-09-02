package com.musicserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Music;
import com.musicserver.entity.Artist;
import com.musicserver.entity.Album;

import java.util.List;

/**
 * 音乐服务接口
 * <p>
 * 提供音乐相关的业务逻辑处理，包括：
 * 1. 音乐信息管理
 * 2. 音乐搜索和推荐
 * 3. 播放统计
 * 4. 音乐分类管理
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface MusicService {

    /**
     * 添加音乐
     *
     * @param music 音乐信息
     * @return 添加成功的音乐信息
     */
    Music addMusic(Music music);

    /**
     * 根据ID查询音乐
     *
     * @param id 音乐ID
     * @return 音乐信息
     */
    Music findById(Long id);

    /**
     * 更新音乐信息
     *
     * @param music 音乐信息
     * @return 更新后的音乐信息
     */
    Music updateMusic(Music music);

    /**
     * 删除音乐（软删除）
     *
     * @param id 音乐ID
     * @return 是否成功
     */
    boolean deleteMusic(Long id);

    /**
     * 分页查询音乐列表
     *
     * @param page       分页参数
     * @param keyword    搜索关键词
     * @param categoryId 分类ID
     * @param artistId   艺术家ID
     * @return 音乐分页列表
     */
    IPage<Music> getMusicList(Page<Music> page, String keyword, Long categoryId, Long artistId);

    /**
     * 根据艺术家查询音乐
     *
     * @param artistId 艺术家ID
     * @param page     分页参数
     * @return 音乐列表
     */
    IPage<Music> getMusicByArtist(Long artistId, Page<Music> page);

    /**
     * 根据专辑查询音乐
     *
     * @param albumId 专辑ID
     * @return 音乐列表
     */
    List<Music> getMusicByAlbum(Long albumId);

    /**
     * 根据分类查询音乐
     *
     * @param categoryId 分类ID
     * @param page       分页参数
     * @return 音乐列表
     */
    IPage<Music> getMusicByCategory(Long categoryId, Page<Music> page);

    /**
     * 搜索音乐
     *
     * @param keyword 搜索关键词
     * @param page    分页参数
     * @return 搜索结果
     */
    IPage<Music> searchMusic(String keyword, Page<Music> page);

    /**
     * 获取热门音乐
     *
     * @param limit 数量限制
     * @return 热门音乐列表
     */
    List<Music> getHotMusic(Integer limit);

    /**
     * 获取最新音乐
     *
     * @param limit 数量限制
     * @return 最新音乐列表
     */
    List<Music> getLatestMusic(Integer limit);

    /**
     * 获取推荐音乐
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 推荐音乐列表
     */
    List<Music> getRecommendedMusic(Long userId, Integer limit);

    /**
     * 增加播放次数
     *
     * @param musicId 音乐ID
     * @param userId  用户ID（可选，用于统计）
     * @return 是否成功
     */
    boolean incrementPlayCount(Long musicId, Long userId);

    /**
     * 增加点赞次数
     *
     * @param musicId 音乐ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean incrementLikeCount(Long musicId, Long userId);

    /**
     * 取消点赞
     *
     * @param musicId 音乐ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean decrementLikeCount(Long musicId, Long userId);

    /**
     * 增加收藏次数
     *
     * @param musicId 音乐ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean incrementCollectCount(Long musicId, Long userId);

    /**
     * 取消收藏
     *
     * @param musicId 音乐ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean decrementCollectCount(Long musicId, Long userId);

    /**
     * 检查用户是否收藏了音乐
     *
     * @param musicId 音乐ID
     * @param userId  用户ID
     * @return 是否已收藏
     */
    boolean isUserCollected(Long musicId, Long userId);

    /**
     * 获取音乐统计信息
     *
     * @param musicId 音乐ID
     * @return 统计信息
     */
    Music getMusicStatistics(Long musicId);

    /**
     * 批量导入音乐
     *
     * @param musicList 音乐列表
     * @return 导入成功数量
     */
    Integer batchImportMusic(List<Music> musicList);

    /**
     * 根据音乐质量查询
     *
     * @param quality 音质等级
     * @param page    分页参数
     * @return 音乐列表
     */
    IPage<Music> getMusicByQuality(Integer quality, Page<Music> page);

    /**
     * 获取相似音乐
     *
     * @param musicId 音乐ID
     * @param limit   数量限制
     * @return 相似音乐列表
     */
    List<Music> getSimilarMusic(Long musicId, Integer limit);
}