package com.project.login.service;

import com.project.login.entity.gen_bill;
import com.project.login.entity.User;
import com.project.login.repository.JobContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobContractService {

    private final JobContractRepository jobContractRepository;

    public JobContractService(JobContractRepository jobContractRepository) {
        this.jobContractRepository = jobContractRepository;
    }

    /* ==========================
       FIND (EDIT / DELETE)
       ========================== */
    public gen_bill getByUserIdAndContractNo(
            Long userId,
            Integer contractNo
    ) {
        return jobContractRepository
                .findByUserIdAndContractNo(userId, contractNo)
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    /* ==========================
       DELETE
       ========================== */
    public void deleteByUserIdAndContractNo(
            Long userId,
            Integer contractNo
    ) {
        gen_bill jobContract = getByUserIdAndContractNo(userId, contractNo);
        jobContractRepository.delete(jobContract);
    }

    /* ==========================
       SAVE OR UPDATE
       ========================== */
    @Transactional
    public gen_bill saveOrUpdate(gen_bill bill, User user) {

        // always bind user
        bill.setUserId(user.getId());

        // 🔹 CREATE
        if (bill.getContractNo() == null) {
            bill.setContractNo(generateContractNo(user.getId()));
            bill.setSrNo(generateSrNo(user.getId()));
        }
        // 🔹 UPDATE
        else {
            gen_bill existing = getByUserIdAndContractNo(
                    user.getId(),
                    bill.getContractNo()
            );

            // preserve immutable fields
            bill.setId(existing.getId());
            bill.setSrNo(existing.getSrNo());
        }

        return jobContractRepository.save(bill);
    }

    /* ==========================
       LEGACY SAVE (KEEP)
       ========================== */
    @Transactional
    public gen_bill saveJobContract(gen_bill jobContract, User user) {

        jobContract.setUserId(user.getId());
        jobContract.setContractNo(generateContractNo(user.getId()));
        jobContract.setSrNo(generateSrNo(user.getId()));

        return jobContractRepository.save(jobContract);
    }

    /* ==========================
       LEGACY UPDATE (KEEP)
       ========================== */
    public void updateJobContract(gen_bill bill) {
        jobContractRepository.save(bill);
    }

    /* ==========================
       REPORT + EXCEL SEARCH
       ========================== */
    public List<gen_bill> searchReportsByUser(
            String userName,
            String weaverName,
            String traderName,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return jobContractRepository.searchReports(
                userName,
                (weaverName == null || weaverName.isBlank())
                        ? null : "%" + weaverName + "%",
                (traderName == null || traderName.isBlank())
                        ? null : "%" + traderName + "%",
                fromDate,
                toDate
        );
    }

    /* ==========================
       CONTRACT NO GENERATOR
       ========================== */
    private Integer generateContractNo(Long userId) {
        Integer maxContractNo =
                jobContractRepository.findMaxContractNoByUser(userId);
        return maxContractNo + 1;
    }

    private Integer generateSrNo(Long userId) {
        Integer maxSrNo =
                jobContractRepository.findMaxSrNoByUser(userId);
        return maxSrNo + 1;
    }
}
