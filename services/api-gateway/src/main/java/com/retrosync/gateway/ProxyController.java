
package com.retrosync.gateway;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;


@RestController
public class ProxyController {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
        "connection", "keep-alive", "proxy-authenticate",
        "proxy-authorization", "te", "trailer",
        "transfer-encoding", "upgrade", "host",
        "content-length"
    );

    private final RestClient restClient;
    private final String userServiceUrl;
    private final String saveServiceUrl;

    public ProxyController(
            RestClient.Builder builder,
            @Value("${retrosync.services.user-url}") String userServiceUrl,
            @Value("${retrosync.services.save-url}") String saveServiceUrl) {

        this.restClient = builder.build();
        this.userServiceUrl = userServiceUrl;
        this.saveServiceUrl = saveServiceUrl;
    }

    @RequestMapping("/api/users/**")
    public ResponseEntity<byte[]> proxyUsers(HttpServletRequest request) {
        return forward(request, userServiceUrl);
    }

    @RequestMapping("/api/saves/**")
    public ResponseEntity<byte[]> proxySaves(HttpServletRequest request) {
        return forward(request, saveServiceUrl);
    }

    private ResponseEntity<byte[]> forward(
            HttpServletRequest request, String serviceUrl) {

        try {
            String query = request.getQueryString();

            URI destination = URI.create(
                serviceUrl + request.getRequestURI()
                    + (query == null ? "" : "?" + query)
            );

            byte[] body = request.getInputStream().readAllBytes();

            return restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(destination)
                .headers(headers -> {
                    copyRequestHeaders(request, headers);

                    // Eliminar Content-Length para que RestClient
                    // calcule la longitud del cuerpo reenviado.
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                })
                .body(body)
                .exchange((clientRequest, clientResponse) -> {

                    HttpHeaders responseHeaders = new HttpHeaders();
                    clientResponse.getHeaders().forEach((name, values) -> {
                        if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                            responseHeaders.put(name, values);
                        }
                    });

                    return ResponseEntity
                        .status(clientResponse.getStatusCode())
                        .headers(responseHeaders)
                        .body(clientResponse.getBody().readAllBytes());
                });

        } catch (IOException | IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No se pudo contactar con el microservicio",
                ex
            );
        }
    }

    private void copyRequestHeaders(
            HttpServletRequest request, HttpHeaders headers) {

        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                Collections.list(request.getHeaders(name))
                    .forEach(value -> headers.add(name, value));
            }
        });
    }
}
