package com.ywf.redislua.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Author:ywf
 */
@Component
public class RedisLockUtil {
    private final static String RESULT_SUCCESS = "1";
    private Logger logger = LoggerFactory.getLogger(RedisLockUtil.class);
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * lua lock脚本
     */
    private DefaultRedisScript lockScript;
    /**
     * lua unlock脚本
     */
    private DefaultRedisScript unLockScript;
    /**
     * redis String 序列化脚本
     */
    private StringRedisSerializer stringRedisSerializer;

    /**
     * @PostConstruct
     * @PostConstruct 顺序
     * 构造方法 => @Autowired => @PostConstruct => init
     */
    @PostConstruct
    public void init() {
        stringRedisSerializer = new StringRedisSerializer();
        lockScript = new DefaultRedisScript();
        lockScript.setResultType(String.class);
        unLockScript = new DefaultRedisScript();
        unLockScript.setResultType(String.class);
        // 加载脚本
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/lock.lua")));
        unLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unLock.lua")));
    }

    /**
     * redis加分布式锁
     *
     * @param key        redis锁的key值
     * @param requestId  请求的id,随机生成，不重复，防止解锁别人产生的锁
     * @param expireTime 超时时间
     * @param retryTimes 重试次数
     * @return 是否枷锁成功
     */
    public boolean lock(String key, String requestId, long expireTime, int retryTimes) {
        if (retryTimes < 1) {
            retryTimes = 1;
        }
        int count = 0;
        try {
            while (true) {
                /**
                 * RedisScript<T> redisScript,
                 * RedisSerializer<?> redisSerializer,
                 * RedisSerializer<T> redisSerializer,
                 * List<K> keyList,
                 * Object... obj..
                 */
                String result = (String) redisTemplate.execute(lockScript, stringRedisSerializer, stringRedisSerializer,
                        Collections.singletonList(key), requestId, String.valueOf(expireTime));
                System.out.println(key + " lock result = " + result);

                // 判断是否成功加锁
                if (RESULT_SUCCESS.equals(result)) {
                    logger.info("对 key = [" + key + "], requestId = [" + requestId + "]获取锁成功，第 " + count + "次加锁");
                    return true;
                } else {
                    //加锁失败 判断是否重试
                    if (count++ >= retryTimes) {
                        logger.info("对 key = [" + key + "], requestId = [" + requestId + "]获取锁失败");
                        return false;
                    } else {
                        logger.info("获取锁失败，尝试对 key = [" + key + "], requestId = [" + requestId + "],第 " + count + "次加锁");
                        // 重试
                        // 休眠200ms
                        TimeUnit.MILLISECONDS.sleep(200);
                        continue;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex.getCause());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 解锁 放在finally
     *
     * @param key       解锁 redis的key值
     * @param requestId 不重复的值，防止解锁了别人加的锁
     * @return
     */
    public boolean unLock(String key, String requestId) {
        String result = (String) redisTemplate.execute(unLockScript, stringRedisSerializer, stringRedisSerializer,
                Collections.singletonList(key), requestId);
        logger.info(key + " unlock result = " + result);
        return RESULT_SUCCESS.equals(result);
    }
}
