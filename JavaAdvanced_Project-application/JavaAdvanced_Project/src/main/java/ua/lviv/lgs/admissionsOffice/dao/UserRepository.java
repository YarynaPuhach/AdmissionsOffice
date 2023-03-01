package ua.lviv.lgs.admissionsOffice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.lgs.admissionsOffice.domain.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	User findByEmail(String email);
	
	User findByActivationCode(String code);
	
}
