package com.neb.service;

import java.util.List;

import com.neb.dto.AddWorkRequestDto;
import com.neb.dto.EmployeeResponseDto;
import com.neb.dto.WorkResponseDto;
import com.neb.dto.client.AddClientRequest;
import com.neb.dto.client.ClientProfileDto;
import com.neb.entity.DailyReport;
import com.neb.entity.Project;
import com.neb.entity.Users;

public interface ClientService {

	public ClientProfileDto getMyProfile();
	public Long createClient(AddClientRequest addClientReq, Users user);
	
	 List<Project> getProjectsForLoggedInClient();

	    String getProjectStatus(Long projectId);

	    List<EmployeeResponseDto> getEmployeesByProject(Long projectId);

	    WorkResponseDto assignWorkToEmployee(Long projectId, AddWorkRequestDto dto);

	    List<WorkResponseDto> getWorkByProject(Long projectId);
	    public List<DailyReport> getReportsByProject(Long projectId);
}
