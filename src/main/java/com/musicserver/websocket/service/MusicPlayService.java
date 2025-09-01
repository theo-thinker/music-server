package com.musicserver.websocket.service;

import com.musicserver.dto.websocket.MusicPlayStatusMessage;
import com.musicserver.dto.websocket.WebSocketMessage;
import com.musicserver.entity.Music;
import com.musicserver.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 音乐播放状态WebSocket服务
 * 
 * 管理音乐播放状态同步、房间播放、播放列表同步等功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicPlayService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MusicService musicService;
    private final OnlineUserService onlineUserService;

    // 用户播放状态缓存 userId -> PlayStatus
    private final Map<Long, PlayStatus> userPlayStatus = new ConcurrentHashMap<>();
    
    // 房间播放状态缓存 roomId -> RoomPlayStatus
    private final Map<String, RoomPlayStatus> roomPlayStatus = new ConcurrentHashMap<>();

    private static final String USER_PLAY_STATUS_KEY = "websocket:play:user:";
    private static final String ROOM_PLAY_STATUS_KEY = "websocket:play:room:";
    private static final long STATUS_EXPIRE_TIME = 24; // 状态过期时间（小时）

    /**
     * 更新用户播放状态
     * 
     * @param userId 用户ID
     * @param musicId 音乐ID
     * @param action 操作类型
     * @param currentTime 当前播放时间
     * @param volume 音量
     * @param playMode 播放模式
     */
    public void updateUserPlayStatus(Long userId, Long musicId, String action, 
                                   Integer currentTime, Integer volume, String playMode) {
        try {
            Music music = musicService.findById(musicId);
            if (music == null) {
                log.warn("音乐不存在: musicId={}", musicId);
                return;
            }

            PlayStatus status = userPlayStatus.computeIfAbsent(userId, k -> new PlayStatus());
            updatePlayStatus(status, music, action, currentTime, volume, playMode);
            
            // 更新用户会话中的音乐信息
            OnlineUserService.UserSession session = onlineUserService.getUserSession(userId);
            if (session != null) {
                session.setCurrentMusicId(musicId);
                session.setCurrentMusicTitle(music.getTitle());
                session.setIsPlaying("PLAYING".equals(status.getPlayStatus()));
            }

            // 保存到Redis
            String redisKey = USER_PLAY_STATUS_KEY + userId;
            redisTemplate.opsForValue().set(redisKey, status, STATUS_EXPIRE_TIME, TimeUnit.HOURS);

            // 构建消息
            MusicPlayStatusMessage message = buildPlayStatusMessage(status, userId, null, action);
            
            // 发送给用户个人频道
            sendToUser(userId, "MUSIC_PLAY_STATUS", message);
            
            // 如果用户在房间中，同步给房间其他成员
            if (status.getRoomId() != null) {
                syncToRoom(status.getRoomId(), message, userId);
            }

            log.debug("更新用户播放状态: userId={}, musicId={}, action={}", userId, musicId, action);
        } catch (Exception e) {
            log.error("更新用户播放状态失败: userId={}, musicId={}", userId, musicId, e);
        }
    }

    /**
     * 创建或加入音乐房间
     * 
     * @param userId 用户ID
     * @param roomId 房间ID
     * @return 是否成功
     */
    public boolean joinMusicRoom(Long userId, String roomId) {
        try {
            // 获取或创建房间状态
            RoomPlayStatus roomStatus = roomPlayStatus.computeIfAbsent(roomId, k -> new RoomPlayStatus());
            roomStatus.getMembers().add(userId);
            roomStatus.setMemberCount(roomStatus.getMembers().size());

            // 更新用户状态
            PlayStatus userStatus = userPlayStatus.get(userId);
            if (userStatus != null) {
                userStatus.setRoomId(roomId);
            }

            // 保存到Redis
            String redisKey = ROOM_PLAY_STATUS_KEY + roomId;
            redisTemplate.opsForValue().set(redisKey, roomStatus, STATUS_EXPIRE_TIME, TimeUnit.HOURS);

            // 通知房间成员
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("USER_JOIN_ROOM")
                    .content(Map.of(
                            "userId", userId,
                            "roomId", roomId,
                            "memberCount", roomStatus.getMemberCount()
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();
            
            messagingTemplate.convertAndSend("/topic/music/room/" + roomId, message);

            // 如果房间有播放状态，同步给新加入的用户
            if (roomStatus.getCurrentMusic() != null) {
                MusicPlayStatusMessage playMessage = buildPlayStatusMessage(
                        roomStatus.getCurrentMusic(), roomStatus.getHostUserId(), roomId, "SYNC");
                sendToUser(userId, "ROOM_MUSIC_SYNC", playMessage);
            }

            log.info("用户加入音乐房间: userId={}, roomId={}, memberCount={}", 
                    userId, roomId, roomStatus.getMemberCount());
            return true;
        } catch (Exception e) {
            log.error("用户加入音乐房间失败: userId={}, roomId={}", userId, roomId, e);
            return false;
        }
    }

    /**
     * 离开音乐房间
     * 
     * @param userId 用户ID
     * @param roomId 房间ID
     * @return 是否成功
     */
    public boolean leaveMusicRoom(Long userId, String roomId) {
        try {
            RoomPlayStatus roomStatus = roomPlayStatus.get(roomId);
            if (roomStatus != null) {
                roomStatus.getMembers().remove(userId);
                roomStatus.setMemberCount(roomStatus.getMembers().size());

                // 如果房间空了，删除房间
                if (roomStatus.getMembers().isEmpty()) {
                    roomPlayStatus.remove(roomId);
                    redisTemplate.delete(ROOM_PLAY_STATUS_KEY + roomId);
                } else {
                    // 如果离开的是房主，转移房主权限
                    if (userId.equals(roomStatus.getHostUserId()) && !roomStatus.getMembers().isEmpty()) {
                        roomStatus.setHostUserId(roomStatus.getMembers().iterator().next());
                    }
                    
                    String redisKey = ROOM_PLAY_STATUS_KEY + roomId;
                    redisTemplate.opsForValue().set(redisKey, roomStatus, STATUS_EXPIRE_TIME, TimeUnit.HOURS);
                }

                // 通知房间成员
                WebSocketMessage message = WebSocketMessage.builder()
                        .type("USER_LEAVE_ROOM")
                        .content(Map.of(
                                "userId", userId,
                                "roomId", roomId,
                                "memberCount", roomStatus.getMemberCount(),
                                "newHostId", roomStatus.getHostUserId()
                        ))
                        .timestamp(LocalDateTime.now())
                        .build();
                
                messagingTemplate.convertAndSend("/topic/music/room/" + roomId, message);
            }

            // 更新用户状态
            PlayStatus userStatus = userPlayStatus.get(userId);
            if (userStatus != null) {
                userStatus.setRoomId(null);
            }

            log.info("用户离开音乐房间: userId={}, roomId={}", userId, roomId);
            return true;
        } catch (Exception e) {
            log.error("用户离开音乐房间失败: userId={}, roomId={}", userId, roomId, e);
            return false;
        }
    }

    /**
     * 房间音乐控制（仅房主）
     * 
     * @param hostUserId 房主用户ID
     * @param roomId 房间ID
     * @param musicId 音乐ID
     * @param action 操作类型
     * @param currentTime 当前播放时间
     * @param volume 音量
     * @param playMode 播放模式
     */
    public void controlRoomMusic(Long hostUserId, String roomId, Long musicId, String action,
                               Integer currentTime, Integer volume, String playMode) {
        try {
            RoomPlayStatus roomStatus = roomPlayStatus.get(roomId);
            if (roomStatus == null || !hostUserId.equals(roomStatus.getHostUserId())) {
                log.warn("用户无权控制房间音乐: userId={}, roomId={}", hostUserId, roomId);
                return;
            }

            Music music = musicService.findById(musicId);
            if (music == null) {
                log.warn("音乐不存在: musicId={}", musicId);
                return;
            }

            // 更新房间播放状态
            PlayStatus status = roomStatus.getCurrentMusic();
            if (status == null) {
                status = new PlayStatus();
                roomStatus.setCurrentMusic(status);
            }
            
            updatePlayStatus(status, music, action, currentTime, volume, playMode);
            status.setRoomId(roomId);

            // 保存到Redis
            String redisKey = ROOM_PLAY_STATUS_KEY + roomId;
            redisTemplate.opsForValue().set(redisKey, roomStatus, STATUS_EXPIRE_TIME, TimeUnit.HOURS);

            // 构建消息
            MusicPlayStatusMessage message = buildPlayStatusMessage(status, hostUserId, roomId, action);
            
            // 同步给房间所有成员
            syncToRoom(roomId, message, null);

            log.debug("房间音乐控制: hostUserId={}, roomId={}, musicId={}, action={}", 
                    hostUserId, roomId, musicId, action);
        } catch (Exception e) {
            log.error("房间音乐控制失败: hostUserId={}, roomId={}, musicId={}", 
                    hostUserId, roomId, musicId, e);
        }
    }

    /**
     * 获取用户播放状态
     * 
     * @param userId 用户ID
     * @return 播放状态
     */
    public MusicPlayStatusMessage getUserPlayStatus(Long userId) {
        try {
            PlayStatus status = userPlayStatus.get(userId);
            if (status == null) {
                return null;
            }
            return buildPlayStatusMessage(status, userId, null, "GET_STATUS");
        } catch (Exception e) {
            log.error("获取用户播放状态失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取房间播放状态
     * 
     * @param roomId 房间ID
     * @return 播放状态
     */
    public MusicPlayStatusMessage getRoomPlayStatus(String roomId) {
        try {
            RoomPlayStatus roomStatus = roomPlayStatus.get(roomId);
            if (roomStatus == null || roomStatus.getCurrentMusic() == null) {
                return null;
            }
            return buildPlayStatusMessage(roomStatus.getCurrentMusic(), 
                    roomStatus.getHostUserId(), roomId, "GET_STATUS");
        } catch (Exception e) {
            log.error("获取房间播放状态失败: roomId={}", roomId, e);
            return null;
        }
    }

    /**
     * 更新播放状态
     */
    private void updatePlayStatus(PlayStatus status, Music music, String action, 
                                Integer currentTime, Integer volume, String playMode) {
        status.setMusicId(music.getId());
        status.setTitle(music.getTitle());
        status.setArtist(music.getArtist() != null ? music.getArtist().getName() : "");
        status.setAlbum(music.getAlbum() != null ? music.getAlbum().getName() : "");
        status.setCoverUrl(music.getAlbumCover());
        status.setMusicUrl(music.getFileUrl());
        status.setDuration(music.getDuration());
        status.setUpdateTime(LocalDateTime.now());

        if (currentTime != null) {
            status.setCurrentTime(currentTime);
            if (music.getDuration() != null && music.getDuration() > 0) {
                status.setProgress((double) currentTime / music.getDuration() * 100);
            }
        }

        if (volume != null) {
            status.setVolume(volume);
        }

        if (playMode != null) {
            status.setPlayMode(playMode);
        }

        // 根据操作类型更新播放状态
        switch (action.toUpperCase()) {
            case "PLAY":
                status.setPlayStatus("PLAYING");
                break;
            case "PAUSE":
                status.setPlayStatus("PAUSED");
                break;
            case "STOP":
                status.setPlayStatus("STOPPED");
                status.setCurrentTime(0);
                status.setProgress(0.0);
                break;
            case "SEEK":
                // 保持当前播放状态
                break;
            default:
                break;
        }
    }

    /**
     * 构建播放状态消息
     */
    private MusicPlayStatusMessage buildPlayStatusMessage(PlayStatus status, Long operatorId, 
                                                         String roomId, String action) {
        OnlineUserService.UserSession session = onlineUserService.getUserSession(operatorId);
        String operatorName = session != null ? session.getUsername() : "Unknown";

        return MusicPlayStatusMessage.builder()
                .musicId(status.getMusicId())
                .title(status.getTitle())
                .artist(status.getArtist())
                .album(status.getAlbum())
                .coverUrl(status.getCoverUrl())
                .musicUrl(status.getMusicUrl())
                .playStatus(status.getPlayStatus())
                .currentTime(status.getCurrentTime())
                .duration(status.getDuration())
                .progress(status.getProgress())
                .volume(status.getVolume())
                .playMode(status.getPlayMode())
                .roomId(roomId)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .action(action)
                .build();
    }

    /**
     * 发送消息给用户
     */
    private void sendToUser(Long userId, String type, Object content) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/music/status", 
                message
        );
    }

    /**
     * 同步消息到房间
     */
    private void syncToRoom(String roomId, MusicPlayStatusMessage content, Long excludeUserId) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("ROOM_MUSIC_SYNC")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();

        // 如果有排除用户，需要单独发送给其他用户
        if (excludeUserId != null) {
            RoomPlayStatus roomStatus = roomPlayStatus.get(roomId);
            if (roomStatus != null) {
                for (Long memberId : roomStatus.getMembers()) {
                    if (!memberId.equals(excludeUserId)) {
                        messagingTemplate.convertAndSendToUser(
                                memberId.toString(),
                                "/queue/music/room",
                                message
                        );
                    }
                }
            }
        } else {
            messagingTemplate.convertAndSend("/topic/music/room/" + roomId, message);
        }
    }

    /**
     * 播放状态数据类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlayStatus {
        private Long musicId;
        private String title;
        private String artist;
        private String album;
        private String coverUrl;
        private String musicUrl;
        private String playStatus = "STOPPED";
        private Integer currentTime = 0;
        private Integer duration;
        private Double progress = 0.0;
        private Integer volume = 80;
        private String playMode = "SEQUENTIAL";
        private String roomId;
        private LocalDateTime updateTime;
    }

    /**
     * 房间播放状态数据类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoomPlayStatus {
        private String roomId;
        private Long hostUserId;
        private PlayStatus currentMusic;
        private java.util.Set<Long> members = new java.util.HashSet<>();
        private Integer memberCount = 0;
        private LocalDateTime createTime = LocalDateTime.now();
        private LocalDateTime updateTime = LocalDateTime.now();
    }
}