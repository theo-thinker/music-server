--[[
计数器限流算法 Lua脚本
基于Redis实现分布式计数器限流

该脚本实现了最简单的计数器算法
使用Lua 5.4.8语法，提供最高性能和最小开销

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 时间窗口(秒)
ARGV[2] - 限流阈值
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 请求数量(可选,默认1)

返回值:
[是否允许通过(1/0), 剩余配额, 重置时间戳, 当前计数]
--]]

-- 获取参数
local key = KEYS[1]
local period = tonumber(ARGV[1]) or 60       -- 时间窗口，默认60秒
local limit = tonumber(ARGV[2]) or 100       -- 限流阈值，默认100
local now = tonumber(ARGV[3])                -- 当前时间戳(毫秒)
local requests = tonumber(ARGV[4]) or 1      -- 请求数量

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if period <= 0 or limit <= 0 or requests <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 构建计数器的Redis key
local counter_key = key .. ":counter"
local reset_time_key = key .. ":reset_time"

-- 转换为秒级时间戳
local current_time = math.floor(now / 1000)

-- 获取当前计数和重置时间
local current_count = tonumber(redis.call('GET', counter_key)) or 0
local reset_time = tonumber(redis.call('GET', reset_time_key)) or 0

-- 检查是否需要重置计数器
if reset_time == 0 or current_time >= reset_time then
    -- 重置计数器
    current_count = 0
    reset_time = current_time + period
    redis.call('SET', reset_time_key, reset_time)
end

-- 检查是否超过限流阈值
local allowed = 0
local remaining = limit - current_count

if current_count + requests <= limit then
    -- 未超过限制，允许请求通过
    current_count = redis.call('INCRBY', counter_key, requests)
    allowed = 1
    remaining = limit - current_count
else
    -- 超过限制，拒绝请求
    allowed = 0
    remaining = 0
end

-- 设置过期时间(重置时间 + 缓冲时间)
local expire_time = reset_time - current_time + 60  -- 额外60秒缓冲
redis.call('EXPIRE', counter_key, expire_time)
redis.call('EXPIRE', reset_time_key, expire_time)

-- 计算重置时间戳(毫秒)
local reset_timestamp = reset_time * 1000

-- 记录限流日志
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Counter limit exceeded for key: %s, count: %d, limit: %d", 
                     key, current_count + requests, limit))
end

-- 返回结果: [是否允许, 剩余配额, 重置时间, 当前计数]
return {allowed, remaining, reset_timestamp, current_count}