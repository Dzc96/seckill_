package com.gdei.service.impl;

import com.gdei.dao.RedisDao;
import com.gdei.dao.SeckillDao;
import com.gdei.dao.SuccessKilledDao;
import com.gdei.dto.Exposer;
import com.gdei.dto.SeckillExecution;
import com.gdei.entity.Seckill;
import com.gdei.entity.SuccessKilled;
import com.gdei.enums.SeckillStatEnum;
import com.gdei.exception.RepeatKillException;
import com.gdei.exception.SeckillCloseException;
import com.gdei.exception.SeckillException;
import com.gdei.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired

    private RedisDao redisDao;
    //日志对象slf4j
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //加入一个混淆字符串，用于配合生成md5值，用来做秒杀的URL
    private final String salt = "asdasdafagzxczxc[]";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //先判断这个商品是不是秒杀列表里的商品
        //从Redis中获取商品对象Seckill缓存，减少对数据库的操作，优化并发处理时的性能
        //Seckill seckill = seckillDao.queryById(seckillId);
        Seckill seckill  = redisDao.getSekill(seckillId);

        if (seckill == null) {
            //如果从Redis缓存中找不到商品对象，才从数据库中查找
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                new Exposer(false, seckillId);
            } else {
                //从数据库中查找到商品对象后，传入Redis缓存中
                redisDao.putSeckill(seckill);
            }
        }

        //判断现在是否在秒杀时间范围内
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false,seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        //上述判定全部满足 可以执行秒杀操作
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId); //为什么返回这个呢？
    }

    //秒杀是否成功，成功就要减库存+插入秒杀成功记录
    //失败的话，抛出异常+事务回滚
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        //md5值为空是什么意思
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");//秒杀出问题了
        }

        //可以执行秒杀

        Date nowTime = new Date();
        try {


            //(1)先插入一条购买记录 insert到success_killed表
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            } else {

                //(2)减库存 update语句
                //update的sql需要当前时间，来判断是否处于秒杀时间段，是的话才执行update语句
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    throw new RepeatKillException("seckill is closed");
                } else {
                    //两条sql语句均执行成功，则秒杀成功，返回成功秒杀的对象，封装到秒杀结果SeckillExecution中
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (SeckillException e) {
            logger.error(e.getMessage(), e);
            //将编译时异常转换为运行时异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }




    /**
     * @Description: 调用MySQL的存储过程来实现秒杀操作(即在MySQL服务器端执行两条sql语句)
     * @Param:
     * @return:
     * @Author: dzc
     * @Date: 2018/9/11
     */
    @Override
    public SeckillExecution executeSeckillProcedue(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId)))
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);

        Date killTime = new Date();
        Map<String , Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null); //这个存放的存储过程在MySQL服务器端执行完后的返回值

        try{
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            } else {//根据存储过程不同的返回值，就会有不同的错误信息
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }

    }








    
    


    












    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

}
