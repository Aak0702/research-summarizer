package com.example.research.search;

import com.example.research.model.ResearchPlan;
import java.util.List;

public interface WebSearchClient {

	WebSearchResult search(ResearchPlan plan);

	record WebSearchResult(String content, List<String> sources) {
	}
}
