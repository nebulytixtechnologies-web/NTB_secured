package com.neb.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neb.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long>{
	
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
}
