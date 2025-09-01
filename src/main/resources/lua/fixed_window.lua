--[[
固定窗口限流算法 Lua脚本
基于Redis实现分布式固定窗口限流

该脚本实现了简单高效的固定窗口算法
使用Lua 5.4.8语法，提供最佳性能和最小内存消耗

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 窗口大小(秒)
ARGV[2] - 限流阈值
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 请求数量(可选,默认1)

返回值:
[是否允许通过(1/0), 剩余配额, 窗口重置时间戳, 当前窗口请求数]
--]]

-- 获取参数
local key = KEYS[1]
local window_size = tonumber(ARGV[1]) or 60  -- 窗口大小，默认60秒
local limit = tonumber(ARGV[2]) or 100       -- 限流阈值，默认100
local now = tonumber(ARGV[3])                -- 当前时间戳(毫秒)
local requests = tonumber(ARGV[4]) or 1      -- 请求数量

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if window_size <= 0 or limit <= 0 or requests <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = math.floor(now / 1000)

-- 计算当前窗口的开始时间
local window_start = math.floor(current_time / window_size) * window_size

-- 构建固定窗口的Redis key
local window_key = key .. ":fixed_window:" .. window_start

-- 获取当前窗口的请求计数
local current_count = tonumber(redis.call('GET', window_key)) or 0

-- 检查是否超过限流阈值
local allowed = 0
local remaining = limit - current_count

if current_count + requests <= limit then
    -- 未超过限制，允许请求通过
    current_count = redis.call('INCRBY', window_key, requests)
    allowed = 1
    remaining = limit - current_count
else
    -- 超过限制，拒绝请求
    allowed = 0
    remaining = 0
end

-- 设置窗口过期时间
-- 过期时间设置为窗口大小的2倍，确保窗口完整性
redis.call('EXPIRE', window_key, window_size * 2)

-- 计算窗口重置时间(下个窗口开始时间)
local window_reset = (window_start + window_size) * 1000

-- 记录限流日志
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Fixed window limit exceeded for key: %s, count: %d, limit: %d", 
                     key, current_count + requests, limit))
end

-- 返回结果: [是否允许, 剩余配额, 窗口重置时间, 当前请求数]
return {allowed, remaining, window_reset, current_count}