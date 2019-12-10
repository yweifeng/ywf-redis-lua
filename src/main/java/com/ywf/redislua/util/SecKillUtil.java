package com.ywf.redislua.util;

import org.omg.PortableInterceptor.SUCCESSFUL;
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

/**
 * 商品秒杀工具类
 * @Author:ywf
 */
@Component
public class SecKillUtil {
    private Logger logger = LoggerFactory.getLogger(SecKillUtil.class);
    private final static String RESULT_SUCCESS = "1";

    @Autowired
    private RedisTemplate redisTemplate;

    private StringRedisSerializer stringRedisSerializer;
    private DefaultRedisScript secKillScript;

    @PostConstruct
    public void init() {
        stringRedisSerializer = new StringRedisSerializer();
        secKillScript = new DefaultRedisScript();
        secKillScript.setResultType(String.class);
        secKillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/secKill.lua")));
    }

    /**
     * 单个商品秒杀
     * @param goodKey 商品主键
     * @param buyNum  购买数量
     * @return
     */
    public boolean doSecKill(String goodKey, int buyNum) {
        String result = (String) redisTemplate.execute(secKillScript, stringRedisSerializer,stringRedisSerializer,
                Collections.singletonList(goodKey), String.valueOf(buyNum));
        logger.info("goodKey = " + goodKey + ", buyNum = " + buyNum + ", result = " +result);
        return RESULT_SUCCESS.equals(result);
    }

}
