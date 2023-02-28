package ua.lviv.lgs.admissionsOffice.domain;

import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "application")
@Inheritance(strategy = InheritanceType.JOINED)
public class Application {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "application_id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "applicant_id", nullable = false)
	private Applicant applicant;
	
	@ManyToOne
	@JoinColumn(name = "speciality_id", nullable = false)
	private Speciality speciality;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "zno_marks")
	@MapKeyColumn(name = "subject_id")
	private Map<Subject, Integer> znoMarks;
	
	@Column
	private Integer attMark;

	
	public Application() {	}

	public Application(Applicant applicant, Speciality speciality, Map<Subject, Integer> znoMarks, Integer attMark) {
		this.applicant = applicant;
		this.speciality = speciality;
		this.znoMarks = znoMarks;
		this.attMark = attMark;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Applicant getApplicant() {
		return applicant;
	}

	public void setApplicant(Applicant applicant) {
		this.applicant = applicant;
	}

	public Speciality getSpeciality() {
		return speciality;
	}

	public void setSpeciality(Speciality speciality) {
		this.speciality = speciality;
	}

	public Map<Subject, Integer> getZnoMarks() {
		return znoMarks;
	}

	public void setZnoMarks(Map<Subject, Integer> znoMarks) {
		this.znoMarks = znoMarks;
	}

	public Integer getAttMark() {
		return attMark;
	}

	public void setAttMark(Integer attMark) {
		this.attMark = attMark;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicant == null) ? 0 : applicant.hashCode());
		result = prime * result + ((attMark == null) ? 0 : attMark.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((speciality == null) ? 0 : speciality.hashCode());
		result = prime * result + ((znoMarks == null) ? 0 : znoMarks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Application other = (Application) obj;
		if (applicant == null) {
			if (other.applicant != null)
				return false;
		} else if (!applicant.equals(other.applicant))
			return false;
		if (attMark == null) {
			if (other.attMark != null)
				return false;
		} else if (!attMark.equals(other.attMark))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (speciality == null) {
			if (other.speciality != null)
				return false;
		} else if (!speciality.equals(other.speciality))
			return false;
		if (znoMarks == null) {
			if (other.znoMarks != null)
				return false;
		} else if (!znoMarks.equals(other.znoMarks))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Application [id=" + id + ", applicant=" + applicant + ", speciality=" + speciality + ", znoMarks="
				+ znoMarks + ", attMark=" + attMark + "]";
	}
}
