package com.gdei.dao;

import com.gdei.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})

public class SeckillDaoTest {

    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() {
        long seckillId = 1000;
        Date date = new Date();
        int i = seckillDao.reduceNumber(seckillId, date);
        System.out.println("更新了：" + i + "条记录");
    }

    @org.junit.Test
    public void queryById() {
        long seckillId = 1000;
        Seckill seckill = seckillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @org.junit.Test
    public void queryAll() {
        List<Seckill> seckills = seckillDao.queryAll(0, 1000);
        for (Seckill s : seckills) {
            System.out.println(s);
        }

    }
}