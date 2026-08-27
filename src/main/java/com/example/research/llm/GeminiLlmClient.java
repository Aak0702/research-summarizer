package com.example.research.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class GeminiLlmClient implements LlmClient {

	private final ChatClient chatClient;

	public GeminiLlmClient(ChatModel chatModel) {
		this.chatClient = ChatClient.builder(chatModel).build();
	}

	@Override
	public String generate(String prompt) {
		return chatClient.prompt().user(prompt).call().content();
	}

	@Override
	public <T> T generateStructured(String systemPrompt, String prompt, Class<T> responseType) {
		return chatClient.prompt().system(systemPrompt).user(prompt).call()
				.entity(responseType, ChatClient.EntityParamSpec::validateSchema);
	}

	@Override
	public <T> T generateStructured(
			String systemPrompt,
			String prompt,
			Class<T> responseType,
			ToolCallbackProvider tools) {
		return chatClient.prompt().system(systemPrompt).user(prompt).tools(tools).call()
				.entity(responseType, ChatClient.EntityParamSpec::validateSchema);
	}
}
