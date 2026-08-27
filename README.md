# Research Service

A Spring Boot service that turns a topic into a sourced research summary. It uses exactly three specialized agents in sequence and routes research to either the OpenAI Docs MCP server or web search based on the planner's understanding of the request.

## Requirements

- Java 17 or newer
- A Gemini API key

## Configuration

Set environment variables before starting the service.

```bash
export GEMINI_API_KEY="your-api-key"
```

Optional model overrides:

```bash
export GEMINI_MODEL="gemini-2.5-flash"
export GEMINI_SEARCH_MODEL="gemini-2.5-flash"
```

## Run the application

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080` by default.

## API

### Summarize research

`POST /api/research/summarize`

Request:

```json
{
  "topic": "Latest OpenAI Responses API features",
  "maxSources": 5
}
```

Validation rules:

- `topic` must not be blank.
- `maxSources` must be between 1 and 20.

Example request:

```bash
curl -X POST http://localhost:8080/api/research/summarize \
  -H "Content-Type: application/json" \
  -d '{"topic":"Latest OpenAI Responses API features","maxSources":5}'
```

Response shape:

```json
{
  "topic": "Latest OpenAI Responses API features",
  "searchSource": "MCP",
  "executiveSummary": "...",
  "keyFindings": ["..."],
  "details": "...",
  "sources": [
    "https://developers.openai.com/..."
  ]
}
```

Validation failures return HTTP `400`. Unexpected failures return HTTP `500`, both with structured JSON error bodies.

## Processing flow

```text
ResearchController
  -> ResearchService
  -> ResearchPlannerAgent
  -> ResearcherAgent
       -> OpenAI Docs MCP client, or
       -> Web search client
  -> SummarizerAgent
  -> ResearchResponse
```

1. `ResearchPlannerAgent` selects either OpenAI Docs MCP or web search.
2. `ResearcherAgent` collects simple factual text and source URLs from that source.
3. `SummarizerAgent` turns the collected text into the API response.

The application is an MCP client. It connects to `https://developers.openai.com/mcp` for OpenAI documentation research and does not expose an MCP server.

## LLM client

All three agents use `GeminiLlmClient` through the small `LlmClient` interface. General web research also uses Gemini with Google Search grounding, so only `GEMINI_API_KEY` is required. The OpenAI Docs integration is an MCP client and does not use the OpenAI LLM API.
