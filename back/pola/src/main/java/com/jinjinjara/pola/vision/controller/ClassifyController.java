package com.jinjinjara.pola.vision.controller;

import com.jinjinjara.pola.vision.service.ClassifierService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/embedding")
public class ClassifyController {

    private final ClassifierService classifier;

    @PostMapping(
            value = "/classify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> classify(
            @RequestBody Req req,
            @RequestParam(defaultValue = "3") @Min(1) int topk
    ) {
        var result = classifier.classify(req.tags(), topk);
        return ResponseEntity.ok(result);
    }

    public record Req(@NotEmpty List<String> tags) {}
}