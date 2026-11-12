package com.cemetery.repository;
import com.cemetery.entity.GravePlot; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface GravePlotRepository extends JpaRepository<GravePlot,Long>{ List<GravePlot> findByAreaId(Long a); List<GravePlot> findByStatus(String s); long countByStatus(String s); long countByAreaId(Long a); }
