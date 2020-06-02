package com.gederin.config;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    public static final String ELECTION_NODE = "/election";
    public static final String LIVE_NODES = "/live_nodes";
    public static final String ALL_NODES = "/all_nodes";

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    @Bean
    public ZkClient zkClient() {
        return new ZkClient("localhost:2181", 12000, 3000);
    }

    public String getHostPort() {
        return host + ":" + port;
    }
}
