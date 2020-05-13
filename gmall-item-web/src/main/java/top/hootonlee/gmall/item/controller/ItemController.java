package top.hootonlee.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import top.hootonlee.gmall.entity.PmsProductSaleAttr;
import top.hootonlee.gmall.entity.PmsSkuInfo;
import top.hootonlee.gmall.entity.PmsSkuSaleAttrValue;
import top.hootonlee.gmall.service.SkuService;
import top.hootonlee.gmall.service.SpuService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lihaotan
 */
@Controller
public class ItemController {

    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, Model model) {
        PmsSkuInfo pmsSkuInfo = skuService.findItemBySkuId(skuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        model.addAttribute("skuInfo", pmsSkuInfo);
        model.addAttribute("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        List<PmsSkuInfo> pmsSkuInfos = skuService.getSKuSaleAttrListBySpu(pmsSkuInfo.getProductId());
        Map<String, String> saleAttrMap = new HashMap<>(16);
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();
            for (PmsSkuSaleAttrValue saleAttrValue : skuInfo.getSkuSaleAttrValueList()) {
                if (saleAttrValue != skuInfo.getSkuSaleAttrValueList().get(skuInfo.getSkuSaleAttrValueList().size()-1)){
                    k += saleAttrValue.getSaleAttrValueId() + "|";
                }else {
                    k += saleAttrValue.getSaleAttrValueId();
                }
            }
            saleAttrMap.put(k, v);
        }
        String skuSaleAttrJsonStr = JSON.toJSONString(saleAttrMap);
        model.addAttribute("skuSaleAttrJsonStr", skuSaleAttrJsonStr);

        return "item";
    }

}
