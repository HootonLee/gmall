package top.hootonlee.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import top.hootonlee.gmall.annotations.LoginRequired;
import top.hootonlee.gmall.constant.InterceptorConstant;
import top.hootonlee.gmall.entity.OmsOrder;
import top.hootonlee.gmall.entity.PaymentInfo;
import top.hootonlee.gmall.service.OrderService;
import top.hootonlee.gmall.service.PaymentService;
import top.hootonlee.gmall.util.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author lihaotan
 */
@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;

    @RequestMapping("/alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String callback(String sign, String trade_no,String out_trade_no, String call_back_content, HttpServletRequest request, Model model) {

        // 回调请求中获取支付宝参数
        /*String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();*/

        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            // 验签成功
            // 更新用户的支付状态
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            // 支付宝的交易凭证号
            paymentInfo.setAlipayTradeNo(trade_no);
            //回调请求字符串
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());

            paymentService.updatePayment(paymentInfo);
        }

        // 支付成功后 -> 引起系统服务，订单服务更新，库存服务锁定库存等，物流服务

        return "finish";
    }

    @RequestMapping("/alipay/submit")
    @LoginRequired(loginSuccess = true)
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, Model model) {

        // 调用阿里等接口进行交易

        // 生成保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderInfoByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("gmall商城商品");
        paymentInfo.setTotalAmount(totalAmount);

        paymentService.savePaymentInfo(paymentInfo);

//        ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8087/alipay/callback/return");
//        mv.addObject("sign","1");
//        mv.addObject("trade_no","20200202000000");
//        mv.addObject("out_trade_no",outTradeNo);
//        mv.addObject("call_back_content","charset=utf-8&out_trade_no=atguigu201903201551591553068319124&method=alipay.trade.page.pay.return&total_amount=0.01&sign=JHMpfB9wv%2FOaNV9Cpjp7%2B6uY83ScfJ4YIG6dsDtrJlbbRJj6Z7%2FMlT3EazeB487wlKGPFim9L2xzIl8MwBpCwOc2qI95pCDwDrXgaO%2F2yA%2FlDp6bDkcRx84Lkm%2F2MNwZ%2FyFSW%2FyyDxWGEI3izHYMm1rf8T6nNDKvfuKTrKiKiIAGSv%2FJX1z7InGW%2BgeWtLlYWdV9fS1aKDEUZwGJaKwQeGf0c2YpZ2u%2FPoBuT32IQTbACx60SO4Jdz4y%2BVjwF5UmvLZD6HP7n5hvcQE833r9FOCU3rOskdAWNWt4wEvaJ%2FAuq%2BFAg6xHTDk1E2iDwkLVjumnYM%2FUcpw6G6Yu60nVtQ%3D%3D&trade_no=2019032022001409701031928056&auth_app_id=2018020102122556&version=1.0&app_id=2018020102122556&sign_type=RSA2&seller_id=2088921750292524&timestamp=2019-03-20+15%3A52%3A40");
//        String url = "http://payment.gmall.com:8087/alipay/callback/return?call_back_content=charset=utf-8&out_trade_no="+outTradeNo+"&method=alipay.trade.page.pay.return&total_amount=0.01&sign=JHMpfB9wv%2FOaNV9Cpjp7%2B6uY83ScfJ4YIG6dsDtrJlbbRJj6Z7%2FMlT3EazeB487wlKGPFim9L2xzIl8MwBpCwOc2qI95pCDwDrXgaO%2F2yA%2FlDp6bDkcRx84Lkm%2F2MNwZ%2FyFSW%2FyyDxWGEI3izHYMm1rf8T6nNDKvfuKTrKiKiIAGSv%2FJX1z7InGW%2BgeWtLlYWdV9fS1aKDEUZwGJaKwQeGf0c2YpZ2u%2FPoBuT32IQTbACx60SO4Jdz4y%2BVjwF5UmvLZD6HP7n5hvcQE833r9FOCU3rOskdAWNWt4wEvaJ%2FAuq%2BFAg6xHTDk1E2iDwkLVjumnYM%2FUcpw6G6Yu60nVtQ%3D%3D&trade_no=20200202000000&auth_app_id=2018020102122556&version=1.0&app_id=2018020102122556&sign_type=RSA2&seller_id=2088921750292524&timestamp=2019-03-20+15%3A52%3A40";
        model.addAttribute("sign", "1");
        model.addAttribute("trade_no", "20200202000000");
        model.addAttribute("out_trade_no", outTradeNo);
        model.addAttribute("call_back_content", "charset=utf-8");
        return "intermediate";
    }

    @RequestMapping("/mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(HttpServletRequest request, Model model) {

        return null;
    }

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = true)
    public String index(HttpServletRequest request, Model model) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        OmsOrder omsOrder = orderService.getOrderInfo(memberId);

        model.addAttribute("nickName", nickname);
        model.addAttribute("outTradeNo", omsOrder.getOrderSn());
        model.addAttribute("totalAmount", omsOrder.getTotalAmount());

        return "index";
    }

}
