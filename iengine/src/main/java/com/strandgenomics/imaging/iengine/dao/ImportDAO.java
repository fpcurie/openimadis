/*
 * ImportDAO.java
 *
 * AVADIS Image Management System
 * Data Access Components
 *
 * Copyright 2011-2012 by Strand Life Sciences
 * 5th Floor, Kirloskar Business Park, 
 * Bellary Road, Hebbal
 * Bangalore 560024
 * Karnataka, India
 * 
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Strand Life Sciences., ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Strand Life Sciences.
 */
package com.strandgenomics.imaging.iengine.dao;

import java.util.List;

import com.strandgenomics.imaging.icore.Status;
import com.strandgenomics.imaging.icore.db.DataAccessException;
import com.strandgenomics.imaging.iengine.models.Import;
import com.strandgenomics.imaging.iengine.system.RecordCreationRequest;

/**
 * imports
 * @author navneet
 *
 */
public interface ImportDAO {
	
	/**
	 * insert import
	 * @throws DataAccessException 
	 */
	public void insertImport(int projectId,long ticketID, long requestTime, Status jobStatus, RecordCreationRequest request) throws DataAccessException;
	
	/**
	 * get all imports with the given status
	 * @throws DataAccessException 
	 */
	public List<Import> getImportsForStatus(int projectId,Status jobStatus) throws DataAccessException;
	
	/**
	 * update status of a import
	 * @throws DataAccessException 
	 */
	public void updateImportStatus(int projectId, long ticketID, Status jobStatus, long lastModificationTime) throws DataAccessException;
}
