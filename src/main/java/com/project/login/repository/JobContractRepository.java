package com.project.login.repository;

import com.project.login.entity.gen_bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobContractRepository extends JpaRepository<gen_bill, Long> {

    /* ==========================
       REPORT + EXCEL QUERY
       ========================== */
    @Query("""
        SELECT j FROM gen_bill j
        WHERE j.brokerName = :brokerName
          AND (:weaverName IS NULL OR j.weaverName LIKE :weaverName)
          AND (:traderName IS NULL OR j.traderName LIKE :traderName)
          AND (:fromDate IS NULL OR j.contractDate >= :fromDate)
          AND (:toDate IS NULL OR j.contractDate <= :toDate)
        ORDER BY j.contractNo  DESC
    """)
    List<gen_bill> searchReports(
            @Param("brokerName") String brokerName,
            @Param("weaverName") String weaverName,
            @Param("traderName") String traderName,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /* ==========================
       FIND FOR EDIT / DELETE
       ========================== */
    Optional<gen_bill> findByUserIdAndContractNo(
            Long userId,
            Long contractNo
    );

    /* ==========================
       (OPTIONAL) EXISTS CHECK
       ========================== */
    boolean existsByUserIdAndContractNo(
            Long userId,
            Long contractNo
    );

    /* ==========================
       USER-WISE AUTO INCREMENT
       ========================== */
    @Query(value = """
        SELECT COALESCE(MAX(contract_no), 0)
        FROM job_contract
        WHERE user_id = :userId
    """, nativeQuery = true)
    Integer findMaxContractNoByUser(
            @Param("userId") Long userId
    );

    @Query(value = """
        SELECT COALESCE(MAX(sr_no), 0)
        FROM job_contract
        WHERE user_id = :userId
    """, nativeQuery = true)
    Integer findMaxSrNoByUser(
            @Param("userId") Long userId
    );
    
    Optional<gen_bill> findByUserIdAndContractNo(
            Long userId,
            Integer contractNo   // ✅ must be Integer
    );

}
