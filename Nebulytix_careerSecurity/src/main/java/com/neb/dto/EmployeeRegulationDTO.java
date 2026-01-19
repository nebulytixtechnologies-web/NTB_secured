package com.neb.dto;



import java.time.LocalDate;
import java.time.LocalDateTime;

import com.neb.entity.MisPunchRequest;
import com.neb.util.ApprovalStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRegulationDTO {

	private Long id;

	// ✅ Employee enters these
	private LocalDateTime loginTime;
	private LocalDateTime logoutTime;

	private LocalDate punchDate;
	private String reason;

	// HR side
	private ApprovalStatus status;
	private LocalDateTime appliedAt;
	private LocalDateTime actionAt;

	// ✅ Entity → DTO (response)
	public EmployeeRegulationDTO(MisPunchRequest request, LocalDateTime loginTime, LocalDateTime logoutTime) {

		this.id = request.getId();
		this.punchDate = request.getPunchDate();
		this.reason = request.getReason();
		this.status = request.getStatus();
		this.appliedAt = request.getAppliedAt();
		this.actionAt = request.getActionAt();

		this.loginTime = loginTime;
		this.logoutTime = logoutTime;
	}

	public EmployeeRegulationDTO(MisPunchRequest request) {

		this.id = request.getId();
		this.punchDate = request.getPunchDate();
		this.reason = request.getReason();
		this.status = request.getStatus();
		this.appliedAt = request.getAppliedAt();
		this.actionAt = request.getActionAt();
	}
}
