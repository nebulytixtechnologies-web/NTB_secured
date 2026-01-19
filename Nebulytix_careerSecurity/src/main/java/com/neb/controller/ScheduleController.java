package com.neb.controller;

/**
 * --------------------------------------------------------------
 * Purpose :
 *   Handles REST API requests for scheduling management.
 *
 * Description :
 *   - Allows Admin or HR to create, view, and delete schedules.
 *   - Converts between Schedule entities and ScheduleDTOs to
 *     prevent circular references in JSON responses.
 *
 * Endpoints :
 *   ✅ POST   /api/schedules              → Create a new schedule
 *   ✅ GET    /api/schedules              → Get all schedules
 *   ✅ GET    /api/schedules/employee/{id} → Get schedules by employee ID
 *   ✅ DELETE /api/schedules/{id}         → Delete a schedule by ID
 * --------------------------------------------------------------
 */

import com.neb.dto.ScheduleDTO;
import com.neb.entity.Employee;
import com.neb.entity.Schedule;
import com.neb.service.ScheduleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
// Allow frontend access (React, Angular, etc.)
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    
    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO dto) {
        Schedule schedule = new Schedule();
        schedule.setTitle(dto.getTitle());
        schedule.setDescription(dto.getDescription());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setLocation(dto.getLocation());
        schedule.setStatus(dto.getStatus());
        // Link the Employee for whom the schedule is created
        Employee employee = new Employee();
        employee.setId(dto.getEmployeeId());
        schedule.setEmployee(employee);

        Schedule saved = scheduleService.createSchedule(schedule);

        ScheduleDTO response = convertToDTO(saved);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByEmployee(@PathVariable Long employeeId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesByEmployee(employeeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(schedules);
    }
    // ✅ Convert Entity to DTO
    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setLocation(schedule.getLocation());
        dto.setStatus(schedule.getStatus());
        if (schedule.getEmployee() != null) {
            dto.setEmployeeId(schedule.getEmployee().getId());
        }
       return dto;
    }
}
