package com.teamexp.learnflowapi.ai.model;

import com.teamexp.learnflowapi.ai.converter.AiContentConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_summaries")
@Getter
@NoArgsConstructor
public class AiSummary {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long lessonId;

    @Convert(converter = AiContentConverter.class)
    @Column(columnDefinition = "JSON")
    private AiSummaryContent content;

    public AiSummary(Long lessonId, AiSummaryContent content) {
        this.lessonId = lessonId;
        this.content = content;
    }
}
