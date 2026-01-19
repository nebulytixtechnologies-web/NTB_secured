package com.neb.service.impl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.neb.dto.EmployeeDTO;
import com.neb.dto.EmployeeRegulationDTO;
import com.neb.constants.WorkStatus;
import com.neb.dto.AddDailyReportRequestDto;
import com.neb.dto.EmployeeLeaveDTO;
import com.neb.dto.WorkResponseDto;
import com.neb.dto.employee.AddEmployeeRequest;
import com.neb.dto.employee.EmployeeProfileDto;
import com.neb.dto.employee.UpdateEmployeeRequestDto;
import com.neb.dto.employee.UpdateEmployeeResponseDto;
import com.neb.entity.DailyReport;
import com.neb.entity.Employee;
import com.neb.entity.EmployeeLeaveBalance;
import com.neb.entity.EmployeeLeaves;
import com.neb.entity.EmployeeLogInDetails;
import com.neb.entity.MisPunchRequest;
import com.neb.entity.Payslip;
import com.neb.entity.Users;
import com.neb.entity.Work;
import com.neb.exception.AlreadyCheckedOutException;
import com.neb.exception.CustomeException;
import com.neb.exception.EmployeeAlreadyLoggedInException;
import com.neb.exception.EmployeeNotFoundException;
import com.neb.exception.EmployeeNotLoggedInException;
import com.neb.exception.InsufficientLeaveBalanceException;
import com.neb.exception.InvalidDateRangeException;
import com.neb.repo.DailyReportRepository;
import com.neb.repo.EmployeeLeaveBalanceRepo;
import com.neb.repo.EmployeeLeaveRepository;
import com.neb.repo.EmployeeLoginDetailsRepo;
import com.neb.repo.MisPunchRequestRepo;
import com.neb.repo.EmployeeRepository;
import com.neb.repo.PayslipRepository;
import com.neb.repo.UsersRepository;
import com.neb.repo.WorkRepository;
import com.neb.service.EmployeeService;
import com.neb.service.NotificationService;
import com.neb.util.ApprovalStatus;
import com.neb.util.AuthUtils;
import com.neb.util.EmployeeDayStatus;
import com.neb.util.EmployeeLeaveType;
import com.neb.util.PdfGeneratorUtil;

import jakarta.transaction.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PayslipRepository payslipRepo;
    @Autowired
	private EmployeeLoginDetailsRepo empLoginRepo;
    @Autowired
    private ModelMapper mapper;
    @Autowired
	private EmployeeLeaveRepository empLeaveRepo;
//    @Autowired
//	private EmployeeLeavePolicyRepo leavePolicyRepo;
	@Autowired
	private NotificationService notificationService;
	@Autowired
