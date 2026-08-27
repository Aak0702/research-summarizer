package com.example.research.service;

import com.example.research.agent.ResearchPlannerAgent;
import com.example.research.agent.ResearcherAgent;
import com.example.research.agent.SummarizerAgent;
import com.example.research.model.ResearchData;
import com.example.research.model.ResearchPlan;
import com.example.research.model.ResearchRequest;
import com.example.research.model.ResearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResearchService {

    private final ResearchPlannerAgent researchPlanner;
    private final ResearcherAgent researcher;
    private final SummarizerAgent summarizer;

    public ResearchResponse research(ResearchRequest request) {
        ResearchPlan plan = researchPlanner.plan(request.topic(), request.maxSources());
        ResearchData researchData = researcher.research(plan);
        return summarizer.summarize(researchData);
    }
}
