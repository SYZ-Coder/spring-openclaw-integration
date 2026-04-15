package com.example.openclaw.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpenClawResponsesClient {

    private static final String RESPONSES_PATH = "/v1/responses";

    private final WebClient webClient;

    public OpenClawResponsesClient(WebClient openClawWebClient) {
        this.webClient = openClawWebClient;
    }

    public Mono<Map> createResponse(String input) {
        Map<String, Object> body = Map.of(
                "model", "openclaw/default",
                "input", input,
                "stream", false
        );

        return webClient.post()
                .uri(RESPONSES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), response -> mapUpstreamError(response, RESPONSES_PATH))
                .bodyToMono(Map.class);
    }

    public Flux<String> streamRawEvents(String input) {
        Map<String, Object> body = Map.of(
                "model", "openclaw/default",
                "input", input,
                "stream", true
        );

        return webClient.post()
                .uri(RESPONSES_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), response -> mapUpstreamError(response, RESPONSES_PATH))
                .bodyToFlux(String.class)
                .filter(line -> line != null && !line.isBlank())
                .map(String::trim)
                .filter(line -> !"data: [DONE]".equals(line));
    }

    private Mono<Throwable> mapUpstreamError(ClientResponse response, String path) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new OpenClawGatewayException(
                        response.statusCode().value(),
                        path,
                        sanitizeBody(body)
                ));
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
    }
}
