-- SETNX 存在则加锁，不存在 不进行业务操作， 设置超时时间 防止死锁
if redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then
    return '1'
else
    return '0'
end