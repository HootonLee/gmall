package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.OmsOrder;

/**
 * @author lihaotan
 */
public interface OrderService {

    /**
     * 生成用户订单校验码
     * @param memberId
     * @return
     */
    String genTradeCode(String memberId);

    /**
     * 比对交易码
     * @param memberId
     * @param tradeCode
     * @return
     */
    String comTradeCode(String memberId, String tradeCode);

    /**
     * 保存订单
     * @param omsOrder
     * @param memberId
     */
    void saveOrder(OmsOrder omsOrder, String memberId);

    /**
     * 查询订单
     * @param memberId
     * @return
     */
    OmsOrder getOrderInfo(String memberId);

    /**
     * 获得订单信息
     * @param outTradeNo
     * @return
     */
    OmsOrder getOrderInfoByOutTradeNo(String outTradeNo);
}
