package ua.lviv.lgs.admissionsOffice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rating_list")
public class RatingList extends Application {
	@Column
	private Double totalMark;
	@Column
	private boolean accepted;


	public RatingList() {
		super();
	}

	public RatingList(Double totalMark, boolean accepted) {
		super();
		this.totalMark = totalMark;
		this.accepted = accepted;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (accepted ? 1231 : 1237);
		result = prime * result + ((totalMark == null) ? 0 : totalMark.hashCode());
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
		RatingList other = (RatingList) obj;
		if (accepted != other.accepted)
			return false;
		if (totalMark == null) {
			if (other.totalMark != null)
				return false;
		} else if (!totalMark.equals(other.totalMark))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RatingList [totalMark=" + totalMark + ", accepted=" + accepted + "]";
	}

}
