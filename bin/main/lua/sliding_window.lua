--[[
滑动窗口限流算法 Lua脚本
基于Redis实现分布式滑动窗口限流

该脚本使用Lua 5.4.8的语法特性，实现高性能的滑动窗口限流算法
支持精确的时间窗口控制和高并发场景下的原子性操作

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 窗口大小(秒)
ARGV[2] - 限流阈值
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 窗口分片数量(可选,默认为窗口大小)

返回值:
[是否允许通过(1/0), 剩余配额, 下次重置时间戳, 当前窗口请求数]
--]]

-- 获取参数
local key = KEYS[1]
local window_size = tonumber(ARGV[1]) or 60  -- 窗口大小，默认60秒
local limit = tonumber(ARGV[2]) or 100       -- 限流阈值，默认100
local now = tonumber(ARGV[3])                -- 当前时间戳(毫秒)
local slices = tonumber(ARGV[4]) or window_size  -- 分片数量，默认等于窗口大小

-- 验证参数有效性
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if limit <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid limit: " .. tostring(limit))
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = math.floor(now / 1000)

-- 计算每个分片的大小(秒)
local slice_size = math.max(1, math.floor(window_size / slices))

-- 计算当前分片的时间戳
local current_slice = math.floor(current_time / slice_size) * slice_size

-- 构建滑动窗口的Redis key
local window_key = key .. ":sliding_window"

-- 清理过期的分片数据
-- 删除超出窗口范围的分片
local expire_before = current_slice - window_size
local expired_keys = {}

-- 获取所有窗口分片
local window_data = redis.call('HGETALL', window_key)
local current_count = 0

-- 遍历现有分片，清理过期数据并统计当前窗口内的请求数
for i = 1, #window_data, 2 do
    local slice_time = tonumber(window_data[i])
    local slice_count = tonumber(window_data[i + 1])
    
    if slice_time and slice_count then
        if slice_time < expire_before then
            -- 标记过期分片待删除
            table.insert(expired_keys, slice_time)
        elseif slice_time >= expire_before and slice_time <= current_slice then
            -- 统计窗口内的请求数
            current_count = current_count + slice_count
        end
    end
end

-- 删除过期分片
for _, expired_key in ipairs(expired_keys) do
    redis.call('HDEL', window_key, expired_key)
end

-- 检查是否超过限流阈值
local allowed = 1
local remaining = limit - current_count

if current_count >= limit then
    allowed = 0
    remaining = 0
else
    -- 增加当前分片的计数
    redis.call('HINCRBY', window_key, current_slice, 1)
    current_count = current_count + 1
    remaining = limit - current_count
end

-- 设置整个窗口数据的过期时间(窗口大小的2倍，确保数据完整性)
redis.call('EXPIRE', window_key, window_size * 2)

-- 计算下次重置时间(下个分片开始时间)
local next_reset = (current_slice + slice_size) * 1000

-- 记录限流日志(仅在调试模式下)
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Rate limit exceeded for key: %s, current: %d, limit: %d", 
                     key, current_count, limit))
end

-- 返回结果: [是否允许, 剩余配额, 下次重置时间, 当前请求数]
return {allowed, remaining, next_reset, current_count}