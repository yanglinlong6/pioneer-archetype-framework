package com.glsx.plat.kafka.config;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author wangxw
 * @version 1.0.0
 * @ClassName KafkaProductorConfig.java
 * @description: obd kafka生产消息
 * @createTime 2020年10月20日 10:21:00
 */
@Configuration
public class KafkaProducerConfig {

    @Autowired
    private MqConfig mqConfig;

    @Bean("producer")
    public Producer getProducer() {
        return new Producer(new ProducerConfig(initProperties()));
    }

    private Properties initProperties() {
        Properties properties = new Properties();
        properties.setProperty("serializer.class", "kafka.serializer.DefaultEncoder");
        properties.setProperty("key.serializer.class", "kafka.serializer.DefaultEncoder");
        properties.setProperty("metadata.broker.list", mqConfig.getProducerServers());
        // 默认同步发送 配置就是异步发送
        properties.setProperty("producer.type", mqConfig.getProducerType());
        properties.setProperty("request.required.acks", "1");
        return properties;
    }

}
