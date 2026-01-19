package com.neb.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neb.entity.Employee;
import com.neb.entity.EmployeeLogInDetails;

public interface EmployeeLoginDetailsRepo extends JpaRepository<EmployeeLogInDetails, Long>{
	
	EmployeeLogInDetails  findTopByEmployeeAndLogoutTimeIsNullOrderByLoginTimeDesc(Employee employee);
	EmployeeLogInDetails findTopByEmployeeAndLogoutTimeIsNull(Employee employee);
	//Optional<EmployeeLogInDetails> findByEmployee(String id);
	List<EmployeeLogInDetails> findByEmployee(Employee employee);
	List<EmployeeLogInDetails> findByEmployeeAndDayStatus(Employee empId,String dayStatus);
	Optional<EmployeeLogInDetails> findByLoginDateAndEmployee(LocalDate date,Employee emp);
	boolean existsByEmployeeAndLoginDate(Employee emp, LocalDate today);

}
