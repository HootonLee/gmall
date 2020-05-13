package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PaymentInfo;

/**
 * @author lihaotan
 */
public interface PaymentService {

    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新支付信息
     * @param paymentInfo
     */
    void updatePayment(PaymentInfo paymentInfo);
}
