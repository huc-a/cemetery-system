package com.cemetery.repository;
import com.cemetery.entity.ManagementFee; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface ManagementFeeRepository extends JpaRepository<ManagementFee,Long>{ List<ManagementFee> findByFamilyId(Long f); List<ManagementFee> findByStatus(String s); List<ManagementFee> findByDueDateBeforeAndStatus(LocalDate d,String s); List<ManagementFee> findByDueDateBetweenAndStatus(LocalDate a,LocalDate b,String s); }
