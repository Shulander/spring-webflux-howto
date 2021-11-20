package us.vicentini.webflux;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@Slf4j
@WireMockTest(httpPort = 8081)
class SetupTest {


    @Test
    void isWiremockWorking() {
        final var expectedBody = "Hello world!";
        // web client base configuration
        var webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        // get request
        var result = webClient
                .get()
                .uri("/hello/world")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //assertions
        assertNotNull(result);
        assertEquals(expectedBody, result);
    }


    @Test
    void isMockitoWorking() {
        var mySupplier = Mockito.mock(Supplier.class);
        when(mySupplier.get()).thenReturn("myMockedString");

        assertEquals("myMockedString", mySupplier.get());

        verify(mySupplier).get();
        verifyNoMoreInteractions(mySupplier);
    }
}
