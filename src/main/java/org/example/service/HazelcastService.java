package org.example.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple example service to demonstrate a Hazelcast map shared between the cluster
 */
@Service
public class HazelcastService {

    @Autowired
    HazelcastInstance hazelcastInstance;

    static final String MAP_ID = "hazelcast_map";

    IMap<String, String> getMap() {
        return hazelcastInstance.getMap(MAP_ID);
    }

    /**
     * Puts new key, value in map
     * @param key - key
     * @param value - value
     * @return returns old value from map if present
     */
    public String put(String key, String value) {
        return getMap().put(key, value);
    }

    /**
     * Returns value from map for specified key
     * @param key - key
     * @return String
     */
    public String get(String key) {
        return getMap().get(key);
    }

    /**
     * Clears map
     */
    public void clear() {
        getMap().clear();
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
