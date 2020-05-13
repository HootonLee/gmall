package top.hootonlee.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解代表是否必须登录才能访问的接口
 * loginSuccess() 代表有注解的方法但是登录成功与否都不会影响
 * @author lihaotan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    boolean loginSuccess() default true;
}
