/*
 * DBMetaDataDAO.java
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.strandgenomics.imaging.icore.AnnotationType;
import com.strandgenomics.imaging.icore.MetaData;
import com.strandgenomics.imaging.icore.SearchField;
import com.strandgenomics.imaging.icore.db.ConnectionProvider;
import com.strandgenomics.imaging.icore.db.DataAccessException;
import com.strandgenomics.imaging.icore.db.RowSet;
import com.strandgenomics.imaging.icore.db.SQLQuery;
import com.strandgenomics.imaging.iengine.dao.ImageSpaceDAOFactory;
import com.strandgenomics.imaging.iengine.dao.MetaDataDAO;
import com.strandgenomics.imaging.iengine.models.Project;

/**
 * Gives access to user annotations
 * 
 * @author Anup Kulkarni
 * 
 */
public class DBMetaDataDAO extends StorageDAO<MetaData> implements
		MetaDataDAO {

	protected final AnnotationType annotationType;

	DBMetaDataDAO(ImageSpaceDAOFactory factory, ConnectionProvider connectionProvider, AnnotationType type) 
	{
		super(factory, connectionProvider, "MetaDataDAO.xml");
		this.annotationType = type;
	}

	@Override
	public MetaData createObject(Object[] columnValues) 
	{
		String name = (String) columnValues[0];
		Object value = columnValues[1];
		
		return MetaData.createInstance(name, value);
	}

	@Override
	public List<MetaData> find(int projectID, long guid) throws DataAccessException 
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_ANNOTATION_FOR_RECORD");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("RecordID", guid, Types.BIGINT);

		RowSet<MetaData> result = find(sqlQuery);
		return result == null ? null : result.getRows();
	}

	@Override
	public MetaData find(int projectID, long guid, String name) throws DataAccessException 
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_ANNOTATION_FOR_NAME");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("RecordID", guid, Types.BIGINT);
		sqlQuery.setValue("Name", name, Types.VARCHAR);

		return fetchInstance(sqlQuery);
	}

	@Override
	public void delete(int projectID, long guid, String name) throws DataAccessException 
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("DELETE_ANNOTATION_FOR_NAME_AND_GUID");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("RecordID", guid, Types.BIGINT);
		sqlQuery.setValue("Name", name, Types.VARCHAR);

		updateDatabase(sqlQuery);
	}

	@Override
	public boolean update(int projectID, long guid, String actorLogin, String name, Object value) throws DataAccessException 
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("UPDATE_ANNOTATION_FOR_NAME");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("RecordID",   guid,       Types.BIGINT);
		sqlQuery.setValue("Name",       name,       Types.VARCHAR);
		sqlQuery.setValue("Value",      value,      annotationType.getSQLType());
		sqlQuery.setValue("ModifiedBy", actorLogin, Types.VARCHAR);

		return updateDatabase(sqlQuery);
	}

	@Override
	public void insertUserAnnotation(int projectID, long guid, String login, String name, Object value) 
			throws DataAccessException 
	{
		if(getAnnotationType(projectID, name) == null)
		{
			registerAnnotationName(name, annotationType.name(), projectID);
		}
		
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("INSERT_ANNOTATION");
		logger.logp(Level.INFO, "DBMetaDataDAO", "insertUserAnnotation", "type="+annotationType);
		
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("RecordID",   guid,  Types.BIGINT);
		sqlQuery.setValue("Name",       name,  Types.VARCHAR);
		sqlQuery.setValue("Value",      value, annotationType.getSQLType());
		sqlQuery.setValue("ModifiedBy", login, Types.VARCHAR);
		sqlQuery.setValue("ModificationTime", new Timestamp(System.currentTimeMillis()), Types.TIMESTAMP);

		updateDatabase(sqlQuery);
	}

	@Override
	public void registerAnnotationName(String name, String type, int projectID) throws DataAccessException
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("INSERT_ANNOTATION_IN_REGISTRY");

		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setValue("Name", name, Types.VARCHAR);
		sqlQuery.setValue("Type", type, Types.VARCHAR);

		logger.logp(Level.INFO, "DBMetaDataDAO", "insertInRegistry", "name ="+ name + " type " + type);
		updateDatabase(sqlQuery);
	}

	/**
	 * returns the list of annotations(name,type) associated with project
	 * @param projectID
	 * @return list of search columns
	 * @throws DataAccessException
	 */
	public List<SearchField> getAnnotationsFromRegistry(int projectID) throws DataAccessException 
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_ANNOTATION_NAMES");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));

		List<SearchField> searchCols = new ArrayList<SearchField>();
		RowSet<Object[]> result = executeQuery(sqlQuery);
		if (result == null)
		    return searchCols;
		
		List<Object[]> rows = result.getRows();
		
		for(int i=0;i<rows.size();i++)
		{
			Object[] item = rows.get(i);
			String name = (String)item[0];
			AnnotationType type = AnnotationType.valueOf((String)item[1]);
			SearchField s = new SearchField(name, type);
			searchCols.add(s);
		}
		
		return searchCols;
	}
	
	@Override
	public AnnotationType getAnnotationType(int projectID, String name) throws DataAccessException
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_ANNOTATION_TYPE_FOR_NAME");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setValue("Name", name, Types.VARCHAR);
		
		String annType = getString(sqlQuery);
		if(annType == null) return null;
		
		return AnnotationType.valueOf(annType);
	}

	@Override
	public Set<MetaData> getUniqueAnnotationValues(int projectID, String name, AnnotationType type, int limit)
			throws DataAccessException
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("GET_UNIQUE_ANNOTATION_VALUES");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", type.toString());
		
		sqlQuery.setValue("Name", name, Types.VARCHAR);
		sqlQuery.setValue("Limit",  new Integer(limit),  Types.INTEGER);
		
		RowSet<MetaData> result = find(sqlQuery);
		return result == null ? null : new HashSet<MetaData>(result.getRows());
	}

	@Override
	public void deleteUserAnnotation(int projectID, String name) throws DataAccessException
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("DELETE_ANNOTATION_FOR_NAME");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());

		sqlQuery.setValue("Name", name, Types.VARCHAR);

		updateDatabase(sqlQuery);
	}
	
	@Override
	public void deleteUserAnnotationFromRegistry(int projectID, String name) throws DataAccessException
	{
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("DELETE_ANNOTATION_FROM_REGISTRY");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setValue("Name", name, Types.VARCHAR);

		updateDatabase(sqlQuery);
	}

	@Override
	public long[] getRecordsForUserAnnotation(int projectID, String annotationName) throws DataAccessException
	{
		logger.logp(Level.INFO, "DBMetaDataDAO", "getRecordsForUserAnnotation", "returning record ids from project "+projectID+" having annotation "+annotationName);
		
		SQLQuery sqlQuery = queryDictionary.createQueryGenerator("FIND_RECORDS_FOR_ANNOTATION");
		sqlQuery.setParameter("Project", Project.getTablePrefix(projectID));
		sqlQuery.setParameter("type", annotationType.toString());
		
		sqlQuery.setValue("Name", annotationName, Types.VARCHAR);
		
		return getRowsWithLongValues(sqlQuery);
	}
}
