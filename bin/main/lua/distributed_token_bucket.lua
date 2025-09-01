--[[
分布式令牌桶限流算法 Lua脚本
基于Redis实现跨节点的分布式令牌桶限流

该脚本在标准令牌桶基础上增加了分布式特性
支持多节点环境下的全局限流控制

作者: Music Server Development Team
版本: 1.0.0
日期: 2025-09-01

参数说明:
KEYS[1] - 限流key
ARGV[1] - 桶容量
ARGV[2] - 令牌生成速率(令牌/秒)
ARGV[3] - 当前时间戳(毫秒)
ARGV[4] - 节点ID
ARGV[5] - 请求令牌数量(可选,默认1)
ARGV[6] - 节点权重(可选,默认1.0)

返回值:
[是否允许通过(1/0), 分配给当前节点的令牌数, 全局剩余令牌数, 下次同步时间戳]
--]]

-- 获取参数
local key = KEYS[1]
local capacity = tonumber(ARGV[1]) or 100      -- 桶容量
local refill_rate = tonumber(ARGV[2]) or 10    -- 令牌生成速率(令牌/秒)
local now = tonumber(ARGV[3])                  -- 当前时间戳(毫秒)
local node_id = ARGV[4] or "default"           -- 节点ID
local requested = tonumber(ARGV[5]) or 1       -- 请求的令牌数量
local node_weight = tonumber(ARGV[6]) or 1.0   -- 节点权重

-- 验证参数
if not now or now <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid timestamp: " .. tostring(now))
    return {0, 0, 0, 0}
end

if capacity <= 0 or refill_rate <= 0 or requested <= 0 or node_weight <= 0 then
    redis.log(redis.LOG_WARNING, "Invalid parameters")
    return {0, 0, 0, 0}
end

-- 转换为秒级时间戳
local current_time = now / 1000

-- 构建分布式令牌桶的Redis key
local bucket_key = key .. ":distributed_bucket"
local global_tokens_key = bucket_key .. ":global_tokens"
local last_refill_key = bucket_key .. ":last_refill"
local nodes_key = bucket_key .. ":nodes"
local node_tokens_key = bucket_key .. ":node:" .. node_id
local node_weight_key = bucket_key .. ":weight:" .. node_id

-- 获取全局桶状态
local global_tokens = tonumber(redis.call('GET', global_tokens_key)) or capacity
local last_refill = tonumber(redis.call('GET', last_refill_key)) or current_time

-- 计算需要补充的令牌数量
local time_passed = current_time - last_refill
local tokens_to_add = math.floor(time_passed * refill_rate)

-- 补充全局令牌
if tokens_to_add > 0 then
    global_tokens = math.min(capacity, global_tokens + tokens_to_add)
    redis.call('SET', global_tokens_key, global_tokens)
    redis.call('SET', last_refill_key, current_time)
end

-- 注册当前节点并更新权重
redis.call('SADD', nodes_key, node_id)
redis.call('SET', node_weight_key, node_weight)

-- 获取所有活跃节点及其权重
local active_nodes = redis.call('SMEMBERS', nodes_key)
local total_weight = 0
local node_weights = {}

-- 计算总权重并清理过期节点
for _, n in ipairs(active_nodes) do
    local nw_key = bucket_key .. ":weight:" .. n
    local nt_key = bucket_key .. ":node:" .. n
    local last_access_key = bucket_key .. ":last_access:" .. n
    
    local weight = tonumber(redis.call('GET', nw_key)) or 0
    local last_access = tonumber(redis.call('GET', last_access_key)) or current_time
    
    -- 清理5分钟内未访问的节点
    if current_time - last_access > 300 then
        redis.call('SREM', nodes_key, n)
        redis.call('DEL', nw_key, nt_key, last_access_key)
    else
        node_weights[n] = weight
        total_weight = total_weight + weight
    end
end

-- 更新当前节点的最后访问时间
local last_access_key = bucket_key .. ":last_access:" .. node_id
redis.call('SET', last_access_key, current_time)

-- 获取当前节点已分配的令牌
local node_tokens = tonumber(redis.call('GET', node_tokens_key)) or 0

-- 根据权重分配令牌给当前节点
local node_allocation = 0
if total_weight > 0 then
    node_allocation = math.floor(global_tokens * (node_weight / total_weight))
    -- 确保每个节点至少能获得一些令牌（如果全局有令牌的话）
    if global_tokens > 0 and node_allocation == 0 then
        node_allocation = 1
    end
end

-- 更新节点令牌数（但不能超过分配额度）
node_tokens = math.min(node_allocation, node_tokens + math.floor(tokens_to_add * (node_weight / math.max(total_weight, 1))))

-- 检查当前节点是否有足够的令牌
local allowed = 0
local actual_node_tokens = node_tokens

if node_tokens >= requested then
    -- 节点有足够令牌
    node_tokens = node_tokens - requested
    global_tokens = math.max(0, global_tokens - requested)
    allowed = 1
else
    -- 节点令牌不足，尝试从全局桶直接获取
    if global_tokens >= requested then
        global_tokens = global_tokens - requested
        allowed = 1
    end
end

-- 更新状态
redis.call('SET', node_tokens_key, node_tokens)
redis.call('SET', global_tokens_key, global_tokens)

-- 设置过期时间
local expire_time = math.max(3600, capacity / refill_rate * 2)
redis.call('EXPIRE', global_tokens_key, expire_time)
redis.call('EXPIRE', last_refill_key, expire_time)
redis.call('EXPIRE', nodes_key, expire_time)
redis.call('EXPIRE', node_tokens_key, expire_time)
redis.call('EXPIRE', node_weight_key, expire_time)
redis.call('EXPIRE', last_access_key, expire_time)

-- 计算下次同步时间(1秒后)
local next_sync = (current_time + 1) * 1000

-- 记录限流日志
if allowed == 0 then
    redis.log(redis.LOG_NOTICE, 
        string.format("Distributed token bucket limit for key: %s, node: %s, tokens: %d, requested: %d", 
                     key, node_id, actual_node_tokens, requested))
end

-- 返回结果: [是否允许, 节点令牌数, 全局令牌数, 下次同步时间]
return {allowed, actual_node_tokens, global_tokens, next_sync}