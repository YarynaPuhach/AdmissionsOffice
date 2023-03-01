package ua.lviv.lgs.admissionsOffice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.lgs.admissionsOffice.domain.Application;

public interface ApplicationRepository extends JpaRepository<Application, Integer>{

}
