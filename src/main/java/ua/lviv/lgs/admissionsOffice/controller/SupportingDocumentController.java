package ua.lviv.lgs.admissionsOffice.controller;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import ua.lviv.lgs.admissionsOffice.domain.SupportingDocument;
import ua.lviv.lgs.admissionsOffice.service.SupportingDocumentService;

@RestController
public class SupportingDocumentController {
	@Autowired
	SupportingDocumentService supportingDocumentService;

	@GetMapping("/downloadFile/{fileId}")
	public ResponseEntity<Resource> downlaodFile(@PathVariable String fileId)
			throws FileNotFoundException, UnsupportedEncodingException {
		SupportingDocument supportingDocument = supportingDocumentService.getFile(fileId);

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(supportingDocument.getFileType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(supportingDocument.getFileName(), "UTF-8") + "\"")
				.body(new ByteArrayResource(supportingDocument.getFileData()));
	}
}
