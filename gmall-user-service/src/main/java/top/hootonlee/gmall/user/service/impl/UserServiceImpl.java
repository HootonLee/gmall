package top.hootonlee.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import top.hootonlee.gmall.constant.RedisConstant;
import top.hootonlee.gmall.entity.UmsMember;
import top.hootonlee.gmall.entity.UmsMemberReceiveAddress;
import top.hootonlee.gmall.service.UserService;
import top.hootonlee.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import top.hootonlee.gmall.user.mapper.UserMapper;
import top.hootonlee.gmall.util.RedisUtils;

import java.util.List;

/**
 * @author lihaotan
 */
@Service(interfaceClass = UserService.class)
@Transactional(rollbackFor = {})

public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<UmsMember> findAllUmsMember() {
        return userMapper.selectAll();
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress receiveAddresses = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return receiveAddresses;
    }

    @Override
    public List<UmsMemberReceiveAddress> getUserAddressList(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> receiveAddressList = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return receiveAddressList;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtils.getJedis();
            if (null != jedis) {
                // 查询缓存中信息
                String userInfo = jedis.get(RedisConstant.USER + umsMember.getUsername() + umsMember.getPassword() + RedisConstant.INFO);
                if (StringUtils.isNotBlank(userInfo)) {
                    UmsMember umsMember4Cache = JSON.parseObject(userInfo, UmsMember.class);
                    return umsMember4Cache;
                }
            }
            // redis连接失败， 启用数据库
            UmsMember umsMember4Db = CheckUser4Db(umsMember);
            if (null != umsMember4Db) {
                jedis.setex(RedisConstant.USER + umsMember4Db.getUsername() + umsMember4Db.getPassword() + RedisConstant.INFO, 60 * 60 * 24, JSON.toJSONString(umsMember4Db));
            }
            return umsMember4Db;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtils.getJedis();
            jedis.setex(RedisConstant.USER + memberId + RedisConstant.TOKEN, 60 * 60 * 2, token);
        } finally {
            jedis.close();
        }
    }

    private UmsMember CheckUser4Db(UmsMember umsMember) {
        List<UmsMember> umsMemberList = userMapper.select(umsMember);
        if (null != umsMemberList) {
            return umsMemberList.get(0);
        }
        return null;
    }

}
