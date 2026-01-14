package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;

@Service
public class LectureAdminService {

    private final LectureAdminRepository lectureAdminRepository;

    public LectureAdminService(LectureAdminRepository lectureAdminRepository) {
        this.lectureAdminRepository = lectureAdminRepository;
    }

    // Only Admin can call this method
    // check admin at controller
    @Transactional
    public void allowPublishLecture(Long lectureId) {
        Lecture lecture = lectureAdminRepository.findById(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        lecture.allowPublish();
        
    }

    // Only Admin can call this method
    // check admin at controller
    @Transactional
    public void notAllowPublishLecture(Long lectureId) {
        Lecture lecture = lectureAdminRepository.findById(lectureId)
            .orElseThrow(LectureNotFoundException::new);

        lecture.notAllowPublish();
    }
}
