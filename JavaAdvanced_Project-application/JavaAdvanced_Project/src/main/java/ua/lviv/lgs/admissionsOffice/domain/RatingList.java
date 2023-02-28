package ua.lviv.lgs.admissionsOffice.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rating_list")
public class RatingList implements Serializable, Comparable<RatingList> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	private Integer id;
	@Column
	private Double totalMark;
	@Column
	private boolean accepted;
	@Column
	private String rejectionMessage;

	@OneToOne
    @MapsId
    private Application application;

	public RatingList() { }

	public RatingList(Integer id, Double totalMark, boolean accepted) {
		this.id = id;
		this.totalMark = totalMark;
		this.accepted = accepted;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getTotalMark() {
		return totalMark;
	}

	public void setTotalMark(Double totalMark) {
		this.totalMark = totalMark;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public String getRejectionMessage() {
		return rejectionMessage;
	}

	public void setRejectionMessage(String rejectionMessage) {
		this.rejectionMessage = rejectionMessage;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((totalMark == null) ? 0 : totalMark.hashCode());
		result = prime * result + (accepted ? 1231 : 1237);
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
		RatingList other = (RatingList) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (totalMark == null) {
			if (other.totalMark != null)
				return false;
		} else if (!totalMark.equals(other.totalMark))
			return false;
		if (accepted != other.accepted)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(RatingList ratingList) {
		if (this.totalMark > ratingList.totalMark) {
			return 1;
		} else if (this.totalMark < ratingList.totalMark) {
			return -1;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "RatingList [id=" + id + ", totalMark=" + totalMark + ", accepted=" + accepted + "]";
	}
}
