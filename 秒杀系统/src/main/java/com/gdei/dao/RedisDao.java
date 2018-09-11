package com.gdei.dao;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.gdei.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class RedisDao {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    private  JedisPool jedisPool;

    //设置Google的序列化工具protostuff，完成对Seckill类对象的序列化和反序列化操作
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }


    
    /** 
    * @Description: 根据商品ID，从Redis缓存中获取对应的商品对象Seckill
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/10 
    */ 
    public Seckill getSekill(long seckillId) {
        try (Jedis jedis = jedisPool.getResource()){
            String key = "seckill:" + seckillId;
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes != null) {
                //从Redis缓存中获取到了对应的对象Seckill，则进行反序列化
                Seckill seckill = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                return seckill;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //Redis缓存中没有对应对象
        return null;
    }

    /** 
    * @Description: 把对应商品对象Seckill序列化为字节数组，传入Redis缓存中
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/10 
    */ 
    public String putSeckill(Seckill seckill) {
        try(Jedis jedis = jedisPool.getResource()) {
            String key = "seckill:" + seckill.getSeckillId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            //设置超时时间
            int timeout = 60*60;
            //把对象序列化后的字节数组和对应的key，放入到Redis缓存中
            return jedis.setex(key.getBytes(), timeout, bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }



}
