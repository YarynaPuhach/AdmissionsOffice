package ua.lviv.lgs.admissionsOffice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.lgs.admissionsOffice.domain.Applicant;

public interface ApplicantRepository extends JpaRepository<Applicant, Integer>{

}
