package com.example.research.model;

import java.util.List;

public record ResearchResponse(
		String topic,
		String searchSource,
		String executiveSummary,
		List<String> keyFindings,
		String details,
		List<String> sources) {
}
