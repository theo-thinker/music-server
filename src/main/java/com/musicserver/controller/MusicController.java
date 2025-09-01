package com.musicserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicserver.common.Result;
import com.musicserver.dto.request.MusicAddRequest;
import com.musicserver.dto.request.PageRequest;
import com.musicserver.dto.response.MusicInfoResponse;
import com.musicserver.dto.response.PageResponse;
import com.musicserver.entity.Music;
import com.musicserver.service.MusicService;
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
 * 音乐管理控制器
 * 
 * 处理音乐查询、搜索、播放统计等请求
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
@Validated
@Tag(name = "音乐管理", description = "音乐查询、搜索、播放等接口")
public class MusicController {

    private final MusicService musicService;

    /**
     * 添加音乐（管理员功能）
     * 
     * @param request 音乐添加请求
     * @return 添加的音乐信息
     */
    @PostMapping
    @Operation(summary = "添加音乐", description = "添加新的音乐到系统（管理员功能）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MusicInfoResponse> addMusic(@Valid @RequestBody MusicAddRequest request) {
        log.info("添加音乐: title={}, artistId={}", request.getTitle(), request.getArtistId());
        
        Music music = new Music();
        BeanUtils.copyProperties(request, music);
        
        Music addedMusic = musicService.addMusic(music);
        
        MusicInfoResponse response = new MusicInfoResponse();
        BeanUtils.copyProperties(addedMusic, response);
        
        return Result.success(response, "音乐添加成功");
    }

    /**
     * 根据ID获取音乐详情
     * 
     * @param id 音乐ID
     * @return 音乐详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取音乐详情", description = "根据ID获取音乐的详细信息")
    public Result<MusicInfoResponse> getMusicById(@PathVariable Long id) {
        log.info("获取音乐详情: musicId={}", id);
        
        Music music = musicService.findById(id);
        
        MusicInfoResponse response = new MusicInfoResponse();
        BeanUtils.copyProperties(music, response);
        
        // 如果用户已登录，检查收藏和点赞状态
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        if (currentUserId != null) {
            response.setIsCollected(musicService.isUserCollected(id, currentUserId));
            // TODO: 实现点赞状态检查
            response.setIsLiked(false);
        }
        
        return Result.success(response);
    }

    /**
     * 分页查询音乐列表
     * 
     * @param pageRequest 分页请求参数
     * @param categoryId 分类ID（可选）
     * @param artistId 艺术家ID（可选）
     * @return 音乐分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "音乐列表", description = "分页查询音乐列表，支持按分类和艺术家筛选")
    public Result<PageResponse<MusicInfoResponse>> getMusicList(
            @Valid PageRequest pageRequest,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long artistId) {
        
        log.info("分页查询音乐列表: page={}, size={}, categoryId={}, artistId={}", 
                pageRequest.getPage(), pageRequest.getSize(), categoryId, artistId);
        
        Page<Music> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var musicPage = musicService.getMusicList(page, pageRequest.getKeyword(), categoryId, artistId);
        
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
     * 搜索音乐
     * 
     * @param keyword 搜索关键词
     * @param pageRequest 分页参数
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索音乐", description = "根据关键词搜索音乐")
    public Result<PageResponse<MusicInfoResponse>> searchMusic(
            @RequestParam String keyword,
            @Valid PageRequest pageRequest) {
        
        log.info("搜索音乐: keyword={}, page={}, size={}", keyword, pageRequest.getPage(), pageRequest.getSize());
        
        Page<Music> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var musicPage = musicService.searchMusic(keyword, page);
        
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
     * 获取热门音乐
     * 
     * @param limit 数量限制
     * @return 热门音乐列表
     */
    @GetMapping("/hot")
    @Operation(summary = "热门音乐", description = "获取热门音乐列表")
    public Result<List<MusicInfoResponse>> getHotMusic(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取热门音乐: limit={}", limit);
        
        List<Music> hotMusic = musicService.getHotMusic(limit);
        
        List<MusicInfoResponse> responseList = hotMusic.stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();
        
        return Result.success(responseList);
    }

    /**
     * 获取最新音乐
     * 
     * @param limit 数量限制
     * @return 最新音乐列表
     */
    @GetMapping("/latest")
    @Operation(summary = "最新音乐", description = "获取最新发布的音乐列表")
    public Result<List<MusicInfoResponse>> getLatestMusic(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取最新音乐: limit={}", limit);
        
        List<Music> latestMusic = musicService.getLatestMusic(limit);
        
        List<MusicInfoResponse> responseList = latestMusic.stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();
        
        return Result.success(responseList);
    }

