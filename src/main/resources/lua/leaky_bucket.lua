--[[
漏桶限流算法 Lua脚本
基于Redis实现分布式漏桶限流

该脚本实现了标准的漏桶算法，提供恒定的输出速率
使用Lua 5.4.8语法，确保原子性操作和稳定的流量整形

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 桶容量
ARGV[2] - 漏出速率(请求/秒)
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 请求数量(可选,默认1)

返回值:
[是否允许通过(1/0), 当前桶中请求数, 下次漏出时间戳, 等待时间(毫秒)]
--]]

-- 获取参数
local key = KEYS[1]
local capacity = tonumber(ARGV[1]) or 100    -- 桶容量
local leak_rate = tonumber(ARGV[2]) or 10    -- 漏出速率(请求/秒)
local now = tonumber(ARGV[3])                -- 当前时间戳(毫秒)
local requests = tonumber(ARGV[4]) or 1      -- 请求数量

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if capacity <= 0 or leak_rate <= 0 or requests <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = now / 1000

-- 构建漏桶的Redis key
local bucket_key = key .. ":leaky_bucket"
local volume_key = bucket_key .. ":volume"
local last_leak_key = bucket_key .. ":last_leak"

-- 获取当前桶状态
local current_volume = tonumber(redis.call('GET', volume_key)) or 0
local last_leak = tonumber(redis.call('GET', last_leak_key)) or current_time

-- 计算应该漏出的请求数量
local time_passed = current_time - last_leak
local leaked_volume = math.floor(time_passed * leak_rate)

-- 执行漏出操作
if leaked_volume > 0 then
    current_volume = math.max(0, current_volume - leaked_volume)
    -- 更新最后漏出时间
    redis.call('SET', last_leak_key, current_time)
end

-- 检查桶是否有足够空间
local allowed = 0
local wait_time = 0

if current_volume + requests <= capacity then
    -- 桶未满，可以接受新请求
    current_volume = current_volume + requests
    allowed = 1
else
    -- 桶满，计算需要等待的时间
    local overflow = (current_volume + requests) - capacity
    wait_time = math.ceil(overflow / leak_rate * 1000)  -- 转换为毫秒
end

-- 更新桶容量
redis.call('SET', volume_key, current_volume)

-- 设置过期时间(预防内存泄漏)
local expire_time = math.max(3600, capacity / leak_rate * 2)  -- 至少1小时
redis.call('EXPIRE', volume_key, expire_time)
redis.call('EXPIRE', last_leak_key, expire_time)

-- 计算下次漏出时间
local next_leak = (current_time + (1 / leak_rate)) * 1000

-- 记录限流日志
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Leaky bucket overflow for key: %s, volume: %d, capacity: %d", 
                     key, current_volume + requests, capacity))
end

-- 返回结果: [是否允许, 当前容量, 下次漏出时间, 等待时间]
return {allowed, current_volume, next_leak, wait_time}