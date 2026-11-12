package com.cemetery.repository;
import com.cemetery.entity.Deceased; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface DeceasedRepository extends JpaRepository<Deceased,Long>{ List<Deceased> findByFamilyId(Long f); List<Deceased> findByPlotId(Long p); }
