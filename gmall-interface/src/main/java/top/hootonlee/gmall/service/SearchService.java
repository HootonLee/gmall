package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.PmsSearchParam;
import top.hootonlee.gmall.entity.PmsSearchSkuInfo;

import java.util.List;

/**
 * @author lihaotan
 */
public interface SearchService {

    /**
     * list
     * @param pmsSearchParam
     * @return
     */
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
