package com.neb.dto.employee;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateEmployeeResponseDto {

	private Long id;
	private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String cardNumber;
    private String gender;
    private LocalDate joiningDate;
    private int daysPresent;
    private int paidLeaves;
    private String designation;
    private String department;
    
    
}