private MisPunchRequestRepo MisPunchRequestRepo;
	
	
    @Autowired
	private EmployeeLeaveBalanceRepo leaveBalanceRepo;
    @Autowired
    private WorkRepository workRepository;
    @Autowired
    private DailyReportRepository dailyReportRepository;
    @Autowired
    private UsersRepository usersRepository;

    
    @Value("${payslip.base-folder}")
    private String baseFolder;
    
    @Value("${task.attachment}")
    private String attachmentFolder;
    
    @Value("${profile.picture.folder}")
    private String baseProfileFolder;
    
    
    
    @Override
	public Long createEmployee(AddEmployeeRequest empReq, Users user) {
    	Employee employee = mapper.map(empReq, Employee.class);
    	employee.setUser(user);
    	System.out.println(employee);
    	Employee saveEmployee = employeeRepository.save(employee);
		return saveEmployee.getId();
	}
    
    @Override
	public EmployeeProfileDto getMyProfile() {
		
		//  Get logged-in user email
        String email = AuthUtils.getCurrentUserEmail();
        if (email == null) throw new RuntimeException("User not authenticated");

        //  Fetch user entity
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  Fetch employee profile
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        
        //  Map to DTO
        EmployeeProfileDto empDetailsDto = mapper.map(emp, EmployeeProfileDto.class);
        empDetailsDto.setEmail(user.getEmail());
        
        return empDetailsDto;
	}
    
    //Getting employee By ID
    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new CustomeException("Employee not found with id: "+id));
    }
   
	@Override
	public Payslip generatePayslip(Long employeeId, String monthYear) throws Exception{
		
		
		Employee emp = employeeRepository.findById(employeeId)
	                            .orElseThrow(() -> new CustomeException("Employee not found with id: "+employeeId));
		
		Payslip p = new Payslip();
        p.setEmployee(emp);
        p.setPayslipMonth(monthYear);
        p.setGeneratedDate(LocalDateTime.now());
        p.setLocation("FLAT NO 501B,PSR PRIME TOWERS,BESIDE DLF,GACHIBOWLI,500032");

        // Salary Calculations
        double salary = emp.getSalary();
        p.setBasic(salary * 0.53);
        p.setHra(salary * 0.20);
        p.setFlexi(salary * 0.27);
        double gross = p.getBasic() + p.getHra() + p.getFlexi();//
        p.setGrossSalary(gross);
        
        // Deductions
        p.setPfDeduction(p.getBasic() * 0.12);
        p.setProfTaxDeduction(200.0);
        double ded = p.getPfDeduction() + p.getProfTaxDeduction();
        p.setTotalDeductions(ded);
        
        // Net Salary Calculation
        double net = gross - ded;
        p.setNetSalary(net);
        p.setBalance(gross);
        p.setAggrgDeduction(ded);
        p.setIncHdSalary(net);
        p.setTaxCredit(net*0.05);//random values added
     
        // Save payslip record
        p = payslipRepo.save(p);
        
        // PDF File Generation
        String fileName = emp.getCardNumber() + "_payslip" + monthYear.replace(" ", "_") + ".pdf";
        String folderPath = baseFolder + "/" + monthYear.replace(" ", "_");
        Files.createDirectories(Paths.get(folderPath));
        String fullPath = folderPath + "/" + fileName;

        
        byte[] pdfBytes = PdfGeneratorUtil.createPayslipPdf(emp, p);
        Files.write(Paths.get(fullPath), pdfBytes);

        p.setPdfPath(fullPath);
        p.setFileName(fileName);
        payslipRepo.save(p);

        return p;
	}
	
    public List<Work> getTasksByEmployee(Long employeeId) {
        Employee emp = getEmployeeById(employeeId);
        List<Work> workListbyEmployee = workRepository.findByEmployee(emp);
        if(workListbyEmployee==null) {
        	throw new CustomeException("work list is empty for employee with id: "+emp.getId());
        }
        return workListbyEmployee;
    }
    
    @Override
    public WorkResponseDto submitReport(Long taskId, String statusStr, String reportDetails, MultipartFile reportAttachment, LocalDate submittedDate) {
        Work task = workRepository.findById(taskId)
                .orElseThrow(() -> new CustomeException("Task not found with taskId: " + taskId));

        task.setReportDetails(reportDetails);
        task.setSubmittedDate(submittedDate);
        task.setStatus(WorkStatus.valueOf(statusStr));

        // Handle file upload
        if (reportAttachment != null && !reportAttachment.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + reportAttachment.getOriginalFilename();
                Path uploadPath = Paths.get(attachmentFolder);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(reportAttachment.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save relative URL for frontend access
                String fileUrl = "/uploads/tasks/" + fileName;
                task.setReportAttachmentUrl(fileUrl);
            } catch (IOException e) {
                throw new CustomeException("Failed to save attachment: " + e.getMessage());
            }
        }

        Work savedWork = workRepository.save(task);
        WorkResponseDto workRes = new WorkResponseDto();
        
        workRes.setId(savedWork.getId());
        workRes.setTitle(savedWork.getTitle());
        workRes.setAssignedDate(savedWork.getAssignedDate());
        workRes.setDueDate(savedWork.getDueDate());
        workRes.setStatus(savedWork.getStatus());
        workRes.setReportDetails(savedWork.getReportDetails());
        workRes.setSubmittedDate(savedWork.getSubmittedDate());
        workRes.setReportAttachmentUrl(savedWork.getReportAttachmentUrl());
        workRes.setAttachmentUrl(savedWork.getAttachmentUrl());
        workRes.setEmployeeId(savedWork.getEmployee().getId());
        workRes.setEmployeeName(savedWork.getEmployee().getFirstName());
      
        
        return workRes ;
    }

	@Override
	public String submitDailyReport(AddDailyReportRequestDto request) {
	
		Employee emp = employeeRepository.findById(request.getEmployee_id())
	            .orElseThrow(() -> new CustomeException("employee not found with id: " + request.getEmployee_id()));

	        LocalDate date = request.getReportDate();
	        if (date == null) {
	            throw new CustomeException("reportDate must not be null");
	        }
	        
	        Optional<DailyReport> existingOpt = dailyReportRepository.findByEmployeeIdAndReportDate(emp.getId(), date);
		
	        DailyReport report;
         if (existingOpt.isPresent()) {
            // update existing
            report = existingOpt.get();
            report.setSummary(request.getSummary());
            
          } else {
            // create new
            report = new DailyReport();
            report.setEmployee(emp);
            report.setReportDate(date);
            report.setSummary(request.getSummary());
         }

        DailyReport saved = dailyReportRepository.save(report);

        if (saved != null && saved.getId() != null) {
            return existingOpt.isPresent() ? "Report updated successfully!" : "Report submitted successfully!";
        } else {
            return "failed to submit report";
        }
    }
	
	@Override
	public String uploadProfilePicture(Long employeeId, MultipartFile file) {
	    Employee emp = employeeRepository.findById(employeeId)
	        .orElseThrow(() -> new CustomeException("employee not found with id: " + employeeId));

	    if (file == null || file.isEmpty()) {
	        throw new CustomeException("No file provided");
	    }

	    // Validate content type (image) - basic check
	    String contentType = file.getContentType();
	    if (contentType == null || !contentType.startsWith("image/")) {
	        throw new CustomeException("Only image files are allowed");
	    }

	    //check size (e.g., <= 5MB)
	    long maxSize = 5 * 1024 * 1024;
	    if (file.getSize() > maxSize) {
	        throw new CustomeException("File too large. Max allowed is 5 MB");
	    }

	    // Build filename - use UUID to avoid name collisions
	    String original = file.getOriginalFilename();
	    String ext = "";
	    if (original != null && original.contains(".")) {
	        ext = original.substring(original.lastIndexOf('.'));
	    }
	    String filename = java.util.UUID.randomUUID().toString() + ext;

	    try {
	        // base folder from property
	        Path uploadDir = Paths.get(baseProfileFolder);
	        if (!Files.exists(uploadDir)) {
	            Files.createDirectories(uploadDir);
	        }

	        Path target = uploadDir.resolve(filename);
	        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

	        // public URL served by resource handler
	        String publicUrl = "/uploads/profiles/" + filename;

	        // update employee entity
	        emp.setProfilePictureUrl(publicUrl);
	        emp.setProfilePicturePath(target.toAbsolutePath().toString());
	        employeeRepository.save(emp);

	        return publicUrl;
	    } catch (IOException e) {
	        throw new CustomeException("Failed to save profile picture: " + e.getMessage());
	    }
	}	
	
	@Override
	public boolean deleteProfilePicture(Long employeeId) {

	    Employee emp = employeeRepository.findById(employeeId)
	            .orElseThrow(() -> new RuntimeException("Employee not found"));

	    String imagePath = emp.getProfilePicturePath(); // FULL path (absolute path)

	    if (imagePath == null || imagePath.isEmpty()) {
	        return false; // nothing to delete
	    }

	    File file = new File(imagePath);

	    // delete file physically from folder
	    if (file.exists()) {
	        boolean deleted = file.delete();
	        System.out.println("File deleted: " + deleted);
	    }

	    // remove from DB
	    emp.setProfilePicturePath(null);
	    emp.setProfilePictureUrl(null); // if you use URLs
	    employeeRepository.save(emp);

	    return true;
	}
	
	@Transactional
	@Override
	public EmployeeDTO webClockin(Long employeeId)  {

	   
	    Employee employee = employeeRepository.findById(employeeId)
	            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

	    EmployeeLogInDetails existing =
	            empLoginRepo.findTopByEmployeeAndLogoutTimeIsNull(employee);

	    if (existing != null) {
	    	throw new EmployeeAlreadyLoggedInException("Employee already logged in");
	    }

	    List<EmployeeLogInDetails> allRecords =
	            empLoginRepo.findByEmployee(employee);

	    boolean checkedOutToday = allRecords.stream()
	            .anyMatch(r ->
	                    r.getLoginTime().toLocalDate().equals(LocalDate.now())
	                            && r.getLogoutTime() != null);

	    if (checkedOutToday) {
	    	throw new AlreadyCheckedOutException("Employee already checked out today");
	    }

	    EmployeeLogInDetails login = new EmployeeLogInDetails();
	    login.setEmployee(employee);
	    login.setLoginTime(LocalDateTime.now());
	    login.setDayStatus(EmployeeDayStatus.PRESENT + " | Missing Swipe");

	    LocalTime cutOff = LocalTime.of(9, 30);

	    if (!LocalTime.now().isAfter(cutOff)) {
	        login.setArrivalTime("On Time");
	    } else {
	        Duration d = Duration.between(cutOff, login.getLoginTime());
	        login.setArrivalTime(
	                String.format("%02d:%02d:%02d Late",
	                        d.toHours(),
	                        d.toMinutes() % 60,
	                        d.getSeconds() % 60)
	        );
	    }

	    EmployeeLogInDetails saved = empLoginRepo.save(login);

	    EmployeeDTO dto = new EmployeeDTO();
	    dto.setId(employee.getId());
	    dto.setFirstName(employee.getFirstName());
	    dto.setLastName(employee.getLastName());
	    dto.setDisplayName(employee.getFirstName() + " " + employee.getLastName());
	    dto.setLoginTime(saved.getLoginTime());
	    dto.setLogoutTime(saved.getLogoutTime());
	    dto.setDuration(saved.getTotalTime());
	    dto.setDayStatus(saved.getDayStatus());
	    dto.setArrivalTime(saved.getArrivalTime());

	    return dto;
	}

	@Transactional
	@Override
	public EmployeeDTO webClockout(Long employeeId) {

	  
	    Employee employee = employeeRepository.findById(employeeId)
	            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
	    


	    EmployeeLogInDetails details =
	            empLoginRepo.findTopByEmployeeAndLogoutTimeIsNull(employee);

	    if (details == null) {
	    	throw new EmployeeNotLoggedInException("Employee not logged in");
	    }

	    LocalDateTime logoutTime = LocalDateTime.now();
	    details.setLogoutTime(logoutTime);

	    Duration duration = Duration.between(details.getLoginTime(), logoutTime);

	    String total = String.format("%02d:%02d:%02d",
	            duration.toHours(),
	            duration.toMinutes() % 60,
	            duration.getSeconds() % 60);

	    details.setTotalTime(total);

	    if (duration.toHours() >= 9) {
	        details.setDayStatus(EmployeeDayStatus.PRESENT+" | Missing Swipe");
	    } else if (duration.toHours() > 4) {
	        details.setDayStatus(EmployeeDayStatus.HALFDAY+" | Missing Swipe");
	    } else {
	        details.setDayStatus("Less Than 4 hours");
	    }
	    empLoginRepo.save(details);

	    EmployeeDTO dto = new EmployeeDTO();
	    dto.setId(employee.getId());
	    dto.setFirstName(employee.getFirstName());
	    dto.setLastName(employee.getLastName());
	    dto.setDisplayName(employee.getFirstName() + " " + employee.getLastName());
	    dto.setLoginTime(details.getLoginTime());
	    dto.setLogoutTime(logoutTime);
	    dto.setDuration(total);
//	    dto.setEmpStatus(employee.getEmpStatus());
	    dto.setDayStatus(details.getDayStatus());
	    dto.setArrivalTime(details.getArrivalTime());

	    return dto;
	}
	


	@Transactional
	@Override
	public EmployeeLeaveDTO applyLeave(EmployeeLeaveDTO dto) {

	    //  Validate dates
	    if (dto.getEnd().isBefore(dto.getStart())) {
	        throw new InvalidDateRangeException("End date cannot be before start date");
	    }

	    //  Fetch Employee USING employeeId ONLY
	    Employee employee = employeeRepository.findById(dto.getEmployeeId())
	            .orElseThrow(() -> new EmployeeNotFoundException("Invalid Employee Id"));
	    System.out.println(employee);

	    int year = LocalDate.now().getYear();

	    //  Fetch Leave Balance
	    EmployeeLeaveBalance balance =
	            leaveBalanceRepo.findByEmployeeAndLeaveTypeAndCurrentYear(
	                    employee, dto.getLeaveType(), year
	            ).orElseThrow(() ->
	                    new RuntimeException("Leave balance not initialized")
	            );

	    //  Calculate days
	    long requestedDays =
	            ChronoUnit.DAYS.between(dto.getStart(), dto.getEnd()) + 1;

	    if (balance.getRemaining() < requestedDays) {
	        throw new InsufficientLeaveBalanceException("Insufficient leave balance");
	    }
	    // s Save Leave
	    EmployeeLeaves leave = new EmployeeLeaves();
	    leave.setEmployee(employee);                 // ✅ CRITICAL
	    leave.setLeaveType(dto.getLeaveType());
	    leave.setLeaveStatus(ApprovalStatus.PENDING);
	    leave.setStartDate(dto.getStart());
	    leave.setEndDate(dto.getEnd());
	    leave.setReason(dto.getReason());
	    leave.setTotalDays(requestedDays);
	    leave.setCurrentYear(year);
	    leave.setCurrentMonth(LocalDate.now().getMonthValue());
	    leave.setAppliedDate(LocalDate.now());

	    EmployeeLeaves savedLeave = empLeaveRepo.save(leave);

	    //  Notify HR
	    notificationService.notifyHrLeaveApplied(savedLeave);

	    //  Response DTO
	    EmployeeLeaveDTO response = new EmployeeLeaveDTO();
	    response.setId(savedLeave.getId());                 // Leave ID
	    response.setEmployeeId(employee.getId());           // Employee ID
	    response.setLeaveType(savedLeave.getLeaveType());
	    response.setStart(savedLeave.getStartDate());
	    response.setEnd(savedLeave.getEndDate());
	    response.setReason(savedLeave.getReason());
	    response.setTotalDays(savedLeave.getTotalDays());
	    response.setLeaveStatus(savedLeave.getLeaveStatus());

	    return response;
	}

	@Transactional
	   public EmployeeLeaveDTO applyWFH(EmployeeLeaveDTO wfh) {

	       
	       if (wfh.getEnd().isBefore(wfh.getStart())) {
	           throw new IllegalArgumentException("End date cannot be before start date");
	       }

	      
	       Employee employee = employeeRepository.findById(wfh.getEmployeeId())
	               .orElseThrow(() -> new EmployeeNotFoundException("Invalid Employee Id"));

	      
	       EmployeeLeaveBalance empAllowedWfh =
	    		   leaveBalanceRepo.findByLeaveTypeAndEmployee_Id(
	                       EmployeeLeaveType.WFH,      // ✅ MODIFIED
	                       employee.getId()
	               );

	      
	       if (empAllowedWfh == null) {
	           throw new RuntimeException("WFH leave balance not configured for this employee");
	       }

	       long days = ChronoUnit.DAYS.between(wfh.getStart(), wfh.getEnd()) + 1;

	       if (empAllowedWfh.getRemaining() < days) {
	           throw new RuntimeException("No more Work From Home balance available");
	       }

	     
	       empAllowedWfh.setUsed(empAllowedWfh.getUsed() + days);
	       empAllowedWfh.setRemaining(empAllowedWfh.getRemaining() - days);
	       leaveBalanceRepo.save(empAllowedWfh);

	
	       EmployeeLeaves empLeave = new EmployeeLeaves();
	       empLeave.setStartDate(wfh.getStart());
	       empLeave.setEndDate(wfh.getEnd());
	       empLeave.setLeaveType(EmployeeLeaveType.WFH);   // ✅ MODIFIED
	       empLeave.setEmployee(employee);
	       empLeave.setReason(wfh.getReason());
	       empLeave.setTotalDays(days);
	       empLeave.setAppliedDate(LocalDate.now());
	       empLeave.setLeaveStatus(ApprovalStatus.PENDING); // ✅ GOOD PRACTICE

	       EmployeeLeaves empSaved = empLeaveRepo.save(empLeave);
	       notificationService.notifyHrLeaveApplied(empSaved);

	       // ✅ RESPONSE DTO
	       EmployeeLeaveDTO empResWfhDto = new EmployeeLeaveDTO();
	       empResWfhDto.setStart(empSaved.getStartDate());
	       empResWfhDto.setEnd(empSaved.getEndDate());
	       empResWfhDto.setEmployeeId(empSaved.getEmployee().getId());
	       empResWfhDto.setLeaveStatus(empSaved.getLeaveStatus());
	       empResWfhDto.setReason(empSaved.getReason());
	       empResWfhDto.setTotalDays(days);
	       empResWfhDto.setLeaveType(empSaved.getLeaveType());
	       empResWfhDto.setId(empSaved.getId());

	       return empResWfhDto;
	   }



	@Override
	public UpdateEmployeeResponseDto updateEmployee(Long employeeId, UpdateEmployeeRequestDto dto) {
		 Employee employee = employeeRepository.findById(employeeId)
	                .orElseThrow(() ->
	                        new RuntimeException("Employee not found with id: " + employeeId));

	        // ===== Update Employee fields =====
	       if(dto.getFirstName() != null) employee.setFirstName(dto.getFirstName());
	       if(dto.getLastName()!=null) employee.setLastName(dto.getLastName());
	       if(dto.getMobile()!=null)  employee.setMobile(dto.getMobile());
	       if(dto.getCardNumber()!=null) employee.setCardNumber(dto.getCardNumber());
	       if(dto.getDepartment()!=null)  employee.setDepartment(dto.getDepartment());
	       if(dto.getDesignation()!= null) employee.setDesignation(dto.getDesignation());
	       if(dto.getGender() !=null) employee.setGender(dto.getGender());
	       employee.setPaidLeaves(dto.getPaidLeaves());
            
	        // ===== Update User email =====
	        Users user = employee.getUser();
	        if (user != null && dto.getEmail() != null) { 
	            user.setEmail(dto.getEmail());
	        }

	        Employee savedEmployee = employeeRepository.save(employee);
            
	        
	        UpdateEmployeeResponseDto response = new UpdateEmployeeResponseDto();

	        response.setId(employee.getId());
	        response.setFirstName(employee.getFirstName());
	        response.setLastName(employee.getLastName());
	        response.setMobile(employee.getMobile());
	        response.setCardNumber(employee.getCardNumber());
	        response.setGender(employee.getGender());
	        response.setJoiningDate(employee.getJoiningDate());
	        response.setDaysPresent(employee.getDaysPresent());
	        response.setPaidLeaves(employee.getPaidLeaves());
            response.setDepartment(employee.getDepartment());
            response.setDesignation(employee.getDesignation());
//	         User data
	        Users user1 = employee.getUser();
	        if (user1 != null) {
	            response.setEmail(user1.getEmail());
	           
	        }

          return response;
	}

	@Override
	public String regularize(EmployeeRegulationDTO regulation) 
	{
		 Employee employee = employeeRepository.findById(regulation.getId())
		            .orElseThrow(() -> new EmployeeNotFoundException("Invalid Employee Id"));		 
		 EmployeeLogInDetails regulariseEmpDetails = empLoginRepo.findByLoginDateAndEmployee(regulation.getPunchDate(),employee)
		 .orElseThrow(()->new RuntimeException("Login Details Not Found for that Date "+regulation.getPunchDate()));

			if((regulariseEmpDetails.getLoginTime()==null||regulariseEmpDetails.getLogoutTime()==null) || (regulariseEmpDetails.getLoginTime()==null && regulariseEmpDetails.getLogoutTime()==null)&& regulariseEmpDetails.getDayStatus().equals(EmployeeDayStatus.ABSENT.toString())) {
				
				MisPunchRequest mpr = new MisPunchRequest();
				mpr.setEmployee(employee);
				mpr.setPunchDate(regulation.getPunchDate());
				mpr.setReason(regulation.getReason());
				mpr.setAppliedAt(LocalDateTime.now());
				mpr.setStatus(ApprovalStatus.PENDING);
				mpr.setLoginTime(regulation.getLoginTime());
				mpr.setLogoutTime(regulation.getLogoutTime());
				
				MisPunchRequestRepo.save(mpr);
				
			}else {
				throw new RuntimeException("cannot regularize");
			}
		
		return "Regulation Applied Successfully";
	}

	

}
	       
