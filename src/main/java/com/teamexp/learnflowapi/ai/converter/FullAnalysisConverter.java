package com.teamexp.learnflowapi.ai.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.ai.exception.AiSerializationException;
import com.teamexp.learnflowapi.ai.model.FullAnalysisContent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
@RequiredArgsConstructor
public class FullAnalysisConverter implements AttributeConverter<FullAnalysisContent, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(FullAnalysisContent attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("[Critical] AI 분석 데이터 직렬화(JSON 변환) 실패 - data: {}", attribute, e);
            throw new AiSerializationException();
        }
    }

    @Override
    public FullAnalysisContent convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, FullAnalysisContent.class);
        } catch (JsonProcessingException e) {
            log.error("[Critical] AI 분석 데이터 역직렬화(객체 변환) 실패 - dbData: {}", dbData, e);
            throw new AiSerializationException();
        }
    }
}
