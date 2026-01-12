package com.teamexp.learnflowapi.lecture.service;

import org.springframework.stereotype.Component;

import com.teamexp.learnflowapi.lecture.exception.LectureInstructorUnauthorizedException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import org.springframework.stereotype.Service;



@Component
@Service
public class LectureAccessValidator {

    public void validateOwnership(Lecture lecture, String instructorId) {
        if (!lecture.getInstructorId().equals(instructorId)) {
            throw new LectureInstructorUnauthorizedException();
        }
    }

    // TODO [Phase 1-1]: 추가 검증 메서드 (예정)
    // public void validatePublicAccess(Lecture lecture) { ... }
    // public void validateEnrollmentAccess(Lecture lecture, String userId) { ... }
}
