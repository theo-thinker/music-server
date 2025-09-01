--[[
令牌桶限流算法 Lua脚本
基于Redis实现分布式令牌桶限流

该脚本实现了标准的令牌桶算法，支持突发流量处理
使用Lua 5.4.8语法，确保原子性操作和高性能

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 桶容量
ARGV[2] - 令牌生成速率(令牌/秒)
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 请求令牌数量(可选,默认1)
ARGV[5] - 预热时间(秒,可选,默认0)

返回值:
[是否允许通过(1/0), 当前桶中令牌数, 下次补充时间戳, 等待时间(毫秒)]
--]]

-- 获取参数
local key = KEYS[1]
local capacity = tonumber(ARGV[1]) or 100      -- 桶容量
local refill_rate = tonumber(ARGV[2]) or 10    -- 令牌生成速率(令牌/秒)
local now = tonumber(ARGV[3])                  -- 当前时间戳(毫秒)
local requested = tonumber(ARGV[4]) or 1       -- 请求的令牌数量
local warmup_period = tonumber(ARGV[5]) or 0   -- 预热时间(秒)

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if capacity <= 0 or refill_rate <= 0 or requested <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = now / 1000

-- 构建令牌桶的Redis key
local bucket_key = key .. ":token_bucket"
local tokens_key = bucket_key .. ":tokens"
local last_refill_key = bucket_key .. ":last_refill"
local created_key = bucket_key .. ":created"

-- 获取当前桶状态
local current_tokens = tonumber(redis.call('GET', tokens_key)) or capacity
local last_refill = tonumber(redis.call('GET', last_refill_key)) or current_time
local bucket_created = tonumber(redis.call('GET', created_key))

-- 如果桶不存在，初始化
if not bucket_created then
    redis.call('SET', created_key, current_time)
    redis.call('SET', tokens_key, capacity)
    redis.call('SET', last_refill_key, current_time)
    current_tokens = capacity
    last_refill = current_time
    bucket_created = current_time
end

-- 计算预热阶段的速率调整
local effective_rate = refill_rate
if warmup_period > 0 then
    local elapsed_since_creation = current_time - bucket_created
    if elapsed_since_creation < warmup_period then
        -- 预热阶段：线性增长到目标速率
        local warmup_ratio = elapsed_since_creation / warmup_period
        effective_rate = refill_rate * warmup_ratio
    end
end

-- 计算需要补充的令牌数量
local time_passed = current_time - last_refill
local tokens_to_add = math.floor(time_passed * effective_rate)

-- 补充令牌，但不能超过桶容量
if tokens_to_add > 0 then
    current_tokens = math.min(capacity, current_tokens + tokens_to_add)
    -- 更新最后补充时间
    redis.call('SET', last_refill_key, current_time)
end

-- 检查是否有足够的令牌
local allowed = 0
local wait_time = 0

if current_tokens >= requested then
    -- 有足够令牌，允许请求通过
    current_tokens = current_tokens - requested
    allowed = 1
else
    -- 令牌不足，计算需要等待的时间
    local tokens_needed = requested - current_tokens
    wait_time = math.ceil(tokens_needed / effective_rate * 1000)  -- 转换为毫秒
end

-- 更新桶中令牌数量
redis.call('SET', tokens_key, current_tokens)

-- 设置过期时间(预防内存泄漏)
local expire_time = math.max(3600, capacity / effective_rate * 2)  -- 至少1小时
redis.call('EXPIRE', tokens_key, expire_time)
redis.call('EXPIRE', last_refill_key, expire_time)
redis.call('EXPIRE', created_key, expire_time)

-- 计算下次补充时间
local next_refill = (current_time + (1 / effective_rate)) * 1000

-- 记录限流日志
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Token bucket empty for key: %s, tokens: %d, requested: %d", 
                     key, current_tokens, requested))
end

-- 返回结果: [是否允许, 当前令牌数, 下次补充时间, 等待时间]
return {allowed, current_tokens, next_refill, wait_time}