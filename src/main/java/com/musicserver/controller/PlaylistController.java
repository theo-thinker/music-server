package com.musicserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.common.Result;
import com.musicserver.dto.request.PageRequest;
import com.musicserver.dto.request.PlaylistCreateRequest;
import com.musicserver.dto.response.MusicInfoResponse;
import com.musicserver.dto.response.PageResponse;
import com.musicserver.dto.response.PlaylistInfoResponse;
import com.musicserver.entity.Music;
import com.musicserver.entity.Playlist;
import com.musicserver.service.PlaylistService;
import com.musicserver.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 播放列表管理控制器
 * <p>
 * 处理播放列表的创建、查询、管理等请求
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Validated
@Tag(name = "播放列表管理", description = "播放列表创建、查询、管理等接口")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 创建播放列表
     *
     * @param request 创建请求参数
     * @return 创建的播放列表信息
     */
    @PostMapping
    @Operation(summary = "创建播放列表", description = "创建新的播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<PlaylistInfoResponse> createPlaylist(@Valid @RequestBody PlaylistCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("创建播放列表: name={}, userId={}", request.getName(), currentUserId);

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(request, playlist);
        playlist.setUserId(currentUserId);

        Playlist createdPlaylist = playlistService.createPlaylist(playlist);

        PlaylistInfoResponse response = new PlaylistInfoResponse();
        BeanUtils.copyProperties(createdPlaylist, response);
        response.setIsOwner(true);

        return Result.success(response, "播放列表创建成功");
    }

    /**
     * 根据ID获取播放列表详情
     *
     * @param id 播放列表ID
     * @return 播放列表详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取播放列表详情", description = "根据ID获取播放列表的详细信息")
    public Result<PlaylistInfoResponse> getPlaylistById(@PathVariable Long id) {
        log.info("获取播放列表详情: playlistId={}", id);

        Playlist playlist = playlistService.findById(id);

        PlaylistInfoResponse response = new PlaylistInfoResponse();
        BeanUtils.copyProperties(playlist, response);

        // 检查是否为当前用户创建
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        if (currentUserId != null) {
            response.setIsOwner(playlist.getUserId().equals(currentUserId));
            response.setIsCollected(playlistService.isUserCollected(id, currentUserId));
        }

        return Result.success(response);
    }

    /**
     * 更新播放列表信息
     *
     * @param id      播放列表ID
     * @param request 更新请求参数
     * @return 更新后的播放列表信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新播放列表", description = "更新播放列表基本信息")
    @PreAuthorize("hasRole('USER')")
    public Result<PlaylistInfoResponse> updatePlaylist(@PathVariable Long id,
                                                       @Valid @RequestBody PlaylistCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("更新播放列表: playlistId={}, userId={}", id, currentUserId);

        // 验证权限
        if (!playlistService.hasPermission(id, currentUserId)) {
            return Result.error("无权限操作此播放列表");
        }

        Playlist playlist = new Playlist();
        BeanUtils.copyProperties(request, playlist);
        playlist.setId(id);

        Playlist updatedPlaylist = playlistService.updatePlaylist(playlist);

        PlaylistInfoResponse response = new PlaylistInfoResponse();
        BeanUtils.copyProperties(updatedPlaylist, response);
        response.setIsOwner(true);

        return Result.success(response, "播放列表更新成功");
    }

    /**
     * 删除播放列表
     *
     * @param id 播放列表ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除播放列表", description = "删除指定的播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deletePlaylist(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("删除播放列表: playlistId={}, userId={}", id, currentUserId);

        boolean success = playlistService.deletePlaylist(id, currentUserId);
        if (success) {
            return Result.success(null, "播放列表删除成功");
        } else {
            return Result.error("播放列表删除失败");
        }
    }

    /**
     * 获取当前用户的播放列表
     *
     * @param pageRequest 分页参数
     * @return 用户播放列表
     */
    @GetMapping("/my")
    @Operation(summary = "我的播放列表", description = "获取当前用户创建的播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<PageResponse<PlaylistInfoResponse>> getMyPlaylists(@Valid PageRequest pageRequest) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取用户播放列表: userId={}, page={}, size={}", currentUserId, pageRequest.getPage(), pageRequest.getSize());

        Page<Playlist> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var playlistPage = playlistService.getUserPlaylists(currentUserId, page);

        // 转换为响应DTO
        var responseList = playlistPage.getRecords().stream()
                .map(playlist -> {
                    PlaylistInfoResponse response = new PlaylistInfoResponse();
                    BeanUtils.copyProperties(playlist, response);
                    response.setIsOwner(true);
                    return response;
                }).toList();

        PageResponse<PlaylistInfoResponse> pageResponse = new PageResponse<>(
                responseList,
                playlistPage.getTotal(),
                (int) playlistPage.getSize(),
                (int) playlistPage.getCurrent()
        );

        return Result.success(pageResponse);
    }

    /**
     * 获取公开播放列表
     *
     * @param pageRequest 分页参数
     * @return 公开播放列表
     */
    @GetMapping("/public")
    @Operation(summary = "公开播放列表", description = "获取所有公开的播放列表")
    public Result<PageResponse<PlaylistInfoResponse>> getPublicPlaylists(@Valid PageRequest pageRequest) {
        log.info("获取公开播放列表: page={}, size={}", pageRequest.getPage(), pageRequest.getSize());

        Page<Playlist> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var playlistPage = playlistService.getPublicPlaylists(page, pageRequest.getKeyword());

        // 转换为响应DTO
        var responseList = playlistPage
                .getRecords()
                .stream()
                .map(playlist -> {
                    PlaylistInfoResponse response = new PlaylistInfoResponse();
                    BeanUtils.copyProperties(playlist, response);
                    return response;
                }).toList();

        PageResponse<PlaylistInfoResponse> pageResponse = new PageResponse<>(
                responseList,
                playlistPage.getTotal(),
                (int) playlistPage.getSize(),
                (int) playlistPage.getCurrent()
        );

        return Result.success(pageResponse);
    }

    /**
     * 搜索播放列表
     *
     * @param keyword     搜索关键词
     * @param pageRequest 分页参数
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索播放列表", description = "根据关键词搜索播放列表")
    public Result<PageResponse<PlaylistInfoResponse>> searchPlaylists(
            @RequestParam String keyword,
            @Valid PageRequest pageRequest) {

        log.info("搜索播放列表: keyword={}, page={}, size={}", keyword, pageRequest.getPage(), pageRequest.getSize());

        Page<Playlist> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var playlistPage = playlistService.searchPlaylists(keyword, page);

        // 转换为响应DTO
        var responseList = playlistPage.getRecords().stream()
                .map(playlist -> {
                    PlaylistInfoResponse response = new PlaylistInfoResponse();
                    BeanUtils.copyProperties(playlist, response);
                    return response;
                }).toList();

        PageResponse<PlaylistInfoResponse> pageResponse = new PageResponse<>(
                responseList,
                playlistPage.getTotal(),
                (int) playlistPage.getSize(),
                (int) playlistPage.getCurrent()
        );

        return Result.success(pageResponse);
    }

    /**
     * 获取热门播放列表
     *
     * @param limit 数量限制
     * @return 热门播放列表
     */
    @GetMapping("/hot")
    @Operation(summary = "热门播放列表", description = "获取热门播放列表")
    public Result<List<PlaylistInfoResponse>> getHotPlaylists(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取热门播放列表: limit={}", limit);

        List<Playlist> hotPlaylists = playlistService.getHotPlaylists(limit);

        List<PlaylistInfoResponse> responseList = hotPlaylists.stream()
                .map(playlist -> {
                    PlaylistInfoResponse response = new PlaylistInfoResponse();
                    BeanUtils.copyProperties(playlist, response);
                    return response;
                }).toList();

        return Result.success(responseList);
    }

    /**
     * 获取播放列表中的音乐
     *
     * @param id          播放列表ID
     * @param pageRequest 分页参数
     * @return 播放列表音乐
     */
    @GetMapping("/{id}/music")
    @Operation(summary = "播放列表音乐", description = "获取播放列表中的音乐列表")
    public Result<PageResponse<MusicInfoResponse>> getPlaylistMusic(@PathVariable Long id,
                                                                    @Valid PageRequest pageRequest) {
        log.info("获取播放列表音乐: playlistId={}, page={}, size={}", id, pageRequest.getPage(), pageRequest.getSize());

        Page<Music> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var musicPage = playlistService.getPlaylistMusic(id, page);

        // 转换为响应DTO
        var responseList = musicPage.getRecords().stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();

        PageResponse<MusicInfoResponse> pageResponse = new PageResponse<>(
                responseList,
                musicPage.getTotal(),
                (int) musicPage.getSize(),
                (int) musicPage.getCurrent()
        );

        return Result.success(pageResponse);
    }

    /**
     * 添加音乐到播放列表
     *
     * @param id      播放列表ID
     * @param musicId 音乐ID
     * @return 操作结果
     */
    @PostMapping("/{id}/music/{musicId}")
    @Operation(summary = "添加音乐", description = "添加音乐到播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> addMusicToPlaylist(@PathVariable Long id, @PathVariable Long musicId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("添加音乐到播放列表: playlistId={}, musicId={}, userId={}", id, musicId, currentUserId);

        boolean success = playlistService.addMusicToPlaylist(id, musicId, currentUserId);
        if (success) {
            return Result.success(null, "音乐添加成功");
        } else {
            return Result.error("音乐添加失败");
        }
    }

    /**
     * 从播放列表移除音乐
     *
     * @param id      播放列表ID
     * @param musicId 音乐ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/music/{musicId}")
    @Operation(summary = "移除音乐", description = "从播放列表移除音乐")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> removeMusicFromPlaylist(@PathVariable Long id, @PathVariable Long musicId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("从播放列表移除音乐: playlistId={}, musicId={}, userId={}", id, musicId, currentUserId);

        boolean success = playlistService.removeMusicFromPlaylist(id, musicId, currentUserId);
        if (success) {
            return Result.success(null, "音乐移除成功");
        } else {
            return Result.error("音乐移除失败");
        }
    }

    /**
     * 批量添加音乐到播放列表
     *
     * @param id       播放列表ID
     * @param musicIds 音乐ID列表
     * @return 操作结果
     */
    @PostMapping("/{id}/music/batch")
    @Operation(summary = "批量添加音乐", description = "批量添加音乐到播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<Integer> batchAddMusicToPlaylist(@PathVariable Long id, @RequestBody List<Long> musicIds) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("批量添加音乐到播放列表: playlistId={}, count={}, userId={}", id, musicIds.size(), currentUserId);

        Integer successCount = playlistService.batchAddMusicToPlaylist(id, musicIds, currentUserId);
        return Result.success(successCount, String.format("成功添加 %d 首音乐", successCount));
    }

    /**
     * 播放播放列表
     *
     * @param id 播放列表ID
     * @return 操作结果
     */
    @PostMapping("/{id}/play")
    @Operation(summary = "播放播放列表", description = "播放播放列表，增加播放次数")
    public Result<Void> playPlaylist(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        log.info("播放播放列表: playlistId={}, userId={}", id, currentUserId);

        boolean success = playlistService.incrementPlayCount(id, currentUserId);
        if (success) {
            return Result.success(null, "播放记录成功");
        } else {
            return Result.error("播放记录失败");
        }
    }

    /**
     * 收藏播放列表
     *
     * @param id 播放列表ID
     * @return 操作结果
     */
    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏播放列表", description = "收藏喜欢的播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> collectPlaylist(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("收藏播放列表: playlistId={}, userId={}", id, currentUserId);

        boolean success = playlistService.collectPlaylist(id, currentUserId);
        if (success) {
            return Result.success(null, "收藏成功");
        } else {
            return Result.error("收藏失败，可能已经收藏过了");
        }
    }

    /**
     * 取消收藏播放列表
     *
     * @param id 播放列表ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/collect")
    @Operation(summary = "取消收藏", description = "取消收藏播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> uncollectPlaylist(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("取消收藏播放列表: playlistId={}, userId={}", id, currentUserId);

        boolean success = playlistService.uncollectPlaylist(id, currentUserId);
        if (success) {
            return Result.success(null, "取消收藏成功");
        } else {
            return Result.error("取消收藏失败");
        }
    }

    /**
     * 复制播放列表
     *
     * @param id      源播放列表ID
     * @param newName 新播放列表名称
     * @return 新创建的播放列表
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制播放列表", description = "复制播放列表为自己的播放列表")
    @PreAuthorize("hasRole('USER')")
    public Result<PlaylistInfoResponse> copyPlaylist(@PathVariable Long id, @RequestParam String newName) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("复制播放列表: sourceId={}, newName={}, userId={}", id, newName, currentUserId);

        Playlist copiedPlaylist = playlistService.copyPlaylist(id, currentUserId, newName);

        PlaylistInfoResponse response = new PlaylistInfoResponse();
        BeanUtils.copyProperties(copiedPlaylist, response);
        response.setIsOwner(true);

        return Result.success(response, "播放列表复制成功");
    }

    /**
     * 清空播放列表
     *
     * @param id 播放列表ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/clear")
    @Operation(summary = "清空播放列表", description = "清空播放列表中的所有音乐")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> clearPlaylist(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("清空播放列表: playlistId={}, userId={}", id, currentUserId);

        boolean success = playlistService.clearPlaylist(id, currentUserId);
        if (success) {
            return Result.success(null, "播放列表清空成功");
        } else {
            return Result.error("播放列表清空失败");
        }
    }
}