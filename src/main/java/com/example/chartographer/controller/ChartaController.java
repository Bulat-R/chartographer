package com.example.chartographer.controller;

import com.example.chartographer.config.Config;
import com.example.chartographer.service.ChartaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/chartas")
@RequiredArgsConstructor
@Validated
public class ChartaController {

    private final ChartaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestParam @Min(1) @Max(20000) int width,
                         @RequestParam @Min(1) @Max(50000) int height) {

        return service.create(width, height);
    }

    @PostMapping(value = "/{id}", consumes = "image/bmp")
    public void save(@PathVariable String id,
                     @RequestParam @Min(0) @Max(20000) int x,
                     @RequestParam @Min(0) @Max(50000) int y,
                     @RequestParam @Min(1) @Max(5000) int width,
                     @RequestParam @Min(1) @Max(5000) int height,
                     HttpServletRequest request) throws IOException {

        service.checkId(id);
        service.save(id, x, y, width, height, request.getInputStream());

    }

    @GetMapping(value = "/{id}", produces = "image/bmp")
    public ByteArrayResource get(@PathVariable String id,
                                 @RequestParam @Min(0) @Max(20000) int x,
                                 @RequestParam @Min(0) @Max(50000) int y,
                                 @RequestParam @Min(1) @Max(5000) int width,
                                 @RequestParam @Min(1) @Max(5000) int height) throws IOException {

        service.checkId(id);
        return service.get(id, x, y, width, height);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws IOException {
        service.checkId(id);
        service.delete(Config.pathToContent + File.separator + id);
    }
}
