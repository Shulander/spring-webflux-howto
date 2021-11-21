package us.vicentini.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                .bodyToMono(Employee.class)
                .block();

        //assertions
        assertNotNull(employee);
        assertEquals(123L, employee.id());
        assertEquals("John Doe", employee.name());
        assertEquals(new Employee(123L, "John Doe"), employee);
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
                .bodyToFlux(Employee.class)
                .collectList()
                .block();

        //assertions
        assertNotNull(employees);
        assertFalse(employees.isEmpty());
        assertEquals(2, employees.size());
        assertEquals(new Employee(123L, "John Doe"), employees.get(0));
        assertEquals(new Employee(456L, "Jane Doe"), employees.get(1));
    }
}
