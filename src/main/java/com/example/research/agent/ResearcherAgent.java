package com.example.research.agent;

import com.example.research.llm.LlmClient;
import com.example.research.mcp.OpenAiDocsMcpClient;
import com.example.research.model.ResearchData;
import com.example.research.model.ResearchPlan;
import com.example.research.model.SearchSource;
import com.example.research.search.WebSearchClient;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResearcherAgent {
	private static final String EXTRACTION_PROMPT = """
			You are Agent 2: Researcher. Turn the supplied web-search output into short,
			factual notes about the topic. Use only the supplied text. Do not write the
			final summary or add facts from memory.
			""";

	private final LlmClient llmClient;
	private final OpenAiDocsMcpClient openAiDocsMcpClient;
	private final WebSearchClient webSearchClient;

	public ResearcherAgent(
			LlmClient llmClient,
			OpenAiDocsMcpClient openAiDocsMcpClient,
			WebSearchClient webSearchClient) {
		this.llmClient = llmClient;
		this.openAiDocsMcpClient = openAiDocsMcpClient;
		this.webSearchClient = webSearchClient;
	}

	public ResearchData research(ResearchPlan plan) {
		ResearchData data = switch (plan.searchSource()) {
			case OPENAI_DOCS_MCP -> openAiDocsMcpClient.research(plan);
			case WEB -> researchWeb(plan);
		};
		List<String> sources = data.sources() == null
				? List.of()
				: new LinkedHashSet<>(data.sources()).stream().limit(plan.maxSources()).toList();
		return new ResearchData(plan.topic(), plan.searchSource(), data.content(), sources);
	}

	private ResearchData researchWeb(ResearchPlan plan) {
		WebSearchClient.WebSearchResult raw = webSearchClient.search(plan);
		String notes = llmClient.generate(EXTRACTION_PROMPT + "\nTopic: " + plan.topic()
				+ "\nWeb-search output:\n" + raw.content());
		return new ResearchData(plan.topic(), SearchSource.WEB, notes, raw.sources());
	}
}
