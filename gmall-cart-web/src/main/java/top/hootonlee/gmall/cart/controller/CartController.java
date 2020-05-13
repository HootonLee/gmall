package top.hootonlee.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import top.hootonlee.gmall.annotations.LoginRequired;
import top.hootonlee.gmall.entity.OmsCartItem;
import top.hootonlee.gmall.entity.PmsSkuInfo;
import top.hootonlee.gmall.service.CartService;
import top.hootonlee.gmall.service.SkuService;
import top.hootonlee.gmall.util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lihaotan
 */
@Controller
public class CartController {

    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/addToCart")
    public String addToCart(String skuId, Integer quantity, HttpServletRequest request, HttpServletResponse response, HttpSession session, RedirectAttributes attributes) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        // 调用商品服务查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.findItemBySkuId(skuId);
        // 封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductSkuCode("00-0000-000-00000-000000");
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());


        //判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        if (StringUtils.isBlank(memberId)) {
            // 用户没有登录

            // 原来购物车cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)) {
                // cookie没值
                omsCartItemList.add(omsCartItem);
            } else {
                // cookie中有值
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //判断添加的数据在cookie中是否存在
                Boolean exist = if_cart_exist(omsCartItemList, omsCartItem);
                if (exist) {
                    // cookie中存在添加购物车数据，更新数量信息
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                } else {
                    // cookie不存在添加购物车数据，新添
                    omsCartItemList.add(omsCartItem);
                }
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList), 60 * 60 * 72, true);
        } else {
            // 用户已经登录

            // 判断数据库是否有用户添加的购物车数据
            OmsCartItem omsCartItem4Db = cartService.ifCartExistByUserId(memberId, skuId);
            if (null != omsCartItem4Db) {
                // 有，更新商品对应的数量
                omsCartItem4Db.setQuantity(omsCartItem4Db.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItem4Db);
            } else {
                // 没有，添加商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickname);
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            }
            cartService.flushCartCache(memberId);
        }

        attributes.addFlashAttribute("skuInfo", pmsSkuInfo);
        attributes.addFlashAttribute("quantity", quantity);
        return "redirect:/success";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();

        String memberId = (String) request.getAttribute("memberId");
        if (StringUtils.isNotBlank(memberId)) {
            // 已经登录，查询缓存、DB
            omsCartItemList = cartService.getCartList(memberId);
            String cookieValue = CookieUtil.getCookieValue(request, "cartListCookie", true);
            // cookie中没有放到缓存中
            if (StringUtils.isNotBlank(cookieValue)) {
                List<OmsCartItem> cartItemListCache = JSON.parseArray(cookieValue, OmsCartItem.class);
                for (OmsCartItem cartItem : cartItemListCache) {
                    OmsCartItem exist = cartService.ifCartExistByUserId(memberId, cartItem.getProductSkuId());
                    if (null == exist) {
                        cartItem.setMemberId(memberId);
                        cartService.addCart(cartItem);
                        omsCartItemList.add(cartItem);
                    }
                }
            }
        } else {
            // 没有登录，查询cookie
            String cookieValue = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cookieValue)) {
                omsCartItemList = JSON.parseArray(cookieValue, OmsCartItem.class);
            }
        }
        for (OmsCartItem cartItem : omsCartItemList) {
            cartItem.setTotalPrice(cartItem.getQuantity().multiply(cartItem.getPrice()));
        }
        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartList", omsCartItemList);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItemList) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if ("1".equals(omsCartItem.getIsChecked())) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/checkCart")
    public String checkCart(String skuId, String isChecked, HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {

        String memberId = (String) request.getAttribute("memberId");

        if (StringUtils.isNotBlank(memberId)) {
            // 异步调用 更新状态
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            omsCartItem.setProductSkuId(skuId);
            omsCartItem.setIsChecked(isChecked);
            cartService.checkCart(omsCartItem);
            // 从缓存中取出最新的数据，片返回
            List<OmsCartItem> cartItemList = cartService.getCartList(memberId);
            BigDecimal totalAmount = getTotalAmount(cartItemList);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("cartList", cartItemList);
        }
        return "cartListInner";
    }

    private Boolean if_cart_exist(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {

        Boolean flag = false;
        for (OmsCartItem cartItem : omsCartItemList) {
            String skuId = cartItem.getProductSkuId();
            if (skuId.equals(omsCartItem.getProductSkuId())) {
                flag = true;
            }
        }
        return flag;
    }

    @RequestMapping("/success")
    public String success() {
        return "success";
    }

}
