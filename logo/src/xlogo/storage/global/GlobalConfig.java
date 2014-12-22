/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the
 * GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage.global;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.samuelcampos.usbdrivedectector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedectector.USBStorageDevice;
import net.samuelcampos.usbdrivedectector.events.IUSBDriveListener;
import net.samuelcampos.usbdrivedectector.events.USBStorageEvent;
import xlogo.AppSettings;
import xlogo.Logo;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.StorableObject;
import xlogo.storage.WSManager;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.WorkspaceConfig;

/**
 * This Config is stored in default location : "user.home". It contains information about the currently used workspaces on the computer
 * @author Marko Zivkovic
 */
public class GlobalConfig extends StorableObject implements Serializable {
	
	private static final long	serialVersionUID	= 2787615728665011813L;
	private static Logger		logger				= LogManager.getLogger(GlobalConfig.class.getSimpleName());
	
	public static final String	LOGO_FILE_EXTENSION	= ".lgo";
	public static boolean		DEBUG				= true;	// TODO set false
																												
	/**
	 * Creates the global config at default location, together with a virtual workspace
	 */
	protected GlobalConfig() {
		try {
			setLocation(getDefaultLocation());
		}
		catch (IllegalArgumentException ignore) {} // This is thrown if name illegal, but it is legal
		workspaces = new TreeMap<String, String>();
		workspaces.put(WorkspaceConfig.VIRTUAL_WORKSPACE, "");
		workspaces.put(WorkspaceConfig.USER_DEFAULT_WORKSPACE, getDefaultLocation().getAbsolutePath());
	}
	
	/**
	 * If GlobalConfig exists on the file system in default location, it is loaded, otherwise it will be created there.
	 * @return
	 */
	public static GlobalConfig create() {
		File gcf = getFile(getDefaultLocation(), GlobalConfig.class);
		
		GlobalConfig globalConfig = null;
		if (gcf.exists()) {
			logger.trace("Try to read GlobalConfig from " + gcf.getAbsolutePath());
			try {
				globalConfig = (GlobalConfig) loadObject(gcf);
			}
			catch (Exception e) {
				logger.error("GlobalConfig was corrupted.");
				String title = AppSettings.getInstance().translate("error.loading.config.files.title");
				String message = AppSettings.getInstance().translate("error.loading.config.files");
				DialogMessenger.getInstance().dispatchError(title, message + e.toString());
				gcf.delete();
				globalConfig = null;
			}
		}
		
		if (globalConfig == null) {
			try {
				logger.info(gcf.getAbsolutePath() + " not found. Creating new.");
				globalConfig = new GlobalConfig();
				globalConfig.store();
			}
			catch (Exception e) {
				// Best effort : We will try to operate the program without storing anything on disk
				logger.error("Cannot store global config at " + gcf.getAbsolutePath() + ". Running in virtual mode.");
				globalConfig = getNewVirtualInstance();
				String title = AppSettings.getInstance().translate("error.setting.up.x4s.title");
				String message = AppSettings.getInstance().translate("error.setting.up.x4s");
				DialogMessenger.getInstance().dispatchError(title, message + e.toString());
			}
		}
		globalConfig.init();
		return globalConfig;
	}
	
	protected void init(){
		logger.trace("Initialize");
		initUSBWorkspaces();
		cleanUpWorkspaces();
		enterInitialWorkspace();
	}
	
