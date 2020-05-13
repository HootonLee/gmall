package top.hootonlee.gmall.manage.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import top.hootonlee.gmall.entity.PmsBaseAttrInfo;

import java.util.List;
import java.util.Set;

/**
 * @author lihaotan
 */
public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {

    /**
     * 查询
     * @param valueIdStr
     * @return
     */
    List<PmsBaseAttrInfo> selectAttrListByValueId(@Param("valueIdStr") String valueIdStr);
}
