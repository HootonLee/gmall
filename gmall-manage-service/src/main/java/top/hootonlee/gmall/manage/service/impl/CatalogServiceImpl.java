package top.hootonlee.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import top.hootonlee.gmall.entity.PmsBaseCatalog1;
import top.hootonlee.gmall.entity.PmsBaseCatalog2;
import top.hootonlee.gmall.entity.PmsBaseCatalog3;
import top.hootonlee.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import top.hootonlee.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import top.hootonlee.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import top.hootonlee.gmall.service.CatalogService;

import java.util.List;


/**
 * @author lihaotan
 */
@Service(interfaceClass = CatalogService.class)
@Transactional(rollbackFor = {})
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    private PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    private PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;


    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        return pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
    }
    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        return pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
    }

}
