package com.cemetery.repository;
import com.cemetery.entity.BurialRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface BurialRecordRepository extends JpaRepository<BurialRecord,Long>{ List<BurialRecord> findByFamilyId(Long f); }
