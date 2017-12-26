package org.ekstep.taxonomy.mgr;

import java.io.File;

import org.ekstep.common.dto.Response;

public interface IReferenceManager {

	Response uploadReferenceDocument(File uploadedFile, String referenceId);

}
