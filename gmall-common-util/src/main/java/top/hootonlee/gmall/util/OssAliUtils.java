package top.hootonlee.gmall.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import java.io.ByteArrayInputStream;

/**
 * @author lihaotan
 */
public class OssAliUtils {

    private static final String URL = "https://gmall-imgs-server.oss-cn-beijing.aliyuncs.com/";
    private static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com";
    private static final String ACCESSKEY = "LTAI4Fy9qnjZEokqmxiY43W3";
    private static final String ACCESSKEY_SECRET = "i1zktfig59lLZPLPyfh2ovfHAbcOl2";
    private static final String BUCKET = "gmall-imgs-server";

    private static final OSS getConnection() {
        return new OSSClientBuilder().build(ENDPOINT, ACCESSKEY, ACCESSKEY_SECRET);
    }

    public static String uploadObject2OSS(byte[] file, String filename) {
        OSS ossClient = OssAliUtils.getConnection();
        try {
            ossClient.putObject(BUCKET, filename, new ByteArrayInputStream(file));
            return URL + filename;
        }finally {
            ossClient.shutdown();
        }
    }

    public static void deleteObject4OSS(String fileName) {
        OSS ossClient = OssAliUtils.getConnection();
        try {
            ossClient.deleteObject(BUCKET, fileName);
        }finally {
            ossClient.shutdown();
        }
    }

}
