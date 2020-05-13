package top.hootonlee.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.hootonlee.gmall.entity.UmsMember;
import top.hootonlee.gmall.service.UserService;
import top.hootonlee.gmall.util.JwtUtil;
import top.hootonlee.gmall.util.Md5Util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lihaotan
 */
@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, String currentIp) {
        Map<String, Object> map = new HashMap<>(3);
        Map<String, Object> decode = JwtUtil.decode(token, "issimplegmall", currentIp);
        if (null != decode) {

            map.put("status", "success");
            map.put("memberId", decode.get("memberId"));
            map.put("nickname", decode.get("nickname"));
        } else {
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        umsMember.setPassword(Md5Util.code(umsMember.getPassword()));
        UmsMember umsMemberLogin = userService.login(umsMember);

        String token = "";
        if (null != umsMemberLogin) {
            // 登录成功
            // 使用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> map = new HashMap<>(2);
            map.put("memberId", memberId);
            map.put("nickname", nickname);
            // 通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                // 从request获得
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            // 生成token
            token = JwtUtil.encode("issimplegmall", map, ip);
            // 将token存Redis
            userService.addUserToken(token, memberId);
        } else {
            // 登录失败
            return "fail";
        }
        return token;
    }

    @RequestMapping("/index")
    public String index(String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        return "index";
    }


}
