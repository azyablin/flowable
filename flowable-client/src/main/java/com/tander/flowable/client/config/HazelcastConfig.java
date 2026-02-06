package com.tander.flowable.client.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class HazelcastConfig {

    @Bean
    public ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();

        // Подключаемся к отдельному серверу Hazelcast
        clientConfig.getNetworkConfig()
            .addAddress("localhost:5701") // отдельный сервер
            .setSmartRouting(true);

        clientConfig.setClusterName("dev");
      //  clientConfig.setInstanceName("my-client");



        return clientConfig;
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        return HazelcastClient.newHazelcastClient(clientConfig());
    }

}