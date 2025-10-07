package shop.francesinha.gateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable) // Not needed for stateless APIs
            .authorizeExchange(auth -> auth
                    .pathMatchers("/auth/**").permitAll() // allow login/signup
                    .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                    .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // enable JWT validation

        return http.build();
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

    // In-memory rate limiter bean
    @Bean
    public RateLimiter inMemoryRateLimiter() {
        return new InMemoryRateLimiter();
    }

    // Custom in-memory RateLimiter
    static class InMemoryRateLimiter implements RateLimiter {

        private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
        private final int LIMIT = 5;                // max requests
        private final long RESET_INTERVAL_MS = 60_000; // reset every 1 minute

        public InMemoryRateLimiter() {
            // background thread to reset counters
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(RESET_INTERVAL_MS);
                        counts.clear();
                    }
                } catch (InterruptedException ignored) {
                }
            }).start();
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            counts.putIfAbsent(id, new AtomicInteger(0));
            boolean allowed = counts.get(id).incrementAndGet() <= LIMIT;
            if (!allowed) counts.get(id).decrementAndGet();
            return Mono.just(new Response(allowed, Collections.emptyMap()));
        }

        @Override
        public Map getConfig() {
            return Map.of();
        }

        @Override
        public Class getConfigClass() {
            return Object.class; // or your custom config class if needed
        }

        @Override
        public Object newConfig() {
            return new Object(); // dummy config
        }
    }
}