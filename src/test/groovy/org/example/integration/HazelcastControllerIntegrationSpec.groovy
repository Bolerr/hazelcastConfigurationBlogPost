package org.example.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.domain.MapRequest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

/**
 * This class uses the fact that the integrationTests gradle task spins up two different Jetty servers
 * to test that we set both those servers in the same cluster
 *
 * SERVER_PORT=8100 gradle clean bootRun -Dspring.profiles.active=dev1
 * SERVER_PORT=8200 gradle clean bootRun -Dspring.profiles.active=dev1
 */
class HazelcastControllerIntegrationSpec extends Specification {

    RestTemplate restTemplate = new RestTemplate()
    ObjectMapper objectMapper = new ObjectMapper()

    String server1Url = "http://localhost:8100/hazelcastConfigurationBlogPost";
    String server2Url = "http://localhost:8200/hazelcastConfigurationBlogPost";

    void "test hazelcast"() {
        given:
        //clear map
        restTemplate.postForLocation(buildClearURI(server1Url), null);
        String key = '1'
        String firstValue = 'server1'
        String secondValue = 'server2'

        when: 'we call server1 with a post request to add something into the map'
        MapRequest request = post(server1Url, key, firstValue)

        then: 'that value should get set in the hazelcast map'
        request.key == key
        request.value == firstValue
        request.oldValue == null

        when: 'we call server2 with a get request'
        request = get(server2Url, key)

        then: 'a rest call to server2 should retrieve that value from the map'
        request.key == key
        request.value == firstValue

        when: 'we post a new value to server2'
        request = post(server2Url, key, secondValue)

        then: 'we should see server1 as the oldValue'
        request.key == key
        request.oldValue == firstValue
        request.value == secondValue

        when: 'we call server1 with a get'
        request = get(server1Url, key)

        then: 'we should get that new value for the key from server1'
        request.key == key
        request.value == secondValue
    }

    URI buildClearURI(String server) {
        return UriComponentsBuilder.fromUriString(server).path("/api/map/clear").build().toUri()
    }

    URI buildGetURI(String server, String key) {
        return UriComponentsBuilder.fromUriString(server).path("/api/map/${key}").build().toUri()
    }

    URI buildPostURI(String server, String key, String value) {
        return UriComponentsBuilder.fromUriString(server).path("/api/map").queryParam("key", key).queryParam("value", value).build().toUri()
    }

    MapRequest get(String server, String key) {

        ParameterizedTypeReference<MapRequest> responseType = new ParameterizedTypeReference<MapRequest>() {};

        ResponseEntity<MapRequest> responseEntity =  restTemplate.exchange(buildGetURI(server, key), HttpMethod.GET, null, responseType);
        return responseEntity.body
    }


    MapRequest post(String server, String key, String value) {

        ParameterizedTypeReference<MapRequest> responseType = new ParameterizedTypeReference<MapRequest>() {};

        ResponseEntity<MapRequest> responseEntity = restTemplate.exchange(buildPostURI(server, key, value), HttpMethod.POST, null, responseType);
        return responseEntity.body
    }
}
