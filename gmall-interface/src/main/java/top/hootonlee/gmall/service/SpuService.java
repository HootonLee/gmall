package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PmsProductImage;
import top.hootonlee.gmall.entity.PmsProductInfo;
import top.hootonlee.gmall.entity.PmsProductSaleAttr;
import top.hootonlee.gmall.entity.PmsSkuInfo;

import java.util.List;

/**
 * @author lihaotan
 */
public interface SpuService {
    /**
     * 查所有spu
     * @param catalog3Id
     * @return
     */
    List<PmsProductInfo> spuList(String catalog3Id);

    /**
     * 添加spu
     * @param pmsProductInfo
     */
    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    /**
     * 查询
     * @param spuId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 查询
     * @param spuId
     * @return
     */
    List<PmsProductImage> spuImageList(String spuId);

    /**
     * 查询销售属性集合
     * @param skuId
     * @param productId
     * @return
     */
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);


}
