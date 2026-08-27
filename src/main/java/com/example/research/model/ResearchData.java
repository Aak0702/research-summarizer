package com.example.research.model;

import java.util.List;

public record ResearchData(
		String topic,
		SearchSource searchSource,
		String content,
		List<String> sources) {
}
