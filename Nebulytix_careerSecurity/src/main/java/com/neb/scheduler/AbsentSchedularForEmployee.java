package com.neb.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.neb.entity.Employee;
import com.neb.entity.EmployeeLogInDetails;
import com.neb.repo.EmployeeLoginDetailsRepo;
import com.neb.repo.EmployeeRepository;
import com.neb.util.EmployeeDayStatus;

import jakarta.transaction.Transactional;


public class AbsentSchedularForEmployee {
	
	 @Autowired
	    private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmployeeLoginDetailsRepo loginRepo;
	
	@Scheduled(cron = "0 58 23 ? * MON-FRI")
	@Transactional
	public void markDailyAttendance() {

	    LocalDate today = LocalDate.now();
	    DayOfWeek day = today.getDayOfWeek();

	    List<Employee> employees = employeeRepository.findAll();

	    for (Employee emp : employees) {

	        // Avoid duplicates
	        if (loginRepo.existsByEmployeeAndLoginDate(emp, today)) {
	            continue;
	        }

	        EmployeeDayStatus status;

	        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
	            status = EmployeeDayStatus.WEEKEND;
	        } else {
	            status = EmployeeDayStatus.ABSENT;
	        }

	        EmployeeLogInDetails todaysAttendance = new EmployeeLogInDetails();
	        todaysAttendance.setEmployee(emp);
	        todaysAttendance.setDayStatus(status.toString());
	        todaysAttendance.setLoginTime(null);
	        todaysAttendance.setLogoutTime(null);
	        todaysAttendance.setTotalTime("00:00:00");
	        todaysAttendance.setArrivalTime(null);

	        loginRepo.save(todaysAttendance);
	    }
	}



}
