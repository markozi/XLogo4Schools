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
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.AppSettings;
import xlogo.interfaces.Observable;
import xlogo.interfaces.PropertyChangePublisher;
import xlogo.storage.StorableObject;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.storage.workspace.WorkspaceConfig.WorkspaceProperty;

/**
 * This Config is stored in default location : "user.home". It contains information about the currently used workspaces on the computer
 * @author Marko Zivkovic
 */
public class GlobalConfig implements Serializable, Observable<GlobalConfig.GlobalProperty> {
	
	private static final long	serialVersionUID	= 2787615728665011813L;
	private static Logger		logger				= LogManager.getLogger(GlobalConfig.class.getSimpleName());
	
	public static final File DEFAULT_LOCATION = new File(System.getProperty("user.home"));

	public static boolean		DEBUG				= true;		// TODO set false
	public static final String	LOGO_FILE_EXTENSION	= ".lgo";
																												
	/**
	 * Creates the global config at default location, together with a virtual workspace
	 */
	public GlobalConfig() {
		workspaces = new TreeMap<String, String>();
	}
		
	public void cleanUpWorkspaces() {
		logger.trace("Cleaning up workspaces.");
		Map<String, String> existingWorkspaces = new TreeMap<String, String>();
		Map<String, String> lostWorkspaces = new TreeMap<String, String>();
		for (Entry<String, String> e : workspaces.entrySet()) {
			File file = new File(e.getValue());
			if (file.exists()) {
				logger.trace("\tConfirmed existence: " + e.getKey() + " at " + e.getValue());
				existingWorkspaces.put(e.getKey(), e.getValue());
			}
			else {
				logger.trace("\tLost workspace: " + e.getKey() + " at " + e.getValue());
				if (e.getKey().equals(lastUsedWorkspace)) {
					lastUsedWorkspace = null;
					currentWorkspace = null;
				}
				lostWorkspaces.put(e.getKey(), e.getValue());
			}
		}
		
		/*// TODO maybe reintroduce this warning. but currently ==> recursive singleton instantiation :-(
		if (lostWorkspaces.size() > 0) {
			StringBuilder msg = new StringBuilder();
			String message = AppSettings.getInstance().translate("message.some.workspaces.not.found");
			String at = AppSettings.getInstance().translate("word.at.filesystem.location");
			msg.append(message);
			for (Entry<String, String> e : lostWorkspaces.entrySet()) {
				msg.append('\t').append(e.getKey()).append(' ').append(at).append(' ').append(e.getValue())
						.append('\n');
			}
			DialogMessenger.getInstance().dispatchMessage(msg.toString());
		}*/
		workspaces = existingWorkspaces;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspaces
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Logical Workspaces (name and location stored in Map)
	 */
	private Map<String, String>	workspaces;

	public void addWorkspace(StorableObject<WorkspaceConfig, WorkspaceProperty> wc) {
		File location = wc.getLocation();
		String name = wc.get().getWorkspaceName();
		addWorkspace(name, location.getParentFile().toString());
	}
	
	/**
	 * @param workspaceName
	 * @param location where the workspace is located: location/workspaceName/
	 */
	public void addWorkspace(String workspaceName, String location) {
		logger.trace("Adding workspace: '" + workspaceName + "' at " + location);
		workspaces.put(workspaceName, location);
		publisher.publishEvent(GlobalProperty.WORKSPACES);
	}
	
	public void removeWorkspace(String workspaceName) {
		logger.trace("Removing workspace: " + workspaceName);
		
		if (currentWorkspace != null && currentWorkspace.get().getWorkspaceName().equals(workspaceName)){
			leaveWorkspace();
		}
		
		if (workspaceName.equals(lastUsedWorkspace)) {
			lastUsedWorkspace = null;
			publisher.publishEvent(GlobalProperty.LAST_USED_WORKSPACE);
		}
		
		workspaces.remove(workspaceName);
		publisher.publishEvent(GlobalProperty.WORKSPACES);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Workspace File Utility
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
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
			publisher.publishEvent(GlobalProperty.LAST_USED_WORKSPACE);
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Current Workspace
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private transient StorableObject<WorkspaceConfig, WorkspaceProperty>	currentWorkspace;
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Entering and Leaving Workspaces
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public StorableObject<WorkspaceConfig, WorkspaceProperty> getCurrentWorkspace() {
		return currentWorkspace;
	}
	
	/**
	 * Load the workspace
	 * <p>Always succeeds if workspaceName equals {@link WorkspaceConfig#VIRTUAL_WORKSPACE}
	 * @param workspaceName - the workspace to load and enter
	 */
	public void enterWorkspace(StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> wc) {
		String name = wc.get().getWorkspaceName();
		logger.trace("Entering workspace: " + name);
		if (currentWorkspace != null) {
			leaveWorkspace();
		}
		currentWorkspace = wc;
		
		setLastUsedWorkspace(name);
		
		publisher.publishEvent(GlobalProperty.CURRENT_WORKSPACE);
	}
	
	/**
	 * Afterwards, currentWorkspace is null
	 */
	public void leaveWorkspace() {
		if (currentWorkspace == null)
			throw new IllegalStateException(AppSettings.getInstance()
					.translate("error.leaving.ws.without.being.in.one"));
		logger.trace("Leaving workspace: " + currentWorkspace.get().getWorkspaceName());
		
		if (currentWorkspace.get().getActiveUser() != null) {
			currentWorkspace.get().leaveUserSpace();
		}
		
		if (currentWorkspace.isDirty())
			currentWorkspace.store();
		
		currentWorkspace = null;

		publisher.publishEvent(GlobalProperty.CURRENT_WORKSPACE);
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
			publisher.publishEvent(GlobalProperty.PASSWORD);
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
		publisher.publishEvent(GlobalProperty.PATH);
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
	
	private transient int	maxMemoryAtNextStart	= getMaximumMemory();
	
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
	
	public static final Font[]	fonts	= GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();	// Toolkit.getDefaultToolkit().getFontList();
																											
	static public int getFontId(Font font) {
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i].getFontName().equals(font.getFontName()))
				return i;
		}
		return 0;
	}
	
	/* * * * * * *
	 * Event Handling : Property Change Listeners
	 * * * * * * */
						
	public enum GlobalProperty {
		PATH, PASSWORD, LAST_USED_WORKSPACE, CURRENT_WORKSPACE, WORKSPACES;
	}

	private transient PropertyChangePublisher<GlobalProperty> publisher = new PropertyChangePublisher<GlobalProperty>();
	
	@Override
	public void addPropertyChangeListener(GlobalProperty property, PropertyChangeListener listener) {
		if (publisher == null){
			publisher = new PropertyChangePublisher<GlobalProperty>();
		}
		publisher.addPropertyChangeListener(property, listener);
	}
	
	@Override
	public void removePropertyChangeListener(GlobalProperty property, PropertyChangeListener listener) {
		publisher.removePropertyChangeListener(property, listener);
	}
}
