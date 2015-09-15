package org.example.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Hazelcast instance, sets up cluster members, etc...
 */
@Configuration
@ComponentScan(basePackages = "org.example")
public class HazelcastConfiguration {

    private Logger logger = LoggerFactory.getLogger(HazelcastConfiguration.class);

    @Value("${hazelcast.group.name}")
    String hazelCastGroupName;

    @Value("${hazelcast.cluster.members}")
    String clusterMembers;

    @Value("${hazelcast.management.enabled}")
    public boolean hazelcastManagementEnabled;

    @Value("${hazelcast.management.url}")
    String hazelcastManagementUrl;

    /**
     * Configure the hazelcast config
     * - set name of group config to establish hazelcast cluster name
     * - setup networking
     *
     * @return Config
     */
    Config configureHazelcast() {
        Config config = new XmlConfigBuilder().build();
        config.setInstanceName("example_hazelcast_instance");
        config.getGroupConfig().setName(hazelCastGroupName);

        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();

        //Turn off multicast because client environment didn't support
        MulticastConfig multicastConfig = joinConfig.getMulticastConfig();
        multicastConfig.setEnabled(false);

        //Set cluster members from properties file for the active spring profile
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);

        logger.info("clusterMembers: {}", clusterMembers);
        String[] servers = clusterMembers.split(";");
        for (String server : servers) {
            tcpIpConfig.addMember(server);
        }

        logger.info("Hazelcast Registered servers: {}", tcpIpConfig.getMembers());

        //Set hazelcast management center settings
        ManagementCenterConfig managementCenterConfig = config.getManagementCenterConfig();
        managementCenterConfig.setEnabled(hazelcastManagementEnabled);
        managementCenterConfig.setUrl(hazelcastManagementUrl);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance() {
        logger.info("hazelcastInstance() called");
        return Hazelcast.getOrCreateHazelcastInstance(configureHazelcast());
    }
}
