package com.cemetery.repository;
import com.cemetery.entity.PaymentRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,Long>{ List<PaymentRecord> findByPayerId(Long p); List<PaymentRecord> findByBizType(String t); }
