package com.cemetery.repository;
import com.cemetery.entity.VisitAppointment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.time.*;
public interface VisitAppointmentRepository extends JpaRepository<VisitAppointment,Long>{ List<VisitAppointment> findByFamilyId(Long f); long countByVisitDateAndTimeSlotAndStatusNot(LocalDate d,String slot,String s); List<VisitAppointment> findByVisitDate(LocalDate d); }
