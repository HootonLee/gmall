package top.hootonlee.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import top.hootonlee.gmall.annotations.LoginRequired;
import top.hootonlee.gmall.constant.InterceptorConstant;
import top.hootonlee.gmall.util.CookieUtil;
import top.hootonlee.gmall.util.HttpclientUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lihaotan
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) o;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);

        // 方法没有@LoginRequired注解放行
        if (null == methodAnnotation) {
            return true;
        }

        /**
         * 逻辑为，初始token 当old token，new token 都为空时，token 无值，表示用户没有进行登录
         * 当cookie中有原始的token表示用户之前登录过，将old token赋值
         * 如果old token为空，表示之前没有登录过，new token不为空，表示刚进行登录过，将new token赋值
         * 如果old token不为空，之前登录过， new token 不为空，刚进行登录，token 赋值为最新。
         */
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        // 此方法是否为必须登录访问
        boolean loginSuccess = methodAnnotation.loginSuccess();

        String success = "fail";
        Map<String, String> successMap = new HashMap<>(3);
        // 调用认证中心进行验证
        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            String jsonSuccess = HttpclientUtil.doGet(InterceptorConstant.PASS_VERIFY_T + token + "&currentIp=" + ip);
            successMap = JSON.parseObject(jsonSuccess, Map.class);
            success = (String) successMap.get("status");
        }

        if (loginSuccess) {
            // 必须登录通过验证才能访问
            if (!InterceptorConstant.SUCCESS.equals(success)) {
                // 验证失败，调回登录页面
                StringBuffer url = request.getRequestURL();
                response.sendRedirect(InterceptorConstant.PASS_RETURN_URL + url);
                return false;
            }
            // 需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            if (StringUtils.isNotBlank(token)) {
                //验证通过，覆盖cookie中的token
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }
        } else {
            // 不登录也可以访问，但需要验证
            if (InterceptorConstant.SUCCESS.equals(success)) {
                // 需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));

                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)) {
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }
        }
        return true;
    }

}
