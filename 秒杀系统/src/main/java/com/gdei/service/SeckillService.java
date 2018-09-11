package com.gdei.service;

import com.gdei.dto.Exposer;
import com.gdei.dto.SeckillExecution;
import com.gdei.entity.Seckill;
import com.gdei.exception.SeckillException;

import java.util.List;

public interface SeckillService {
    /**
    * @Description:  得到可以被秒杀的商品集合
    * @Param:
    * @return:
    * @Author: dzc
    * @Date: 2018/9/8
    */
    List<Seckill> getSeckillList();
    
    /** 
    * @Description: 根据商品ID查询某个可以被秒杀的商品信息
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    Seckill getById(long seckillId);
    
    /** 
    * @Description: 在秒杀开始时输出秒杀接口的地址，否则输出系统时间和秒杀时间 
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    Exposer exportSeckillUrl(long seckillId);
    
    /** 
    * @Description: 执行秒杀操作 可能会失败 所以要抛出我们允许的异常 
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8
    */ 
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5);

    /**
    * @Description: 调用MySQL的存储过程来实现秒杀操作 (即两条sql语句在MySQL服务执行完毕，返回结果给Tomcat服务器)
    * @Param:
    * @return:
    * @Author: dzc
    * @Date: 2018/9/11
    */
    SeckillExecution executeSeckillProcedue(long seckillId, long userPhone, String md5);
}
