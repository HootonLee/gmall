package top.hootonlee.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.hootonlee.gmall.entity.PmsSearchSkuInfo;
import top.hootonlee.gmall.entity.PmsSkuInfo;
import top.hootonlee.gmall.service.SkuService;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    private SkuService skuService;
    @Autowired
    JestClient jestClientl;

    @Test
    public void contextLoads() throws Exception {

        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSkuInfo();

        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {

            PmsSearchSkuInfo searchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(searchSkuInfo, skuInfo);
            Index build = new Index.Builder(searchSkuInfo).index("gmall").type("PmsSkuInfo").id(skuInfo.getId()).build();
            jestClientl.execute(build);
        }

    }

}
