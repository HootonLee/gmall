package top.hootonlee.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import top.hootonlee.gmall.annotations.LoginRequired;
import top.hootonlee.gmall.constant.InterceptorConstant;
import top.hootonlee.gmall.entity.OmsCartItem;
import top.hootonlee.gmall.entity.OmsOrder;
import top.hootonlee.gmall.entity.OmsOrderItem;
import top.hootonlee.gmall.entity.UmsMemberReceiveAddress;
import top.hootonlee.gmall.service.CartService;
import top.hootonlee.gmall.service.OrderService;
import top.hootonlee.gmall.service.SkuService;
import top.hootonlee.gmall.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author lihaotan
 */
@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private OrderService orderService;
    @Reference
    private CartService cartService;
    @Reference
    private SkuService skuService;


    @RequestMapping("/submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, Model model, HttpServletRequest request) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        // 比对页面交易码
        String success = orderService.comTradeCode(memberId, tradeCode);
        if (InterceptorConstant.SUCCESS.equals(success)) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount(); 运费，支付后，在生成物流信息时
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");
            String outTradeNo = "gmall";
            // 将毫秒时间戳拼接到外部订单号
            outTradeNo = outTradeNo + System.currentTimeMillis();
            LocalDateTime localDateTime = LocalDateTime.now();
            // 将时间字符串拼接到外部订单号
            outTradeNo = outTradeNo + localDateTime;
            //外部订单号
            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType("1");

            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType("0");
            omsOrder.setStatus("0");
            omsOrder.setOrderType("0");
            omsOrder.setTotalAmount(totalAmount);

            List<OmsCartItem> cartItemList = cartService.getCartList(memberId);
            for (OmsCartItem omsCartItem : cartItemList) {
                if ("1".equals(omsCartItem.getIsChecked())) {
                    // 获取订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    // 检验价格
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (!b) {
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    // 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setOrderSn(outTradeNo);
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("000-00-00-0000-000000-0");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    // 在仓库中的skuId
                    omsOrderItem.setProductSn("仓库对应的商品编号");

                    omsOrderItems.add(omsOrderItem);
                }
                omsOrder.setOmsOrderItems(omsOrderItems);
            }
            orderService.saveOrder(omsOrder, memberId);
            // 调用支付系统
            ModelAndView mv = new ModelAndView("redirect:http://payment.gmall.com:8087/index");
//            mv.addObject("outTradeNo", outTradeNo);
//            mv.addObject("totalAmount", totalAmount);
            return mv;
        } else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

    @RequestMapping("/toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(Model model, HttpServletRequest request) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<UmsMemberReceiveAddress> userAddressList = userService.getUserAddressList(memberId);

        List<OmsCartItem> cartItemList = cartService.getCartList(memberId);
        List<OmsOrderItem> omsOrderItemList = new ArrayList<>();
        for (OmsCartItem cartItem : cartItemList) {
            if ("1".equals(cartItem.getIsChecked())) {
                OmsOrderItem orderItem = new OmsOrderItem();
                orderItem.setProductName(cartItem.getProductName());
                orderItem.setProductPic(cartItem.getProductPic());
                omsOrderItemList.add(orderItem);
            }
        }
        model.addAttribute("userAddressList", userAddressList);
        model.addAttribute("omsOrderItems", omsOrderItemList);
        model.addAttribute("totalAmount", getTotalAmount(cartItemList));
        // 生产交易码，为提交订单做校验
        String tradeCode = orderService.genTradeCode(memberId);
        model.addAttribute("tradeCode", tradeCode);
        return "trade";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> cartItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem cartItem : cartItemList) {
            BigDecimal price = cartItem.getTotalPrice();
            if ("1".equals(cartItem.getIsChecked())) {
                totalAmount = totalAmount.add(price);
            }
        }
        return totalAmount;
    }

}
