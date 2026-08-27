package com.example.research.model;

public enum SearchSource {
	OPENAI_DOCS_MCP("MCP"),
	WEB("WEB");

	private final String responseValue;

	SearchSource(String responseValue) {
		this.responseValue = responseValue;
	}

	public String responseValue() {
		return responseValue;
	}
}
