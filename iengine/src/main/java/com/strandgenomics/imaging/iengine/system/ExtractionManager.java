/**
 * openImaDis - Open Image Discovery: Image Life Cycle Management Software
 * Copyright (C) 2011-2016  Strand Life Sciences
 *   
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ExtractionManager.java
 *
 * AVADIS Image Management System
 * Core Engine
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

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;

import com.strandgenomics.imaging.icore.Constants;
import com.strandgenomics.imaging.iengine.ImagingEngineException;

/**
 * This class submits record extraction request to the workers
 * @author arunabha
 *
 */
public class ExtractionManager extends SystemManager implements ExtractionService {
	
	/**
	 * extraction manager
	 */
	ExtractionManager()
	{}
	
	@Override
    public boolean submitTask(long ticketID, RecordCreationRequest request) throws ImagingEngineException,IOException 
    {
		logger.logp(Level.INFO, "ExtractionManager", "submitTask", "submitting ticket("+ticketID +") for execuion "+request);
		if(request == null || !request.isValid()) return false;
		
        Registry registry = LocateRegistry.getRegistry(Constants.getRMIServicePort());
        ExtractionService serviceStub = null;;
		try 
		{
			serviceStub = (ExtractionService) registry.lookup(ExtractionService.class.getCanonicalName());
		} 
		catch (NotBoundException e)
		{
			logger.logp(Level.WARNING, "ExtractionManager", "submitTask", "error submitting ticket("+ticketID +") for execuion "+request, e);
			throw new IOException(e);
		}
                    
        boolean status = serviceStub.submitTask(ticketID, request);
        logger.logp(Level.INFO, "ExtractionManager", "submitTask", "successfully submitted request for extracting records from "+request +" for ticket "+ticketID +", status="+status);
        return status;
    }
}