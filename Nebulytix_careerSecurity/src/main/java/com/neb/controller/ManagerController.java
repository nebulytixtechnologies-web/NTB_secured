package com.neb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neb.dto.ResponseMessage;
import com.neb.dto.employee.EmployeeProfileDto;
import com.neb.service.EmployeeService;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

	@Autowired
    private EmployeeService employeeService;

    
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/me")
    public ResponseEntity<ResponseMessage<EmployeeProfileDto>> getMyProfile() {

        EmployeeProfileDto dto = employeeService.getMyProfile();

        return ResponseEntity.ok(
                new ResponseMessage<>(200, "SUCCESS", "Manager profile fetched", dto)
        );
    }
}
