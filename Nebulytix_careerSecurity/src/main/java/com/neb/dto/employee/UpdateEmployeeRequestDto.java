package com.neb.dto.employee;


import lombok.Data;

@Data
public class UpdateEmployeeRequestDto {

	private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String cardNumber;   
    private String designation;
    private String department;
    private String gender;
    private int paidLeaves;
    
   
}
