package top.hootonlee.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import top.hootonlee.gmall.constant.RedisConstant;
import top.hootonlee.gmall.entity.PmsSkuAttrValue;
import top.hootonlee.gmall.entity.PmsSkuImage;
import top.hootonlee.gmall.entity.PmsSkuInfo;
import top.hootonlee.gmall.entity.PmsSkuSaleAttrValue;
import top.hootonlee.gmall.manage.mapper.PmsSkuAttrValueMapper;
import top.hootonlee.gmall.manage.mapper.PmsSkuImageMapper;
import top.hootonlee.gmall.manage.mapper.PmsSkuInfoMapper;
import top.hootonlee.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import top.hootonlee.gmall.service.SkuService;
import top.hootonlee.gmall.util.RedisUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lihaotan
 */
@Service(interfaceClass = SkuService.class)
@Transactional(rollbackFor = {})
public class SkuServiceImpl implements SkuService {

    Logger logger = LoggerFactory.getLogger(SkuServiceImpl.class);

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        if (StringUtils.isBlank(pmsSkuInfo.getSkuDefaultImg())) {
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        List<PmsSkuSaleAttrValue> saleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue saleAttrValue : saleAttrValueList) {
            saleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }
        List<PmsSkuAttrValue> AttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue attrValue : AttrValueList) {
            attrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(attrValue);
        }
        List<PmsSkuImage> imageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage image : imageList) {
            image.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(image);
        }
    }

    public PmsSkuInfo getSkuById4Db(String skuId) {
        PmsSkuInfo skuInfo = new PmsSkuInfo();
        try {
            PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
            pmsSkuInfo.setId(skuId);
            skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
            if (null != skuInfo) {
                PmsSkuImage pmsSkuImage = new PmsSkuImage();
                pmsSkuImage.setSkuId(skuId);
                List<PmsSkuImage> skuImages = pmsSkuImageMapper.select(pmsSkuImage);
                skuInfo.setSkuImageList(skuImages);
            }
        } catch (NullPointerException e) {
            return skuInfo;
        }
        return skuInfo;
    }

    @Override
    public PmsSkuInfo findItemBySkuId(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        Jedis jedis = redisUtils.getJedis();
        String skuKey = RedisConstant.SKU + skuId + RedisConstant.INFO;
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            String flag = jedis.set(RedisConstant.SKU + skuId + RedisConstant.LOCK, Thread.currentThread().getName(), "nx", "ex", 10);
            if (StringUtils.isNotBlank(flag) && RedisConstant.OK.equals(flag)) {
                // 缓存没有查询db
                pmsSkuInfo = getSkuById4Db(skuId);
                if (null != pmsSkuInfo) {
                    jedis.set(RedisConstant.SKU + skuId + RedisConstant.INFO, JSON.toJSONString(pmsSkuInfo));
                } else {
                    jedis.setex(RedisConstant.SKU + skuId + RedisConstant.INFO, 60 * 3, JSON.toJSONString(""));
                }
                if (Thread.currentThread().getName().equals(jedis.get(RedisConstant.SKU + skuId + RedisConstant.LOCK))) {
                    jedis.del(RedisConstant.SKU + skuId + RedisConstant.LOCK);
                }
            }
        }
        if (null != jedis) {
            jedis.close();
        }
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSKuSaleAttrListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSKuSaleAttrListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        List<PmsSkuInfo> skuInfoList = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo skuInfo : skuInfoList) {
            PmsSkuAttrValue attrValue = new PmsSkuAttrValue();
            attrValue.setSkuId(skuInfo.getId());
            List<PmsSkuAttrValue> attrValues = pmsSkuAttrValueMapper.select(attrValue);
            skuInfo.setSkuAttrValueList(attrValues);
        }
        return skuInfoList;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal priceDb = skuInfo.getPrice();
        if (price.compareTo(priceDb) == 0) {
            b = true;
        }
        return b;
    }
}