	private void cleanUpWorkspaces() {
		logger.trace("Cleaning up workspaces.");
		Map<String, String> existingWorkspaces = new TreeMap<String, String>();
		Map<String, String> lostWorkspaces = new TreeMap<String, String>();
		for (Entry<String, String> e : workspaces.entrySet()) {
			File file = new File(e.getValue());
			if (file.exists() || WorkspaceConfig.isSpecialWorkspace(e.getKey(), e.getValue())) {
				logger.trace("\tConfirmed existence: " + e.getKey() + " at " + e.getValue());
				existingWorkspaces.put(e.getKey(), e.getValue());
			}
			else {
				logger.trace("\tLost workspace: " + e.getKey() + " at " + e.getValue());
				if (e.getKey().equals(lastUsedWorkspace)){
					lastUsedWorkspace = null;
					currentWorkspace = null;
				}
				lostWorkspaces.put(e.getKey(), e.getValue());
			}
		}
		
		if(!existingWorkspaces.containsKey(WorkspaceConfig.USER_DEFAULT_WORKSPACE)){
			// This might be the case if the GlobalConfig version stored on the disk comes from a version
			// that did not contain a default workspace
			existingWorkspaces.put(WorkspaceConfig.USER_DEFAULT_WORKSPACE, getDefaultLocation().getAbsolutePath());
		}
		
		if (lostWorkspaces.size() > 0) {
			StringBuilder msg = new StringBuilder();
			String message = AppSettings.getInstance().translate("message.some.workspaces.not.found");
			String at = AppSettings.getInstance().translate("word.at.filesystem.location");
			msg.append(message);
			for (Entry<String, String> e : lostWorkspaces.entrySet()) {
				msg.append('\t').append(e.getKey())
					.append(' ').append(at).append(' ')
					.append(e.getValue()).append('\n');
			}
			DialogMessenger.getInstance().dispatchMessage(msg.toString());
		}
		workspaces = existingWorkspaces;
	}
		
