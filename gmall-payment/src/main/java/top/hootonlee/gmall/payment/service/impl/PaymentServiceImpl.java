package top.hootonlee.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import top.hootonlee.gmall.entity.PaymentInfo;
import top.hootonlee.gmall.payment.mapper.PaymentInfoMapper;
import top.hootonlee.gmall.service.PaymentService;
import top.hootonlee.gmall.util.RabbitUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lihaotan
 */
@Service(interfaceClass = PaymentService.class)
@Transactional(rollbackFor = {Exception.class})
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private RabbitUtils rabbitUtils;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Connection connection = rabbitUtils.getConnection();
        Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            channel.txSelect();
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("OrderSn", paymentInfo.getOrderSn());
            paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
            channel.queueDeclare("PAYMENT_SUCCESS_QUEUE", true, false, false, null);
            Map<String, String> map = new HashMap<>(2);
            map.put("out_trade_no", paymentInfo.getOrderSn());
            String jsonString = JSON.toJSONString(map);
            channel.basicPublish("", "PAYMENT_SUCCESS_QUEUE", MessageProperties.PERSISTENT_TEXT_PLAIN, jsonString.getBytes());
            channel.txCommit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                channel.txRollback();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
