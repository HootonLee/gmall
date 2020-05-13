package top.hootonlee.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hootonlee.gmall.util.RedisUtils;

/**
 * @author lihaotan
 */
@Configuration
public class RedisConfig {

    private static final String VALUE = "disabled";
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtils getRedisUtil() {
        if (VALUE.equals(host)) {
            return null;
        }
        RedisUtils redisUtils = new RedisUtils();
        redisUtils.initPool(host, port, database, password);
        return redisUtils;
    }

}
