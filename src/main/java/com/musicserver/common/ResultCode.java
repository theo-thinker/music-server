package com.musicserver.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回结果状态码枚举
 * 
 * 定义了系统中所有可能的返回状态码和对应的消息
 * 便于统一管理和维护错误码
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Getter
public enum ResultCode {
    
    // ========================================
    // 通用状态码 (200-299)
    // ========================================
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    
    // ========================================
    // 客户端错误状态码 (400-499)
    // ========================================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不被允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "请求冲突"),
    UNPROCESSABLE_ENTITY(422, "请求参数验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    // ========================================
    // 服务器错误状态码 (500-599)
    // ========================================
    INTERNAL_ERROR(500, "服务器内部错误"),
    NOT_IMPLEMENTED(501, "功能未实现"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    
    // ========================================
    // 业务逻辑错误状态码 (1000-1999)
    // ========================================
    
    // 通用业务错误码 (1050-1099)
    BUSINESS_ERROR(1050, "业务处理失败"),
    OPERATION_FAILED(1051, "操作失败"),
    PARAMETER_ERROR(1052, "参数错误"),
    DATA_NOT_FOUND(1053, "数据不存在"),
    PASSWORD_ERROR(1054, "密码错误"),
    
    // 用户相关错误码 (1000-1049)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_LOCKED(1004, "用户账户已被锁定"),
    INVALID_USERNAME_OR_PASSWORD(1005, "用户名或密码错误"),
    PASSWORD_NOT_MATCH(1006, "密码不匹配"),
    OLD_PASSWORD_ERROR(1007, "原密码错误"),
    USER_NOT_LOGIN(1008, "用户未登录"),
    LOGIN_EXPIRED(1009, "登录已过期，请重新登录"),
    USERNAME_ALREADY_EXISTS(1010, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1011, "邮箱已被注册"),
    PHONE_ALREADY_EXISTS(1012, "手机号已被注册"),
    INVALID_EMAIL_FORMAT(1013, "邮箱格式不正确"),
    INVALID_PHONE_FORMAT(1014, "手机号格式不正确"),
    VERIFICATION_CODE_ERROR(1015, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1016, "验证码已过期"),
    
    // 音乐相关错误码 (1100-1199)
    MUSIC_NOT_FOUND(1101, "音乐不存在"),
    MUSIC_DISABLED(1102, "音乐已下架"),
    MUSIC_FILE_NOT_FOUND(1103, "音乐文件不存在"),
    MUSIC_FORMAT_NOT_SUPPORTED(1104, "不支持的音乐格式"),
    MUSIC_UPLOAD_FAILED(1105, "音乐上传失败"),
    MUSIC_SIZE_EXCEEDED(1106, "音乐文件过大"),
    MUSIC_ALREADY_EXISTS(1107, "音乐已存在"),
    INVALID_MUSIC_DURATION(1108, "无效的音乐时长"),
    LYRICS_NOT_FOUND(1109, "歌词文件不存在"),
    LYRICS_FORMAT_ERROR(1110, "歌词格式错误"),
    
    // 播放列表相关错误码 (1200-1299)
    PLAYLIST_NOT_FOUND(1201, "播放列表不存在"),
    PLAYLIST_ACCESS_DENIED(1202, "无权访问该播放列表"),
    PLAYLIST_NAME_EXISTS(1203, "播放列表名称已存在"),
    PLAYLIST_MUSIC_EXISTS(1204, "音乐已在播放列表中"),
    PLAYLIST_MUSIC_NOT_EXISTS(1205, "音乐不在播放列表中"),
    PLAYLIST_IS_EMPTY(1206, "播放列表为空"),
    PLAYLIST_LIMIT_EXCEEDED(1207, "播放列表歌曲数量超出限制"),
    CANNOT_DELETE_DEFAULT_PLAYLIST(1208, "不能删除默认播放列表"),
    MUSIC_ALREADY_IN_PLAYLIST(1209, "音乐已在播放列表中"),
    ACCESS_DENIED(1210, "访问被拒绝"),
    
    // 艺术家相关错误码 (1300-1399)
    ARTIST_NOT_FOUND(1301, "艺术家不存在"),
    ARTIST_ALREADY_EXISTS(1302, "艺术家已存在"),
    ARTIST_DISABLED(1303, "艺术家已被禁用"),
    ARTIST_HAS_MUSIC(1304, "艺术家下还有音乐，不能删除"),
    
    // 专辑相关错误码 (1400-1499)
    ALBUM_NOT_FOUND(1401, "专辑不存在"),
    ALBUM_ALREADY_EXISTS(1402, "专辑已存在"),
    ALBUM_DISABLED(1403, "专辑已被禁用"),
    ALBUM_HAS_MUSIC(1404, "专辑下还有音乐，不能删除"),
    
    // 文件相关错误码 (1500-1599)
    FILE_NOT_FOUND(1501, "文件不存在"),
    FILE_UPLOAD_FAILED(1502, "文件上传失败"),
    FILE_DELETE_FAILED(1503, "文件删除失败"),
    FILE_TYPE_NOT_SUPPORTED(1504, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(1505, "文件大小超出限制"),
    FILE_NAME_INVALID(1506, "文件名不合法"),
    STORAGE_SPACE_INSUFFICIENT(1507, "存储空间不足"),
    FILE_DOWNLOAD_ERROR(1508, "文件下载失败"),
    
    // Minio相关错误码 (1550-1599)
    MINIO_OPERATION_ERROR(1550, "Minio操作失败"),
    MINIO_CONNECTION_ERROR(1551, "Minio连接失败"),
    MINIO_BUCKET_ERROR(1552, "Minio存储桶操作失败"),
    MINIO_BUCKET_NOT_FOUND(1553, "Minio存储桶不存在"),
    MINIO_BUCKET_ALREADY_EXISTS(1554, "Minio存储桶已存在"),
    MINIO_OBJECT_NOT_FOUND(1555, "Minio对象不存在"),
    MINIO_UPLOAD_ERROR(1556, "Minio文件上传失败"),
    MINIO_DOWNLOAD_ERROR(1557, "Minio文件下载失败"),
    MINIO_DELETE_ERROR(1558, "Minio文件删除失败"),
    MINIO_ACCESS_DENIED(1559, "Minio访问被拒绝"),
    MINIO_CONFIG_ERROR(1560, "Minio配置错误"),
    
    // JWT相关错误码 (1600-1699)
    JWT_TOKEN_INVALID(1601, "JWT令牌无效"),
    JWT_TOKEN_EXPIRED(1602, "JWT令牌已过期"),
    JWT_TOKEN_NOT_FOUND(1603, "JWT令牌不存在"),
    JWT_TOKEN_MALFORMED(1604, "JWT令牌格式错误"),
    JWT_SIGNATURE_INVALID(1605, "JWT签名验证失败"),
    JWT_TOKEN_BLACKLISTED(1606, "JWT令牌已被列入黑名单"),
    
    // 权限相关错误码 (1700-1799)
    PERMISSION_DENIED(1701, "权限不足"),
    ROLE_NOT_FOUND(1702, "角色不存在"),
    ROLE_ALREADY_EXISTS(1703, "角色已存在"),
    USER_ROLE_NOT_MATCH(1704, "用户角色不匹配"),
    
    // 缓存相关错误码 (1800-1899)
    CACHE_ERROR(1801, "缓存异常"),
    CACHE_KEY_NOT_FOUND(1802, "缓存键不存在"),
    CACHE_EXPIRED(1803, "缓存已过期"),
    
    // 数据库相关错误码 (1900-1999)
    DATABASE_ERROR(1901, "数据库异常"),
    DATA_INTEGRITY_ERROR(1902, "数据完整性约束违反"),
    DUPLICATE_KEY_ERROR(1903, "数据重复"),
    FOREIGN_KEY_ERROR(1904, "外键约束违反"),
    
    // ========================================
    // 第三方服务错误码 (2000-2999)
    // ========================================
    THIRD_PARTY_SERVICE_ERROR(2001, "第三方服务异常"),
    API_RATE_LIMIT_EXCEEDED(2002, "API调用频率超限"),
    EXTERNAL_SERVICE_UNAVAILABLE(2003, "外部服务不可用"),
    
    // ========================================
    // 配置相关错误码 (3000-3099)
    // ========================================
    CONFIG_ERROR(3001, "配置错误"),
    CONFIG_NOT_FOUND(3002, "配置不存在"),
    INVALID_CONFIG_VALUE(3003, "无效的配置值");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态消息
     */
    private final String message;
    
    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 状态消息
     */
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 根据状态码获取对应的枚举值
     * 
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为成功状态码
     * 
     * @param code 状态码
     * @return true-成功，false-失败
     */
    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }
    
    /**
     * 判断是否为客户端错误状态码
     * 
     * @param code 状态码
     * @return true-客户端错误，false-非客户端错误
     */
    public static boolean isClientError(Integer code) {
        return code != null && code >= 400 && code < 500;
    }
    
    /**
     * 判断是否为服务器错误状态码
     * 
     * @param code 状态码
     * @return true-服务器错误，false-非服务器错误
     */
    public static boolean isServerError(Integer code) {
        return code != null && code >= 500 && code < 600;
    }
}