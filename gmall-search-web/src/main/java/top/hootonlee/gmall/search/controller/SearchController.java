package top.hootonlee.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import top.hootonlee.gmall.annotations.LoginRequired;
import top.hootonlee.gmall.entity.*;
import top.hootonlee.gmall.service.AttrService;
import top.hootonlee.gmall.service.SearchService;

import java.util.*;

/**
 * @author lihaotan
 */
@Controller
public class SearchController {

    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @LoginRequired(loginSuccess = false)
    @RequestMapping({"/index", "/"})
    public String index() {
        return "index";
    }

    @RequestMapping("/list.html")
    public String list(PmsSearchParam pmsSearchParam, Model model) {
        // 对应参数查询ES出来的所有商品
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);
        model.addAttribute("skuLsInfoList", pmsSearchSkuInfoList);

        // 根据去重后的valueId，查询对应属性名，属性值等
        Set<String> valueIdSet = new HashSet<>();
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = null;
        if (null != pmsSearchSkuInfoList && pmsSearchSkuInfoList.size() > 0) {
            for (PmsSearchSkuInfo searchSkuInfo : pmsSearchSkuInfoList) {
                List<PmsSkuAttrValue> attrValueList = searchSkuInfo.getSkuAttrValueList();
                for (PmsSkuAttrValue attrValue : attrValueList) {
                    String valueId = attrValue.getValueId();
                    valueIdSet.add(valueId);
                }
            }
            pmsBaseAttrInfoList = attrService.getAttrValueByValueId(valueIdSet);
            model.addAttribute("attrList", pmsBaseAttrInfoList);
        }
        // 对平台属性列表，去除当前valueId所在的属性组
        String[] delValueId = pmsSearchParam.getValueId();
        if (null != delValueId) {
            // 整合面包屑，每次接收value去掉属性组的同时，添加面包屑
            List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
            for (String value2Crumb : delValueId) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                // 生成面包屑的参数
                pmsSearchCrumb.setValueId(value2Crumb);
                pmsSearchCrumb.setUrlParam(getUrlParam4Crumb(pmsSearchParam, value2Crumb));
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue baseAttrValue : attrValueList) {
                        String valueId = baseAttrValue.getId();
                        if (valueId.equals(value2Crumb)) {
                            pmsSearchCrumb.setValueName(baseAttrValue.getValueName());
                            // 删除该属性所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbList.add(pmsSearchCrumb);
            }
            model.addAttribute("attrValueSelectList", pmsSearchCrumbList);
        }
        String urlParam = getUrlParam(pmsSearchParam);
        model.addAttribute("urlParam", urlParam);
        return "search";
    }

    private String getUrlParam4Crumb(PmsSearchParam pmsSearchParam, String value2Crumb) {
        StringBuilder urlParam = new StringBuilder();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valuesId = pmsSearchParam.getValueId();
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam.append("&");
            }
            urlParam.append("catalog3Id=").append(catalog3Id);
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam.append("&");
            }
            urlParam.append("keyword=").append(keyword);
        }
        if (null != valuesId) {
            for (String s : valuesId) {
                if (!s.equals(value2Crumb)) {
                    urlParam.append("&valueId=").append(s);
                }
            }
        }
        return urlParam.toString();
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        StringBuilder urlParam = new StringBuilder();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valuesId = pmsSearchParam.getValueId();
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam.append("&");
            }
            urlParam.append("catalog3Id=").append(catalog3Id);
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam.append("&");
            }
            urlParam.append("keyword=").append(keyword);
        }
        if (null != valuesId) {
            for (String s : valuesId) {
                urlParam.append("&valueId=").append(s);
            }
        }
        return urlParam.toString();
    }
}
