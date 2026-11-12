package com.cemetery.repository;
import com.cemetery.entity.WorkOrder; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface WorkOrderRepository extends JpaRepository<WorkOrder,Long>{ List<WorkOrder> findByAssigneeId(Long a); List<WorkOrder> findByStatus(String s); List<WorkOrder> findByReporterId(Long r); }
