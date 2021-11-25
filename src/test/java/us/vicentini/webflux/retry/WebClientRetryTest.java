package us.vicentini.webflux.retry;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;
import us.vicentini.webflux.entity.Employee;
import us.vicentini.webflux.filter.ResponseTimeFilter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@WireMockTest(httpPort = 8081)
class WebClientRetryTest {

    @Test
    void shouldRetryUntilGetResult() {
        // instantiate a new filter
        var filter = new ResponseTimeFilter();
        var requestCount = new AtomicInteger();
        filter.addListener(value -> requestCount.incrementAndGet());


        // set a timeout to force a retry
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofMillis(5));

        // web client base configuration
        var webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8081")
                .filter(filter)
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/123")
                .retrieve()
                .bodyToMono(Employee.class)
                .retry();

        // assertions
        StepVerifier.create(employee)
                .expectNext(new Employee(123L, "John Doe"))
                .verifyComplete();

        assertTrue(requestCount.get() > 1);
    }


    @Test
    void shouldMaxRetryFor404() {
        // instantiate a new filter
        var filter = new ResponseTimeFilter();
        var requestCount = new AtomicInteger();
        filter.addListener(value -> requestCount.incrementAndGet());


        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .filter(filter)
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/404")
                .retrieve()
                .bodyToMono(Employee.class)
                .retry(5);

        // assertions
        StepVerifier.create(employee)
                .expectError(WebClientResponseException.NotFound.class)
                .verify();

        // 1 first call + 5 retries
        assertEquals(6, requestCount.get());
    }


}
