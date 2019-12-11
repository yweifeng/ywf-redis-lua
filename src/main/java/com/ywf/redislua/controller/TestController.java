package com.ywf.redislua.controller;

import com.ywf.redislua.util.DistributedIdUtil;
import com.ywf.redislua.util.RedisLockUtil;
import com.ywf.redislua.util.SecKillUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:ywf
 */

@RestController
public class TestController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private SecKillUtil secKillUtil;

    @Autowired
    private DistributedIdUtil distributedIdUtil;

    @GetMapping("/test")
    public void test() {
        System.out.println(redisTemplate.hasKey("name"));
        redisTemplate.opsForValue().set("name", "wyp");
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

    /**
     * 加锁
     *
     * @param key
     * @param requestId
     */
    @RequestMapping("/testRedisLock")
    public String testRedisLock(@RequestParam String key, @RequestParam String requestId) {
        if (redisLockUtil.lock(key, requestId, 60000, 3)) {
            return "lock success";
        }
        return "fail lock";
    }

    /**
     * 解锁
     *
     * @param key
     * @param requestId
     * @return
     */
    @RequestMapping("/testRedisUnLock")
    public String testRedisUnLock(@RequestParam String key, @RequestParam String requestId) {
        if (redisLockUtil.unLock(key, requestId)) {
            return "unLock success";
        }
        return "fail unLock";
    }

    @RequestMapping("/testSecKill")
    public String testSecKill(@RequestParam String goodKey, @RequestParam int buyNum) {
        if (secKillUtil.doSecKill(goodKey, buyNum)) {
            return "secKill success";
        }
        return "fail secKill";
    }

    @RequestMapping("/testGeneratorId")
    public String testGeneratorId(@RequestParam String key) {
        return distributedIdUtil.generateId(key);
    }
}
