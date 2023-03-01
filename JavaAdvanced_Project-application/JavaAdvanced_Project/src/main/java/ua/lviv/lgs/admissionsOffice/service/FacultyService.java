package ua.lviv.lgs.admissionsOffice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.lgs.admissionsOffice.dao.FacultyRepository;
import ua.lviv.lgs.admissionsOffice.dao.SubjectRepository;
import ua.lviv.lgs.admissionsOffice.domain.Faculty;
import ua.lviv.lgs.admissionsOffice.domain.Subject;

@Service
public class FacultyService {
	@Autowired
	private FacultyRepository facultyRepository;
	@Autowired
	private SubjectRepository subjectRepository;

	public List<Faculty> findAll() {
		return facultyRepository.findAll();
	}

	public boolean createFaculty(Faculty faculty, Map<String, String> form) {
		Optional<Faculty> facultyFromDb = facultyRepository.findByTitle(faculty.getTitle());
		
		if (facultyFromDb.isPresent()) {
			return false;
		}

		facultyRepository.save(faculty);
		updateFaculty(faculty, form);
		return true;
	}

	public void updateFaculty(Faculty faculty, Map<String, String> form) {
		Set<Subject> examSubjects = parseExamSubjects(form);
		faculty.setExamSubjects(examSubjects);

		facultyRepository.save(faculty);
	}

	public void deleteFaculty(Faculty faculty) {
		facultyRepository.delete(faculty);
	}

	public Set<Subject> parseExamSubjects(Map<String, String> form) {
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