package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es= Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker(){
        long id = redisIdWorker.nextId("order");
        System.out.println("id = " + id);
    }

    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(2L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+2L, shop,10L, TimeUnit.MINUTES);
    }


}
