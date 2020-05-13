package top.hootonlee.gmall.order.mq;

import com.alibaba.fastjson.JSON;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hootonlee.gmall.service.OrderService;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lihaotan
 */
@Component
public class OrderServiceMqListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = {"PAYMENT_SUCCESS_QUEUE"})
    @RabbitHandler
    public void consumePaymentResult(String message) {
        System.out.println(message);
//        Map map = JSON.parseObject(message, Map.class);
//        String out_trade_no = (String) map.get("out_trade_no");
//        System.out.println(out_trade_no);
    }


}
