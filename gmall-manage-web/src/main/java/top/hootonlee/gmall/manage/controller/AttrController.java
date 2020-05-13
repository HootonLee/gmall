package top.hootonlee.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import top.hootonlee.gmall.entity.PmsBaseAttrInfo;
import top.hootonlee.gmall.entity.PmsBaseAttrValue;
import top.hootonlee.gmall.entity.PmsBaseSaleAttr;
import top.hootonlee.gmall.service.AttrService;

import java.util.List;

/**
 * @author lihaotan
 */
@RestController
@CrossOrigin
public class AttrController {

    @Reference
    private AttrService attrService;

    @RequestMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(@RequestParam("catalog3Id") String catalog3Id) {
        List<PmsBaseAttrInfo> attrInfos = attrService.getAttrInfoList(catalog3Id);
        return attrInfos;
    }

    @RequestMapping("/getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam("attrId") String attrId) {
        List<PmsBaseAttrValue> attrValues = attrService.getAttrValueList(attrId);
        return attrValues;
    }

    @RequestMapping("/saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {
        attrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    @RequestMapping("/baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> baseSaleAttrs = attrService.baseSaleAttrList();
        return baseSaleAttrs;
    }
}
