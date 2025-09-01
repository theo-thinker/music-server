package com.musicserver.dto.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 音乐播放状态消息DTO
 * 
 * 用于WebSocket传输音乐播放状态信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音乐播放状态消息DTO")
public class MusicPlayStatusMessage {

    /**
     * 音乐ID
     */
    @Schema(description = "音乐ID", example = "1")
    private Long musicId;

    /**
     * 音乐标题
     */
    @Schema(description = "音乐标题", example = "告白气球")
    private String title;

    /**
     * 艺术家
     */
    @Schema(description = "艺术家", example = "周杰伦")
    private String artist;

    /**
     * 专辑名称
     */
    @Schema(description = "专辑名称", example = "周杰伦的床边故事")
    private String album;

    /**
     * 封面图片URL
     */
    @Schema(description = "封面图片URL", example = "http://localhost:9000/image-files/cover.jpg")
    private String coverUrl;

    /**
     * 音乐文件URL
     */
    @Schema(description = "音乐文件URL", example = "http://localhost:9000/music-files/music.mp3")
    private String musicUrl;

    /**
     * 播放状态
     * PLAYING: 正在播放
     * PAUSED: 已暂停
     * STOPPED: 已停止
     */
    @Schema(description = "播放状态", example = "PLAYING")
    private String playStatus;

    /**
     * 当前播放时间（秒）
     */
    @Schema(description = "当前播放时间(秒)", example = "120")
    private Integer currentTime;

    /**
     * 音乐总时长（秒）
     */
    @Schema(description = "音乐总时长(秒)", example = "240")
    private Integer duration;

    /**
     * 播放进度百分比
     */
    @Schema(description = "播放进度百分比", example = "50.0")
    private Double progress;

    /**
     * 音量大小（0-100）
     */
    @Schema(description = "音量大小", example = "80")
    private Integer volume;

    /**
     * 播放模式
     * SINGLE: 单曲循环
     * REPEAT: 列表循环
     * SHUFFLE: 随机播放
     * SEQUENTIAL: 顺序播放
     */
    @Schema(description = "播放模式", example = "SEQUENTIAL")
    private String playMode;

    /**
     * 播放列表ID
     */
    @Schema(description = "播放列表ID", example = "1")
    private Long playlistId;

    /**
     * 房间ID（多人同步播放）
     */
    @Schema(description = "房间ID", example = "room_001")
    private String roomId;

    /**
     * 操作者用户ID
     */
    @Schema(description = "操作者用户ID", example = "1")
    private Long operatorId;

    /**
     * 操作者用户名
     */
    @Schema(description = "操作者用户名", example = "admin")
    private String operatorName;

    /**
     * 操作类型
     * PLAY: 播放
     * PAUSE: 暂停
     * STOP: 停止
     * SEEK: 跳转
     * NEXT: 下一首
     * PREVIOUS: 上一首
     * VOLUME_CHANGE: 音量变化
     * MODE_CHANGE: 模式变化
     */
    @Schema(description = "操作类型", example = "PLAY")
    private String action;
}