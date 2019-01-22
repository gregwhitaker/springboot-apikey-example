package springboot.apikey.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FooController {
    private static final Logger LOG = LoggerFactory.getLogger(FooController.class);

    @GetMapping(value = "/api/v1/foobar")
    public ResponseEntity foobar() {
        return ResponseEntity.ok().build();
    }
}
