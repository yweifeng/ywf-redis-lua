package com.ywf.redislua.util;

import com.sun.xml.internal.bind.v2.model.core.ID;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * 分布式ID
 * 需求：
 * - ID不重复且递增。
 * - 和日期有关联，同一天的日期从1开始递增。
 * - 支持高并发
 * - 高可靠、高容错
 *
 * @Author:ywf
 */
@Component
public class DistributedIdUtil {
    private Logger logger = LoggerFactory.getLogger(DistributedIdUtil.class);

    @Autowired
    private RedisTemplate redisTemplate;

    private StringRedisSerializer stringRedisSerializer;

    private DefaultRedisScript distributedIdScript;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @PostConstruct
    public void init() {
        stringRedisSerializer = new StringRedisSerializer();
        distributedIdScript = new DefaultRedisScript();
        distributedIdScript.setResultType(Long.class);
        distributedIdScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/distributedId.lua")));
    }

    /**
     * 获取分布式ID  前缀+日期+8位自增序列号（不够补0）
     * @param preKey key前缀
     * @return
     */
    public String generateId(String preKey) {
        // 获取当前日期
        Date d = new Date();
        String key = preKey + sdf.format(d);
        Long id = (Long) redisTemplate.execute(distributedIdScript, stringRedisSerializer, stringRedisSerializer,
                Collections.singletonList(key));
        return key + String.format("%08d", id.intValue());
    }
}
