if redis.call('get', KEYS[1]) == ARGV[1] then
    return tostring(redis.call('del', KEYS[1]))
else
    return '0'
end