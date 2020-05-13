package top.hootonlee.gmall.manage.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import top.hootonlee.gmall.entity.PmsProductSaleAttr;

import java.util.List;

/**
 * @author lihaotan
 */
public interface PmsProductSalaAttrMapper extends Mapper<PmsProductSaleAttr> {

    /**
     * 查询
     * @param skuId
     * @param productId
     * @return
     */
    List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("productId") String productId,@Param("skuId") String skuId);
}
