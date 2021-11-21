package us.vicentini.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@WireMockTest(httpPort = 8081)
class WebClientEmployeeTest {
    @Test
    void shouldGetSingleObject() {
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/123")
                .retrieve()
                .bodyToMono(Employee.class);

        //assertions
        StepVerifier.create(employee)
                .expectNext(new Employee(123L, "John Doe"))
                .verifyComplete();
    }


    @Test
    void shouldGetCollectionObject() {
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var employees = webClient
                .get()
                .uri("/employees")
                .retrieve()
                .bodyToFlux(Employee.class);

        //assertions
        StepVerifier.create(employees)
                .expectNext(new Employee(123L, "John Doe"))
                .expectNext(new Employee(456L, "Jane Doe"))
                .verifyComplete();
    }
}
