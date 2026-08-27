package com.example.research.search;

import com.example.research.model.ResearchPlan;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiWebSearchClient implements WebSearchClient {
	private final RestClient restClient;
	private final String model;

	public GeminiWebSearchClient(
			@Value("${research.web-search.base-url}") String baseUrl,
			@Value("${spring.ai.google.genai.api-key}") String apiKey,
			@Value("${research.web-search.model}") String model) {
		this.restClient = RestClient.builder().baseUrl(baseUrl)
				.defaultHeader("x-goog-api-key", apiKey)
				.build();
		this.model = model;
	}

	@Override
	public WebSearchResult search(ResearchPlan plan) {
		Map<String, Object> request = Map.of(
				"contents", List.of(Map.of(
						"parts", List.of(Map.of(
								"text", "Find reliable information about: " + plan.topic()
										+ ". Use no more than " + plan.maxSources() + " sources.")))),
				"tools", List.of(Map.of("google_search", Map.of())));

		JsonNode response = restClient.post()
				.uri("/v1beta/models/{model}:generateContent", model)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(JsonNode.class);

		if (response == null) {
			throw new IllegalStateException("Gemini web search returned an empty response");
		}

		List<String> text = new ArrayList<>();
		Set<String> sources = new LinkedHashSet<>();
		for (JsonNode candidate : response.path("candidates")) {
			for (JsonNode part : candidate.path("content").path("parts")) {
				if (part.hasNonNull("text")) {
					text.add(part.get("text").asText());
				}
			}
			for (JsonNode chunk : candidate.path("groundingMetadata").path("groundingChunks")) {
				if (chunk.path("web").hasNonNull("uri")) {
					sources.add(chunk.path("web").get("uri").asText());
				}
			}
		}

		if (text.isEmpty()) {
			throw new IllegalStateException("Gemini web search returned no content");
		}

		return new WebSearchResult(
				String.join("\n", text),
				sources.stream().limit(plan.maxSources()).toList());
	}
}
