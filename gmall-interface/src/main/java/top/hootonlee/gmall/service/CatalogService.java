package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PmsBaseCatalog1;
import top.hootonlee.gmall.entity.PmsBaseCatalog2;
import top.hootonlee.gmall.entity.PmsBaseCatalog3;

import java.util.List;

/**
 * @author lihaotan
 */
public interface CatalogService {
    /**
     * 查询catalog1
     * @return
     */
    List<PmsBaseCatalog1> getCatalog1();

    /**
     * 查询catalog2
     * @param catalog1Id
     * @return
     */
    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 查询catalog3
     * @param catalog2Id
     * @return
     */
    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
