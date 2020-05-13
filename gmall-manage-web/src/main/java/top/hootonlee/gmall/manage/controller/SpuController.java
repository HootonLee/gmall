package top.hootonlee.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.hootonlee.gmall.entity.PmsProductImage;
import top.hootonlee.gmall.entity.PmsProductInfo;
import top.hootonlee.gmall.entity.PmsProductSaleAttr;
import top.hootonlee.gmall.service.SpuService;
import top.hootonlee.gmall.util.FileRenameUtils;
import top.hootonlee.gmall.util.OssAliUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author lihaotan
 */
@RestController
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String fileName = FileRenameUtils.FileRename4UUID(multipartFile.getOriginalFilename());
        String imgUrl = OssAliUtils.uploadObject2OSS(multipartFile.getBytes(), fileName);
        return imgUrl;
    }

    @RequestMapping("/spuList")
    public List<PmsProductInfo> spuList(@RequestParam("catalog3Id") String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    @RequestMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(@RequestParam("spuId") String spuId) {
        List<PmsProductSaleAttr> spuSaleAttrList = spuService.spuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(@RequestParam("spuId") String spuId) {
        List<PmsProductImage> imageList = spuService.spuImageList(spuId);
        return imageList;
    }
}