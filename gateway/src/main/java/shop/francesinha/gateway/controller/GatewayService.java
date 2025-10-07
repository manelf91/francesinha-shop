package shop.francesinha.gateway.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GatewayService {
    private final RouteLocator routeLocator;

    public GatewayService(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Retry(name = "serviceRetry", fallbackMethod = "fallback_Retry")
    @CircuitBreaker(name = "serviceCircuitBreaker", fallbackMethod = "fallback_CB")
    public Mono<ResponseEntity<?>> fetchProducts() {
        Mono<String> productEndpointMono = getEndpointUrl("products");

        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(jwtAuth -> jwtAuth.getToken().getTokenValue())
            .flatMap(token ->
                productEndpointMono.flatMap(endpointUrl -> {
                    String serviceUrl = endpointUrl + "/products";
                    System.out.println("Making request to " + serviceUrl + " with token at " + java.time.LocalDateTime.now());

                    WebClient client = WebClient.create();
                    return client.get()
                        .uri(serviceUrl)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(body -> ResponseEntity
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body)
                        );
                })
            );
    }

    public Mono<ResponseEntity<?>> fallback_Retry(RuntimeException e) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Service is currently unavailable. Please try again later."));
    }

    public Mono<String> fallback_CB(Throwable e) {
        throw new RuntimeException(e.getMessage());
    }

    private Mono<String> getEndpointUrl(String serviceName) {
        return routeLocator.getRoutes()
                .filter(route -> route.getId().contains(serviceName))
                .next()
                .map(route -> route.getUri().toString());
    }
}