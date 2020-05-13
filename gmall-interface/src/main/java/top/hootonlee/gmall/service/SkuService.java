package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lihaotan
 */
public interface SkuService {

    /**
     * 保存
     * @param pmsSkuInfo
     */
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    /**
     * 查询
     * @param skuId
     * @return
     */
    PmsSkuInfo findItemBySkuId(String skuId);

    /**
     * 查询sku相关集合
     * @param productId
     * @return
     */
    List<PmsSkuInfo> getSKuSaleAttrListBySpu(String productId);

    /**
     * es
     * @return
     */
    List<PmsSkuInfo> getAllSkuInfo();

    /**
     * 检验价格
     * @param productSkuId
     * @param price
     * @return
     */
    boolean checkPrice(String productSkuId, BigDecimal price);
}
