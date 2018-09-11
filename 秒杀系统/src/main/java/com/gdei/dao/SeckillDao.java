package com.gdei.dao;

import com.gdei.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {

    /**
    * @Description: 减库存
    * @Param:
    * @return:  如果影响行数>0，则表示更新了数据库表中的几条记录
    * @Author: dzc
    * @Date: 2018/9/8
    */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);
    
    /** 
    * @Description: 根据ID查询要秒杀的商品信息 
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    Seckill queryById(long seckillId);

    /** 
    * @Description: 查询一个ID在范围内的，可以被秒杀的商品【返回多个商品的集合】
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);


    /**
    * @Description: 使用存储过程在MySQL服务器中执行秒杀操作，即在数据库端执行两条sql语句
    * @Param: 存储过程需要的参数，用Map保存，传给Mybatis一一取出，然后赋值到调用存储过程的语句上
    * @return:
    * @Author: dzc
    * @Date: 2018/9/11
    */
    void killByProcedure(Map<String, Object> paramMap);

}
