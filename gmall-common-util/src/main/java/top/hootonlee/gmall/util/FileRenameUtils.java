package top.hootonlee.gmall.util;


import java.util.UUID;

/**
 * @author lihaotan
 */
public class FileRenameUtils {

    public static String FileRename4UUID(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (-1 == index) {
            return UUID.randomUUID().toString().toLowerCase();
        }
        return UUID.randomUUID().toString().replace("-", "").toLowerCase() + fileName.substring(index);
    }

}
