local buyNum = tonumber(ARGV[1])
local goodKey = KEYS[1]

-- 判断是否存在商品
local goodNum = redis.call('get',goodKey)
-- 商品不存在
if goodNum == nil then
    return '-1'
else
    goodNum = tonumber(goodNum)
    -- 判断商品库存是否足够
    if goodNum < buyNum then -- 库存不够
        return '0'
    else
        -- 库存足够，扣除库存
        redis.call('decrby', goodKey, buyNum);
        return '1'
    end
end
