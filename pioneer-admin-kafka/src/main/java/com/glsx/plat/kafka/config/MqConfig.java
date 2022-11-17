package com.glsx.plat.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangxw
 * @version 1.0.0
 * @ClassName KafkaProductorConfig.java
 * @description: ${description}
 * @createTime 2020年10月20日 10:21:00
 */
@Data
@Configuration
@ConfigurationProperties("mq.kafka")
public class MqConfig {

    /**
     * kafka 生产者服务端地址
     */
    private String producerServers;

    /**
     * kafka 同步和异步发送消息配置  async:异步 sync:同步
     */
    private String producerType;

    /**
     * 围栏规则数据发送 topic
     */
    private String topic;

}
