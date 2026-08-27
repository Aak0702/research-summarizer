package com.example.research.mcp;

import com.example.research.llm.LlmClient;
import com.example.research.model.ResearchData;
import com.example.research.model.ResearchPlan;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class OpenAiDocsMcpClient {
	private static final String SYSTEM_PROMPT = """
			Use only the connected OpenAI Docs MCP tools. Return simple factual research
			text and the documentation URLs used. Do not write the final summary and do
			not use model memory.
			""";

	private final LlmClient llmClient;
	private final ToolCallbackProvider openAiDocsTools;

	public OpenAiDocsMcpClient(LlmClient llmClient, ToolCallbackProvider openAiDocsTools) {
		this.llmClient = llmClient;
		this.openAiDocsTools = openAiDocsTools;
	}

	public ResearchData research(ResearchPlan plan) {
		String prompt = "Topic: %s%nMaximum sources: %d"
				.formatted(plan.topic(), plan.maxSources());
		return llmClient.generateStructured(SYSTEM_PROMPT, prompt, ResearchData.class, openAiDocsTools);
	}
}
