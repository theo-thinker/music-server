package com.musicserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.entity.Playlist;
import com.musicserver.entity.Music;

import java.util.List;

/**
 * 播放列表服务接口
 * 
 * 提供播放列表相关的业务逻辑处理，包括：
 * 1. 播放列表创建、更新、删除
 * 2. 播放列表音乐管理
 * 3. 播放列表分享和收藏
 * 4. 播放列表推荐
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface PlaylistService {

    /**
     * 创建播放列表
     * 
     * @param playlist 播放列表信息
     * @return 创建成功的播放列表
     */
    Playlist createPlaylist(Playlist playlist);

    /**
     * 根据ID查询播放列表
     * 
     * @param id 播放列表ID
     * @return 播放列表信息
     */
    Playlist findById(Long id);

    /**
     * 更新播放列表信息
     * 
     * @param playlist 播放列表信息
     * @return 更新后的播放列表
     */
    Playlist updatePlaylist(Playlist playlist);

    /**
     * 删除播放列表
     * 
     * @param id 播放列表ID
     * @param userId 用户ID（权限验证）
     * @return 是否成功
     */
    boolean deletePlaylist(Long id, Long userId);

    /**
     * 获取用户的播放列表
     * 
     * @param userId 用户ID
     * @param page 分页参数
     * @return 播放列表分页
     */
    IPage<Playlist> getUserPlaylists(Long userId, Page<Playlist> page);

    /**
     * 获取公开播放列表
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 播放列表分页
     */
    IPage<Playlist> getPublicPlaylists(Page<Playlist> page, String keyword);

    /**
     * 添加音乐到播放列表
     * 
     * @param playlistId 播放列表ID
     * @param musicId 音乐ID
     * @param userId 用户ID（权限验证）
     * @return 是否成功
     */
    boolean addMusicToPlaylist(Long playlistId, Long musicId, Long userId);

    /**
     * 从播放列表移除音乐
     * 
     * @param playlistId 播放列表ID
     * @param musicId 音乐ID
     * @param userId 用户ID（权限验证）
     * @return 是否成功
     */
    boolean removeMusicFromPlaylist(Long playlistId, Long musicId, Long userId);

    /**
     * 批量添加音乐到播放列表
     * 
     * @param playlistId 播放列表ID
     * @param musicIds 音乐ID列表
     * @param userId 用户ID（权限验证）
     * @return 成功添加的数量
     */
    Integer batchAddMusicToPlaylist(Long playlistId, List<Long> musicIds, Long userId);

    /**
     * 获取播放列表中的音乐
     * 
     * @param playlistId 播放列表ID
     * @param page 分页参数
     * @return 音乐分页列表
     */
    IPage<Music> getPlaylistMusic(Long playlistId, Page<Music> page);

    /**
     * 调整播放列表中音乐的顺序
     * 
     * @param playlistId 播放列表ID
     * @param musicId 音乐ID
     * @param newOrder 新的排序位置
     * @param userId 用户ID（权限验证）
     * @return 是否成功
     */
    boolean reorderPlaylistMusic(Long playlistId, Long musicId, Integer newOrder, Long userId);

    /**
     * 收藏播放列表
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean collectPlaylist(Long playlistId, Long userId);

    /**
     * 取消收藏播放列表
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean uncollectPlaylist(Long playlistId, Long userId);

    /**
     * 检查用户是否收藏了播放列表
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean isUserCollected(Long playlistId, Long userId);

    /**
     * 增加播放列表播放次数
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID（可选）
     * @return 是否成功
     */
    boolean incrementPlayCount(Long playlistId, Long userId);

    /**
     * 搜索播放列表
     * 
     * @param keyword 搜索关键词
     * @param page 分页参数
     * @return 搜索结果
     */
    IPage<Playlist> searchPlaylists(String keyword, Page<Playlist> page);

    /**
     * 获取热门播放列表
     * 
     * @param limit 数量限制
     * @return 热门播放列表
     */
    List<Playlist> getHotPlaylists(Integer limit);

    /**
     * 获取推荐播放列表
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 推荐播放列表
     */
    List<Playlist> getRecommendedPlaylists(Long userId, Integer limit);

    /**
     * 复制播放列表
     * 
     * @param sourcePlaylistId 源播放列表ID
     * @param userId 用户ID
     * @param newName 新播放列表名称
     * @return 新创建的播放列表
     */
    Playlist copyPlaylist(Long sourcePlaylistId, Long userId, String newName);

    /**
     * 清空播放列表
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID（权限验证）
     * @return 是否成功
     */
    boolean clearPlaylist(Long playlistId, Long userId);

    /**
     * 检查用户是否有播放列表的操作权限
     * 
     * @param playlistId 播放列表ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long playlistId, Long userId);
}