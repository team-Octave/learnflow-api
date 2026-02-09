package com.teamexp.learnflowapi.log.repository;

import com.teamexp.learnflowapi.log.domain.TrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TrackingRepository  extends JpaRepository<TrackingLog, Long> {

    @Query("SELECT  t.referrer, COUNT(t) FROM TrackingLog t " +
           "WHERE t.event = 'landing' AND t. referrer IS NOT NULL AND t.referrer != '' " +
           "GROUP BY t. referrer ORDER BY COUNT(t) DESC")
    List<Object[]> findReferrerStats();

}
