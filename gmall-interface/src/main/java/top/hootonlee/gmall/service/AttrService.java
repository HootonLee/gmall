package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PmsBaseAttrInfo;
import top.hootonlee.gmall.entity.PmsBaseAttrValue;
import top.hootonlee.gmall.entity.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

/**
 * @author lihaotan
 */
public interface AttrService {
    /**
     * 查询所有attr
     * @param catalog3Id
     * @return
     */
    List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id);

    /**
     * 查询所有attr值
     * @param attrId
     * @return
     */
    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    /**
     * 保存attr和值
     * @param pmsBaseAttrInfo
     */
    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    /**
     * 查询销售属性
     * @return
     */
    List<PmsBaseSaleAttr> baseSaleAttrList();

    /**
     * 查询
     * @param valueIdSet
     * @return
     */
    List<PmsBaseAttrInfo> getAttrValueByValueId(Set<String> valueIdSet);
}
