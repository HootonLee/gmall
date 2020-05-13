package top.hootonlee.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import top.hootonlee.gmall.entity.*;
import top.hootonlee.gmall.manage.mapper.PmsProductImageMapper;
import top.hootonlee.gmall.manage.mapper.PmsProductInfoMapper;
import top.hootonlee.gmall.manage.mapper.PmsProductSalaAttrMapper;
import top.hootonlee.gmall.manage.mapper.PmsProductSalaAttrValueMapper;
import top.hootonlee.gmall.service.SpuService;

import java.util.List;

/**
 * @author lihaotan
 */
@Service(interfaceClass = SpuService.class)
@Transactional(rollbackFor = {})
public class SpuServiceImpl implements SpuService {

    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSalaAttrMapper pmsProductSalaAttrMapper;
    @Autowired
    private PmsProductSalaAttrValueMapper pmsProductSalaAttrValueMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return pmsProductInfoMapper.select(pmsProductInfo);
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();

        if (StringUtils.isBlank(pmsProductInfo.getId())) {
            pmsProductInfoMapper.insertSelective(pmsProductInfo);
            for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
                pmsProductSaleAttr.setProductId(pmsProductInfo.getId());
                pmsProductSalaAttrMapper.insertSelective(pmsProductSaleAttr);
                for (PmsProductSaleAttrValue saleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                    saleAttrValue.setProductId(pmsProductInfo.getId());
                    pmsProductSalaAttrValueMapper.insertSelective(saleAttrValue);
                }
            }
            for (PmsProductImage pmsProductImage : spuImageList) {
                pmsProductImage.setProductId(pmsProductInfo.getId());
                pmsProductImageMapper.insertSelective(pmsProductImage);
            }
        }
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> saleAttrList = pmsProductSalaAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr saleAttr : saleAttrList) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> attrValues = pmsProductSalaAttrValueMapper.select(pmsProductSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(attrValues);
        }
        return saleAttrList;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> imageList = pmsProductImageMapper.select(pmsProductImage);
        return imageList;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {
        return pmsProductSalaAttrMapper.selectSpuSaleAttrListCheckBySku(productId, skuId);
    }


}
