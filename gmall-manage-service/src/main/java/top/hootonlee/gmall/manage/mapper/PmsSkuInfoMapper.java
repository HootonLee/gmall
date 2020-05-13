package top.hootonlee.gmall.manage.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import top.hootonlee.gmall.entity.PmsSkuInfo;

import java.util.List;

/**
 * @author lihaotan
 */
public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    /**
     * 查询
     * @param productId
     * @return
     */
    List<PmsSkuInfo> selectSKuSaleAttrListBySpu(@Param("productId") String productId);
}
