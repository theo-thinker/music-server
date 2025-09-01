package com.musicserver.websocket.constant;

/**
 * WebSocket常量定义
 * 
 * 定义WebSocket相关的常量值
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public final class WebSocketConstants {

    private WebSocketConstants() {
        // 工具类不允许实例化
    }

    /**
     * WebSocket端点
     */
    public static final class Endpoints {
        public static final String WEBSOCKET = "/ws";
        public static final String WEBSOCKET_NATIVE = "/ws-native";
    }

    /**
     * 消息类型常量
     */
    public static final class MessageType {
        // 系统消息
        public static final String HEARTBEAT = "HEARTBEAT";
        public static final String HEARTBEAT_RESPONSE = "HEARTBEAT_RESPONSE";
        public static final String SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";
        public static final String USER_NOTIFICATION = "USER_NOTIFICATION";
        public static final String FORCE_LOGOUT = "FORCE_LOGOUT";
        
        // 用户状态消息
        public static final String USER_ONLINE = "USER_ONLINE";
        public static final String USER_OFFLINE = "USER_OFFLINE";
        public static final String USER_STATUS_CHANGE = "USER_STATUS_CHANGE";
        public static final String ONLINE_USERS_LIST = "ONLINE_USERS_LIST";
        
        // 音乐播放消息
        public static final String MUSIC_PLAY_STATUS = "MUSIC_PLAY_STATUS";
        public static final String MUSIC_CONTROL = "MUSIC_CONTROL";
        public static final String ROOM_MUSIC_SYNC = "ROOM_MUSIC_SYNC";
        public static final String MUSIC_RECOMMEND = "MUSIC_RECOMMEND";
        
        // 房间消息
        public static final String USER_JOIN_ROOM = "USER_JOIN_ROOM";
        public static final String USER_LEAVE_ROOM = "USER_LEAVE_ROOM";
        public static final String ROOM_PLAY_STATUS = "ROOM_PLAY_STATUS";
        
        // 聊天消息
        public static final String CHAT_MESSAGE = "CHAT_MESSAGE";
        public static final String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
        public static final String GROUP_MESSAGE = "GROUP_MESSAGE";
        
        // 测试消息
        public static final String PING_TEST = "PING_TEST";
        public static final String PONG_TEST = "PONG_TEST";
    }

    /**
     * 目标地址常量
     */
    public static final class Destinations {
        // 订阅地址
        public static final String TOPIC_ONLINE_STATUS = "/topic/online/status";
        public static final String TOPIC_ONLINE_USERS = "/topic/online/users";
        public static final String TOPIC_SYSTEM_NOTIFICATIONS = "/topic/system/notifications";
        public static final String TOPIC_MUSIC_RECOMMEND = "/topic/music/recommend";
        public static final String TOPIC_TEST_PING = "/topic/test/ping";
        
        // 音乐房间
        public static final String TOPIC_MUSIC_ROOM = "/topic/music/room/";
        
        // 聊天房间
        public static final String TOPIC_CHAT_ROOM = "/topic/chat/room/";
        
        // 用户队列
        public static final String QUEUE_USER_ONLINE = "/queue/online/users";
        public static final String QUEUE_USER_STATUS = "/queue/user/status";
        public static final String QUEUE_MUSIC_STATUS = "/queue/music/status";
        public static final String QUEUE_MUSIC_ROOM = "/queue/music/room";
        public static final String QUEUE_CHAT_PRIVATE = "/queue/chat/private";
        public static final String QUEUE_NOTIFICATIONS = "/queue/notifications";
        public static final String QUEUE_HEARTBEAT = "/queue/heartbeat";
        public static final String QUEUE_SYSTEM_FORCE_LOGOUT = "/queue/system/force-logout";
        
        // 应用消息映射
        public static final String APP_MUSIC_CONTROL = "/app/music/control";
        public static final String APP_MUSIC_ROOM_CONTROL = "/app/music/room/{roomId}/control";
        public static final String APP_MUSIC_ROOM_JOIN = "/app/music/room/{roomId}/join";
        public static final String APP_MUSIC_ROOM_LEAVE = "/app/music/room/{roomId}/leave";
        public static final String APP_CHAT_SEND = "/app/chat/send";
        public static final String APP_USER_STATUS = "/app/user/status";
        public static final String APP_USER_ONLINE_LIST = "/app/user/online/list";
        public static final String APP_HEARTBEAT = "/app/heartbeat";
        public static final String APP_MUSIC_STATUS_GET = "/app/music/status/get";
        public static final String APP_MUSIC_ROOM_STATUS = "/app/music/room/{roomId}/status";
        public static final String APP_SYSTEM_NOTIFICATION = "/app/system/notification";
    }

    /**
     * 用户状态常量
     */
    public static final class UserStatus {
        public static final String ONLINE = "ONLINE";
        public static final String OFFLINE = "OFFLINE";
        public static final String AWAY = "AWAY";
        public static final String BUSY = "BUSY";
        public static final String INVISIBLE = "INVISIBLE";
    }

    /**
     * 播放状态常量
     */
    public static final class PlayStatus {
        public static final String PLAYING = "PLAYING";
        public static final String PAUSED = "PAUSED";
        public static final String STOPPED = "STOPPED";
        public static final String LOADING = "LOADING";
    }

    /**
     * 播放模式常量
     */
    public static final class PlayMode {
        public static final String SINGLE = "SINGLE";           // 单曲循环
        public static final String REPEAT = "REPEAT";           // 列表循环
        public static final String SHUFFLE = "SHUFFLE";         // 随机播放
        public static final String SEQUENTIAL = "SEQUENTIAL";   // 顺序播放
    }

    /**
     * 播放操作常量
     */
    public static final class PlayAction {
        public static final String PLAY = "PLAY";
        public static final String PAUSE = "PAUSE";
        public static final String STOP = "STOP";
        public static final String SEEK = "SEEK";
        public static final String NEXT = "NEXT";
        public static final String PREVIOUS = "PREVIOUS";
        public static final String VOLUME_CHANGE = "VOLUME_CHANGE";
        public static final String MODE_CHANGE = "MODE_CHANGE";
        public static final String SYNC = "SYNC";
        public static final String GET_STATUS = "GET_STATUS";
    }

    /**
     * 消息状态常量
     */
    public static final class MessageStatus {
        public static final String SENDING = "SENDING";
        public static final String SENT = "SENT";
        public static final String DELIVERED = "DELIVERED";
        public static final String READ = "READ";
        public static final String FAILED = "FAILED";
        public static final String SUCCESS = "SUCCESS";
        public static final String ERROR = "ERROR";
    }

    /**
     * 聊天消息类型常量
     */
    public static final class ChatMessageType {
        public static final String TEXT = "TEXT";
        public static final String IMAGE = "IMAGE";
        public static final String AUDIO = "AUDIO";
        public static final String FILE = "FILE";
        public static final String MUSIC = "MUSIC";
        public static final String SYSTEM = "SYSTEM";
        public static final String EMOJI = "EMOJI";
        public static final String LINK = "LINK";
    }

    /**
     * 通知类型常量
     */
    public static final class NotificationType {
        public static final String SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
        public static final String NEW_MUSIC = "NEW_MUSIC";
        public static final String FRIEND_REQUEST = "FRIEND_REQUEST";
        public static final String COMMENT = "COMMENT";
        public static final String LIKE = "LIKE";
        public static final String FOLLOW = "FOLLOW";
        public static final String PLAYLIST_SHARE = "PLAYLIST_SHARE";
        public static final String SYSTEM_UPDATE = "SYSTEM_UPDATE";
        public static final String ANNOUNCEMENT = "ANNOUNCEMENT";
        public static final String FORCE_LOGOUT = "FORCE_LOGOUT";
    }

    /**
     * 通知级别常量
     */
    public static final class NotificationLevel {
        public static final String INFO = "INFO";
        public static final String WARNING = "WARNING";
        public static final String ERROR = "ERROR";
        public static final String SUCCESS = "SUCCESS";
    }

    /**
     * 设备类型常量
     */
    public static final class DeviceType {
        public static final String WEB = "WEB";
        public static final String MOBILE = "MOBILE";
        public static final String DESKTOP = "DESKTOP";
        public static final String TABLET = "TABLET";
        public static final String UNKNOWN = "UNKNOWN";
    }

    /**
     * Redis键常量
     */
    public static final class RedisKeys {
        public static final String ONLINE_USERS = "websocket:online:users";
        public static final String USER_SESSION_PREFIX = "websocket:session:";
        public static final String USER_PLAY_STATUS_PREFIX = "websocket:play:user:";
        public static final String ROOM_PLAY_STATUS_PREFIX = "websocket:play:room:";
        public static final String CHAT_ROOM_PREFIX = "websocket:chat:room:";
        public static final String USER_NOTIFICATIONS_PREFIX = "websocket:notifications:user:";
    }

    /**
     * 超时时间常量（分钟）
     */
    public static final class Timeout {
        public static final long SESSION_TIMEOUT = 30;          // 会话超时时间
        public static final long HEARTBEAT_INTERVAL = 1;        // 心跳间隔
        public static final long NOTIFICATION_EXPIRE = 1440;    // 通知过期时间（24小时）
        public static final long PLAY_STATUS_EXPIRE = 1440;     // 播放状态过期时间（24小时）
        public static final long ROOM_EXPIRE = 60;              // 房间过期时间
    }

    /**
     * 限制常量
     */
    public static final class Limits {
        public static final int MAX_MESSAGE_LENGTH = 1000;      // 最大消息长度
        public static final int MAX_ROOM_MEMBERS = 50;          // 房间最大成员数
        public static final int MAX_ONLINE_USERS_DISPLAY = 100; // 最大显示在线用户数
        public static final int MAX_NOTIFICATION_COUNT = 50;    // 最大通知数量
        public static final int MAX_CHAT_HISTORY = 100;         // 最大聊天历史记录
    }

    /**
     * 错误代码常量
     */
    public static final class ErrorCode {
        public static final String AUTHENTICATION_FAILED = "WS_AUTH_FAILED";
        public static final String CONNECTION_LIMIT_EXCEEDED = "WS_CONN_LIMIT";
        public static final String MESSAGE_TOO_LARGE = "WS_MSG_TOO_LARGE";
        public static final String ROOM_FULL = "WS_ROOM_FULL";
        public static final String PERMISSION_DENIED = "WS_PERMISSION_DENIED";
        public static final String USER_NOT_FOUND = "WS_USER_NOT_FOUND";
        public static final String ROOM_NOT_FOUND = "WS_ROOM_NOT_FOUND";
        public static final String INVALID_MESSAGE_FORMAT = "WS_INVALID_MSG_FORMAT";
    }

    /**
     * 权限常量
     */
    public static final class Permission {
        public static final String ROOM_HOST = "ROOM_HOST";         // 房主权限
        public static final String ROOM_ADMIN = "ROOM_ADMIN";       // 房间管理员
        public static final String ROOM_MEMBER = "ROOM_MEMBER";     // 房间成员
        public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";   // 系统管理员
    }
}