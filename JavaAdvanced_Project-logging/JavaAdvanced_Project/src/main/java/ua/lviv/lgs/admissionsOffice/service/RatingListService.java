package ua.lviv.lgs.admissionsOffice.service;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.lgs.admissionsOffice.dao.ApplicantRepository;
import ua.lviv.lgs.admissionsOffice.dao.RatingListRepository;
import ua.lviv.lgs.admissionsOffice.dao.SpecialityRepository;
import ua.lviv.lgs.admissionsOffice.domain.Applicant;
import ua.lviv.lgs.admissionsOffice.domain.Application;
import ua.lviv.lgs.admissionsOffice.domain.RatingList;
import ua.lviv.lgs.admissionsOffice.domain.Speciality;
import ua.lviv.lgs.admissionsOffice.domain.Subject;
import ua.lviv.lgs.admissionsOffice.dto.SpecialityDTO;

@Service
public class RatingListService {
	Logger logger = LoggerFactory.getLogger(RatingListService.class);
	
	@Autowired
	private RatingListRepository ratingListRepository;
	@Autowired
	private SpecialityRepository specialityRepository;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private MailSender mailSender;
	
	public Optional<RatingList> findById(Integer id) {
		logger.trace("Getting rating list by id=" + id + " from database...");
		
		return ratingListRepository.findById(id);
	}

	public RatingList initializeRatingList(Application application, Map<String, String> form) {
		logger.trace("Initializing rating list for specified application...");
		
		Optional<RatingList> ratingListFromDb = findById(application.getId());
		RatingList ratingList = ratingListFromDb.orElse(new RatingList());
		
		ratingList.setId(application.getId());
		
		Double totalMark = calculateTotalMark(application.getZnoMarks(), application.getAttMark());
		ratingList.setTotalMark(totalMark);
				
		checkApplicationForRejectionMessage(application, form, ratingList);
		
		checkApplicationForBeingAccepted(application, form, ratingList);

		ratingList.setApplication(application);
		
		return ratingList;
	}

	public void checkApplicationForRejectionMessage(Application application, Map<String, String> form, RatingList ratingList) {
		logger.trace("Checking application for rejection message present...");
		
		for (String key : form.keySet()) {
			if (key.equals("rejectionMessage") && !form.get(key).isEmpty()) {
				ratingList.setRejectionMessage(form.get(key));
				sendApplicationRejectionEmail(application, form.get(key));
			} else {
				ratingList.setRejectionMessage(null);
			}
		}
	}
	
	public void checkApplicationForBeingAccepted(Application application, Map<String, String> form,	RatingList ratingList) {
		logger.trace("Checking application for being accepted...");
		
		for (String key : form.keySet()) {
			if (key.equals("accept")) {
				ratingList.setAccepted(true);
				ratingList.setRejectionMessage(null);
				sendApplicationAcceptanceEmail(application);
			}
		}
	}
	
	public void sendApplicationAcceptanceEmail(Application application) {
		logger.trace("Sending application acceptance message to user's email...");
		
		String message = String.format(
				"Hello, %s %s! \n\n" +
						"\n" +
						"Your introductory application for the specialty \"%s\" accepted by the administrator.\n" +
						"You can track the results of the competitive selection for the chosen specialty in your personal account.",
					application.getApplicant().getUser().getFirstName(),
					application.getApplicant().getUser().getLastName(),
					application.getSpeciality().getTitle()					
				);

		mailSender.send(application.getApplicant().getUser().getEmail(), "Introductory application for a specialty \"" + application.getSpeciality().getTitle() + "\" принята", message);
	}
	
	public void sendApplicationRejectionEmail(Application application, String rejectionMessage) {
		logger.trace("Sending application rejection message to user's email...");
		
		String message = String.format(
				"Hello, %s %s! \n\n" +
						"Your introductory application for the specialty \"%s\" was rejected by the administrator for the following reason: \"%s\".\n" +
						"To participate in the competitive selection for the chosen specialty, please correct the identified shortcomings in the application in your personal account.",
					application.getApplicant().getUser().getFirstName(),
					application.getApplicant().getUser().getLastName(),
					application.getSpeciality().getTitle(),
					rejectionMessage					
				);

		mailSender.send(application.getApplicant().getUser().getEmail(), "Introductory application for a specialty \"" + application.getSpeciality().getTitle() + "\" rejected", message);
	}

	public Double calculateTotalMark(Map<Subject, Integer> znoMarks, Integer attMark) {
		logger.trace("Calculating application total mark...");
		
		Integer i = 1;
		Double totalMark = Double.valueOf(attMark);
		
		for (Integer znoMark : znoMarks.values()) {
			i += 1;
			totalMark += znoMark;
		}
		
		totalMark = totalMark/i;
		
		return totalMark;
	}

