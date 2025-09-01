package com.musicserver.ratelimit.enums;

/**
 * 限流类型枚举
 * 
 * 定义了限流的不同维度和类型
 * 支持多维度组合限流，提供灵活的限流策略
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public enum RateLimitType {

    /**
     * 全局限流
     * 
     * 特点：
     * - 对所有请求进行统一限流
     * - 不区分用户、IP等维度
     * - 适用于保护系统整体性能
     */
    GLOBAL("global", "全局限流", "对所有请求统一限流"),

    /**
     * IP限流
     * 
     * 特点：
     * - 基于客户端IP地址进行限流
     * - 防止单个IP恶意请求
     * - 适用于防刷和DDoS防护
     */
    IP("ip", "IP限流", "基于客户端IP地址限流"),

    /**
     * 用户限流
     * 
     * 特点：
     * - 基于用户ID进行限流
     * - 需要用户认证信息
     * - 适用于用户级别的配额控制
     */
    USER("user", "用户限流", "基于用户ID限流"),

    /**
     * 接口限流
     * 
     * 特点：
     * - 基于具体的接口URI进行限流
     * - 保护特定的API端点
     * - 适用于接口级别的保护
     */
    API("api", "接口限流", "基于接口URI限流"),

    /**
     * 方法限流
     * 
     * 特点：
     * - 基于具体的方法进行限流
     * - 粒度最细的限流方式
     * - 适用于方法级别的精确控制
     */
    METHOD("method", "方法限流", "基于方法签名限流"),

    /**
     * 参数限流
     * 
     * 特点：
     * - 基于请求参数进行限流
     * - 支持热点参数保护
     * - 适用于参数级别的限流
     */
    PARAMETER("parameter", "参数限流", "基于请求参数限流"),

    /**
     * 设备限流
     * 
     * 特点：
     * - 基于设备标识进行限流
     * - 适用于移动端应用
     * - 防止设备级别的恶意请求
     */
    DEVICE("device", "设备限流", "基于设备标识限流"),

    /**
     * 应用限流
     * 
     * 特点：
     * - 基于应用标识进行限流
     * - 适用于多租户系统
     * - 不同应用分配不同配额
     */
    APP("app", "应用限流", "基于应用标识限流"),

    /**
     * 地理位置限流
     * 
     * 特点：
     * - 基于地理位置进行限流
     * - 支持国家、省份、城市级别
     * - 适用于地域性服务限制
     */
    GEOGRAPHY("geography", "地理位置限流", "基于地理位置限流"),

    /**
     * 时间段限流
     * 
     * 特点：
     * - 基于时间段进行限流
     * - 支持工作时间、节假日等
     * - 适用于分时段的流量控制
     */
    TIME_PERIOD("time_period", "时间段限流", "基于时间段限流"),

    /**
     * 资源限流
     * 
     * 特点：
     * - 基于具体资源进行限流
     * - 保护特定的资源访问
     * - 适用于资源级别的保护
     */
    RESOURCE("resource", "资源限流", "基于资源标识限流"),

    /**
     * 角色限流
     * 
     * 特点：
     * - 基于用户角色进行限流
     * - 不同角色不同配额
     * - 适用于基于权限的限流
     */
    ROLE("role", "角色限流", "基于用户角色限流"),

    /**
     * 自定义限流
     * 
     * 特点：
     * - 支持自定义的限流维度
     * - 通过SpEL表达式定义key
     * - 最灵活的限流方式
     */
    CUSTOM("custom", "自定义限流", "基于自定义规则限流"),

    /**
     * 组合限流
     * 
     * 特点：
     * - 多个维度组合限流
     * - 支持AND、OR逻辑
     * - 适用于复杂的限流场景
     */
    COMBINATION("combination", "组合限流", "多维度组合限流");

    /**
     * 类型代码，用于配置和日志记录
     */
    private final String code;

    /**
     * 类型名称，用于显示
     */
    private final String name;

    /**
     * 类型描述，说明适用场景
     */
    private final String description;

    /**
     * 构造函数
     * 
     * @param code 类型代码
     * @param name 类型名称
     * @param description 类型描述
     */
    RateLimitType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 获取类型代码
     * 
     * @return 类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型名称
     * 
     * @return 类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取类型枚举
     * 
     * @param code 类型代码
     * @return 类型枚举，未找到时返回GLOBAL
     */
    public static RateLimitType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return GLOBAL;
        }
        
        for (RateLimitType type : values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        
        return GLOBAL;
    }

    /**
     * 判断是否需要用户认证信息
     * 
     * @return 是否需要用户认证信息
     */
    public boolean requiresAuthentication() {
        return this == USER || this == ROLE;
    }

    /**
     * 判断是否需要IP信息
     * 
     * @return 是否需要IP信息
     */
    public boolean requiresIpAddress() {
        return this == IP || this == GEOGRAPHY;
    }

    /**
     * 判断是否需要设备信息
     * 
     * @return 是否需要设备信息
     */
    public boolean requiresDeviceInfo() {
        return this == DEVICE || this == APP;
    }

    /**
     * 判断是否需要参数信息
     * 
     * @return 是否需要参数信息
     */
    public boolean requiresParameters() {
        return this == PARAMETER || this == RESOURCE;
    }

    /**
     * 判断是否为时间相关的限流
     * 
     * @return 是否为时间相关的限流
     */
    public boolean isTimeRelated() {
        return this == TIME_PERIOD;
    }

    /**
     * 判断是否支持动态配置
     * 
     * @return 是否支持动态配置
     */
    public boolean supportsDynamicConfig() {
        return this == CUSTOM || this == COMBINATION;
    }

    /**
     * 获取默认的key前缀
     * 
     * @return 默认的key前缀
     */
    public String getDefaultKeyPrefix() {
        return "rate_limit:" + code + ":";
    }

    /**
     * 获取类型的优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    public int getPriority() {
        switch (this) {
            case GLOBAL:
                return 1;
            case IP:
                return 2;
            case USER:
                return 3;
            case API:
                return 4;
            case METHOD:
                return 5;
            case PARAMETER:
                return 6;
            case DEVICE:
                return 7;
            case APP:
                return 8;
            case GEOGRAPHY:
                return 9;
            case TIME_PERIOD:
                return 10;
            case RESOURCE:
                return 11;
            case ROLE:
                return 12;
            case CUSTOM:
                return 13;
            case COMBINATION:
                return 14;
            default:
                return 99;
        }
    }

    @Override
    public String toString() {
        return String.format("RateLimitType{code='%s', name='%s', description='%s'}", 
                            code, name, description);
    }
}