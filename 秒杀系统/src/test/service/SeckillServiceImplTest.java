package test;

import com.gdei.dto.Exposer;
import com.gdei.dto.SeckillExecution;
import com.gdei.service.SeckillService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"
        , "classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {

    @Autowired
    private SeckillService seckillService;

    @Test
    public void exportSeckillUrl() {
        Exposer exposer = seckillService.exportSeckillUrl(1001);
        System.out.println(exposer);
    }

    @Test
    public void executeSeckill() {
    }

    @Test
    public void executeSeckillProcedureTest() {
        long seckillId = 1001;
        long phone = 1368011101;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            //executeSeckillProcedure(seckillId, phone, md5);
            SeckillExecution execution = seckillService.executeSeckillProcedue(seckillId, phone, md5);
            System.out.println(execution.getStateInfo());
        }
    }


}