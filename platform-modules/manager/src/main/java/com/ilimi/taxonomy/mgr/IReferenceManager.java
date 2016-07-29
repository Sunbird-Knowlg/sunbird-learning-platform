package com.ilimi.taxonomy.mgr;

import java.io.File;

import com.ilimi.common.dto.Response;

public interface IReferenceManager {

	Response uploadReferenceDocument(File uploadedFile, String referenceId);

}
