/*
 * DBImportDAO.java
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
package com.strandgenomics.imaging.iengine.dao.db;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.logging.Level;

import javax.activation.DataSource;

import com.strandgenomics.imaging.icore.Status;
import com.strandgenomics.imaging.icore.db.ConnectionProvider;
import com.strandgenomics.imaging.icore.db.DataAccessException;
import com.strandgenomics.imaging.icore.db.RowSet;
import com.strandgenomics.imaging.icore.db.SQLQuery;
import com.strandgenomics.imaging.icore.util.Util;
import com.strandgenomics.imaging.iengine.dao.ImageSpaceDAOFactory;
import com.strandgenomics.imaging.iengine.dao.ImportDAO;
import com.strandgenomics.imaging.iengine.models.Import;
import com.strandgenomics.imaging.iengine.models.Project;
import com.strandgenomics.imaging.iengine.system.RecordCreationRequest;

/**
 * queries related to imports
 * @author navneet
 *
 */
public class DBImportDAO extends StorageDAO<Import> implements ImportDAO{

	DBImportDAO(ImageSpaceDAOFactory factory,ConnectionProvider connectionProvider) {
		super(factory, connectionProvider, "ImportDAO.xml");
	}

	@Override
	public void insertImport(int projectId,long ticketID, long requestTime, Status jobStatus,
			RecordCreationRequest request) throws DataAccessException {
		
        SQLQuery sqlQuery = queryDictionary.createQueryGenerator("INSERT_IMPORT");
        sqlQuery.setParameter("Project", Project.getTablePrefix(projectId));
        logger.logp(Level.INFO, "DBImportDAO", "insertImport", "ticketID="+ticketID);
        
        sqlQuery.setValue("TicketID",          ticketID,                    Types.BIGINT);
        sqlQuery.setValue("Status",            jobStatus.name(),            Types.VARCHAR);
        sqlQuery.setValue("RequestTime",       new Timestamp(requestTime),  Types.TIMESTAMP);
        sqlQuery.setValue("Request",       	toByteArray(request),  Types.BLOB);
        
        updateDatabase(sqlQuery);
	}

	@Override
	public List<Import> getImportsForStatus(int projectId,Status jobStatus) throws DataAccessException {
		
		if(jobStatus == null) throw new NullPointerException("unexpected null value");
        logger.logp(Level.FINEST, "DBTicketDAO", "getImportsForStatus", "jobStatus="+jobStatus);

        SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_IMPORTS_FOR_STATUS");
        sqlQuery.setParameter("Project", Project.getTablePrefix(projectId));
        sqlQuery.setValue("Status", jobStatus.name(), Types.VARCHAR);

        RowSet<Import> result = find(sqlQuery);
        return result == null ? null : result.getRows();
	}
	
	@Override
	public void updateImportStatus(int projectId, long ticketID, Status jobStatus, long lastModificationTime) throws DataAccessException {
        SQLQuery sqlQuery = queryDictionary.createQueryGenerator("UPDATE_IMPORT_STATUS");
        
        sqlQuery.setParameter("Project", Project.getTablePrefix(projectId));
        logger.logp(Level.INFO, "DBImportDAO", "updateImportStatus", "ticketID="+ticketID);
        
        sqlQuery.setValue("TicketID",          ticketID,                    Types.BIGINT);
        sqlQuery.setValue("Status",            jobStatus.name(),            Types.VARCHAR);
        sqlQuery.setValue("ModificationTime",       	new Timestamp(lastModificationTime),  Types.TIMESTAMP);
        
        updateDatabase(sqlQuery);		
	}
	
	@Override
	public Import createObject(Object[] columnValues) {
		long ticketID              = Util.getLong(columnValues[0]);
		Timestamp requestTime      = Util.getTimestamp(columnValues[1]);
		Status status              = Status.valueOf((String)columnValues[2]);
		RecordCreationRequest request = (RecordCreationRequest) toJavaObject((DataSource)columnValues[3]);
		
		return new Import(ticketID, requestTime.getTime(), status, request);
	}

}
