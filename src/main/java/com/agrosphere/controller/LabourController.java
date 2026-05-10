package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.enums.TransactionType;
import com.agrosphere.repository.*;
import com.agrosphere.service.impl.FileStorageService;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/farmer/workforce")
@RequiredArgsConstructor
public class LabourController {

    private final LabourWorkerRepository       workerRepo;
    private final LabourLogRepository          logRepo;
    private final TenantRepository             tenantRepo;
    private final CropRepository               cropRepo;
    private final FinanceTransactionRepository financeRepo;
    private final FileStorageService           fileStorage;
    private final AuthUtil                     authUtil;

    private double calcWage(LabourLog log) {
        if (log.getPaidAmount() != null && log.getPaidAmount() > 0) return log.getPaidAmount();
        double daily = log.getWorker() != null && log.getWorker().getDailyWage() != null
                ? log.getWorker().getDailyWage() : 0;
        return daily * (log.getDaysWorked() != null ? log.getDaysWorked() : 1);
    }

    private void syncToFinance(LabourLog log, Tenant tenant, double amount) {
        String cropName = log.getCrop() != null && log.getCrop().getCropType() != null
                ? " [" + log.getCrop().getCropType().getName() + "]" : "";
        FinanceTransaction tx = FinanceTransaction.builder()
                .tenant(tenant)
                .title("Farm Workforce – " + log.getWorker().getName() + " (" + log.getWorkType() + ")" + cropName)
                .description("Auto-synced · Work date: " + log.getWorkDate() + " · Days: " + log.getDaysWorked())
                .amount(BigDecimal.valueOf(amount))
                .type(TransactionType.EXPENSE)
                .category("LABOUR")
                .transactionDate(LocalDate.now())
                .cropType(log.getCrop() != null ? log.getCrop().getCropType() : null)
                .build();
        financeRepo.save(tx);
    }

