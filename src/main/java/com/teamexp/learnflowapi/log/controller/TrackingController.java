package com.teamexp.learnflowapi.log.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.log.domain.TrackingLog;
import com.teamexp.learnflowapi.log.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final TrackingRepository trackingRepository;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/v1/track", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<Void>> track(@RequestBody String body) {
        try {
            JsonNode node = objectMapper.readTree(body);

            String event = node.path("event").asText();
            String path = node.path("path").asText();
            long ts = node.path("ts").asLong();

            if (event == null || event.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("BAD_REQUEST", "event 필드는 필수입니다.", MDC.get("traceId")));
            }

            TrackingLog.TrackingLogBuilder logBuilder = TrackingLog.builder()
                    .event(event)
                    .path(path)
                    .ts(ts);

            if ("landing".equals(event)) {
                logBuilder.url(node.path("landingUrl").asText())
                        .referrer(node.path("referrer").asText(null));
            } else if ("exit".equals(event)) {
                logBuilder.url(node.path("exitUrl").asText())
                        .durationMs(node.path("durationMs").asLong(0));
            } else {
                log.warn("Unknown Event Type: {}", event);
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("BAD_REQUEST", "지원하지 않는 이벤트 타입입니다: " + event, MDC.get("traceId")));
            }

            trackingRepository.save(logBuilder.build());

            return ResponseEntity.ok(BaseResponse.ok(null));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("BAD_REQUEST", "잘못된 요청 형식입니다.", MDC.get("traceId")));
        } catch (Exception e) {
            log.error("Tracking Save Error", e);
            return ResponseEntity.internalServerError()
                    .body(BaseResponse.error("INTERNAL_SERVER_ERROR", "로그 저장 중 오류가 발생했습니다.", MDC.get("traceId")));
        }
    }
}
