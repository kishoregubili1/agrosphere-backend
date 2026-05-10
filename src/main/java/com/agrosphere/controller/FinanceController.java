package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.enums.TransactionType;
import com.agrosphere.repository.*;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/farmer/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceTransactionRepository repo;
    private final TenantRepository             tenantRepository;
    private final CropTypeRepository           cropTypeRepository;
    private final AuthUtil                     authUtil;

    // ── GET ALL (with optional filters) ─────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required=false) String type,
            @RequestParam(required=false) String category,
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to,
            @RequestParam(required=false) Long cropTypeId) {

        Tenant tenant = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();
        List<FinanceTransaction> list = repo.findByTenantOrderByTransactionDateDesc(tenant);

        if (type       != null && !type.isBlank())
            list = list.stream().filter(t -> t.getType().name().equals(type)).toList();
        if (category   != null && !category.isBlank())
            list = list.stream().filter(t -> category.equals(t.getCategory())).toList();
        if (cropTypeId != null)
            list = list.stream().filter(t -> t.getCropType() != null && t.getCropType().getId().equals(cropTypeId)).toList();
        if (from != null && !from.isBlank()) {
            LocalDate d = LocalDate.parse(from);
            list = list.stream().filter(t -> t.getTransactionDate() != null && !t.getTransactionDate().isBefore(d)).toList();
        }
        if (to != null && !to.isBlank()) {
            LocalDate d = LocalDate.parse(to);
            list = list.stream().filter(t -> t.getTransactionDate() != null && !t.getTransactionDate().isAfter(d)).toList();
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // ── SUMMARY ──────────────────────────────────────────────
    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        Long tid = authUtil.getCurrentTenantId();
        BigDecimal inc = repo.sumByTenantAndType(tid, TransactionType.INCOME);
        BigDecimal exp = repo.sumByTenantAndType(tid, TransactionType.EXPENSE);
        if (inc == null) inc = BigDecimal.ZERO;
        if (exp == null) exp = BigDecimal.ZERO;
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("totalIncome",  inc);
        m.put("totalExpense", exp);
        m.put("netProfit",    inc.subtract(exp));
        m.put("profitMargin", inc.compareTo(BigDecimal.ZERO)>0 ?
                inc.subtract(exp).divide(inc,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1,RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return ResponseEntity.ok(ApiResponse.success(m));
    }

    // ── MONTHLY BREAKDOWN ────────────────────────────────────
    @GetMapping("/monthly")
    public ResponseEntity<?> monthly() {
        Long tid = authUtil.getCurrentTenantId();
        List<Object[]> rows = repo.monthlyBreakdown(tid);
        List<Map<String,Object>> result = rows.stream().map(r -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("month",   r[0]); m.put("monthDate", r[1]);
            m.put("income",  r[2]); m.put("expense",   r[3]);
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── BY CATEGORY ──────────────────────────────────────────
    @GetMapping("/by-category")
    public ResponseEntity<?> byCategory() {
        Long tid = authUtil.getCurrentTenantId();
        List<Object[]> rows = repo.sumByCategory(tid);
        List<Map<String,Object>> result = rows.stream().map(r -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("category", r[0]); m.put("income", r[1]); m.put("expense", r[2]);
            return m;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── CROP TYPE ANALYTICS ──────────────────────────────────
    @GetMapping("/by-crop-type")
    public ResponseEntity<?> byCropType() {
        Tenant tenant = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();
        List<FinanceTransaction> all = repo.findByTenantOrderByTransactionDateDesc(tenant);

        // Group by cropType
        Map<Long, Map<String,Object>> map = new LinkedHashMap<>();

        // "Untagged" bucket
        Map<String,Object> untagged = new LinkedHashMap<>();
        untagged.put("cropTypeId", null);
        untagged.put("cropTypeName", "Untagged");
        untagged.put("emoji", "📝");
        untagged.put("income", 0.0); untagged.put("expense", 0.0); untagged.put("count", 0);
        untagged.put("transactions", new ArrayList<>());

        for (FinanceTransaction tx : all) {
            if (tx.getCropType() == null) {
                untagged.put("income",  (double)untagged.get("income")  + (tx.getType()==TransactionType.INCOME  ? tx.getAmount().doubleValue() : 0));
                untagged.put("expense", (double)untagged.get("expense") + (tx.getType()==TransactionType.EXPENSE ? tx.getAmount().doubleValue() : 0));
                untagged.put("count",   (int)untagged.get("count") + 1);
                ((List)untagged.get("transactions")).add(tx);
            } else {
                Long ctId = tx.getCropType().getId();
                if (!map.containsKey(ctId)) {
                    Map<String,Object> entry = new LinkedHashMap<>();
                    entry.put("cropTypeId",   ctId);
                    entry.put("cropTypeName", tx.getCropType().getName());
                    entry.put("emoji",        tx.getCropType().getIconEmoji() != null ? tx.getCropType().getIconEmoji() : "🌿");
                    entry.put("income",  0.0); entry.put("expense", 0.0); entry.put("count", 0);
                    entry.put("transactions", new ArrayList<>());
                    map.put(ctId, entry);
                }
                Map<String,Object> entry = map.get(ctId);
                entry.put("income",  (double)entry.get("income")  + (tx.getType()==TransactionType.INCOME  ? tx.getAmount().doubleValue() : 0));
                entry.put("expense", (double)entry.get("expense") + (tx.getType()==TransactionType.EXPENSE ? tx.getAmount().doubleValue() : 0));
                entry.put("count",   (int)entry.get("count") + 1);
                ((List)entry.get("transactions")).add(tx);
            }
        }

        // Add net profit to each
        List<Map<String,Object>> result = new ArrayList<>(map.values());
        result.forEach(e -> e.put("netProfit", (double)e.get("income") - (double)e.get("expense")));

        if ((int)untagged.get("count") > 0) {
            untagged.put("netProfit", (double)untagged.get("income") - (double)untagged.get("expense"));
            result.add(untagged);
        }

        // Sort by total spend desc
        result.sort((a, b) -> Double.compare((double)b.get("income")+(double)b.get("expense"), (double)a.get("income")+(double)a.get("expense")));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── ADD TRANSACTION ──────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> add(@RequestBody TxReq req) {
        Tenant tenant = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();

        CropType cropType = null;
        if (req.cropTypeId != null) {
            cropType = cropTypeRepository.findById(req.cropTypeId).orElse(null);
        }

        FinanceTransaction tx = FinanceTransaction.builder()
                .tenant(tenant)
                .title(req.title)
                .description(req.description)
                .amount(BigDecimal.valueOf(req.amount))
                .type(TransactionType.valueOf(req.type))
                .category(req.category)
                .transactionDate(req.transactionDate != null ? LocalDate.parse(req.transactionDate) : LocalDate.now())
                .cropType(cropType)
                .build();

        return ResponseEntity.ok(ApiResponse.success(repo.save(tx), "Transaction added"));
    }

    // ── DELETE ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        FinanceTransaction tx = repo.findById(id).orElseThrow();
        if (!tx.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        repo.delete(tx);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @Data
    static class TxReq {
        String title, description, type, category, transactionDate;
        Double amount;
        Long   cropTypeId;
    }
}
