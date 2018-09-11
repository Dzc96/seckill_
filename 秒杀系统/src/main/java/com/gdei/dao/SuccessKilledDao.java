package com.gdei.dao;

import com.gdei.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao {
    /** 
    * @Description: 用户秒杀成功后在表中插入数据
    * @Param:
    * @return: 返回影响到的行数
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /** 
    * @Description: 查询某个具体的秒杀成功的明细数据，包含了被秒杀的商品信息
    * @Param:
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/8 
    */ 
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    
}
