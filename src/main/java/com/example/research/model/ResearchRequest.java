package com.example.research.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ResearchRequest(
		@NotBlank(message = "topic must not be blank") String topic,
		@Min(value = 1, message = "maxSources must be at least 1")
		@Max(value = 20, message = "maxSources must not exceed 20") int maxSources) {
}
