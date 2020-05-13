package top.hootonlee.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hootonlee.gmall.entity.UmsMember;
import top.hootonlee.gmall.service.UserService;

import java.util.List;

/**
 * @author lihaotan
 */
@RestController
public class UserController {


    @Reference
    private UserService userService;

    @RequestMapping("/getAllUmsMember")
    public List<UmsMember> getAllUmsMember() {
        List<UmsMember> umsMembers = userService.findAllUmsMember();
        return umsMembers;
    }

}