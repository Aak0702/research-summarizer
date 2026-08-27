package com.example.research.model;

public record ResearchPlan(
		String topic,
		SearchSource searchSource,
		int maxSources) {
}
