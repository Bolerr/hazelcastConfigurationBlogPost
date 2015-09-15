package org.example.config

import com.hazelcast.config.Config
import spock.lang.Specification

class HazelcastConfigurationSpec extends Specification {

    HazelcastConfiguration hazelcastConfiguration = new HazelcastConfiguration()

    void 'configureHazelcast() should properly configure the hazelcast Config'() {
        given:
        hazelcastConfiguration.hazelCastGroupName = 'cluster-name'
        hazelcastConfiguration.clusterMembers = 'localhost'
        hazelcastConfiguration.hazelcastManagementEnabled = false
        hazelcastConfiguration.hazelcastManagementUrl = 'http://localhost:8080/mancenter'

        when:
        Config config = hazelcastConfiguration.configureHazelcast()

        then: 'name of group config is set'
        config.getGroupConfig().name == 'cluster-name'

        then: 'network is configured correctly'
        config.getNetworkConfig().getJoin().getMulticastConfig().enabled == false
        config.getNetworkConfig().getJoin().tcpIpConfig.isEnabled()
        config.getNetworkConfig().getJoin().tcpIpConfig.members.contains('localhost')
        config.managementCenterConfig.isEnabled() == false
        config.managementCenterConfig.url == 'http://localhost:8080/mancenter'

        when: 'multiple cluster members are supplied'
        hazelcastConfiguration.clusterMembers = 'devGroup1Server1;devGroup1Server2'
        config = hazelcastConfiguration.configureHazelcast()

        then: 'each cluster member is registered'
        config.getNetworkConfig().getJoin().tcpIpConfig.members.contains('devGroup1Server1')
        config.getNetworkConfig().getJoin().tcpIpConfig.members.contains('devGroup1Server2')
    }
}
