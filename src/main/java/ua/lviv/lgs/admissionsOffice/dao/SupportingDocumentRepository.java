package ua.lviv.lgs.admissionsOffice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.lgs.admissionsOffice.domain.Application;
import ua.lviv.lgs.admissionsOffice.domain.SupportingDocument;

@Repository
public interface SupportingDocumentRepository extends JpaRepository<SupportingDocument, String>{

	List<SupportingDocument> findAllByApplication(Application application);

}
