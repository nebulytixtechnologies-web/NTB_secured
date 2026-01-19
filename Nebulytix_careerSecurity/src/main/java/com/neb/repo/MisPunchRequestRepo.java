package com.neb.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neb.entity.Employee;
import com.neb.entity.MisPunchRequest;
import com.neb.util.ApprovalStatus;



public interface MisPunchRequestRepo extends JpaRepository<MisPunchRequest, Long> {

	 Optional<MisPunchRequest> findByEmployeeAndPunchDate(Employee employee, LocalDate misPunchDate);

		List<MisPunchRequest> findAllByStatus(ApprovalStatus pending);

}
