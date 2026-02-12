package com.teamexp.learnflowapi.ai.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.ai.exception.AiSerializationException;
import com.teamexp.learnflowapi.ai.model.AiSummaryContent;
import com.teamexp.learnflowapi.ai.model.FullAnalysisContent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
@Converter
@RequiredArgsConstructor
public class AiContentConverter implements AttributeConverter<AiSummaryContent, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(AiSummaryContent attribute) {
        return toJson(objectMapper, attribute, "AI 요약 데이터");
    }

    @Override
    public AiSummaryContent convertToEntityAttribute(String dbData) {
        return fromJson(objectMapper, dbData, AiSummaryContent.class, "AI 요약 데이터");
    }

    private static String toJson(ObjectMapper objectMapper, Object attribute, String logLabel) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(AiContentConverter.class)
                .error("[Critical] {} 직렬화(JSON 변환) 실패 - data: {}", logLabel, attribute, e);
            throw new AiSerializationException();
        }
    }

    private static <T> T fromJson(ObjectMapper objectMapper, String dbData, Class<T> clazz, String logLabel) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, clazz);
        } catch (JsonProcessingException e) {
            LoggerFactory.getLogger(AiContentConverter.class)
                .error("[Critical] {} 역직렬화(객체 변환) 실패 - dbData: {}", logLabel, dbData, e);
            throw new AiSerializationException();
        }
    }

    @Slf4j
    @Converter
    @RequiredArgsConstructor
    public static class FullAnalysisConverter implements AttributeConverter<FullAnalysisContent, String> {

        private final ObjectMapper objectMapper;

        @Override
        public String convertToDatabaseColumn(FullAnalysisContent attribute) {
            return toJson(objectMapper, attribute, "AI 분석 데이터");
        }

        @Override
        public FullAnalysisContent convertToEntityAttribute(String dbData) {
            return fromJson(objectMapper, dbData, FullAnalysisContent.class, "AI 분석 데이터");
        }
    }
}
