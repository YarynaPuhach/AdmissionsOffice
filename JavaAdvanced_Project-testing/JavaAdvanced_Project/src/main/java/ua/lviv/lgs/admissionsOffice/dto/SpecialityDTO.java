package ua.lviv.lgs.admissionsOffice.dto;

public class SpecialityDTO extends AbstractDTO implements Comparable<SpecialityDTO> {

	public SpecialityDTO(Integer id, String title) {
		super(id, title);
	}

	@Override
	public int compareTo(SpecialityDTO specialityDTO) {
		if (this.id > specialityDTO.id) {
			return 1;
		} else if (this.id < specialityDTO.id) {
			return -1;
		}
		return 0;
	}
}
