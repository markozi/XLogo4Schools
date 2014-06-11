/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loïc Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * 
 * Contact Information: marko88zivkovic at gmail dot com
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.  This program is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.  You should have received a copy of the 
 * GNU General Public License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA 02110-1301, USA.
 * 
 * 
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zürich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loïc Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
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
import java.util.TreeMap;

import xlogo.Logo;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.StorableObject;
import xlogo.storage.WSManager;
import xlogo.storage.workspace.WorkspaceConfig;

/**
 * This Config is stored in default location : "user.home". It contains information about the currently used workspaces on the computer
 * @author Marko Zivkovic
 */
public class GlobalConfig extends StorableObject implements Serializable {

	private static final long serialVersionUID = 2787615728665011813L;
	
	public static final String LOGO_FILE_EXTENSION = ".lgo";
	public static boolean DEBUG = true; // TODO set false
	
	/**
	 * Creates the global config at default location, together with a virtual workspace
	 */
	protected GlobalConfig()
	{
		try
		{
			setLocation(getDefaultLocation());
		}
		catch (IllegalArgumentException ignore) { } // This is thrown if name illegal, but it is legal
		workspaces = new TreeMap<String, String>();
		workspaces.put(WorkspaceConfig.VIRTUAL_WORKSPACE, "");
	}
	
	/**
	 * If GlobalConfig exists on the file system in default location, it is loaded, otherwise it will be created there.
	 * @return
	 */
	public static GlobalConfig create()
	{
		File gcf = getFile(getDefaultLocation(), GlobalConfig.class);
		
		GlobalConfig globalConfig = null;
		
		try
		{
			if (gcf.exists())
				globalConfig = (GlobalConfig) loadObject(gcf);
			else
			{
				globalConfig = new GlobalConfig();
				globalConfig.store();
			}
		}catch(Exception e)
		{
			// Best effort : We will try to operate the program without storing anything on disk
			globalConfig = getNewVirtualInstance();
			DialogMessenger.getInstance().dispatchError("Error while setting up XLogo4Schools", "Could not create or open GlobalConfig file at default location: " + e.toString());
		}
		globalConfig.enterLastUsedWorkspace();// This is used to have a workspace ready at the beginning, without any user interaction.
		return globalConfig;
	}
	
	public static GlobalConfig getNewVirtualInstance()
	{
		GlobalConfig gc = new GlobalConfig();
		gc.makeVirtual();
		return gc;
	}
	
	/**
	 * @return File from system property "user.home"
	 */
	public static File getDefaultLocation()
	{
		return new File(System.getProperty("user.home"));
	}
	
	/*
	 * Physical Workspaces (stored on file system)
	 */
	
	public void createWorkspace(File dir, String workspaceName) throws IOException
	{
		if (WorkspaceConfig.createNewWorkspace(dir, workspaceName) != null)
			addWorkspace(workspaceName, dir.toString());
	}
	
