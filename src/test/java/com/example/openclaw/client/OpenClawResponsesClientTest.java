package com.example.openclaw.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenClawResponsesClientTest {

    @Test
    void createResponseMapsUpstream500ToStructuredException() {
        ExchangeFunction exchangeFunction = request -> reactor.core.publisher.Mono.just(
                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .body("gateway exploded")
                        .build()
        );
        WebClient webClient = WebClient.builder()
                .baseUrl("http://openclaw.example")
                .exchangeFunction(exchangeFunction)
                .build();

        OpenClawResponsesClient client = new OpenClawResponsesClient(webClient);

        assertThatThrownBy(() -> client.createResponse("ping").block())
                .isInstanceOf(OpenClawGatewayException.class)
                .hasMessageContaining("POST /v1/responses")
                .hasMessageContaining("500")
                .hasMessageContaining("gateway exploded");
    }
}
