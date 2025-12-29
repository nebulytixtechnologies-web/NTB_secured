package com.neb.dto;


import lombok.Data;

@Data
public class UpdateEmployeeRequestDto {

	private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String cardNumber;
    private String jobRole;     
    private String designation;
    private String department;
    private String gender;
    private Double salary;
    private int paidLeaves;
    
   
}
