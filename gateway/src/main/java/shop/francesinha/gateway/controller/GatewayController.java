package shop.francesinha.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GatewayController {

    @Autowired
    private GatewayService service;

    @GetMapping("/products")
    public Mono<ResponseEntity<?>> getValuesMono() {
        return service.fetchProducts();
    }
}