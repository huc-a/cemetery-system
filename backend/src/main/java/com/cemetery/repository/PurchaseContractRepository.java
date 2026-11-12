package com.cemetery.repository;
import com.cemetery.entity.PurchaseContract; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface PurchaseContractRepository extends JpaRepository<PurchaseContract,Long>{ List<PurchaseContract> findByBuyerId(Long b); boolean existsByPlotIdAndStatusNot(Long p,String s); List<PurchaseContract> findByStatus(String s); }
