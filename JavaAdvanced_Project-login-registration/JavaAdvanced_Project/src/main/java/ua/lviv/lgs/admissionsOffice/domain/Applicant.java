package ua.lviv.lgs.admissionsOffice.domain;

import java.time.LocalDate;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "applicant")
public class Applicant extends User {
	@Column
	private LocalDate birthDate;
	@Column
	private String city;
	@Column
	private String school;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "speciality_applicant", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "speciality_id"))
	private Set<Speciality> applicantSpecialities;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "applicant")
	@Column(nullable = false)
	private Set<Application> applications;

	
	public Applicant() {
		super();
	}

	public Applicant(LocalDate birthDate, String city, String school) {
		super();
		this.birthDate = birthDate;
		this.city = city;
		this.school = school;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public Set<Speciality> getApplicantSpecialities() {
		return applicantSpecialities;
	}

	public void setApplicantSpecialities(Set<Speciality> applicantSpecialities) {
		this.applicantSpecialities = applicantSpecialities;
	}

	public Set<Application> getApplications() {
		return applications;
	}

	public void setApplications(Set<Application> applications) {
		this.applications = applications;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((applicantSpecialities == null) ? 0 : applicantSpecialities.hashCode());
		result = prime * result + ((applications == null) ? 0 : applications.hashCode());
		result = prime * result + ((birthDate == null) ? 0 : birthDate.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((school == null) ? 0 : school.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Applicant other = (Applicant) obj;
		if (applicantSpecialities == null) {
			if (other.applicantSpecialities != null)
				return false;
		} else if (!applicantSpecialities.equals(other.applicantSpecialities))
			return false;
		if (applications == null) {
			if (other.applications != null)
				return false;
		} else if (!applications.equals(other.applications))
			return false;
		if (birthDate == null) {
			if (other.birthDate != null)
				return false;
		} else if (!birthDate.equals(other.birthDate))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (school == null) {
			if (other.school != null)
				return false;
		} else if (!school.equals(other.school))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Applicant [birthDate=" + birthDate + ", city=" + city + ", school=" + school + "]";
	}
}
