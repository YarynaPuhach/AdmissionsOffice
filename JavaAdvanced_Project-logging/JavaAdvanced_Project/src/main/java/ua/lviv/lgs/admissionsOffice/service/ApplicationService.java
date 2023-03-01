package ua.lviv.lgs.admissionsOffice.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.lgs.admissionsOffice.dao.ApplicationRepository;
import ua.lviv.lgs.admissionsOffice.dao.SubjectRepository;
import ua.lviv.lgs.admissionsOffice.domain.Applicant;
import ua.lviv.lgs.admissionsOffice.domain.Application;
import ua.lviv.lgs.admissionsOffice.domain.RatingList;
import ua.lviv.lgs.admissionsOffice.domain.Subject;
import ua.lviv.lgs.admissionsOffice.domain.SupportingDocument;

@Service
public class ApplicationService {
	Logger logger = LoggerFactory.getLogger(ApplicationService.class);
	
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private SubjectRepository subjectRepository;
	@Autowired
	SupportingDocumentService supportingDocumentService;
	@Autowired
	private RatingListService ratingListService;
	
	public List<Application> findByApplicant(Applicant applicant) {
		logger.trace("Getting all applications by specified applicant from database...");
		
		return applicationRepository.findByApplicant(applicant);
	}
	
	public boolean createApplication(Application application, Map<String, String> form, MultipartFile[] supportingDocuments) throws IOException {
		logger.trace("Adding new application to database...");
		
		Optional<Application> applicationFromDb = applicationRepository.findByApplicantAndSpeciality(application.getApplicant(), application.getSpeciality());
		
		if (applicationFromDb.isPresent()) {
			logger.warn("Application with applicant " + applicationFromDb.get().getApplicant().getUser().getFirstName()
					+ " " + applicationFromDb.get().getApplicant().getUser().getLastName() + " and speciality \""
					+ applicationFromDb.get().getSpeciality().getTitle() + "\" already exists in database...");
			return false;
		}

		Map<Subject, Integer> znoMarks = parseZnoMarks(form);
		application.setZnoMarks(znoMarks);
		
		logger.trace("Saving new application in database...");
		applicationRepository.save(application);
		
		Set<SupportingDocument> supportingDocumentsSet = supportingDocumentService.initializeSupportingDocumentSet(application,	supportingDocuments);		
		application.setSupportingDocuments(supportingDocumentsSet);
		
		RatingList ratingList = ratingListService.initializeRatingList(application, form);
		application.setRatingList(ratingList);
		
		logger.trace("Saving updated application in database...");
		applicationRepository.save(application);
		return true;
	}

	public void updateApplication(Application application, Map<String, String> form, MultipartFile[] supportingDocuments) throws IOException {
		logger.trace("Updating application in database...");
		
		Map<Subject, Integer> znoMarks = parseZnoMarks(form);
		application.setZnoMarks(znoMarks);
		
		supportingDocumentService.deleteSupportingDocuments(form);
		
		Set<SupportingDocument> supportingDocumentsSet = supportingDocumentService.initializeSupportingDocumentSet(application,	supportingDocuments);		
		application.setSupportingDocuments(supportingDocumentsSet);
		
		RatingList ratingList = ratingListService.initializeRatingList(application, form);
		application.setRatingList(ratingList);
		
		logger.trace("Saving updated application in database...");
		applicationRepository.save(application);
	}
	
	public Map<String, String> getZnoMarksErrors(Map<String, String> form) {
		logger.trace("Checking ZNO Marks for input errors...");
		
		Set<Integer> subjectIds = subjectRepository.findAll().stream().map(Subject::getId).collect(Collectors.toSet());
		Map<String, String> znoMarksErrors = new HashMap<>();

		for (String key : form.keySet()) {
			if (key.startsWith("subject")) {
				Integer keyId = Integer.valueOf(key.replace("subject", ""));
				if (subjectIds.contains(keyId)) {
					Subject subject = subjectRepository.findById(keyId).get();
					if (form.get(key).isEmpty()) {
						znoMarksErrors.put(key + "Error", "Subject score field  " + subject.getTitle() + " cannot be empty!");
					}
					if (!form.get(key).isEmpty() && !form.get(key).matches("\\d+")) {
						znoMarksErrors.put(key + "Error", "Subject scores " + subject.getTitle()	+ "must be a number!");
					}
					if (!form.get(key).isEmpty() && form.get(key).matches("\\d+")) {
						if (Integer.valueOf(form.get(key)) < 100) {
							znoMarksErrors.put(key + "Error", "Subject scores  " + subject.getTitle()	+ "cannot be less than 100!");
						}
						if (Integer.valueOf(form.get(key)) > 200) {
							znoMarksErrors.put(key + "Error", "Subject scores " + subject.getTitle()	+ "cannot be more than 200!");
						}
					}
				}
			}
		}
		return znoMarksErrors;
	}
	
	public Map<Subject, Integer> parseZnoMarks(Map<String, String> form) {
		logger.trace("Parsing ZNO Marks from Form Strings and mapping to Java Collection of objects...");
		
		Set<Integer> subjectIds = subjectRepository.findAll().stream().map(Subject::getId).collect(Collectors.toSet());
		Map<Subject, Integer> znoMarks = new HashMap<>();

		for (String key : form.keySet()) {
			if (key.startsWith("subject")) {
				Integer keyId = Integer.valueOf(key.replace("subject", ""));
				if (subjectIds.contains(keyId)) {
					Subject subject = subjectRepository.findById(keyId).get();
					znoMarks.put(subject, Integer.valueOf(form.get(key)));
				}
			}
		}
		return znoMarks;
	}

	public void deleteApplication(Application application) {
		logger.trace("Deleting application from database...");
		
		applicationRepository.delete(application);		
	}

	public Map<Integer, String> getApplicationsStatus(List<Application> applicationsList) {
		logger.trace("Determining current application status...");
		
		Map<Integer, String> applicationsStatus = new HashMap<>();
		for (Application application : applicationsList) {
			if (!application.getRatingList().isAccepted() && application.getRatingList().getRejectionMessage() == null) {
				applicationsStatus.put(application.getId(), "Awaiting processing");
			} else if (!application.getRatingList().isAccepted() && application.getRatingList().getRejectionMessage() != null) {
				applicationsStatus.put(application.getId(), "Rejected");
			} else if (application.getRatingList().isAccepted()) {
				applicationsStatus.put(application.getId(), "Accepted");
			}			
		}
		return applicationsStatus;
	}
	
	public boolean checkForRejectedApplications(List<Application> applicationsList) {
		logger.trace("Checking applications list for rejected application status present...");
		
		boolean isRejectedApplicationPresent = false;
		
		for (Application application : applicationsList) {
			if (application != null && application.getRatingList().getRejectionMessage() != null) {
				isRejectedApplicationPresent = true;
				break;
			}
		}
		return isRejectedApplicationPresent;	
	}
}
