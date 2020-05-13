package top.hootonlee.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;
import top.hootonlee.gmall.cart.mapper.OmsCartItemMapper;
import top.hootonlee.gmall.constant.RedisConstant;
import top.hootonlee.gmall.entity.OmsCartItem;
import top.hootonlee.gmall.service.CartService;
import top.hootonlee.gmall.util.RedisUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lihaotan
 */
@Service(interfaceClass = CartService.class)
@Transactional(rollbackFor = {})
public class CartServiceImpl implements CartService {

    private Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public OmsCartItem ifCartExistByUserId(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem selectOne = omsCartItemMapper.selectOne(omsCartItem);
        return selectOne;
    }

    @Override
    public void updateCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItem.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        String memberId = omsCartItem.getMemberId();
        if (StringUtils.isNotBlank(memberId)) {
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> cartItemList = omsCartItemMapper.select(omsCartItem);

        Jedis jedis = redisUtils.getJedis();
        Map<String, String> map = new HashMap<>(2);

        for (OmsCartItem cartItem : cartItemList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        try {
            jedis.del(RedisConstant.USER + memberId + RedisConstant.CART);
            jedis.hmset(RedisConstant.USER + memberId + RedisConstant.CART, map);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("REDIS-CLIENT IS NOT CLOSE");
        } finally {
            jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> getCartList(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        try {
            jedis = redisUtils.getJedis();
            List<String> hvals = jedis.hvals(RedisConstant.USER + memberId + RedisConstant.CART);
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                if (0 == omsCartItem.getDeleteStatus()) {
                    omsCartItemList.add(omsCartItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("REDIS-CLIENT IS NOT CLOSE");
            return null;
        } finally {
            jedis.close();
        }
        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId())
                .andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
        //更新缓存
        flushCartCache(omsCartItem.getMemberId());
    }

    @Override
    public void delCartItem(String productId, String productSkuId, String memberId) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("productId", productId).andEqualTo("productSkuId", productSkuId)
                .andEqualTo("memberId", memberId);
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setDeleteStatus(1);
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
    }

}
