/*
 * Ticket.java
 *
 * AVADIS Image Management System
 * Core Engine Components
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
package com.strandgenomics.imaging.iengine.system;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.logging.Logger;

import com.strandgenomics.imaging.icore.Status;
import com.strandgenomics.imaging.icore.db.DataAccessException;
import com.strandgenomics.imaging.iengine.dao.ImageSpaceDAOFactory;
import com.strandgenomics.imaging.iengine.dao.TicketDAO;

/**
 * A ticket for uploading and download record archives
 * @author arunabha
 *
 */
public class Ticket implements Serializable {
	
	/**
	 * used generates the ID of a ticket, initializes with current system time
	 */
	private static volatile long idSequence = System.currentTimeMillis();
	/**
	 * ID of the ticket
	 */
	public final long ID;
	/**
	 * the client request that is being served by this ticket
	 */
	protected RecordCreationRequest request = null;
	/**
	 * time in milliseconds when the ticket was formed, will be used to invalidate a ticket
	 */
	final long requestTime;
	/**
	 * the URL to use for uploading archives etc
	 */
	protected String uploadURL = null;
	/**
	 * the URL to use for downloading archives etc
	 */
	protected String downloadURL = null;
	/**
	 * The logger
	 */
	protected Logger logger = null;
	
	/**
	 * Create a ticket with the specified client request 
	 * @param request the client request
	 * @throws DataAccessException 
	 */
	public Ticket(RecordCreationRequest request) throws DataAccessException
	{
		this.request = request;
		this.requestTime = System.currentTimeMillis();
		this.ID = generateID();
		logger = Logger.getLogger("com.strandgenomics.imaging.iengine.system");
		
		ImageSpaceDAOFactory factory = ImageSpaceDAOFactory.getDAOFactory();
		TicketDAO ticketDao = factory.getTicketDAO();
		ticketDao.insertTicket(ID, requestTime, Status.WAITING);
	}
	
	/**
	 * Returns the time when the ticket was created
	 * @return he time when the ticket was created
	 */
	public long getRequestTime()
	{
		return requestTime;
	}
	
	public long getLastModification() throws DataAccessException
	{
		ImageSpaceDAOFactory factory = ImageSpaceDAOFactory.getDAOFactory();
		TicketDAO ticketDao = factory.getTicketDAO();
		return ticketDao.findTicket(ID).getLastModificationTime();
	}
	
	public Status getStatus() throws DataAccessException
	{
		ImageSpaceDAOFactory factory = ImageSpaceDAOFactory.getDAOFactory();
		TicketDAO ticketDao = factory.getTicketDAO();
		return ticketDao.findTicket(ID).getJobStatus();
	}

	/**
	 * Returns the user owning the ticket
	 * @return the user owning the ticket
	 */
	public String getActor()
	{
		return request.getActor();
	}
	
	public BigInteger getArchiveSignature()
	{
		return request.archiveHash;
	}

	public RecordCreationRequest getRequest()
	{
		return this.request;
	}
	
	private static final long generateID()
	{
		synchronized(Ticket.class)
		{
			return ++idSequence;
		}
	}

	public String getUploadURL()
	{
		if(uploadURL == null)
		{
			uploadURL = DataExchange.UPLOAD_ARCHIVE.generateURL(request.getActor(), request.getClientIPAddress(), System.currentTimeMillis(), ID);
		}
		return uploadURL;
	}
	
	public String getDownloadURL()
	{
		if(downloadURL == null)
		{
			downloadURL = DataExchange.DOWNLOAD_ARCHIVE.generateURL(request.getActor(), request.getClientIPAddress(), System.currentTimeMillis(), ID);
		}
		return downloadURL;
	}
}
