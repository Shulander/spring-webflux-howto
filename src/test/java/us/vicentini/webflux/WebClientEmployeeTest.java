package us.vicentini.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import us.vicentini.webflux.entity.Employee;
import us.vicentini.webflux.exception.EmployeeException;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
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


    @Test
    void shouldHandle404Response() {
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/404")
                .retrieve()
                .bodyToMono(Employee.class);

        //assertions
        StepVerifier.create(employee)
                .expectError(WebClientResponseException.NotFound.class)
                .verify();
    }


    @Test
    void shouldConvert404ToAppException() {
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var employee = webClient
                .get()
                .uri("/employees/404")
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                          clientResponse -> Mono.just(new EmployeeException("Client Not Found Error")))
                .bodyToMono(Employee.class);

        //assertions
        StepVerifier.create(employee)
                .expectErrorMatches(throwable -> throwable instanceof EmployeeException &&
                                                 throwable.getMessage().equals("Client Not Found Error"))
                .verify();
    }


    @Test
    void shouldGetResponseEntityWithSingleObject() {
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var entity = webClient
                .get()
                .uri("/employees/123")
                .retrieve()
                .toEntity(Employee.class)
                .block();

        //assertions
        assertNotNull(entity);
        var status = entity.getStatusCode();
        var cookie = getCookie(entity.getHeaders(), "SESSION");
        var employee = entity.getBody();
        assertNotNull(status);
        assertNotNull(cookie);
        assertNotNull(employee);
        assertEquals(HttpStatus.OK, status);
        assertEquals("SESSION", cookie.getName());
        assertEquals("GPUTWPm4dJTyHbrun3HSbm", cookie.getValue());
        assertEquals(new Employee(123L, "John Doe"), employee);
    }


    private HttpCookie getCookie(HttpHeaders headers, String cookieName) {
        return Objects.requireNonNull(headers.get(HttpHeaders.SET_COOKIE))
                .stream()
                .map(HttpCookie::parse)
                .flatMap(Collection::stream)
                .filter(httpCookie -> httpCookie.getName().equals(cookieName))
                .findFirst().orElse(null);
    }

}
