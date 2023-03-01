package ua.lviv.lgs.admissionsOffice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.lgs.admissionsOffice.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer>{

}
