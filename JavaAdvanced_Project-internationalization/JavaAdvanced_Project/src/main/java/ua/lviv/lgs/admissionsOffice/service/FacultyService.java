package ua.lviv.lgs.admissionsOffice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.lgs.admissionsOffice.dao.FacultyRepository;
import ua.lviv.lgs.admissionsOffice.dao.SubjectRepository;
import ua.lviv.lgs.admissionsOffice.domain.Faculty;
import ua.lviv.lgs.admissionsOffice.domain.Subject;

@Service
public class FacultyService {
	Logger logger = LoggerFactory.getLogger(FacultyService.class);
	
	@Autowired
	private FacultyRepository facultyRepository;
	@Autowired
	private SubjectRepository subjectRepository;

	public List<Faculty> findAll() {
		logger.trace("Getting all faculties from database...");
		
		return facultyRepository.findAll();
	}

	public boolean createFaculty(Faculty faculty, Map<String, String> form) {
		logger.trace("Adding new faculty to database...");
		
		Optional<Faculty> facultyFromDb = facultyRepository.findByTitle(faculty.getTitle());
		
		if (facultyFromDb.isPresent()) {
			logger.warn("Faculty with title \"" + facultyFromDb.get().getTitle() + "\" already exists in database...");
			return false;
		}
		
		logger.trace("Saving new faculty in database...");
		facultyRepository.save(faculty);
		updateFaculty(faculty, form);
		return true;
	}

	public void updateFaculty(Faculty faculty, Map<String, String> form) {
		logger.trace("Updating faculty in database...");
		
		Set<Subject> examSubjects = parseExamSubjects(form);
		faculty.setExamSubjects(examSubjects);

		logger.trace("Saving updated faculty in database...");
		facultyRepository.save(faculty);
	}

	public void deleteFaculty(Faculty faculty) {
		logger.trace("Deleting faculty from database...");
		
		facultyRepository.delete(faculty);
	}

	public Set<Subject> parseExamSubjects(Map<String, String> form) {
		logger.trace("Parsing exam subjects from Form Strings and mapping to Java Collection of objects...");
		
		Set<String> subjectTitles = subjectRepository.findAll().stream().map(Subject::getTitle).collect(Collectors.toSet());
		Set<Subject> examSubjects = new HashSet<>();

		for (String key : form.keySet()) {
			if (subjectTitles.contains(form.get(key))) {
				examSubjects.add(new Subject(Integer.valueOf(key), form.get(key)));
			}
		}
		return examSubjects;
	}
}