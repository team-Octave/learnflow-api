package com.teamexp.learnflowapi.ai.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FullAnalysisContent {
    private List<String> topics;          // 3-5 main topics
    private String difficulty;            // beginner, intermediate, advanced
    private List<String> prerequisites;   // Required prior knowledge
    private List<OutlineItem> outline;    // Lecture sections with timestamps
    private List<String> keyPoints;       // 3-5 key takeaways
    private List<String> keywords;        // 5-10 important terms

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OutlineItem {
        private String timestamp;         // "MM:SS" or "HH:MM:SS"
        private Integer timestampSeconds; // Seconds from start
        private String title;             // Section title
        private String content;           // Section description
    }
}
