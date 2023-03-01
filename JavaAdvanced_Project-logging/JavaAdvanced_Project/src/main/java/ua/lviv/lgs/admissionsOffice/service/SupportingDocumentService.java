package ua.lviv.lgs.admissionsOffice.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.lgs.admissionsOffice.dao.SupportingDocumentRepository;
import ua.lviv.lgs.admissionsOffice.domain.Application;
import ua.lviv.lgs.admissionsOffice.domain.SupportingDocument;

@Service
public class SupportingDocumentService {
	Logger logger = LoggerFactory.getLogger(SupportingDocumentService.class);
	
	@Autowired
	private SupportingDocumentRepository supportingDocumentRepository;

	public List<SupportingDocument> findAllByApplication(Application application) {
		logger.trace("Getting all supporting documents by specified application from database...");
		
		return supportingDocumentRepository.findAllByApplication(application);
	}
	
	public SupportingDocument getFile(String fileId) throws FileNotFoundException {
		logger.trace("Getting supporting document file by id from database...");
		
		Optional<SupportingDocument> fileFromDb = supportingDocumentRepository.findById(fileId);
		SupportingDocument file = fileFromDb.orElseThrow(() -> new FileNotFoundException("There is no file with id " + fileId + " in database!"));
				
		return file;
	}
	
	public SupportingDocument storeFile(MultipartFile file, Application application) throws IOException {
		logger.trace("Saving new supporting document file in database...");
		
		SupportingDocument supportingDocument = new SupportingDocument();
		
		supportingDocument.setFileName(StringUtils.cleanPath(file.getOriginalFilename()));
		supportingDocument.setFileType(file.getContentType());
		supportingDocument.setFileData(file.getBytes());
		supportingDocument.setApplication(application);
				
		return supportingDocumentRepository.save(supportingDocument);
	}

	public void deleteFile(String fileId) {
		logger.trace("Deleting supporting document file from database...");
		
		supportingDocumentRepository.deleteById(fileId);		
	}
	
	public Map<String, String> getSupportingDocumentErrors(MultipartFile[] supportingDocuments) throws IOException {
		logger.trace("Checking attached supporting documents for upload errors...");
		
		Map<String, String> supportingDocumentErrors = new HashMap<>();
		
		for (MultipartFile file : supportingDocuments) {
			if (file.getBytes().length > 8388608) {
				supportingDocumentErrors.put("supportingDocumentError", "Размер файла не может быть более 8 Мб!");
				break;				
			}
		}
		return supportingDocumentErrors;
	}
	
	public Set<SupportingDocument> initializeSupportingDocumentSet(Application application, MultipartFile[] supportingDocuments) throws IOException {
		logger.trace("Initializing supporting documents set for specified application...");
		
		List<SupportingDocument> supportingDocumentListFromDb = findAllByApplication(application);
		Set<SupportingDocument> supportingDocumentsSet;
		
		if (supportingDocumentListFromDb == null || supportingDocumentListFromDb.isEmpty()) {
			supportingDocumentsSet = new HashSet<>();
		} else {
			supportingDocumentsSet = supportingDocumentListFromDb.stream().collect(Collectors.toSet());
		}
		
		for (MultipartFile file : supportingDocuments) {
			if (!file.isEmpty()) {	
				if (!supportingDocumentsSet.contains(new SupportingDocument(
						StringUtils.cleanPath(file.getOriginalFilename()), file.getContentType(), file.getBytes()))) {
					SupportingDocument supportingDocument = storeFile(file, application);
					supportingDocumentsSet.add(supportingDocument);
				}
			}
		}
		return supportingDocumentsSet;
	}

	public void deleteSupportingDocuments(Map<String, String> form) {
		logger.trace("Deleting supporting documents...");
		
		for (String key : form.keySet()) {
			if (key.startsWith("delete")) {
				String fileId = key.replace("delete", "");
				deleteFile(fileId);
			}
		}
	}
}
