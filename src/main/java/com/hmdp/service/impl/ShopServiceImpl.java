package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR= Executors.newFixedThreadPool(10);

    @Override
    public Result queryById(Long id) {
        //缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById,CACHE_SHOP_TTL,TimeUnit.MINUTES);

        //互斥锁解决缓存击穿
//        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById,CACHE_SHOP_TTL,TimeUnit.SECONDS);

        //互斥锁解决缓存击穿
        //Shop shop = queryWithLogicalExpire(id);
        if (shop==null){
            return Result.fail("店铺不存在！");
        }
        return Result.ok(shop);
    }

    /*private Shop queryWithLogicalExpire(Long id){
        String key=CACHE_SHOP_KEY+id;
        //1.从redis中查询缓存数据
        String shopJSON = stringRedisTemplate.opsForValue().get(key);
        //2.断是否存在
        if (StrUtil.isBlank(shopJSON)){
            //3.不存在，直接返回
            return null;
        }
        //4.命中，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJSON, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONUtil.toJsonStr(redisData.getData())), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        //5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())){
            //5.1未过期，直接返回店铺信息
            return shop;
        }
        //5.2过期，需要缓存重建
        //6.缓存重建
        String lockKey=LOCK_SHOP_KEY+id;
        //6.1 获取互斥锁
        boolean isLock = tryLock(lockKey);
        //6.2 判断是否获取成功
        if (isLock){
            //6.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    this.saveShop2Redis(id,LOCK_SHOP_TTL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unlock(lockKey);
                }
            });
        }
        //6.4 失败，返回过期的商铺信息
        return shop;
    }*/
    //互斥锁解决缓存击穿
    /*private Shop queryWithMutex(Long id){
        String key=CACHE_SHOP_KEY+id;
        //1.从redis中查询缓存数据
        String shopJSON = stringRedisTemplate.opsForValue().get(key);
        //2.断是否存在
        if (StrUtil.isNotBlank(shopJSON)){
            //3.存在，直接返回
            return JSONUtil.toBean(shopJSON, Shop.class);
        }
        //判断是否存在（这个if判断的是否是空字符串）
        if (shopJSON !=null){
            return null;
        }

        //4.实现缓存击穿
        //4.1 获取互斥锁
        String lockKey="lock:shop:"+id;
        Shop shop= null;
        try {
            boolean isLock = tryLock(lockKey);
            //4.2判断是否获取成功
            if (!isLock){
                //4.3失败，则休眠成功
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            // 4.4不存在，根据id查询数据库
            shop = getById(id);
            //模拟重建的演示
            Thread.sleep(200);
            //5.不存在，返回错误
            if (shop==null){
                //将空值返回给redis
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //6.存在，写入redis
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //7.释放互斥锁
            unlock(lockKey);
        }

        //8.返回
        return shop;
    }*/

    /*private Shop queryWithPassThrough(Long id){
        String key=CACHE_SHOP_KEY+id;
        //1.从redis中查询缓存数据
        String shopJSON = stringRedisTemplate.opsForValue().get(key);
        //2.断是否存在
        if (StrUtil.isNotBlank(shopJSON)){
            //3.存在，直接返回
            return JSONUtil.toBean(shopJSON, Shop.class);
        }
        //判断是否存在（这个if判断的是否是空字符串）
        if (shopJSON !=null){
            return null;
        }

        // 4.不存在，根据id查询数据库
        Shop shop=getById(id);
        //5.不存在，返回错误
        if (shop==null){
            //将空值返回给redis
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //6.存在，写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //7.返回
        return shop;
    }*/

    /* private boolean tryLock(String key){
        Boolean fLag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(fLag);
    }*/

    /* private void unlock(String key){
        stringRedisTemplate.delete(key);
    }*/

    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
        //1.查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入seconds
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id==null){
            return Result.fail("店铺id不能为空");
        }
        //1. 更新数据库
        updateById(shop);
        //2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY+id);
        return Result.ok();
    }
}
