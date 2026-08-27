package com.example.research.llm;

import org.springframework.ai.tool.ToolCallbackProvider;

public interface LlmClient {
	String generate(String prompt);

	<T> T generateStructured(String systemPrompt, String prompt, Class<T> responseType);

	<T> T generateStructured(
			String systemPrompt,
			String prompt,
			Class<T> responseType,
			ToolCallbackProvider tools);
}
