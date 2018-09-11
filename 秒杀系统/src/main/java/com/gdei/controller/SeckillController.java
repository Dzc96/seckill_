package com.gdei.controller;


import com.gdei.dto.Exposer;
import com.gdei.dto.SeckillExecution;
import com.gdei.dto.SeckillResult;
import com.gdei.entity.Seckill;
import com.gdei.enums.SeckillStatEnum;
import com.gdei.exception.RepeatKillException;
import com.gdei.exception.SeckillCloseException;
import com.gdei.exception.SeckillException;
import com.gdei.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /** 
    * @Description: 获得所有秒杀商品的列表 
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/9 
    */ 
    @RequestMapping(value="/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> seckillList = seckillService.getSeckillList();
        model.addAttribute("list", seckillList);
        return "list";
    }
    
    /** 
    * @Description: 点击某个可以被秒杀的商品，获取详细信息，跳转到可以点击秒杀的页面
    * @Param:  
    * @return:  
    * @Author: dzc
    * @Date: 2018/9/9 
    */ 
    @RequestMapping(value="/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        //进行逻辑判断，如果这个商品id不存在或根据该id找不到商品，则重定向回列表页面
        System.out.println("进入商品详情页！！！！！！");
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "redirect:/seckill/list";
        }

        //找到对应商品的数据，跳转到detail页面
        model.addAttribute("seckill", seckill);
        return "detail";
    }
    
    /** 
    * @Description: 暴露秒杀接口的方法，根据商品id在后台自动生成对应md5码的URL
    * @Param:  
    * @return:  返回的是封装了json相关数据的对象
    * @Author: dzc
    * @Date: 2018/9/9 
    */
    @RequestMapping(value="/{seckillId}/exposer", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result; //这个SeckillResult对象是用来方便做json解析的
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            e.printStackTrace();
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    /**
    * @Description: 在商品详情页，判断是否登陆，点击执行秒杀，后台尝试进行秒杀，并将结果封装成json，返回给前端
    * @Param:
    * @return:
    * @Author: dzc
    * @Date: 2018/9/9
    */
    @RequestMapping(value="/{seckillId}/{md5}/execution", method = RequestMethod.POST)
    @ResponseBody
    public  SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                           @PathVariable("md5") String md5,
                                           @CookieValue(value = "userPhone", required = false) Long userPhone) {
        //该系统中判断是否登陆，用的是Cookie，登陆后会把userPhone写入Cookie中
        //如果Cookie中有这个数据，则证明登陆，否则没有登陆
        if (userPhone == null) {
            return new SeckillResult<SeckillExecution>(false, "没有注册");
        }

        try {
            //原先是发送一条sql语句，等待MYSQL执行完后返回一次结果，这样一共两条sql语句，MYSQL和TOMCAT之间就有两次网络延迟
           // SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);

            //现在调用的是Service层写好的，调用存储过程来一次性在MYSQL中执行所有sql语句的方法，MYSQL和TOMCAT之间只有一次网络延迟
            SeckillExecution execution = seckillService.executeSeckillProcedue(seckillId, userPhone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);

        } catch (RepeatKillException e1) {
            // 重复秒杀
            System.out.println("重复秒杀！！！！！");
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e2) {
            // 秒杀关闭
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillException e) {
            // 不能判断的异常
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }
    }


    /**
    * @Description:获取服务器端数据，防止用户篡改客户端时间提前参加秒杀
    * @Param:
    * @return:
    * @Author: dzc
    * @Date: 2018/9/9
    */
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<LocalDateTime> time() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return new SeckillResult<>(true, localDateTime);
    }



}
