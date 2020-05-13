package top.hootonlee.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import top.hootonlee.gmall.constant.RedisConstant;
import top.hootonlee.gmall.entity.OmsOrder;
import top.hootonlee.gmall.entity.OmsOrderItem;
import top.hootonlee.gmall.order.mapper.OmsOrderItemMapper;
import top.hootonlee.gmall.order.mapper.OmsOrderMapper;
import top.hootonlee.gmall.service.CartService;
import top.hootonlee.gmall.service.OrderService;
import top.hootonlee.gmall.util.RedisUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author lihaotan
 */
@Service(interfaceClass = OrderService.class)
@Transactional(rollbackFor = {})
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private OmsOrderMapper omsOrderMapper;
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
//    @Reference
//    private CartService cartService;

    @Override
    public OmsOrder getOrderInfoByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder order = omsOrderMapper.selectOne(omsOrder);
        return order;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder, String memberId) {
        // 保存订单
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        List<OmsOrderItem> orderItemList = omsOrder.getOmsOrderItems();
        // 保存订单详情
        for (OmsOrderItem omsOrderItem : orderItemList) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            // 删除购物车商品
//            cartService.delCartItem(omsOrderItem.getProductId(), omsOrderItem.getProductSkuId(), memberId);
        }
    }

    @Override
    public OmsOrder getOrderInfo(String memberId) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setMemberId(memberId);
        omsOrder.setStatus("0");
        omsOrder.setDeleteStatus("0");
        List<OmsOrder> orderList = omsOrderMapper.select(omsOrder);
        OmsOrder order = orderList.get(orderList.size() - 1);
        return order;
    }

    @Override
    public String comTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtils.getJedis();
            // 可以使用lua脚本删除，防止并发订单攻击
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

//            String tradeCode4Cache = jedis.get(RedisConstant.USER + memberId + RedisConstant.TRADE_CODE);
//            if (StringUtils.isNotBlank(tradeCode4Cache) && tradeCode4Cache.equals(tradeCode)) {
//                jedis.del(RedisConstant.USER + memberId + RedisConstant.TRADE_CODE);

            Long eval = (Long) jedis.eval(script, Collections.singletonList(RedisConstant.USER + memberId + RedisConstant.TRADE_CODE), Collections.singletonList(tradeCode));
            if (null != eval && 0 != eval) {
                return "success";
            } else {
                return "fail";
            }
        } finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtils.getJedis();
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(RedisConstant.USER + memberId + RedisConstant.TRADE_CODE, 60 * 15, tradeCode);
            return tradeCode;
        } finally {
            jedis.close();
        }
    }

}
