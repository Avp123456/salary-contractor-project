package com.project.login.repository;

import com.project.login.entity.ReportEntity;
import com.project.login.entity.gen_bill;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query("""
        SELECT r FROM ReportEntity r
        WHERE r.brokerName = :brokerName
          AND (:weaverName IS NULL OR r.weaverName LIKE %:weaverName%)
          AND (:traderName IS NULL OR r.traderName LIKE %:traderName%)
          AND (:fromDate IS NULL OR r.contractDate >= :fromDate)
          AND (:toDate IS NULL OR r.contractDate <= :toDate)
        ORDER BY r.contractDate DESC
    """)
    Page<ReportEntity> filterReport(
            @Param("brokerName") String brokerName,
            @Param("weaverName") String weaverName,
            @Param("traderName") String traderName,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
