
package com.neb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neb.constants.Role;
import com.neb.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

//    public boolean existsByEmail(String email);
//    public Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);
    @Query("""
    	    SELECT e FROM Employee e 
    	    JOIN e.user u 
    	    JOIN u.roles r
    	    WHERE r IN (:roles)
    	    """)
    	List<Employee> findAllByRolesHrAndEmployee(@Param("roles") List<Role> roles);
    
    @Query("""
    	    SELECT e FROM Employee e
    	    JOIN e.user u
    	    JOIN u.roles r
    	    WHERE r IN (Role.ROLE_HR,Role.ROLE_EMPLOYEE)
    	    """)
    	List<Employee> findAllHrAndEmployee();
    
    @Query("""
    	    SELECT e 
    	    FROM Employee e
    	    JOIN e.user u
    	    JOIN u.roles r
    	    WHERE r = Role.ROLE_EMPLOYEE
    	    """)
    	List<Employee> findAllOnlyEmployees();
}
