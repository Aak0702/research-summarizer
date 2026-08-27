package com.example.research.agent;

import com.example.research.llm.LlmClient;
import com.example.research.model.ResearchData;
import com.example.research.model.ResearchResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SummarizerAgent {
	private static final String SYSTEM_PROMPT = """
			You are Agent 3: Summarizer. Create a clear ResearchResponse using only the
			provided research text. Do not invent facts. Do not add source URLs.
			""";

	private final LlmClient llmClient;

	public SummarizerAgent(LlmClient llmClient) {
		this.llmClient = llmClient;
	}

	public ResearchResponse summarize(ResearchData data) {
		ResearchResponse generated = llmClient.generateStructured(
				SYSTEM_PROMPT,
				"Topic: %s%nResearch text:%n%s".formatted(data.topic(), data.content()),
				ResearchResponse.class);

		return new ResearchResponse(
				data.topic(),
				data.searchSource().responseValue(),
				generated.executiveSummary(),
				generated.keyFindings() == null ? List.of() : generated.keyFindings(),
				generated.details(),
				data.sources());
	}
}
