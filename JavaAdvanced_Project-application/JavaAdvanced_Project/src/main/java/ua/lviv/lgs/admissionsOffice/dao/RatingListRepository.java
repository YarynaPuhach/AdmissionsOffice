package ua.lviv.lgs.admissionsOffice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ua.lviv.lgs.admissionsOffice.domain.RatingList;

public interface RatingListRepository extends JpaRepository<RatingList, Integer>{

	@Query(value = "SELECT s.speciality_id AS speciality_id, count(s.speciality_id) AS submittedApps " +
						"FROM rating_list AS rl " +
							"INNER JOIN application AS app " +
								"ON rl.application_application_id = app.application_id " +
							"INNER JOIN speciality AS s " +
								"ON app.speciality_id = s.speciality_id " +
							"WHERE rl.accepted IS TRUE " +
							"GROUP BY s.speciality_id", nativeQuery = true)
	List<Object[]> countApplicationsBySpeciality();
	
	@Query(value = "SELECT a.user_user_id, rl.total_mark " +
						"FROM rating_list AS rl " +
							"INNER JOIN application AS app " +
								"ON rl.application_application_id = app.application_id " +
							"INNER JOIN applicant AS a " +
								"ON app.applicant_id = a.user_user_id " +    
							"WHERE rl.accepted IS TRUE AND app.speciality_id = ?1 " +
							"ORDER BY rl.total_mark DESC", nativeQuery = true)
	List<Object[]> getApplicantsRankBySpeciality(Integer specialityId);
	
	@Query(value = "SELECT s.speciality_id " +
						"FROM rating_list AS rl " +
							"INNER JOIN application AS app " +
								"ON rl.application_application_id = app.application_id " +
							"INNER JOIN speciality AS s " +
								"ON app.speciality_id = s.speciality_id " +
							"WHERE rl.accepted IS TRUE AND app.applicant_id = ?1", nativeQuery = true)
	List<Integer> findSpecialitiesByApplicant(Integer applicantId);

	List<RatingList> findByAcceptedFalseAndRejectionMessageIsNull();
}
