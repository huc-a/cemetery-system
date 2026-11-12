package com.cemetery.repository;
import com.cemetery.entity.ServiceProduct; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface ServiceProductRepository extends JpaRepository<ServiceProduct,Long>{ List<ServiceProduct> findByStatus(Integer s); }
