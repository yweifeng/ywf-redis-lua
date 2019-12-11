local key = KEYS[1]
-- 判断当天是否存在主键
return tonumber(redis.call('INCRBY', key, 1))