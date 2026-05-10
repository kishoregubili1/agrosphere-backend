package com.agrosphere.service.impl;

import com.agrosphere.dto.request.FinanceRequest;
import com.agrosphere.entity.*;
import com.agrosphere.enums.TransactionType;
import com.agrosphere.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceTransactionRepository txRepo;
    private final CropRepository cropRepository;
    private final TenantRepository tenantRepository;

    public List<FinanceTransaction> getMyTransactions(Long tenantId) {
        return txRepo.findByTenantIdOrderByTransactionDateDesc(tenantId);
    }

    public FinanceSummary getSummary(Long tenantId) {
        BigDecimal income = txRepo.sumIncomeByTenant(tenantId);
        BigDecimal expense = txRepo.sumExpenseByTenant(tenantId);
        BigDecimal profit = income.subtract(expense);
        return new FinanceSummary(income, expense, profit);
    }

    @Transactional
    public FinanceTransaction create(FinanceRequest req, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Crop crop = req.getCropId() != null
                ? cropRepository.findByIdAndTenantId(req.getCropId(), tenantId).orElse(null)
                : null;

        FinanceTransaction tx = FinanceTransaction.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .amount(req.getAmount())
                .type(req.getType())
                .category(req.getCategory())
                .transactionDate(req.getTransactionDate() != null ? req.getTransactionDate() : LocalDate.now())
                .crop(crop)
                .tenant(tenant)
                .build();
        return txRepo.save(tx);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        FinanceTransaction tx = txRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!tx.getTenant().getId().equals(tenantId))
            throw new RuntimeException("Access denied");
        txRepo.delete(tx);
    }

    @Data
    public static class FinanceSummary {
        private final BigDecimal totalIncome;
        private final BigDecimal totalExpense;
        private final BigDecimal netProfit;
    }
}
