package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.OmsCartItem;

import java.util.List;

/**
 * @author lihaotan
 */
public interface CartService {

    /**
     * 判断DB是否有对应商品
     * @param memberId
     * @param skuId
     * @return
     */
    OmsCartItem ifCartExistByUserId(String memberId, String skuId);

    /**
     * 更新
     * @param omsCartItem
     */
    void updateCart(OmsCartItem omsCartItem);

    /**
     * 添加
     * @param omsCartItem
     */
    void addCart(OmsCartItem omsCartItem);

    /**
     * 更新购物车缓存
     * @param memberId
     */
    void flushCartCache(String memberId);

    /**
     * 查询用户对于的购物车
     * @param memberId
     * @return
     */
    List<OmsCartItem> getCartList(String memberId);

    /**
     * 异步更新 返回片
     * @param omsCartItem
     * @return
     */
    void checkCart(OmsCartItem omsCartItem);

    /**
     * 删除购物车
     * @param productId
     * @param productSkuId
     * @param memberId
     */
    void delCartItem(String productId, String productSkuId, String memberId);

}
