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
export GEMINI_MODEL="gemini-3.6-flash"
export GEMINI_SEARCH_MODEL="gemini-3.6-flash"
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

## Architecture diagram

```text
                              ┌───────────────────────┐
                              │   Client (curl/UI)     │
                              └───────────┬────────────┘
                                          │ POST /api/research/summarize
                                          ▼
                              ┌───────────────────────┐
                              │   ResearchController   │
                              │  (validates request)   │
                              └───────────┬────────────┘
                                          ▼
                              ┌───────────────────────┐
                              │     ResearchService     │
                              │  (orchestrates agents)  │
                              └───────────┬────────────┘
                                          ▼
                       ┌─────────────────────────────────────┐
                       │        ResearchPlannerAgent           │
                       │  Decides routing: MCP vs Web Search   │
                       │  (uses GeminiLlmClient to interpret   │
                       │   the topic/intent)                   │
                       └───────────────────┬───────────────────┘
                                           │
                         ┌─────────────────┴──────────────────┐
                         ▼                                     ▼
             ┌─────────────────────┐                ┌─────────────────────┐
             │   searchSource=MCP   │                │  searchSource=WEB    │
             └───────────┬──────────┘                └───────────┬──────────┘
                         ▼                                       ▼
             ┌─────────────────────┐                ┌─────────────────────┐
             │   ResearcherAgent    │                │   ResearcherAgent    │
             │  -> OpenAI Docs MCP  │                │  -> Web Search       │
             │     client           │                │     client (Gemini + │
             │  (developers.openai  │                │     Google Search    │
             │   .com/mcp)          │                │     grounding)       │
             └───────────┬──────────┘                └───────────┬──────────┘
                         │                                       │
                         └───────────────┬───────────────────────┘
                                         ▼
                              ┌───────────────────────┐
                              │   collected text +     │
                              │      source URLs       │
                              └───────────┬────────────┘
                                          ▼
                              ┌───────────────────────┐
                              │    SummarizerAgent      │
                              │  (GeminiLlmClient turns  │
                              │   raw text into summary, │
                              │   key findings, details) │
                              └───────────┬────────────┘
                                          ▼
                              ┌───────────────────────┐
                              │    ResearchResponse     │
                              │   (returned to client)  │
                              └───────────────────────┘
```

## Processing flow

1. `ResearchPlannerAgent` selects either OpenAI Docs MCP or web search.
2. `ResearcherAgent` collects simple factual text and source URLs from that source.
3. `SummarizerAgent` turns the collected text into the API response.

The application is an MCP client. It connects to `https://developers.openai.com/mcp` for OpenAI documentation research and does not expose an MCP server.

## Routing logic explanation

`ResearchPlannerAgent` classifies the topic once, via `GeminiLlmClient`. OpenAI API/SDK/docs topics route to the **OpenAI Docs MCP client**; everything else routes to the **web search client** (Gemini + Google Search grounding). The choice is returned in the response as `searchSource` (`MCP` or `WEB`) and is never split across both sources.

## LLM client

All three agents use `GeminiLlmClient` (via the `LlmClient` interface), so only `GEMINI_API_KEY` is required. The OpenAI Docs integration is an MCP client, not an LLM call.

## Known issues / incomplete items

This submission is **partial**. The end-to-end flow (planner → researcher → summarizer → response) is implemented and works as designed, but it could not be fully verified under sustained/repeated testing due to the following:

- **Gemini API rate limiting on the free tier**: the free-tier `GEMINI_API_KEY` used during development enforces a low requests-per-minute / requests-per-day quota. Because each `/api/research/summarize` call triggers at least three separate Gemini calls (planner, researcher/web-search grounding, summarizer), repeated or back-to-back testing frequently hit **HTTP 429 (Too Many Requests)** from the Gemini API.
- As a result, some end-to-end runs failed mid-flow with a `429` bubbling up as a `500` response from the service, rather than a graceful retry or fallback.
- **Recommended next step**: run against a paid/higher-quota Gemini API key, or add exponential backoff + a small in-memory cache, to confirm stable behavior under repeated load.