    // ── WORKERS ──────────────────────────────────────────────
    @GetMapping("/workers")
    public ResponseEntity<?> getWorkers() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(workerRepo.findByTenantOrderByCreatedAtDesc(t)));
    }

    @PostMapping("/workers")
    public ResponseEntity<?> addWorker(@RequestBody WorkerReq req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        LabourWorker w = LabourWorker.builder()
                .tenant(t).name(req.name).phone(req.phone).village(req.village)
                .skill(req.skill == null ? "General" : req.skill)
                .dailyWage(req.dailyWage).notes(req.notes).photoUrl(req.photoUrl).build();
        return ResponseEntity.ok(ApiResponse.success(workerRepo.save(w), "Added"));
    }

    @PutMapping("/workers/{id}")
    public ResponseEntity<?> updateWorker(@PathVariable Long id, @RequestBody WorkerReq req) {
        LabourWorker w = workerRepo.findById(id).orElseThrow();
        if (!w.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        w.setName(req.name); w.setPhone(req.phone); w.setVillage(req.village);
        if (req.skill    != null) w.setSkill(req.skill);
        if (req.photoUrl != null) w.setPhotoUrl(req.photoUrl);
        w.setDailyWage(req.dailyWage); w.setNotes(req.notes);
        return ResponseEntity.ok(ApiResponse.success(workerRepo.save(w), "Updated"));
    }

    @PostMapping("/workers/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        LabourWorker w = workerRepo.findById(id).orElseThrow();
        if (!w.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        String url = fileStorage.store(file, "workforce");
        w.setPhotoUrl(url); workerRepo.save(w);
        return ResponseEntity.ok(ApiResponse.success(Map.of("photoUrl", url)));
    }

    @DeleteMapping("/workers/{id}")
    public ResponseEntity<?> deleteWorker(@PathVariable Long id) {
        LabourWorker w = workerRepo.findById(id).orElseThrow();
        if (!w.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        logRepo.findByTenantOrderByWorkDateDescCreatedAtDesc(w.getTenant())
                .stream().filter(l -> l.getWorker().getId().equals(id)).forEach(logRepo::delete);
        workerRepo.delete(w);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ── WORK LOGS ─────────────────────────────────────────────
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(logRepo.findByTenantOrderByWorkDateDescCreatedAtDesc(t)));
    }

    @PostMapping("/logs")
    public ResponseEntity<?> addLog(@RequestBody LogReq req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        LabourWorker w = workerRepo.findById(req.workerId).orElseThrow();

        Crop crop = null;
        if (req.cropId != null) {
            crop = cropRepo.findById(req.cropId).orElse(null);
        }

        LabourLog log = LabourLog.builder()
                .tenant(t).worker(w).workType(req.workType)
                .workDate(req.date != null ? LocalDate.parse(req.date) : LocalDate.now())
                .hoursWorked(req.hoursWorked == null ? 8 : req.hoursWorked)
                .daysWorked(req.daysWorked == null ? 1 : req.daysWorked)
                .paid(false).notes(req.notes).crop(crop).build();
        return ResponseEntity.ok(ApiResponse.success(logRepo.save(log), "Work logged"));
    }

    @PatchMapping("/logs/{id}/pay")
    public ResponseEntity<?> markPaid(@PathVariable Long id, @RequestBody(required = false) PayReq req) {
        LabourLog log = logRepo.findById(id).orElseThrow();
        Tenant tenant = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        if (!log.getTenant().getId().equals(tenant.getId())) return ResponseEntity.status(403).build();
        log.setPaid(true);
        double amount = calcWage(log);
        if (req != null && req.paidAmount != null && req.paidAmount > 0) {
            log.setPaidAmount(req.paidAmount); amount = req.paidAmount;
        }
        logRepo.save(log);
        syncToFinance(log, tenant, amount);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("amountSynced", amount, "message", "Paid & synced to Finance"), "Paid"));
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable Long id) {
        LabourLog log = logRepo.findById(id).orElseThrow();
        if (!log.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        logRepo.delete(log);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ── ANALYTICS ─────────────────────────────────────────────
    @GetMapping("/analytics")
    public ResponseEntity<?> analytics() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        List<LabourWorker> workers = workerRepo.findByTenantOrderByCreatedAtDesc(t);
        List<LabourLog>    logs    = logRepo.findByTenantOrderByWorkDateDescCreatedAtDesc(t);

        List<Map<String,Object>> workerStats = workers.stream().map(w -> {
            List<LabourLog> wl = logs.stream().filter(l -> l.getWorker() != null && l.getWorker().getId().equals(w.getId())).toList();
            double earned  = wl.stream().filter(l -> Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
            double pending = wl.stream().filter(l -> !Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
            int days = wl.stream().mapToInt(l -> l.getDaysWorked() != null ? l.getDaysWorked() : 0).sum();
            Map<String,Long> byType = new LinkedHashMap<>();
            wl.forEach(l -> byType.merge(l.getWorkType() != null ? l.getWorkType() : "General", 1L, Long::sum));
            Map<String,Object> s = new LinkedHashMap<>();
            s.put("workerId",w.getId()); s.put("workerName",w.getName()); s.put("skill",w.getSkill());
            s.put("village",w.getVillage()); s.put("dailyWage",w.getDailyWage()); s.put("photoUrl",w.getPhotoUrl());
            s.put("totalLogs",wl.size()); s.put("totalDays",days);
            s.put("totalEarned",earned); s.put("totalPending",pending);
            s.put("paidLogs",wl.stream().filter(l->Boolean.TRUE.equals(l.getPaid())).count());
            s.put("unpaidLogs",wl.stream().filter(l->!Boolean.TRUE.equals(l.getPaid())).count());
            s.put("byWorkType",byType); s.put("recentLogs",wl.stream().limit(5).toList());
            return s;
        }).toList();

        double farmPaid    = logs.stream().filter(l->Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
        double farmPending = logs.stream().filter(l->!Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
        int    totalDays   = logs.stream().mapToInt(l->l.getDaysWorked()!=null?l.getDaysWorked():0).sum();

        Map<String,Double> monthly = new LinkedHashMap<>();
        logs.stream().filter(l->Boolean.TRUE.equals(l.getPaid())&&l.getWorkDate()!=null).forEach(l -> {
            String k = l.getWorkDate().getYear()+"-"+String.format("%02d",l.getWorkDate().getMonthValue());
            monthly.merge(k, calcWage(l), Double::sum);
        });

        Map<String,Double> byType = new LinkedHashMap<>();
        logs.stream().filter(l->Boolean.TRUE.equals(l.getPaid())).forEach(l ->
                byType.merge(l.getWorkType()!=null?l.getWorkType():"General", calcWage(l), Double::sum));

        // Crop-wise workforce spend
        Map<String,Object> byCrop = new LinkedHashMap<>();
        logs.stream().filter(l->Boolean.TRUE.equals(l.getPaid())&&l.getCrop()!=null).forEach(l -> {
            String key = l.getCrop().getCropType() != null ? l.getCrop().getCropType().getName() : "Unknown";
            byCrop.merge(key, calcWage(l), (a,b) -> (double)a + (double)b);
        });

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("workerStats",workerStats); result.put("farmTotalPaid",farmPaid);
        result.put("farmTotalPending",farmPending); result.put("totalWorkDays",totalDays);
        result.put("totalWorkers",workers.size()); result.put("totalLogs",logs.size());
        result.put("monthlySpend",monthly); result.put("byWorkType",byType);
        result.put("byCrop",byCrop);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/workers/{id}/ledger")
    public ResponseEntity<?> ledger(@PathVariable Long id) {
        LabourWorker w = workerRepo.findById(id).orElseThrow();
        if (!w.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        List<LabourLog> logs = logRepo.findByTenantOrderByWorkDateDescCreatedAtDesc(w.getTenant())
                .stream().filter(l->l.getWorker().getId().equals(id)).toList();
        double earned  = logs.stream().filter(l->Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
        double pending = logs.stream().filter(l->!Boolean.TRUE.equals(l.getPaid())).mapToDouble(this::calcWage).sum();
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("worker",w); r.put("logs",logs);
        r.put("totalEarned",earned); r.put("totalPending",pending);
        r.put("totalDays",logs.stream().mapToInt(l->l.getDaysWorked()!=null?l.getDaysWorked():0).sum());
        r.put("totalLogs",logs.size());
        return ResponseEntity.ok(ApiResponse.success(r));
    }

    @Data static class WorkerReq { String name, phone, village, skill, notes, photoUrl; Double dailyWage; }
    @Data static class LogReq    { Long workerId, cropId; String workType, date, notes; Integer hoursWorked, daysWorked; }
    @Data static class PayReq    { Double paidAmount; }
}
