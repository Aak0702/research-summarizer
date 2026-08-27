package com.example.research.controller;

import com.example.research.model.ResearchRequest;
import com.example.research.model.ResearchResponse;
import com.example.research.service.ResearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResearchController {

    private final ResearchService researchService;

    @PostMapping("/summarize")
    public ResponseEntity<ResearchResponse> summarize(@Valid @RequestBody ResearchRequest request) {
        return ResponseEntity.ok(researchService.research(request));
    }
}
