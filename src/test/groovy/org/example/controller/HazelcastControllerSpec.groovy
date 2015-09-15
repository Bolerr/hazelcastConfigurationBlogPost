package org.example.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.domain.MapRequest
import org.example.service.HazelcastService
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.mock.web.MockServletContext
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringApplicationConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
class HazelcastControllerSpec extends Specification {

    MockMvc mvc

    HazelcastController hazelcastController
    HazelcastService hazelcastServiceMock = Mock()

    ObjectMapper jaxonObjectMapper

    void setup() throws Exception {
        hazelcastController = new HazelcastController()
        hazelcastController.hazelcastService = hazelcastServiceMock

        mvc = MockMvcBuilders.standaloneSetup(hazelcastController).build();

        jaxonObjectMapper = new ObjectMapper()
    }

    void "Get"() {
        when: 'we call get with key 1'
        MapRequest mapRequest = new MapRequest(key: '1', value: 'server1', oldValue: null)
        String expectedJson = jaxonObjectMapper.writeValueAsString(mapRequest)

        def response = mvc.perform(get("/api/map/1").accept(MediaType.APPLICATION_JSON)).andReturn().response

        then: 'we should see the service called with that key'
        1 * hazelcastServiceMock.get("1") >> {
            return 'server1'
        }
        response.contentAsString == expectedJson
    }

    void "Put"() {
        when: 'object is posted'
        MapRequest mapRequest = new MapRequest(key: '1', value: '2', oldValue: 'server1')
        String expectedJson = jaxonObjectMapper.writeValueAsString(mapRequest)

        def createResponse = mvc.perform(post('/api/map?key=1&value=2').contentType(MediaType.APPLICATION_JSON)).andReturn().response

        then: 'object was put in the map'
        1 * hazelcastServiceMock.put('1', '2') >> {
            return 'server1'
        }
        createResponse.contentAsString == expectedJson
    }

    void "clear"() {
        when: 'clear is called'
        mvc.perform(get('/api/map/clear').contentType(MediaType.APPLICATION_JSON))

        then: 'we call .clear on the service'
        1 * hazelcastServiceMock.clear()
    }
}


