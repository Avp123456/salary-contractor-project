// added cut length, minimum delivery, rolling/folding

package com.project.login.controller;

import com.project.login.entity.User;
import com.project.login.entity.gen_bill;
import com.project.login.service.JobContractService;
import com.project.login.service.QualityMasterService;
import com.project.login.service.WeaverTraderService;
import com.project.security.CustomUserDetails;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class JobContractController {

	private final QualityMasterService qulitymasterMasterService;
    private final JobContractService jobContractService;
    private final WeaverTraderService weaverTraderService;
    

    public JobContractController(JobContractService jobContractService,
                                 WeaverTraderService weaverTraderService, QualityMasterService qulitymasterMasterService ) {
        this.qulitymasterMasterService = qulitymasterMasterService;
		this.jobContractService = jobContractService;
        this.weaverTraderService = weaverTraderService;
    }

    /* =====================
       SHOW GEN BILL PAGE (CREATE)
       ===================== */
    @GetMapping("/gen-bill")
    public String genBillPage(Model model, Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        model.addAttribute("weavers", weaverTraderService.getWeavers(userId));
        model.addAttribute("traders", weaverTraderService.getTraders(userId));
        model.addAttribute("quality", qulitymasterMasterService.findByUser(userId));

        model.addAttribute("editMode", false);
        model.addAttribute("job", new gen_bill());

        return "gen_bill";
    }

    /* =====================
       SHOW GEN BILL PAGE (EDIT)
       ===================== */
    @GetMapping("/gen-bill/edit/{userId}/{contractNo}")
    public String editGenBill(
            @PathVariable Long userId,
            @PathVariable Integer contractNo,   // ✅ FIXED
            Model model,
            Authentication authentication
    ) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        // 🔐 Security check
        if (!userDetails.getId().equals(userId)) {
            return "redirect:/report";
        }

        gen_bill job = jobContractService
                .getByUserIdAndContractNo(userId, contractNo); // ✅ FIXED

        model.addAttribute("weavers", weaverTraderService.getWeavers(userId));
        model.addAttribute("traders", weaverTraderService.getTraders(userId));
        model.addAttribute("quality", qulitymasterMasterService.findByUser(userId));

        model.addAttribute("editMode", true);
        model.addAttribute("job", job);

        return "gen_bill";
    }

    /* =====================
       SAVE / UPDATE BILL
       ===================== */
    @PostMapping(
            value = "/gen_bill",
            consumes = "application/x-www-form-urlencoded"
    )
    @ResponseBody
    public ResponseEntity<String> saveBill(

            @RequestParam(required = false) Integer contractNo, // ✅ FIXED

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate contract_date,

            @RequestParam String weaver_name,
            @RequestParam String trader_name,
            @RequestParam String quality,
            @RequestParam Integer quantity_meters,
            @RequestParam Double job_rate,
            @RequestParam Integer payment_days,
            @RequestParam Integer beams,
            @RequestParam String production_schedule,
            @RequestParam Integer no_of_machines,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String cut_length,
            @RequestParam(required = false) String minimum_delivery,
            @RequestParam(required = false) String rolling_folding
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
            !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        try {
            User user = new User();
            user.setId(userDetails.getId());
            user.setName(userDetails.getName());

            gen_bill bill = new gen_bill();

            // 🔑 UPDATE CASE
            if (contractNo != null) {
                bill.setContractNo(contractNo);          // ✅ FIXED
            }

            bill.setUserId(user.getId());                // ✅ FIXED
            bill.setContractDate(contract_date);
            bill.setWeaverName(weaver_name);
            bill.setTraderName(trader_name);
            bill.setBrokerName(userDetails.getName());
            bill.setQuality(quality);
            bill.setQuantityMeters(quantity_meters);
            bill.setJobRate(job_rate);
            bill.setPaymentDays(payment_days);
            bill.setProductionSchedule(production_schedule);
            bill.setNoOfMachines(no_of_machines);
            bill.setRemark(remark);
            bill.setBeams(beams);
            bill.setCutLength(cut_length);
            bill.setMinimumDelivery(minimum_delivery);
            bill.setRollingFolding(rolling_folding);

            // 🔄 Same service handles save or update
            jobContractService.saveOrUpdate(bill, user);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("ERROR");
        }

        return ResponseEntity.ok("SUCCESS");
    }
}
