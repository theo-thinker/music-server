package com.musicserver.common;

/**
 * 系统常量定义类
 * 
 * 集中管理系统中使用的各种常量，便于统一维护和修改
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public final class Constants {
    
    /**
     * 私有构造函数，防止实例化
     */
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ========================================
    // JWT相关常量
    // ========================================
    
    /**
     * JWT令牌请求头名称
     */
    public static final String JWT_HEADER = "Authorization";
    
    /**
     * JWT令牌前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    
    /**
     * JWT用户ID声明键名
     */
    public static final String JWT_USER_ID_KEY = "userId";
    
    /**
     * JWT用户名声明键名
     */
    public static final String JWT_USERNAME_KEY = "username";
    
    /**
     * JWT权限声明键名
     */
    public static final String JWT_AUTHORITIES_KEY = "authorities";
    
    // ========================================
    // Redis缓存相关常量
    // ========================================
    
    /**
     * Redis键分隔符
     */
    public static final String REDIS_KEY_SEPARATOR = ":";
    
    /**
     * 用户信息缓存键前缀
     */
    public static final String REDIS_USER_KEY = "user";
    
    /**
     * 音乐信息缓存键前缀
     */
    public static final String REDIS_MUSIC_KEY = "music";
    
    /**
     * 播放列表缓存键前缀
     */
    public static final String REDIS_PLAYLIST_KEY = "playlist";
    
    /**
     * 验证码缓存键前缀
     */
    public static final String REDIS_CAPTCHA_KEY = "captcha";
    
    /**
     * 登录失败次数缓存键前缀
     */
    public static final String REDIS_LOGIN_FAIL_KEY = "login_fail";
    
    /**
     * 用户会话缓存键前缀
     */
    public static final String REDIS_USER_SESSION_KEY = "user_session";
    
    /**
     * JWT黑名单缓存键前缀
     */
    public static final String REDIS_JWT_BLACKLIST_KEY = "jwt_blacklist";
    
    // ========================================
    // 用户状态常量
    // ========================================
    
    /**
     * 用户状态：禁用
     */
    public static final Integer USER_STATUS_DISABLED = 0;
    
    /**
     * 用户状态：正常
     */
    public static final Integer USER_STATUS_NORMAL = 1;
    
    /**
     * 用户状态：冻结
     */
    public static final Integer USER_STATUS_FROZEN = 2;
    
    /**
     * 用户性别：未知
     */
    public static final Integer GENDER_UNKNOWN = 0;
    
    /**
     * 用户性别：男
     */
    public static final Integer GENDER_MALE = 1;
    
    /**
     * 用户性别：女
     */
    public static final Integer GENDER_FEMALE = 2;
    
    // ========================================
    // 音乐相关常量
    // ========================================
    
    /**
     * 音乐状态：下架
     */
    public static final Integer MUSIC_STATUS_DISABLED = 0;
    
    /**
     * 音乐状态：正常
     */
    public static final Integer MUSIC_STATUS_NORMAL = 1;
    
    /**
     * 音质等级：标准
     */
    public static final Integer QUALITY_STANDARD = 1;
    
    /**
     * 音质等级：高品质
     */
    public static final Integer QUALITY_HIGH = 2;
    
    /**
     * 音质等级：无损
     */
    public static final Integer QUALITY_LOSSLESS = 3;
    
    /**
     * 支持的音频格式
     */
    public static final String[] SUPPORTED_AUDIO_FORMATS = {
        "mp3", "flac", "wav", "aac", "m4a", "ogg"
    };
    
    /**
     * 支持的图片格式
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = {
        "jpg", "jpeg", "png", "gif", "webp"
    };
    
    /**
     * 支持的歌词格式
     */
    public static final String[] SUPPORTED_LYRIC_FORMATS = {
        "lrc", "txt"
    };
    
    // ========================================
    // 播放列表相关常量
    // ========================================
    
    /**
     * 播放列表可见性：私有
     */
    public static final Integer PLAYLIST_PRIVATE = 0;
    
    /**
     * 播放列表可见性：公开
     */
    public static final Integer PLAYLIST_PUBLIC = 1;
    
    /**
     * 播放模式：顺序播放
     */
    public static final Integer PLAYBACK_MODE_SEQUENTIAL = 1;
    
    /**
     * 播放模式：随机播放
     */
    public static final Integer PLAYBACK_MODE_SHUFFLE = 2;
    
    /**
     * 播放模式：单曲循环
     */
    public static final Integer PLAYBACK_MODE_REPEAT_ONE = 3;
    
    // ========================================
    // 文件相关常量
    // ========================================
    
    /**
     * 默认上传文件大小限制（100MB）
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 100 * 1024 * 1024L;
    
    /**
     * 音乐文件上传路径
     */
    public static final String MUSIC_UPLOAD_PATH = "music";
    
    /**
     * 图片文件上传路径
     */
    public static final String IMAGE_UPLOAD_PATH = "images";
    
    /**
     * 歌词文件上传路径
     */
    public static final String LYRIC_UPLOAD_PATH = "lyrics";
    
    /**
     * 头像文件上传路径
     */
    public static final String AVATAR_UPLOAD_PATH = "avatars";
    
    // ========================================
    // 分页相关常量
    // ========================================
    
    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;
    
    // ========================================
    // 时间相关常量
    // ========================================
    
    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
    
    // ========================================
    // 正则表达式常量
    // ========================================
    
    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * 手机号正则表达式（中国大陆）
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    /**
     * 用户名正则表达式（4-20位字母数字下划线）
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{4,20}$";
    
    /**
     * 密码正则表达式（6-20位，至少包含字母和数字）
     */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,20}$";
    
    // ========================================
    // HTTP相关常量
    // ========================================
    
    /**
     * 请求追踪ID头名称
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * 用户代理头名称
     */
    public static final String USER_AGENT_HEADER = "User-Agent";
    
    /**
     * 客户端IP头名称
     */
    public static final String CLIENT_IP_HEADER = "X-Real-IP";
    
    // ========================================
    // 默认值常量
    // ========================================
    
    /**
     * 默认头像URL
     */
    public static final String DEFAULT_AVATAR_URL = "/static/images/default_avatar.jpg";
    
    /**
     * 默认专辑封面URL
     */
    public static final String DEFAULT_ALBUM_COVER_URL = "/static/images/default_album_cover.jpg";
    
    /**
     * 默认播放列表封面URL
     */
    public static final String DEFAULT_PLAYLIST_COVER_URL = "/static/images/default_playlist_cover.jpg";
}