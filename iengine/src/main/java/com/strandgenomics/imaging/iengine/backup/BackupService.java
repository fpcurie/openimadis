package com.strandgenomics.imaging.iengine.backup;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * service used to backup/restore project raw data
 * 
 * @author Anup Kulkarni
 */
public interface BackupService extends Remote {
	
	/**
	 * submits request for project backup
	 * @param actor logged in user
	 * @param projectId specified project
	 * @param location where to backup the project
	 * @throws RemoteException
	 */
	public void submitArchivalRequest(String actor, int projectId, String location) throws RemoteException;
	
	/**
	 * cancel the archival request
	 * @param actor logged in user
	 * @param projectId specified project
	 * @throws RemoteException
	 */
	public void cancelArchivalRequest(String actor, int projectId) throws RemoteException;

	/**
	 * clean specified project id from archival cache
	 * @param projectId
	 * @throws RemoteException
	 */
	public void cleanArchivalCache(int projectId) throws RemoteException;
	
	/**
	 * clean specified project id from restoration cache
	 * @param projectId
	 * @throws RemoteException
	 */
	public void cleanRestoreCache(int projectId) throws RemoteException;
	
	/**
	 * submits request for project restoration
	 * @param actor logged in user
	 * @param projectId specified project
	 * @param location 
	 * @param sign signature computed while backing up the data
	 * @throws RemoteException
	 */
	public void submitRestorationRequest(String actor, int projectId, String location, String sign) throws RemoteException;
	
	/**
	 * cancel the restoration request
	 * @param actor logged in user
	 * @param projectId specified project
	 * @throws RemoteException
	 */
	public void cancelRestorationRequest(String actor, int projectId) throws RemoteException;
}
