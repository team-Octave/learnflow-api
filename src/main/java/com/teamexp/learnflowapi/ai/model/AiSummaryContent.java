package com.teamexp.learnflowapi.ai.model;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AiSummaryContent {
    private String overview;          // 상단 줄글 요약
    private List<String> keyTakeaways; // 하단 핵심 요약 (Bullet Points)
}
