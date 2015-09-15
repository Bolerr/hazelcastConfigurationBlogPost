package org.example.controller;

import org.example.domain.MapRequest;
import org.example.service.HazelcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Simple example controller for showing off hazelcast, gets / puts values in shared hazelcast map
 */
@RestController
@RequestMapping(value = "/api/map")
public class HazelcastController {

    Logger logger = LoggerFactory.getLogger(HazelcastController.class);

    @Autowired
    HazelcastService hazelcastService;

    @ResponseBody
    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    public MapRequest get(@PathVariable("key") String key) {
        MapRequest request = new MapRequest();
        request.setKey(key);
        request.setValue(hazelcastService.get(key));
        return request;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public MapRequest put(@RequestParam("key") String key, @RequestParam("value") String value) {
        MapRequest request = new MapRequest();
        request.setKey(key);
        request.setValue(value);
        request.setOldValue(hazelcastService.put(key, value));
        return request;
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public void clear() {
        hazelcastService.clear();
    }

    public void setHazelcastService(HazelcastService hazelcastService) {
        this.hazelcastService = hazelcastService;
    }
}
