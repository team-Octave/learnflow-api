package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.SettlementDto;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SettlementService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    // 가상 가격 (10,000원)
    private static final long LECTURE_PRICE = 10_000L;
    // 수수료 (30%)
    private static final double FEE_RATE = 0.3;

    public SettlementService(EnrollmentRepository enrollmentRepository,
                             LectureRepository lectureRepository,
                             UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
    }

    /**
     * 강사별 정산 목록 조회
     */
    public List<SettlementDto> getSettlementList() {
        // 1. 강의별 판매량 조회 (Enrollment 테이블 집계)
        List<Object[]> salesData = enrollmentRepository.countEnrollmentsGroupByLectureId();

        Map<Long, Long> salesByLectureId = salesData.stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));

        if (salesByLectureId.isEmpty()) {
            return List.of();
        }

        // 2. 판매된 강의 정보 조회
        List<Long> lectureIds = salesByLectureId.keySet().stream().toList();
        List<Lecture> lectures = lectureRepository.findAllById(lectureIds);

        // 3. 강사별 판매량 집계
        Map<String, Long> salesByInstructor = new HashMap<>();

        for (Lecture lecture : lectures) {
            String instructorId = lecture.getInstructorId();
            long count = salesByLectureId.getOrDefault(lecture.getId(), 0L);
            salesByInstructor.merge(instructorId, count, Long::sum);
        }

        // 4. 강사 정보 조회 및 DTO 변환
        return salesByInstructor.entrySet().stream()
            .map(entry -> {
                String instructorId = entry.getKey();
                long totalCount = entry.getValue();

                String instructorName = userRepository.findById(instructorId)
                    .map(User::getNickname)
                    .orElse("Unknown Instructor");

                long totalSalesAmount = totalCount * LECTURE_PRICE;
                long feeAmount = (long) (totalSalesAmount * FEE_RATE);
                long settlementAmount = totalSalesAmount - feeAmount;

                return new SettlementDto(
                    instructorId,
                    instructorName,
                    totalCount,
                    totalSalesAmount,
                    feeAmount,
                    settlementAmount
                );
            })
            .toList();
    }

    /**
     * 정산 내역 엑셀 다운로드
     */
    public ByteArrayInputStream createExcel() {
        List<SettlementDto> data = getSettlementList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Settlement");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"강사명", "총 판매량", "총 매출액", "수수료(30%)", "정산금"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data
            int rowIdx = 1;
            for (SettlementDto dto : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.instructorName());
                row.createCell(1).setCellValue(dto.totalSalesCount());
                row.createCell(2).setCellValue(dto.totalSalesAmount());
                row.createCell(3).setCellValue(dto.feeAmount());
                row.createCell(4).setCellValue(dto.settlementAmount());
            }
            
            // 컬럼 너비 자동 조절
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("엑셀 생성 중 오류 발생", e);
        }
    }
}
