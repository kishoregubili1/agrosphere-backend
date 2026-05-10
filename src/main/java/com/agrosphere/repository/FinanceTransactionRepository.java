package com.agrosphere.repository;

import com.agrosphere.entity.FinanceTransaction;
import com.agrosphere.entity.Tenant;
import com.agrosphere.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {

    // By Tenant object (used by FinanceController)
    List<FinanceTransaction> findByTenantOrderByTransactionDateDesc(Tenant tenant);
    void deleteByCropId(Long cropId);

    // By tenantId (used by FinanceService)
    @Query("SELECT f FROM FinanceTransaction f WHERE f.tenant.id = :tid ORDER BY f.transactionDate DESC")
    List<FinanceTransaction> findByTenantIdOrderByTransactionDateDesc(@Param("tid") Long tid);

    // Sum methods (used by FinanceService)
    @Query("SELECT COALESCE(SUM(f.amount),0) FROM FinanceTransaction f WHERE f.tenant.id=:tid AND f.type='INCOME'")
    BigDecimal sumIncomeByTenant(@Param("tid") Long tid);

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM FinanceTransaction f WHERE f.tenant.id=:tid AND f.type='EXPENSE'")
    BigDecimal sumExpenseByTenant(@Param("tid") Long tid);

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM FinanceTransaction f WHERE f.tenant.id=:tid AND f.type=:type")
    BigDecimal sumByTenantAndType(@Param("tid") Long tid, @Param("type") TransactionType type);

    // Analytics (used by FinanceController)
    @Query(value="SELECT f.category, SUM(CASE WHEN f.type='INCOME' THEN f.amount ELSE 0 END) as income, SUM(CASE WHEN f.type='EXPENSE' THEN f.amount ELSE 0 END) as expense FROM finance_transactions f WHERE f.tenant_id=:tid GROUP BY f.category ORDER BY (SUM(f.amount)) DESC", nativeQuery=true)
    List<Object[]> sumByCategory(@Param("tid") Long tid);

    @Query(value="SELECT TO_CHAR(f.transaction_date,'Mon YYYY') as month, TO_CHAR(DATE_TRUNC('month',f.transaction_date),'YYYY-MM-DD') as monthDate, SUM(CASE WHEN f.type='INCOME' THEN f.amount ELSE 0 END) as income, SUM(CASE WHEN f.type='EXPENSE' THEN f.amount ELSE 0 END) as expense FROM finance_transactions f WHERE f.tenant_id=:tid AND f.transaction_date>=CURRENT_DATE-INTERVAL '6 months' GROUP BY month,monthDate ORDER BY monthDate", nativeQuery=true)
    List<Object[]> monthlyBreakdown(@Param("tid") Long tid);
}