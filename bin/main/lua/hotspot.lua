--[[
热点数据限流算法 Lua脚本
基于Redis实现热点数据识别和限流

该脚本能够识别热点参数并进行特殊限流处理
使用Lua 5.4.8语法，支持动态热点检测和自适应限流

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 基础限流key
ARGV[1] - 参数值(用于热点检测)
ARGV[2] - 普通限流阈值
ARGV[3] - 热点限流阈值
ARGV[4] - 时间窗口(秒)
ARGV[5] - 当前时间戳(毫秒)
ARGV[6] - 热点检测阈值(可选,默认为普通阈值的2倍)
ARGV[7] - 热点检测窗口(秒,可选,默认300秒)

返回值:
[是否允许通过(1/0), 是否为热点(1/0), 剩余配额, 热点级别(0-3)]
--]]

-- 获取参数
local base_key = KEYS[1]
local param_value = ARGV[1] or ""              -- 参数值
local normal_limit = tonumber(ARGV[2]) or 100  -- 普通限流阈值
local hotspot_limit = tonumber(ARGV[3]) or 10  -- 热点限流阈值
local window_size = tonumber(ARGV[4]) or 60    -- 时间窗口
local now = tonumber(ARGV[5])                  -- 当前时间戳(毫秒)
local hotspot_threshold = tonumber(ARGV[6]) or (normal_limit * 2)  -- 热点检测阈值
local detection_window = tonumber(ARGV[7]) or 300  -- 热点检测窗口(5分钟)

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if normal_limit <= 0 or hotspot_limit <= 0 or window_size <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = math.floor(now / 1000)

-- 构建热点检测相关的key
local param_key = base_key .. ":param:" .. param_value
local hotspot_stats_key = base_key .. ":hotspot_stats:" .. param_value
local hotspot_list_key = base_key .. ":hotspot_list"
local detection_key = base_key .. ":detection:" .. math.floor(current_time / detection_window)

-- 1. 更新参数访问统计(用于热点检测)
redis.call('HINCRBY', detection_key, param_value, 1)
redis.call('EXPIRE', detection_key, detection_window * 2)

-- 2. 获取参数在检测窗口内的访问次数
local detection_count = tonumber(redis.call('HGET', detection_key, param_value)) or 0

-- 3. 判断是否为热点
local is_hotspot = 0
local hotspot_level = 0

-- 检查是否已经在热点列表中
local hotspot_score = tonumber(redis.call('ZSCORE', hotspot_list_key, param_value))

if hotspot_score then
    -- 已在热点列表中
    is_hotspot = 1
    hotspot_level = math.min(3, math.floor(hotspot_score / hotspot_threshold))
elseif detection_count >= hotspot_threshold then
    -- 新发现的热点
    is_hotspot = 1
    hotspot_level = 1
    -- 添加到热点列表，分数为访问次数
    redis.call('ZADD', hotspot_list_key, detection_count, param_value)
    -- 设置热点列表过期时间
    redis.call('EXPIRE', hotspot_list_key, detection_window * 2)
end

-- 4. 如果是热点，更新热点级别
if is_hotspot == 1 and detection_count > 0 then
    redis.call('ZADD', hotspot_list_key, detection_count, param_value)
    hotspot_level = math.min(3, math.floor(detection_count / hotspot_threshold))
end

-- 5. 根据是否为热点选择不同的限流策略
local current_limit = normal_limit
local window_key = param_key .. ":window:" .. math.floor(current_time / window_size)

if is_hotspot == 1 then
    -- 热点数据使用更严格的限流
    current_limit = math.max(1, hotspot_limit / (hotspot_level + 1))
    window_key = param_key .. ":hotspot_window:" .. math.floor(current_time / window_size)
end

-- 6. 执行限流检查
local current_count = tonumber(redis.call('GET', window_key)) or 0
local allowed = 0
local remaining = current_limit - current_count

if current_count < current_limit then
    -- 允许请求通过
    current_count = redis.call('INCR', window_key)
    allowed = 1
    remaining = current_limit - current_count
else
    -- 超过限制
    allowed = 0
    remaining = 0
end

-- 设置窗口过期时间
redis.call('EXPIRE', window_key, window_size * 2)

-- 7. 更新热点统计信息
if is_hotspot == 1 then
    local stats_key = hotspot_stats_key .. ":" .. math.floor(current_time / 60)  -- 按分钟统计
    redis.call('HINCRBY', stats_key, 'requests', 1)
    redis.call('HINCRBY', stats_key, 'blocked', allowed == 0 and 1 or 0)
    redis.call('EXPIRE', stats_key, 3600)  -- 保留1小时
end

-- 8. 清理过期的热点数据(随机清理，避免集中操作)
if math.random(100) <= 5 then  -- 5%的概率执行清理
    local expired_time = current_time - detection_window
    redis.call('ZREMRANGEBYSCORE', hotspot_list_key, '-inf', expired_time)
end

-- 记录日志
if allowed == 0 then
    local log_msg = string.format("Hotspot rate limit for key: %s, param: %s, level: %d, count: %d, limit: %d", 
                                 base_key, param_value, hotspot_level, current_count, current_limit)
    redis.log(redis.LOG_NOTICE, log_msg)
end

-- 返回结果: [是否允许, 是否为热点, 剩余配额, 热点级别]
return {allowed, is_hotspot, remaining, hotspot_level}