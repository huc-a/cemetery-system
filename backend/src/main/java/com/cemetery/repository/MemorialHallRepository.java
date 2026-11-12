package com.cemetery.repository;
import com.cemetery.entity.MemorialHall; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface MemorialHallRepository extends JpaRepository<MemorialHall,Long>{ List<MemorialHall> findByFamilyId(Long f); Optional<MemorialHall> findByDeceasedId(Long d); }
