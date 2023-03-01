package ua.lviv.lgs.admissionsOffice.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "application")
public class Application implements Serializable {
	private static final long serialVersionUID = 1L;

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
	@NotNull(message = "Средний балл аттестата не может быть пустым!")
	@Min(value = 100, message = "Средний балл аттестата не может быть меньше 100!")
	@Max(value = 200, message = "Средний балл аттестата не может быть больше 200!")
	private Integer attMark;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "application")
	private Set<SupportingDocument> supportingDocuments;

	@OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private RatingList ratingList;

	
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

	public Set<SupportingDocument> getSupportingDocuments() {
		return supportingDocuments;
	}

	public void setSupportingDocuments(Set<SupportingDocument> supportingDocuments) {
		this.supportingDocuments = supportingDocuments;
	}

	public RatingList getRatingList() {
		return ratingList;
	}

	public void setRatingList(RatingList ratingList) {
		this.ratingList = ratingList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((applicant == null) ? 0 : applicant.hashCode());
		result = prime * result + ((speciality == null) ? 0 : speciality.hashCode());
		result = prime * result + ((znoMarks == null) ? 0 : znoMarks.hashCode());
		result = prime * result + ((attMark == null) ? 0 : attMark.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (applicant == null) {
			if (other.applicant != null)
				return false;
		} else if (!applicant.equals(other.applicant))
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
		if (attMark == null) {
			if (other.attMark != null)
				return false;
		} else if (!attMark.equals(other.attMark))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Application [id=" + id + ", applicant=" + applicant + ", speciality=" + speciality + ", znoMarks="
				+ znoMarks + ", attMark=" + attMark + "]";
	}
}
