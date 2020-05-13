package top.hootonlee.gmall.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * @author lihaotan
 */
public class RabbitUtils {


    private ConnectionFactory connectionFactory;


    public void init(String host, Integer port, String username, String password, String virtualHost){
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
    }

    public Connection getConnection() {
        try {
            return connectionFactory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeChannelAndConnection(Channel channel, Connection connection) {
        try {
            if (null != channel) {
                channel.close();
            }
            if (null != connection) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}