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
	@Autowired
	private RatingListRepository ratingListRepository;
	@Autowired
	private SpecialityRepository specialityRepository;
	@Autowired
	private ApplicantRepository applicantRepository;
	@Autowired
	private MailSender mailSender;
	
	public Optional<RatingList> findById(Integer id) {
		return ratingListRepository.findById(id);
	}

	public RatingList initializeRatingList(Application application, Map<String, String> form) {
		Optional<RatingList> ratingListFromDb = findById(application.getId());
		RatingList ratingList = ratingListFromDb.orElse(new RatingList());
		
		ratingList.setId(application.getId());
		
		Double totalMark = calculateTotalMark(application.getZnoMarks(), application.getAttMark());
		ratingList.setTotalMark(totalMark);
				
		for (String key : form.keySet()) {
			if (key.equals("rejectionMessage") && !form.get(key).isEmpty()) {
				ratingList.setRejectionMessage(form.get(key));
				sendRejectionEmail(application, form.get(key));
			} else {
				ratingList.setRejectionMessage(null);
			}
		}
		
		for (String key : form.keySet()) {
			if (key.equals("accept")) {
				ratingList.setAccepted(true);
				ratingList.setRejectionMessage(null);
				sendAcceptionEmail(application);
			}
		}

		ratingList.setApplication(application);
		
		return ratingList;
	}
	
	public void sendAcceptionEmail(Application application) {
		String message = String.format(
				"Hello, %s %s! \n\n" +
						"\n" +
						"Your introductory application for the specialty \"%s\" accepted by the administrator.\n" +
						"You can track the results of the competitive selection for the chosen specialty in your personal account.",
					application.getApplicant().getUser().getFirstName(),
					application.getApplicant().getUser().getLastName(),
					application.getSpeciality().getTitle()					
				);

		mailSender.send(application.getApplicant().getUser().getEmail(), "Introductory application for a specialty \"" + application.getSpeciality().getTitle() + "\" accepted", message);
	}
	
	public void sendRejectionEmail(Application application, String rejectionMessage) {
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
		Integer i = 1;
		Double totalMark = Double.valueOf(attMark);
		
		for (Integer znoMark : znoMarks.values()) {
			i += 1;
			totalMark += znoMark;
		}
		
		totalMark = totalMark/i;
		
		return totalMark;
	}

	public Map<Speciality, Integer> parseApplicationsBySpeciality() {
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
	
	public List<Speciality> findSpecialitiesByApplicant(Integer applicantId) {
		List<Integer> specialitiesByApplicantFromDb = ratingListRepository.findSpecialitiesByApplicant(applicantId);
		List<Speciality> specialitiesList = specialityRepository.findAll();
		
		return specialitiesList.stream()
				.filter(speciality -> specialitiesByApplicantFromDb.stream()
						.anyMatch(specialityId -> specialityId.equals(speciality.getId())))
				.collect(Collectors.toList());
	}
	
	public Set<SpecialityDTO> parseSpecialitiesByApplicant(Integer applicantId) {
		List<Speciality> specialities = findSpecialitiesByApplicant(applicantId);

		return specialities.stream().map(speciality -> new SpecialityDTO(speciality.getId(), speciality.getTitle()))
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public List<RatingList> findNotAcceptedApps() {
		return ratingListRepository.findByAcceptedFalseAndRejectionMessageIsNull();
	}

	public void completeRecruitmentBySpeciality(Speciality speciality) {
		Set<Applicant> enrolledApplicants = getEnrolledApplicantsBySpeciality(speciality);
		enrolledApplicants.stream().forEach(applicant -> sendEnrolledEmail(applicant, speciality));
//		enrolledApplicants.stream().forEach(applicant -> System.out.println(applicant.getUser().getFirstName() + " " + applicant.getUser().getLastName() + ", You are accepted!"));
	}

	public Set<Applicant> getEnrolledApplicantsBySpeciality(Speciality speciality) {
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

	public void sendEnrolledEmail(Applicant applicant, Speciality speciality) {
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
