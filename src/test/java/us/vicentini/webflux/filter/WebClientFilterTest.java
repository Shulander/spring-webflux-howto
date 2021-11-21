package us.vicentini.webflux.filter;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import us.vicentini.webflux.entity.Employee;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@WireMockTest(httpPort = 8081)
class WebClientFilterTest {

    @Test
    void shouldRegisterResponseTimeFilter() {
        // instantiate a new filter
        var filter = new ResponseTimeFilter();
        var responseTime = new AtomicLong();
        filter.addListener(responseTime::set);

        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                // register filter
                .filter(filter)
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/123")
                .retrieve()
                .bodyToMono(Employee.class);

        // assertions
        StepVerifier.create(employee)
                .expectNext(new Employee(123L, "John Doe"))
                .verifyComplete();
        // verify the response time is greater than the initial value
        assertTrue(responseTime.get() > 0);
    }


}
