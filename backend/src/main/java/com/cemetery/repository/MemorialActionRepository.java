package com.cemetery.repository;
import com.cemetery.entity.MemorialAction; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface MemorialActionRepository extends JpaRepository<MemorialAction,Long>{ List<MemorialAction> findByHallIdOrderByCreateTimeDesc(Long h); }