	public static GlobalConfig getNewVirtualInstance() {
		GlobalConfig gc = new GlobalConfig();
		gc.makeVirtual();
		return gc;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Physical Workspaces (stored on file system)
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public void createWorkspace(File dir, String workspaceName) throws IOException {
		logger.trace("Creating workspace '" + workspaceName + "' at " + dir.getAbsolutePath());
		if (WorkspaceConfig.createNewWorkspace(dir, workspaceName) != null)
			addWorkspace(workspaceName, dir.toString());
	}
	
	public void importWorkspace(File workspaceDir, String workspaceName) {
		logger.trace("Importing workspace '" + workspaceName + "' from " + workspaceDir.getAbsolutePath());
		if (!WSManager.isWorkspaceDirectory(workspaceDir)) {
			DialogMessenger.getInstance().dispatchError(Logo.messages.getString(MessageKeys.WS_ERROR_TITLE),
					workspaceDir + " " + Logo.messages.getString(MessageKeys.WS_NOT_A_WORKSPACE_DIRECTORY));
			return;
		}
		
		addWorkspace(workspaceName, workspaceDir.getParent());
	}
	
	/**
	 * Load the specified workspace from the file system.
	 * @param workspaceName
	 * @return the specified workspace or null if it does not exist.
	 * @throws IOException
	 */
	private WorkspaceConfig retrieveWorkspace(String workspaceName) throws IOException {
		WorkspaceConfig wsc = getCachedWorkspace(workspaceName);
		if (wsc != null) {
			logger.trace("Retrieving cached workspace: " + workspaceName);
			return wsc;
		}

		if (!existsWorkspace(workspaceName)) {
			logger.warn("Attempting to load an inexistent workspace: " + workspaceName);
			return null;
		}
		
		File location = getWorkspaceLocation(workspaceName);
		
		if (WorkspaceConfig.isDefaultWorkspace(workspaceName, location)) {
			logger.trace("Retrieving Default workspace from: " + location.getAbsolutePath());
			wsc = getDefaultWorkspace();
		}
		else if (isUSBDrive(workspaceName)) {
			logger.trace("Retrieving USB workspace: " + workspaceName);
			wsc = initUSBDrive(workspaceName);
		}
		else {
			logger.trace("Retrieving workspace: " + workspaceName + " from " + location.getAbsolutePath());
			wsc = WorkspaceConfig.loadWorkspace(location, workspaceName);
		}
		
		if (wsc == null) {
			WSManager.getInstance().deleteWorkspace(workspaceName, false);
		}
		
		cacheWorkspace(workspaceName, wsc);
		
		return wsc;
	}
	
	private WorkspaceConfig getDefaultWorkspace() throws IOException{
		logger.trace("Get Default Workspace");
		File wsDir = WorkspaceConfig.getDefaultWorkspaceDirectory();
		File wsFile = getFile(wsDir, WorkspaceConfig.class);
		File wsLocation = getDefaultLocation();
		WorkspaceConfig wsc = null;
		if (wsFile.exists()) {
			wsc = WorkspaceConfig.loadWorkspace(wsLocation, WorkspaceConfig.USER_DEFAULT_WORKSPACE);
		} else {
			wsc = WorkspaceConfig.createNewWorkspace(wsLocation, WorkspaceConfig.USER_DEFAULT_WORKSPACE);
			wsc.setAllowUserCreation(true);
			wsc.createUser(UserConfig.DEFAULT_USER);
		}
		return wsc;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspace Cache
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Workspace Objects that have already been created or loaded from disk.
	 */
	private transient Map<String, WorkspaceConfig>	cachedWorkspaces;
	
	private WorkspaceConfig getCachedWorkspace(String workspaceName) {
		if (cachedWorkspaces == null) {
			cachedWorkspaces = new TreeMap<String, WorkspaceConfig>();
		}
		return cachedWorkspaces.get(workspaceName);
	}
	
	private void cacheWorkspace(String workspaceName, WorkspaceConfig wsc) {
		cachedWorkspaces.put(workspaceName, wsc);
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspaces
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Logical Workspaces (name and location stored in Map)
	 */
	private Map<String, String>	workspaces;
	
	/**
	 * @param workspaceName
	 * @param location where the workspace is located: location/workspaceName/
	 */
	public void addWorkspace(String workspaceName, String location) {
		logger.trace("Adding workspace: '" + workspaceName + "' at " + location);
		workspaces.put(workspaceName, location);
		makeDirty();
		notifyWorkspaceListChanged();
		setLastUsedWorkspace(workspaceName);
		enterInitialWorkspace();
	}
	
	public void addWorkspace(String workspaceName, File location) {
		addWorkspace(workspaceName, location.getAbsolutePath());
	}
	
	public void removeWorkspace(String workspaceName) {
		logger.trace("Removing workspace: " + workspaceName);
		workspaces.remove(workspaceName);
		cachedWorkspaces.remove(workspaceName);
		makeDirty();
		notifyWorkspaceListChanged();
		if(lastUsedWorkspace.equals(workspaceName)){
			lastUsedWorkspace = null;
			currentWorkspace = null;
			enterInitialWorkspace();
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspace File Utility
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * @return File from system property "user.home"
	 */
	public static File getDefaultLocation() {
		return new File(System.getProperty("user.home"));
	}
	
	/**
	 * @param wsName
	 * @return the parent directory of the workspace directory, or null if the workspace does not exist
	 */
	public File getWorkspaceLocation(String wsName) {
		String location = workspaces.get(wsName);
		if (location == null)
			return null;
		return new File(location);
	}
	
	/**
	 * @param wsName
	 * @return The workspace Directory that contains a physical representation of {@link WorkspaceConfig}
	 */
	public File getWorkspaceDirectory(String wsName) {
		File wsLocation = getWorkspaceLocation(wsName);
		if (wsLocation == null)
			return null;
		return new File(wsLocation.toString() + File.separator + wsName);
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspaces
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * @return the names of all existing workspaces
	 */
	public String[] getAllWorkspaces() {
		return (String[]) workspaces.keySet().toArray(new String[workspaces.size()]);
	}
	
	/**
	 * A workspace exists logically, if its location is known by the GlobalConfig.
	 * @param workspace
	 * @return
	 */
	public boolean existsWorkspace(String workspace) {
		return workspaces.get(workspace) != null;
	}
	
	
	public String getFirstUSBWorkspace() {
		for (String ws : workspaces.keySet()) {
			if (isUSBDrive(ws)) { return ws; }
		}
		return null;
	}
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Last used workspace
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private String	lastUsedWorkspace;
	
	public String getLastUsedWorkspace() {
		return lastUsedWorkspace;
	}
	
	/**
	 * Succeeds if the workspace exists
	 * @param workspace
	 */
	private void setLastUsedWorkspace(String workspace) {
		if (existsWorkspace(workspace)) {
			lastUsedWorkspace = workspace;
			makeDirty();
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Current Workspace
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private transient WorkspaceConfig	currentWorkspace;
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Entering and Leaving Workspaces
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public WorkspaceConfig getCurrentWorkspace() {
		return currentWorkspace;
	}
	
	/**
	 * This is used to have a workspace ready at the beginning, without any user interaction.
	 * <p>
	 * Tries to enter workspaces with the following priority.
	 * 1. Last used workspace (if any)
	 * 2. Default workspace, if there is no last used workspace
	 * 3. Virtual Workspace, if entering or creating the default workspace failed for some reason.
	 */
	private void enterInitialWorkspace() {
		logger.trace("Entering initial workspace.");
		
		String initialWs = getFirstUSBWorkspace();
		
		if (initialWs == null) {
			initialWs = getLastUsedWorkspace();
		}
		
		if (initialWs == null) {
			initialWs = WorkspaceConfig.USER_DEFAULT_WORKSPACE;
		}
		
		if (initialWs == null) {
			initialWs = WorkspaceConfig.VIRTUAL_WORKSPACE;	// this exists, see constructor
		}
		
		try {
			enterWorkspace(initialWs);
		}
		catch (IOException e1) {
			try {
				enterWorkspace(WorkspaceConfig.VIRTUAL_WORKSPACE);
			}
			catch (IOException e2) {}
			DialogMessenger.getInstance().dispatchError("Workspace Error", "Cannot enter workspace: " + e1.toString());
		}
	}
	
	/**
	 * Load the workspace
	 * <p>Always succeeds if workspaceName equals {@link WorkspaceConfig#VIRTUAL_WORKSPACE}
	 * @param workspaceName - the workspace to load and enter
	 * @throws IOException - if the workspace could not be loaded
	 */
	public void enterWorkspace(String workspaceName) throws IOException {
		logger.trace("Entering workspace: " + workspaceName);
		if (currentWorkspace != null) {
			leaveWorkspace();
		}
		currentWorkspace = retrieveWorkspace(workspaceName);
		if (currentWorkspace == null)
			currentWorkspace = retrieveWorkspace(WorkspaceConfig.VIRTUAL_WORKSPACE);
		
		setLastUsedWorkspace(workspaceName);
		
		notifyWorkspacEntered();
		
		currentWorkspace.enterInitialUserSpace();
		
		AppSettings.getInstance().setLanguage(currentWorkspace.getLanguage());
	}
	
	/**
	 * Afterwards, currentWorkspace is null
	 * @throws IOException If workspace could not be saved.
	 */
	public void leaveWorkspace() throws IOException {
		if (currentWorkspace == null)
			throw new IllegalStateException(AppSettings.getInstance().translate("error.leaving.ws.without.being.in.one"));
		logger.trace("Leaving workspace: " + currentWorkspace.getWorkspaceName());
		
		if (currentWorkspace.getActiveUser() != null) {
			currentWorkspace.leaveUserSpace();
		}
		
		if (currentWorkspace.isDirty())
			currentWorkspace.store();
		
		currentWorkspace = null;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * USB Detection & Handling
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private transient USBDeviceDetectorManager	driveDetector;
		
	/**
	 * Detect External Drives
	 */
	protected void initUSBWorkspaces() {
		driveDetector = new USBDeviceDetectorManager(800);
		
		for (USBStorageDevice rmDevice : driveDetector.getRemovableDevices()) {
			if (rmDevice.canRead() && rmDevice.canWrite()) {
				addUSBDrive(rmDevice);
			}
		}
		
		driveDetector.addDriveListener(new IUSBDriveListener(){
			
			@Override
			public void usbDriveEvent(USBStorageEvent event) {
				USBStorageDevice rmDevice = event.getStorageDevice();
				switch (event.getEventType()) {
					case CONNECTED:
						addUSBDrive(rmDevice);
						break;
					case REMOVED:
						removeUSBDrive(rmDevice);
						break;
				}
			}
		});
	}
	
	protected void addUSBDrive(USBStorageDevice rmDevice) {
		if (getWorkspaceDirectory(rmDevice.getSystemDisplayName()) == null) {
			logger.trace("USB Drive attached: " + rmDevice);
			String deviceName = rmDevice.getSystemDisplayName();
			File location = rmDevice.getRootDirectory();
			addWorkspace(deviceName, location.getAbsolutePath());
		}
	}
	
	protected void removeUSBDrive(USBStorageDevice rmDevice) {
		logger.trace("USB Drive removed: " + rmDevice);
		String deviceName = rmDevice.getSystemDisplayName();
		removeWorkspace(deviceName);
	}
	
	protected WorkspaceConfig initUSBDrive(String deviceName) throws IOException {
		logger.trace("Initializing USB Drive: " + deviceName);
		File usbRoot = null;
		for (USBStorageDevice device : driveDetector.getRemovableDevices()) {
			if (deviceName.equals(device.getSystemDisplayName())) {
				usbRoot = device.getRootDirectory();
				break;
			}
		}
		if (usbRoot == null) { return null; }
		
		File wsDir = WorkspaceConfig.getDirectory(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
		File wsConfigFile = WorkspaceConfig.getFile(wsDir, WorkspaceConfig.class);
		
		WorkspaceConfig wsc = null;
		if (wsConfigFile.exists()) {
			logger.trace("Loading USB workspace from " + wsDir.getAbsolutePath());
			wsc = WorkspaceConfig.loadWorkspace(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
			for (String user : wsc.getUserList()) {
				logger.trace("\t Having user " + user);
			}
		}
		else {
			logger.trace("Creating new temporary USB workspace at " + usbRoot);
			wsc = WorkspaceConfig.createDeferredWorkspace(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
			wsc.setAllowUserCreation(true);
		}
		return wsc;
	}
	
	/* *
	 * Workspace Types
	 * */
	
	public boolean isUSBDrive(String workspaceName) {
		List<USBStorageDevice> devices = driveDetector.getRemovableDevices();
		logger.trace("Is '" + workspaceName + "' on a USB Drive?");
		for (USBStorageDevice device : devices) {
			if (workspaceName.equals(device.getSystemDisplayName())) { 
				logger.trace("\t = Yes, corresponding USB Device found.");
				return true; 
			} else {
				logger.trace("\t Does not corresponding to " + device.getSystemDisplayName());
			}
		}
		logger.trace("\t = No, could not find corresponding USB Drive.");
		return false;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Password protection
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * if null, no password is requested
	 */
	private byte[]	masterPassword	= null;
	
	/**
	 * Need old password to authenticate
	 * @param oldPw initially null
	 * @param newPw
	 * @return success
	 */
	public boolean setNewPassword(String oldPw, String newPw) {
		if (masterPassword == null || authenticate(oldPw)) {
			if (newPw == null)
				masterPassword = null;
			else
				masterPassword = hash(newPw);
			makeDirty();
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isPasswordRequired() {
		return masterPassword != null;
	}
	
	public boolean authenticate(String password) {
		if (masterPassword == null)
			return true;
		String entered = null;
		if (password != null)
			entered = new String(hash(password));
		String master = new String(masterPassword);
		boolean auth = master.equals(entered);
		return auth;
	}
	
	/**
	 * Hashing the password with MD5 is enough for this application. We just don't want to store readable plain text.
	 * Note that MD5 is generally considered insecure for security critical applications.
	 * @param text
	 * @return hashed bytes
	 */
	private byte[] hash(String text) {
		if (text == null)
			return null;
		
		byte[] bytesOfMessage;
		try {
			bytesOfMessage = text.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e1) {
			bytesOfMessage = text.getBytes();	// this should not happen anyway
		}
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(bytesOfMessage);
		}
		catch (NoSuchAlgorithmException e) {
			return bytesOfMessage;	// this should not happen anyway
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Path variable
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Path to the current directory for the Logo environment
	 */
	private ArrayList<String>	path	= new ArrayList<String>();
	
	public ArrayList<String> getPath() {
		return path;
	}
	
	public void setPath(ArrayList<String> path) {
		this.path = path;
		makeDirty();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Application Meta Data
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * This is not really used. Artifact from the past.
	 * @return
	 */
	public static String getVersion() {
		return "XLogo4Schools 0.0.1";
	}
	
	/* *
	 * Note : should be equal as in {@link Lanceur}
	 */
	//private static String	PROPERTIES_PREFIX		= "ch.ethz.abz.xlogo4schools";
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * JRE Memory allocation parameters
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Note : should be equal as in {@link Lanceur}
	 */
	private static int	DEFAULT_MEMORY_ALLOC	= 128;
	
	private static int	maximumMemory;
	
	/**
	 * The Maximum amount of memory that this application is allowed to consume by the JVM
	 * @return
	 */
	public static int getMaximumMemory() {
		if (maximumMemory < 64) {
			// This doesn't work as expected :-(
			//Preferences prefs = Preferences.systemRoot().node(PROPERTIES_PREFIX);
			//maximumMemory = prefs.getInt("appMemory", DEFAULT_MEMORY_ALLOC);
			maximumMemory = DEFAULT_MEMORY_ALLOC;
		}
		return maximumMemory;
	}
	
	private transient int		maxMemoryAtNextStart	= getMaximumMemory();
															
	/**
	 * @return The amount of memory in MB that Lanceur will cause JVM to allocate to XLogo4Schools the next time this application is started.
	 */
	public int getMaxMemoryAtNextStart() {
		if (maxMemoryAtNextStart < 64)
			maxMemoryAtNextStart = getMaximumMemory();
		return maxMemoryAtNextStart;
	}
	
	/**
	 * @see #getMaxMemoryAtNextStart()
	 * cannot set this below 64MB
	 * @param maxMemory
	 */
	public void setMaxMemoryAtNextStart(int maxMemory) {
		if (maxMemory < 64)
			return;
		// This doesn't work as well :-(
		//Preferences prefs = Preferences.systemRoot().node(PROPERTIES_PREFIX);
		//prefs.putInt("appMemory", maxMemory);
	}

	/**
	 * The amount of memory that the memory checker allows the application to consume.
	 * It's 0.9*{@link #getMaximumMemory()}} in bytes.
	 */
	public static long getMemoryThreshold() {
		return (long) (0.9 * ((long) GlobalConfig.getMaximumMemory() * 1024L * 1024L));
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Fonts
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static final Font[]	fonts					= GraphicsEnvironment.getLocalGraphicsEnvironment()
																.getAllFonts();	// Toolkit.getDefaultToolkit().getFontList();
	static public int getFontId(Font font) {
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i].getFontName().equals(font.getFontName()))
				return i;
		}
		return 0;
	}
	
	/* * * * * * *
	 * Event Handling
	 * * * * * * */
	
	// workspace list change
	
	private transient ArrayList<ActionListener>	workspaceListChangeListeners;
	
	public void addWorkspaceListChangeListener(ActionListener listener) {
		if (workspaceListChangeListeners == null)
			workspaceListChangeListeners = new ArrayList<ActionListener>();
		workspaceListChangeListeners.add(listener);
	}
	
	public void removeWorkspaceListChangeListener(ActionListener listener) {
		workspaceListChangeListeners.remove(listener);
	}
	
	private void notifyWorkspaceListChanged() {
		if (workspaceListChangeListeners == null)
			workspaceListChangeListeners = new ArrayList<ActionListener>();
		ActionEvent event = new ActionEvent(this, 0, "workspaceListChanged");
		for (ActionListener listener : workspaceListChangeListeners)
			listener.actionPerformed(event);
	}
	
	// enter workspace event
	
	private transient ArrayList<ActionListener>	enterWorkspaceListeners;
	
	public void addEnterWorkspaceListener(ActionListener listener) {
		if (enterWorkspaceListeners == null)
			enterWorkspaceListeners = new ArrayList<ActionListener>();
		enterWorkspaceListeners.add(listener);
	}
	
	public void removeEnterWorkspaceListener(ActionListener listener) {
		enterWorkspaceListeners.remove(listener);
	}
	
	private void notifyWorkspacEntered() {
		if (enterWorkspaceListeners == null)
			return;
		ActionEvent event = new ActionEvent(this, 0, "workspaceEntered");
		for (ActionListener listener : enterWorkspaceListeners)
			listener.actionPerformed(event);
	}
	
}
