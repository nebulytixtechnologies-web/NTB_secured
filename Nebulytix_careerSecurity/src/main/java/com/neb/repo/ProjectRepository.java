package com.neb.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neb.entity.Client;
import com.neb.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

//    // Get project list of that client
	List<Project> findByClient_Id(Long clientId);
    
	@Query("SELECT p FROM Project p WHERE p.client.id = :clientId")
	List<Project> findProjectsByClientId(@Param("clientId") Long clientId);
	 List<Project> findByClientId(Long clientId);
	 @Query("""
		        SELECT p
		        FROM Project p
		        JOIN p.employees e
		        WHERE e.id = :employeeId
		          AND p.status = 'ongoing'
		        ORDER BY p.startDate DESC
		    """)
		    List<Project> findOngoingProjectsByEmployeeId(@Param("employeeId") Long employeeId);
	 
	 @Query("""
		        SELECT p 
		        FROM Project p
		        JOIN p.employees e
		        WHERE e.id = :employeeId
		    """)
		    List<Project> findProjectsByEmployeeId(@Param("employeeId") Long employeeId);
}
