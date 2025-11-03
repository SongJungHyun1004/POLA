package com.jinjinjara.pola.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Favorite API", description = "즐겨찾기 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteController {


}
