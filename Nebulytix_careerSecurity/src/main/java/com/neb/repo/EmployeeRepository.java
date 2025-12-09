
package com.neb.repo;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;

import com.neb.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

//    public boolean existsByEmail(String email);
//    public Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);
}