	public void importWorkspace(File workspaceDir, String wsName)
	{
		if(!WSManager.isWorkspaceDirectory(workspaceDir))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.WS_ERROR_TITLE), 
					workspaceDir + " " + Logo.messages.getString(MessageKeys.WS_NOT_A_WORKSPACE_DIRECTORY));
			return;
		}
		
		addWorkspace(wsName, workspaceDir.getParent());
	}
	
	/**
	 * Load the specified workspace from the file system.
	 * @param workspaceName
	 * @return the specified workspace or null if it does not exist.
	 * @throws IOException
	 */
	private WorkspaceConfig retrieveWorkspace(String workspaceName) throws IOException
	{
		if(!existsWorkspace(workspaceName))
		{
			System.out.print("Attempting to load an inexistent workspace.");
			return null;
		}
		File dir = getWorkspaceLocation(workspaceName);
		WorkspaceConfig wc = WorkspaceConfig.loadWorkspace(dir, workspaceName);
		
		if (wc == null)
		{
			WSManager.getInstance().deleteWorkspace(workspaceName, false);
		}
		return wc;
		
	}
	
	/*
	 * Logical Workspaces (name and location stored in Map)
	 */
	
	private TreeMap<String,String> workspaces;
	
	/**
	 * @param workspaceName
	 * @param location where the workspace is located: location/workspaceName/
	 */
	public void addWorkspace(String workspaceName, String location)
	{
		workspaces.put(workspaceName, location);
		makeDirty();
		notifyWorkspaceListChanged();
	}
	
	public void removeWorkspace(String workspaceName)
	{
		workspaces.remove(workspaceName);
		makeDirty();
		notifyWorkspaceListChanged();
	}
	
	/**
	 * @param wsName
	 * @return the location of the workspace in the file system, or null if the workspace does not exist
	 */
	public File getWorkspaceLocation(String wsName)
	{
		String location = workspaces.get(wsName);
		if(location == null)
			return null;
		return new File(location);
	}
	
	public File getWorkspaceDirectory(String wsName)
	{
		File wsLocation = getWorkspaceLocation(wsName);
		if(wsLocation == null)
			return null;
		return new File(wsLocation.toString() + File.separator + wsName);
	}
	
	/**
	 * @return the names of all existing workspaces
	 */
	public String[] getAllWorkspaces()
	{
		return (String[]) workspaces.keySet().toArray(new String[workspaces.size()]);
	}

	/**
	 * A workspace exists logically, if its location is known by the GlobalConfig.
	 * @param workspace
	 * @return
	 */
	public boolean existsWorkspace(String workspace)
	{
		return getWorkspaceLocation(workspace) != null;
	}
	
	/*
	 * Last used workspace
	 */
	private String lastUsedWorkspace;
	
	public String getLastUsedWorkspace()
	{
		return lastUsedWorkspace;
	}
	
	/**
	 * Succeeds if the workspace exists
	 * @param workspace
	 */
	private void setLastUsedWorkspace(String workspace)
	{
		if(existsWorkspace(workspace))
		{
			lastUsedWorkspace = new String(workspace);
			makeDirty();
		}
	}
	
	/**
	 * This is used to have a workspace ready at the beginning, without any user interaction.
	 * <p>
	 * Enters the workspace that was used the last time XLogo4Schools was run on this computer.
	 * If no regular workspace is available, a purely logical "virtual workspace" is entered instead. 
	 */
	private void enterLastUsedWorkspace()
	{
		String last = getLastUsedWorkspace();
		
		if(last == null || !existsWorkspace(last))
			last = WorkspaceConfig.VIRTUAL_WORKSPACE;	// this exists, see constructor
		
		try {
			enterWorkspace(last);
		} catch (IOException e1) {
			try { enterWorkspace(WorkspaceConfig.VIRTUAL_WORKSPACE); } catch (IOException e2) { }
			DialogMessenger.getInstance().dispatchError("Workspace Error", "Cannot enter workspace: " + e1.toString());
		}
	}
		
	/*
	 * Current Workspace
	 */
	
	private transient WorkspaceConfig currentWorkspace;
	
	public WorkspaceConfig getCurrentWorkspace()
	{
		return currentWorkspace;
	}
	
	/**
	 * Load the workspace
	 * <p>Always succeeds if workspaceName equals {@link WorkspaceConfig#VIRTUAL_WORKSPACE}
	 * @param workspaceName - the workspace to load and enter
	 * @throws IOException - if the workspace could not be loaded
	 */
	public void enterWorkspace(String workspaceName) throws IOException
	{
		if(currentWorkspace != null)
		{
			leaveWorkspace();
		}
		currentWorkspace = retrieveWorkspace(workspaceName);
		if (currentWorkspace == null)
			currentWorkspace = retrieveWorkspace(WorkspaceConfig.VIRTUAL_WORKSPACE);
		
		setLastUsedWorkspace(workspaceName);
		
		notifyWorkspacEntered();
	}
	
	/**
	 * @throws IOException If workspace could not be saved.
	 */
	void leaveWorkspace() throws IOException
	{
		if(currentWorkspace == null)
			throw new IllegalStateException("Attempt to leave workspace without being in one.");
		
		if(currentWorkspace.getActiveUser() != null)
		{
			currentWorkspace.leaveUserSpace();
		}
		
		if(currentWorkspace.isDirty())
			currentWorkspace.store();
		
		currentWorkspace = null;
	}

	/*
	 * Password protection
	 */
	
	/**
	 * if null, no password is requested
	 */
	private byte[] masterPassword = null;
	
	/**
	 * Need old password to authenticate
	 * @param oldPw initially null
	 * @param newPw
	 * @return success
	 */
	public boolean setNewPassword(String oldPw, String newPw)
	{
		if(masterPassword == null || authenticate(oldPw))
		{
			if (newPw == null)
				masterPassword = null;
			else
				masterPassword = hash(newPw);
			makeDirty();
			return true;
		}else
		{
			return false;
		}
	}
	
	public boolean isPasswordRequired()
	{
		return masterPassword != null;
	}
	
	public boolean authenticate(String password){
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
	private byte[] hash(String text)
	{
		if (text == null)
			return null;
		
		byte[] bytesOfMessage;
		try {
			bytesOfMessage = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			bytesOfMessage = text.getBytes();	// this should not happen anyway
		}

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(bytesOfMessage);
		} catch (NoSuchAlgorithmException e) {
			return bytesOfMessage;	// this should not happen anyway
		}
	}
	
	/*
	 * PATH
	 */
	
	/**
	 * This Stack contains all startup files
	 */
	private ArrayList<String> path = new ArrayList<String>();
	
	public ArrayList<String> getPath() {
		return path;
	}


	public void setPath(ArrayList<String> path) {
		this.path = path;
		makeDirty();
	}
	
	public static String getVersion() {
		return "XLogo4Schools 0.0.1";
	}

	/**
	 * Note : should be equal as in {@link Lanceur}
	 */
	//private static String	PROPERTIES_PREFIX		= "ch.ethz.abz.xlogo4schools";
	
	/**
	 * Note : should be equal as in {@link Lanceur}
	 */
	private static int		DEFAULT_MEMORY_ALLOC	= 128;
	
	private static int maximumMemory;
	
	/**
	 * The Maximum amount of memory that this application is allowed to consume by the JVM
	 * @return
	 */
	public static int getMaximumMemory()
	{
		if (maximumMemory < 64)
		{
			// This doesn't work as expected :-(
			//Preferences prefs = Preferences.systemRoot().node(PROPERTIES_PREFIX);
			//maximumMemory = prefs.getInt("appMemory", DEFAULT_MEMORY_ALLOC);
			maximumMemory = DEFAULT_MEMORY_ALLOC;
		}
		return maximumMemory;
	}
	
	private transient int maxMemoryAtNextStart = getMaximumMemory();

	public static final Font[] fonts = GraphicsEnvironment
	.getLocalGraphicsEnvironment().getAllFonts();// Toolkit.getDefaultToolkit().getFontList();
	
	/**
	 * @return The amount of memory in MB that Lanceur will cause JVM to allocate to XLogo4Schools the next time this application is started.
	 */
	public int getMaxMemoryAtNextStart()
	{
		if (maxMemoryAtNextStart < 64)
			maxMemoryAtNextStart = getMaximumMemory();
		return maxMemoryAtNextStart;
	}
	/**
	 * @see #getMaxMemoryAtNextStart()
	 * cannot set this below 64MB
	 * @param maxMemory
	 */
	public void setMaxMemoryAtNextStart(int maxMemory)
	{
		if (maxMemory < 64)
			return;
		// This doesn't work as well :-(
		//Preferences prefs = Preferences.systemRoot().node(PROPERTIES_PREFIX);
		//prefs.putInt("appMemory", maxMemory);
	}
	
	static public int police_id(Font font) {
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i].getFontName().equals(font.getFontName()))
				return i;
		}
		return 0;
	}

	/**
	 * The amount of memory that the memory checker allows the application to consume.
	 * It's 0.9*{@link #getMaximumMemory()}} in bytes.
	 */
	public static long getMemoryThreshold()
	{
		return (long) (0.9 * ((long) GlobalConfig.getMaximumMemory() * 1024L * 1024L));
	}

	
	/* * * * * * *
	 * Event Handling
	 * * * * * * */
	
	// workspace list change
	
	private transient ArrayList<ActionListener> workspaceListChangeListeners;
	
	public void addWorkspaceListChangeListener(ActionListener listener)
	{
		if (workspaceListChangeListeners == null)
			workspaceListChangeListeners = new ArrayList<ActionListener>();
		workspaceListChangeListeners.add(listener);
	}
	
	public void removeWorkspaceListChangeListener(ActionListener listener)
	{
		workspaceListChangeListeners.remove(listener);
	}
	
	private void notifyWorkspaceListChanged()
	{
		ActionEvent event = new ActionEvent(this, 0, "workspaceListChanged");
		for (ActionListener listener : workspaceListChangeListeners)
			listener.actionPerformed(event);
	}

	// enter workspace event
	
	private transient ArrayList<ActionListener> enterWorkspaceListeners;
	
	public void addEnterWorkspaceListener(ActionListener listener)
	{
		if (enterWorkspaceListeners == null)
			enterWorkspaceListeners = new ArrayList<ActionListener>();
		enterWorkspaceListeners.add(listener);
	}
	
	public void removeEnterWorkspaceListener(ActionListener listener)
	{
		enterWorkspaceListeners.remove(listener);
	}
	
	private void notifyWorkspacEntered()
	{
		if (enterWorkspaceListeners == null)
			return;
		ActionEvent event = new ActionEvent(this, 0, "workspaceEntered");
		for (ActionListener listener : enterWorkspaceListeners)
			listener.actionPerformed(event);
	}

}
