package com.teamexp.learnflowapi.ai.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.ai.model.AiSummaryContent;
import com.teamexp.learnflowapi.global.exception.BaseException;
import com.teamexp.learnflowapi.global.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Converter
@RequiredArgsConstructor
public class AiContentConverter implements AttributeConverter<AiSummaryContent, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(AiSummaryContent attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("[Critical] AI 요약 데이터 직렬화 실패", e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AiSummaryContent convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return objectMapper.readValue(dbData, AiSummaryContent.class);
        } catch (JsonProcessingException e) {
            log.error("[Critical] AI 요약 데이터 역직렬화 실패", e);
            // UI 깨짐 방지를 위한 빈 객체 반환 (Fail-Safe)
            return new AiSummaryContent("내용을 불러올 수 없습니다.", List.of());
        }
    }
}
