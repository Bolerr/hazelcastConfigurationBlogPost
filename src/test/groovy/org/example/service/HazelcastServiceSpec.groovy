package org.example.service

import com.hazelcast.config.Config
import com.hazelcast.config.GroupConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.IMap
import spock.lang.Specification

class HazelcastServiceSpec extends Specification {

    HazelcastService service = Spy(HazelcastService)

    void setup() {
        service.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(new Config(instanceName: 'spockServiceTest', groupConfig: new GroupConfig(name: 'spockServiceTest')))
        service.map.clear()
    }

    void "GetMap"() {
        when:
        IMap<String, String> map = service.getMap()

        then:
        map != null
    }

    void "Put"() {
        given:
        String oldValue = 'server1'
        String newValue = 'server2'
        String key = 'processing'

        when: 'we first put in a value'
        String result = service.put(key, oldValue)

        then: 'there should be no old value'
        !result

        when: 'we add a new value for the key'
        result = service.put(key, newValue)

        then: 'we should get the existing value in the map back'
        result == oldValue
    }

    void "Get"() {
        given:
        String value = 'server2'
        String key = 'processing'
        service.put(key, value)

        when:
        String result = service.get(key)

        then:
        result == value
    }

    void 'clear should clear the map'() {
        given: 'values in the map'
        service.put('1', 'server1')
        service.put('2', 'server2')
        assert service.map.size() == 2

        when: 'we call clear'
        service.clear()

        then: 'the map should be empty'
        service.map.size() == 0
    }
}