    /**
     * 获取推荐音乐
     * 
     * @param limit 数量限制
     * @return 推荐音乐列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "推荐音乐", description = "根据用户偏好推荐音乐")
    @PreAuthorize("hasRole('USER')")
    public Result<List<MusicInfoResponse>> getRecommendedMusic(@RequestParam(defaultValue = "20") Integer limit) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取推荐音乐: userId={}, limit={}", currentUserId, limit);
        
        List<Music> recommendedMusic = musicService.getRecommendedMusic(currentUserId, limit);
        
        List<MusicInfoResponse> responseList = recommendedMusic.stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();
        
        return Result.success(responseList);
    }

    /**
     * 播放音乐（增加播放次数）
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @PostMapping("/{id}/play")
    @Operation(summary = "播放音乐", description = "播放音乐，增加播放次数")
    public Result<Void> playMusic(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        log.info("播放音乐: musicId={}, userId={}", id, currentUserId);
        
        boolean success = musicService.incrementPlayCount(id, currentUserId);
        if (success) {
            return Result.success(null, "播放记录成功");
        } else {
            return Result.error("播放记录失败");
        }
    }

    /**
     * 收藏音乐
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏音乐", description = "收藏喜欢的音乐")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> collectMusic(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("收藏音乐: musicId={}, userId={}", id, currentUserId);
        
        boolean success = musicService.incrementCollectCount(id, currentUserId);
        if (success) {
            return Result.success(null, "收藏成功");
        } else {
            return Result.error("收藏失败，可能已经收藏过了");
        }
    }

    /**
     * 取消收藏音乐
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/collect")
    @Operation(summary = "取消收藏", description = "取消收藏音乐")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> uncollectMusic(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("取消收藏音乐: musicId={}, userId={}", id, currentUserId);
        
        boolean success = musicService.decrementCollectCount(id, currentUserId);
        if (success) {
            return Result.success(null, "取消收藏成功");
        } else {
            return Result.error("取消收藏失败");
        }
    }

    /**
     * 点赞音乐
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞音乐", description = "为音乐点赞")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> likeMusic(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("点赞音乐: musicId={}, userId={}", id, currentUserId);
        
        boolean success = musicService.incrementLikeCount(id, currentUserId);
        if (success) {
            return Result.success(null, "点赞成功");
        } else {
            return Result.error("点赞失败");
        }
    }

    /**
     * 取消点赞音乐
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞", description = "取消音乐点赞")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> unlikeMusic(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("取消点赞音乐: musicId={}, userId={}", id, currentUserId);
        
        boolean success = musicService.decrementLikeCount(id, currentUserId);
        if (success) {
            return Result.success(null, "取消点赞成功");
        } else {
            return Result.error("取消点赞失败");
        }
    }

    /**
     * 获取相似音乐
     * 
     * @param id 音乐ID
     * @param limit 数量限制
     * @return 相似音乐列表
     */
    @GetMapping("/{id}/similar")
    @Operation(summary = "相似音乐", description = "获取与指定音乐相似的音乐列表")
    public Result<List<MusicInfoResponse>> getSimilarMusic(@PathVariable Long id, 
                                                          @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取相似音乐: musicId={}, limit={}", id, limit);
        
        List<Music> similarMusic = musicService.getSimilarMusic(id, limit);
        
        List<MusicInfoResponse> responseList = similarMusic.stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();
        
        return Result.success(responseList);
    }

    /**
     * 根据艺术家获取音乐
     * 
     * @param artistId 艺术家ID
     * @param pageRequest 分页参数
     * @return 音乐分页列表
     */
    @GetMapping("/artist/{artistId}")
    @Operation(summary = "艺术家音乐", description = "获取指定艺术家的音乐列表")
    public Result<PageResponse<MusicInfoResponse>> getMusicByArtist(
            @PathVariable Long artistId,
            @Valid PageRequest pageRequest) {
        
        log.info("获取艺术家音乐: artistId={}, page={}, size={}", artistId, pageRequest.getPage(), pageRequest.getSize());
        
        Page<Music> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        var musicPage = musicService.getMusicByArtist(artistId, page);
        
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
     * 根据专辑获取音乐
     * 
     * @param albumId 专辑ID
     * @return 专辑音乐列表
     */
    @GetMapping("/album/{albumId}")
    @Operation(summary = "专辑音乐", description = "获取指定专辑的音乐列表")
    public Result<List<MusicInfoResponse>> getMusicByAlbum(@PathVariable Long albumId) {
        log.info("获取专辑音乐: albumId={}", albumId);
        
        List<Music> albumMusic = musicService.getMusicByAlbum(albumId);
        
        List<MusicInfoResponse> responseList = albumMusic.stream()
                .map(music -> {
                    MusicInfoResponse response = new MusicInfoResponse();
                    BeanUtils.copyProperties(music, response);
                    return response;
                }).toList();
        
        return Result.success(responseList);
    }
}