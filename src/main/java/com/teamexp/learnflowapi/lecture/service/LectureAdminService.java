package com.teamexp.learnflowapi.lecture.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamexp.learnflowapi.lecture.model.Lecture;

@Service
@Transactional(readOnly = true)
public class LectureAdminService {

    private final LectureService lectureService;

    public LectureAdminService(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    // Only Admin can call this method
    // check admin at controller
    public void allowPublishLecture(Long lectureId) {
        Lecture lecture = lectureService.findLectureWithChaptersAndLessons(lectureId);

        lecture.allowPublish();
    }

    // Only Admin can call this method
    // check admin at controller
    public void notAllowPublishLecture(Long lectureId) {
        Lecture lecture = lectureService.findLectureWithChaptersAndLessons(lectureId);

        lecture.notAllowPublish();
    }
}
