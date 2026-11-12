package com.cemetery.repository;
import com.cemetery.entity.Notice; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface NoticeRepository extends JpaRepository<Notice,Long>{ List<Notice> findAllByOrderByPinnedDescCreateTimeDesc(); }
