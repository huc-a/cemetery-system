package com.cemetery.repository;
import com.cemetery.entity.ServiceOrder; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder,Long>{ List<ServiceOrder> findByFamilyId(Long f); List<ServiceOrder> findByStatus(String s); }
