package com.cemetery.repository;
import com.cemetery.entity.CemeteryArea; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface CemeteryAreaRepository extends JpaRepository<CemeteryArea,Long>{  }
