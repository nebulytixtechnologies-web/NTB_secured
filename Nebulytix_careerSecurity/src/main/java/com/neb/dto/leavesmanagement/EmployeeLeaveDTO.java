package com.neb.dto.leavesmanagement;



import java.time.LocalDate;

import com.neb.enumclasses.ApprovalStatus;
import com.neb.enumclasses.EmployeeLeaveType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeLeaveDTO {
	
	
	
	private Long employeeId;
	private Long id; //Please use This id to send The Request (DB generated ID)
	private EmployeeLeaveType leaveType;//not using while Request but Using while Response
	private LocalDate start;
	private LocalDate end;
	private String reason;
	private Long totalDays;
	private ApprovalStatus leaveStatus;

}
