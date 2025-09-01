package com.musicserver.ratelimit.enums;

/**
 * 限流策略枚举
 * 
 * 定义了系统支持的各种限流算法策略
 * 每种策略适用于不同的业务场景和性能要求
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public enum RateLimitStrategy {

    /**
     * 滑动窗口算法
     * 
     * 特点：
     * - 精度高，能够平滑地限制请求速率
     * - 内存消耗相对较大
     * - 适用于对限流精度要求较高的场景
     * 
     * 实现原理：
     * 将时间窗口分成多个小片段，记录每个片段的请求次数
     * 滑动时间窗口，统计窗口内的总请求次数
     */
    SLIDING_WINDOW("sliding_window", "滑动窗口算法", "高精度限流，适用于平滑限流场景"),

    /**
     * 令牌桶算法
     * 
     * 特点：
     * - 支持突发流量
     * - 平均速率控制
     * - 适用于需要处理突发请求的场景
     * 
     * 实现原理：
     * 以固定速率向桶中添加令牌，请求需要消耗令牌才能通过
     * 桶满时多余的令牌会溢出，桶空时请求被拒绝
     */
    TOKEN_BUCKET("token_bucket", "令牌桶算法", "支持突发流量，适用于突发请求处理"),

    /**
     * 漏桶算法
     * 
     * 特点：
     * - 强制限制输出速率
     * - 平滑处理突发流量
     * - 适用于需要恒定速率输出的场景
     * 
     * 实现原理：
     * 请求进入漏桶，以固定速率从桶底流出
     * 桶满时新请求被丢弃或排队等待
     */
    LEAKY_BUCKET("leaky_bucket", "漏桶算法", "恒定速率输出，适用于流量整形场景"),

    /**
     * 固定窗口算法
     * 
     * 特点：
     * - 实现简单，性能高
     * - 内存消耗小
     * - 可能存在边界突发问题
     * 
     * 实现原理：
     * 将时间分成固定大小的窗口，统计每个窗口内的请求次数
     * 窗口重置时计数器归零
     */
    FIXED_WINDOW("fixed_window", "固定窗口算法", "简单高效，适用于一般限流场景"),

    /**
     * 计数器算法
     * 
     * 特点：
     * - 最简单的限流算法
     * - 性能最高，内存消耗最小
     * - 适用于简单的限流需求
     * 
     * 实现原理：
     * 维护一个计数器，每次请求时递增
     * 到达限制时拒绝新请求，定时重置计数器
     */
    COUNTER("counter", "计数器算法", "最简单的限流算法，适用于基础限流需求"),

    /**
     * 加权轮询算法
     * 
     * 特点：
     * - 支持不同权重的请求
     * - 适用于多租户或分级服务
     * - 能够根据优先级分配资源
     * 
     * 实现原理：
     * 为不同类型的请求分配不同的权重
     * 按照权重比例分配请求配额
     */
    WEIGHTED_ROUND_ROBIN("weighted_round_robin", "加权轮询算法", "支持优先级限流，适用于多租户场景"),

    /**
     * 分布式令牌桶算法
     * 
     * 特点：
     * - 支持集群环境下的限流
     * - 全局统一限流配额
     * - 适用于微服务架构
     * 
     * 实现原理：
     * 在分布式环境中维护全局令牌桶
     * 各个节点从全局桶中获取令牌
     */
    DISTRIBUTED_TOKEN_BUCKET("distributed_token_bucket", "分布式令牌桶算法", "集群限流，适用于微服务架构"),

    /**
     * 自适应限流算法
     * 
     * 特点：
     * - 根据系统负载动态调整限流阈值
     * - 智能化限流
     * - 适用于负载变化较大的系统
     * 
     * 实现原理：
     * 监控系统指标（CPU、内存、响应时间等）
     * 根据系统状态动态调整限流参数
     */
    ADAPTIVE("adaptive", "自适应限流算法", "智能限流，根据系统负载动态调整"),

    /**
     * 热点数据限流算法
     * 
     * 特点：
     * - 针对热点数据进行限流
     * - 支持参数级别的限流
     * - 适用于热点数据保护
     * 
     * 实现原理：
     * 识别热点参数或数据
     * 对热点数据单独进行限流控制
     */
    HOTSPOT("hotspot", "热点数据限流算法", "热点保护，适用于热点数据访问控制");

    /**
     * 策略代码，用于配置和日志记录
     */
    private final String code;

    /**
     * 策略名称，用于显示
     */
    private final String name;

    /**
     * 策略描述，说明适用场景
     */
    private final String description;

    /**
     * 构造函数
     * 
     * @param code 策略代码
     * @param name 策略名称
     * @param description 策略描述
     */
    RateLimitStrategy(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 获取策略代码
     * 
     * @return 策略代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取策略描述
     * 
     * @return 策略描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取策略枚举
     * 
     * @param code 策略代码
     * @return 策略枚举，未找到时返回SLIDING_WINDOW
     */
    public static RateLimitStrategy fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return SLIDING_WINDOW;
        }
        
        for (RateLimitStrategy strategy : values()) {
            if (strategy.getCode().equalsIgnoreCase(code.trim())) {
                return strategy;
            }
        }
        
        return SLIDING_WINDOW;
    }

    /**
     * 判断是否为分布式策略
     * 
     * @return 是否为分布式策略
     */
    public boolean isDistributed() {
        return this == DISTRIBUTED_TOKEN_BUCKET;
    }

    /**
     * 判断是否支持突发流量
     * 
     * @return 是否支持突发流量
     */
    public boolean supportsBurst() {
        return this == TOKEN_BUCKET || this == DISTRIBUTED_TOKEN_BUCKET;
    }

    /**
     * 判断是否为动态策略
     * 
     * @return 是否为动态策略
     */
    public boolean isDynamic() {
        return this == ADAPTIVE || this == HOTSPOT;
    }

    /**
     * 获取策略的默认时间窗口大小（秒）
     * 
     * @return 默认时间窗口大小
     */
    public long getDefaultPeriod() {
        switch (this) {
            case SLIDING_WINDOW:
            case FIXED_WINDOW:
                return 60; // 1分钟
            case TOKEN_BUCKET:
            case DISTRIBUTED_TOKEN_BUCKET:
                return 1; // 1秒
            case LEAKY_BUCKET:
                return 1; // 1秒
            case COUNTER:
                return 60; // 1分钟
            case WEIGHTED_ROUND_ROBIN:
                return 10; // 10秒
            case ADAPTIVE:
                return 30; // 30秒
            case HOTSPOT:
                return 10; // 10秒
            default:
                return 60; // 默认1分钟
        }
    }

    /**
     * 获取策略的默认限流次数
     * 
     * @return 默认限流次数
     */
    public long getDefaultLimit() {
        switch (this) {
            case SLIDING_WINDOW:
            case FIXED_WINDOW:
                return 100; // 1分钟100次
            case TOKEN_BUCKET:
            case DISTRIBUTED_TOKEN_BUCKET:
                return 10; // 1秒10次
            case LEAKY_BUCKET:
                return 5; // 1秒5次
            case COUNTER:
                return 1000; // 1分钟1000次
            case WEIGHTED_ROUND_ROBIN:
                return 50; // 10秒50次
            case ADAPTIVE:
                return 200; // 30秒200次
            case HOTSPOT:
                return 20; // 10秒20次
            default:
                return 100; // 默认100次
        }
    }

    @Override
    public String toString() {
        return String.format("RateLimitStrategy{code='%s', name='%s', description='%s'}", 
                            code, name, description);
    }
}