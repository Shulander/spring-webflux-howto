package us.vicentini.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.function.LongConsumer;

@Slf4j
public class ResponseTimeFilter implements ExchangeFilterFunction {
    private static final String EXTERNAL_CALL_EXCHANGER_FILTER = "EXTERNAL_CALL_EXCHANGER_FILTER.START_TIME";
    private final List<LongConsumer> listeners = new LinkedList<>();


    public void addListener(LongConsumer listener) {
        this.listeners.add(listener);
    }


    @Override
    @Nonnull
    public Mono<ClientResponse> filter(@Nonnull ClientRequest clientRequest, ExchangeFunction exchangeFunction) {
        return exchangeFunction
                .exchange(clientRequest)
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        Long startTime = signal.getContextView().get(EXTERNAL_CALL_EXCHANGER_FILTER);
                        ClientResponse clientResponse = signal.get();
                        long responseTime = System.currentTimeMillis() - startTime;

                        logRequestResponseTime(clientRequest, clientResponse, responseTime);
                        notifyListeners(responseTime);
                    }
                })
                .contextWrite(context -> context.put(EXTERNAL_CALL_EXCHANGER_FILTER, System.currentTimeMillis()));
    }


    private void logRequestResponseTime(ClientRequest clientRequest, ClientResponse clientResponse, long responseTime) {
        var method = clientRequest.method();
        var url = clientRequest.url();
        var logPrefix = clientRequest.logPrefix();
        var request = method + " " + url;
        var httpStatus = clientResponse == null ? null : clientResponse.statusCode();
        log.info("{}# Perf Log Response for {}: {}: time: {} ms", logPrefix, request,
                 httpStatus, responseTime);
    }


    private void notifyListeners(long responseTime) {
        for (LongConsumer listener : listeners) {
            listener.accept(responseTime);
        }
    }
}
