package com.example.research.agent;

import com.example.research.llm.LlmClient;
import com.example.research.model.ResearchPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResearchPlannerAgent {

	private static final String SYSTEM_PROMPT = """
			You are Agent 1: Research Planner. Choose OPENAI_DOCS_MCP when official
			OpenAI documentation is the right source. Choose WEB for everything else.
			Decide from the meaning of the topic, not keyword matching. Return only a
			ResearchPlan and do not perform research.
			""";

	private final LlmClient llmClient;

	public ResearchPlan plan(String topic, int maxSources) {
		ResearchPlan generated = llmClient.generateStructured(
				SYSTEM_PROMPT,
				"Topic: %s%nMaximum sources: %d".formatted(topic, maxSources),
				ResearchPlan.class);
		return new ResearchPlan(topic, generated.searchSource(), maxSources);
	}
}
