package ua.lviv.lgs.admissionsOffice.dto;

public class SubjectDTO extends AbstractDTO implements Comparable<SubjectDTO>{

	public SubjectDTO(Integer id, String title) {
		super(id, title);
	}

	@Override
	public int compareTo(SubjectDTO subjectDTO) {
		if (this.id > subjectDTO.id) {
			return 1;
		} else if (this.id < subjectDTO.id) {
			return -1;
		}
		return 0;
	}
}
