package com.jinjinjara.pola.search.controller;

import com.jinjinjara.pola.search.model.FileSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final com.jinjinjara.pola.opensearch.service.FileSearchService service;

    @PostMapping
    public void save(@RequestBody FileSearch file) throws IOException {
        service.save(file);
    }

    @GetMapping("/{id}")
    public FileSearch get(@PathVariable Long id) throws IOException {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws IOException {
        service.delete(id);
    }

    @GetMapping("/user/{userId}")
    public List<FileSearch> byUser(@PathVariable Long userId) throws IOException {
        return service.searchByUserId(userId);
    }

    @GetMapping("/category")
    public List<FileSearch> byCategory(@RequestParam String q) throws IOException {
        return service.searchByCategory(q);
    }
}