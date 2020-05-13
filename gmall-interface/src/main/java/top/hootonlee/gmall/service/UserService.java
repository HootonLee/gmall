package top.hootonlee.gmall.service;

import top.hootonlee.gmall.entity.UmsMember;
import top.hootonlee.gmall.entity.UmsMemberReceiveAddress;

import java.util.List;

/**
 * @author lihaotan
 */

public interface UserService {

    /**
     * 查询所有
     * @return
     */
    List<UmsMember> findAllUmsMember();

    /**
     * 登录
     * @param umsMember
     * @return
     */
    UmsMember login(UmsMember umsMember);

    /**
     * 保存token Redis
     * @param token
     * @param memberId
     */
    void addUserToken(String token, String memberId);

    /**
     * 查询地址信息
     * @param memberId
     * @return
     */
    List<UmsMemberReceiveAddress> getUserAddressList(String memberId);

    /**
     * 查询地址
     * @param receiveAddressId
     * @return
     */
    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
