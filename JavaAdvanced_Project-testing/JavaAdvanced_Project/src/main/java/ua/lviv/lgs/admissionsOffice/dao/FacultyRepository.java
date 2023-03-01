package ua.lviv.lgs.admissionsOffice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.lgs.admissionsOffice.domain.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Integer>{
	
	Optional<Faculty> findByTitle(String title);

}