	public Map<Speciality, Integer> parseNumberOfApplicationsBySpeciality() {
		logger.trace("Parsing number of applications by specialty from DB array and mapping to Java Collection of objects...");
		
		List<Object[]> submittedAppsFromDb = ratingListRepository.countApplicationsBySpeciality();
		List<Speciality> specialitiesList = specialityRepository.findAll();
		Map<Speciality, Integer> submittedApps = new HashMap<>();
		
		for (Speciality speciality : specialitiesList) {
			for (Object[] object : submittedAppsFromDb) {
				
				if (((Integer) object[0]).equals(speciality.getId())) {
					submittedApps.put(speciality, ((BigInteger) object[1]).intValue());
					break;
				} else {
					submittedApps.put(speciality, 0);
				}
			}
		}
		return submittedApps;
	}
	
	public Map<Applicant, Double> parseApplicantsRankBySpeciality(Integer specialityId) {
		logger.trace("Parsing applicants rank by specialty from DB array and mapping to Java Collection of objects...");
		
		List<Object[]> applicantsRankFromDb = ratingListRepository.getApplicantsRankBySpeciality(specialityId);
		List<Applicant> applicantsList = applicantRepository.findAll();
		Map<Applicant, Double> applicantsRank = new HashMap<>();
		Comparator<Map.Entry<Applicant, Double>> mapValuesComparator = Comparator.comparing(Map.Entry::getValue);
		
		for (Applicant applicant : applicantsList) {
			for (Object[] object : applicantsRankFromDb) {
				if (((Integer) object[0]).equals(applicant.getId())) {
					applicantsRank.put(applicant, (Double) object[1]);
				}
			}
		}
		return applicantsRank.entrySet().stream().sorted(mapValuesComparator.reversed())
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(),
						(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}
	
	public List<Speciality> findSpecialitiesAppliedByApplicant(Integer applicantId) {
		logger.trace("Getting all specialities applied by applicant from database...");
		
		List<Integer> specialitiesByApplicantFromDb = ratingListRepository.findSpecialitiesByApplicant(applicantId);
		List<Speciality> specialitiesList = specialityRepository.findAll();
		
		return specialitiesList.stream()
				.filter(speciality -> specialitiesByApplicantFromDb.stream()
						.anyMatch(specialityId -> specialityId.equals(speciality.getId())))
				.collect(Collectors.toList());
	}
	
	public Set<SpecialityDTO> parseSpecialitiesAppliedByApplicant(Integer applicantId) {
		logger.trace("Parsing specialities applied by applicant and mapping to Set of Speciality DTO objects...");
		
		List<Speciality> specialities = findSpecialitiesAppliedByApplicant(applicantId);

		return specialities.stream().map(speciality -> new SpecialityDTO(speciality.getId(), speciality.getTitle()))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public List<RatingList> findNotAcceptedApps() {
		logger.trace("Getting all not accepted applications from database...");
		
		return ratingListRepository.findByAcceptedFalseAndRejectionMessageIsNull();
	}

	public void announceRecruitmentResultsBySpeciality(Speciality speciality) {
		logger.trace("Preparing to announce recruitment results by specified speciality...");
		
		Set<Applicant> enrolledApplicants = getEnrolledApplicantsBySpeciality(speciality);
		enrolledApplicants.stream().forEach(applicant -> sendApplicantEnrollmentEmail(applicant, speciality));
//		enrolledApplicants.stream().forEach(applicant -> System.out.println(applicant.getUser().getFirstName() + " " + applicant.getUser().getLastName() + ", Вы приняты!"));
	}

	public Set<Applicant> getEnrolledApplicantsBySpeciality(Speciality speciality) {
		logger.trace("Getting all enrolled applicants by speciality...");
		
		Map<Applicant, Double> applicantsRank = parseApplicantsRankBySpeciality(speciality.getId());
		Set<Applicant> enrolledApplicants = new LinkedHashSet<>();
		Integer i = 1;
		
		if (speciality.isRecruitmentCompleted()) {
			for (Entry<Applicant, Double> entry : applicantsRank.entrySet()) {
				if (i <= speciality.getEnrollmentPlan()) {
					enrolledApplicants.add(entry.getKey());
					i++;
				}
			}
		}
		return enrolledApplicants;
	}

	public void sendApplicantEnrollmentEmail(Applicant applicant, Speciality speciality) {
		logger.trace("Sending applicant enrollment message to user's email...");
		
		String message = String.format(
				"Hello, %s %s! \n\n" +
						"Congratulations! Based on the results of the competitive selection for the specialty \"%s\", you were among the applicants recommended for enrollment.\n" +
						"Please, within 10 days, submit the original documents to the Admissions Committee.",
					applicant.getUser().getFirstName(),
					applicant.getUser().getLastName(),
					speciality.getTitle()									
				);

		mailSender.send(applicant.getUser().getEmail(), "Recruitment for the specialty\"" + speciality.getTitle() + "\" is completed", message);
	}
}
