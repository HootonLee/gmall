package top.hootonlee.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import top.hootonlee.gmall.entity.PmsBaseAttrInfo;
import top.hootonlee.gmall.entity.PmsBaseAttrValue;
import top.hootonlee.gmall.entity.PmsBaseSaleAttr;
import top.hootonlee.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import top.hootonlee.gmall.manage.mapper.PmsBaseAttrValueMapper;
import top.hootonlee.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import top.hootonlee.gmall.service.AttrService;

import java.util.List;
import java.util.Set;

/**
 * @author lihaotan
 */
@Service(interfaceClass = AttrService.class)
@Transactional(rollbackFor = {})
public class AttrServiceImpl implements AttrService {

    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> attrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo attrInfo : attrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(attrInfo.getId());
            List<PmsBaseAttrValue> attrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            attrInfo.setAttrValueList(attrValues);
        }
        return attrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String attrInfoId = pmsBaseAttrInfo.getId();
        if (StringUtils.isBlank(attrInfoId)) {
            // 新建
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            for (PmsBaseAttrValue attrValue : pmsBaseAttrInfo.getAttrValueList()) {
                attrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }
        } else {
            if (StringUtils.isEmpty(pmsBaseAttrInfo.getAttrName()) && pmsBaseAttrInfo.getAttrValueList().size() <= 0) {
                PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
                pmsBaseAttrInfoMapper.delete(pmsBaseAttrInfo);
            }
            // 更新
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);
            // 值表
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
            for (PmsBaseAttrValue attrValue : attrValueList) {
                attrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueByValueId(Set<String> valueIdSet) {
        // 1,2,3
        String valueIdStr = StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = pmsBaseAttrInfoMapper.selectAttrListByValueId(valueIdStr);
        return pmsBaseAttrInfoList;
    }
}
