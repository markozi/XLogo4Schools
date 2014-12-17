/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage;

import java.io.File;
import java.io.IOException;

import xlogo.AppSettings;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;

/**
 * Singleton Class for maintaining XLogo4Schools workspace, properties, reading and writing the various config files
 * <p>
 * The workspace can be entered without using a user account. in that case, one enters as a <b>virtual user</b>.
 * While working as virtual user, the changes of preferences and the programs are not stored persistently.
 * However, one can still export and import program files. This behavior is similar to XLogo's file management that uses the classic save/open machanisms.
 * <br><br>
 * <p> XLogo4Schools maintains the following files and directories on the file system
 * <p>
 * <li> user_home/X4S_GlobalConfig.ser - this file stores information about how to access the various workspaces ({@link GlobalConfig})</li>
 * 
 * <p>
 * <li> workspaceLocation/ - the folder of a workspace on the file system</li>
 * <li> workspaceLocation/X4S_WorkspaceConfig.ser - Information about the workspace ({@link WorkspaceConfig})</li>
 * <li> workspaceLocation/user_i/ - Project folder of "user i" in the workspace</li>
 * <li> workspaceLocation/user_i/X4S_UserConfig.ser - User preferences and settings ({@link UserConfig})</li>
 * <li> workspaceLocation/user_i/file_j_v.lgo - Version v of file_j.lgo (the last n versions are kept)</li>
 * <li> workspaceLocation/user_i/competition_protocol_k.txt - the protocol of the k'th recorded competition/session of user i</li>
 * <p>
 * The files with the ending .ser are serialized objects. They are loaded from the file system when a workspace or userspace is entered.
 * The files are (re-)written to the file system whenever a user space, a workspace or XLogo4Schools is left.
 * <p>
 * <br><br>
 * <p> <b>Invariant</b> : there is always an active GlobalConfig, WorkspaceConfig, and UserConfig.
 * <p> Upon creation or loading of some config, it decides which sub-config should be loaded.
 * <li> GlobalConfig tries to enter the last used workspace if possible, otherwise it enters the virtual workspace. </li>
 * <li> WorksspaceConfig tries to enter the last active userspace if possible, otherwise it enters the virtual userspace. </li>
 * 
 * @author Marko
 */
public class WSManager {
	

	/*
	 * Singleton instantiation
	 */
	private static WSManager instance;

	public static WSManager getInstance(){
		if (instance == null){
			instance = new WSManager();
		}
		return instance;
	}
	
	/**
	 * This is a shortcut for {@code WSManager.getInstance().getWorkspaceConfigInstance()}
	 * This is usually not null {@link WSManager},
	 * but it is for a short time while a workspace is being switched or the program fails to enter a workspace.
	 * @return
	 */
	public static WorkspaceConfig getWorkspaceConfig()
	{
		return getInstance().getWorkspaceConfigInstance();
	}

	/**
	 * This is a shortcut for {@code WSManager.getInstance().getGlobalConfigInstance()}
	 * This is never null by definition of {@link WSManager}
	 * @return
	 */
	public static GlobalConfig getGlobalConfig()
	{
		return getInstance().getGlobalConfigInstance();
	}

	/**
	 * This is a shortcut for {@code WSManager.getInstance().getUserConfigInstance()}
	 * <p> Note that this might be null, if no user has entered his user space.
	 * @return
	 */
	public static UserConfig getUserConfig()
	{
		return getInstance().getUserConfigInstance();
	}
	
	private WSManager() {
		globalConfig = GlobalConfig.create();
	}
	
	/*
	 * Config access
	 */
	private final GlobalConfig globalConfig;
	
	/**
	 * @return the instance of the GlobalConfig
	 */
	public GlobalConfig getGlobalConfigInstance()
	{
		return globalConfig;
	}
	
	/**
	 * @return the active workspace
	 */
	public WorkspaceConfig getWorkspaceConfigInstance()
	{
		return getGlobalConfigInstance().getCurrentWorkspace();
	}
	
	/**
	 * @return the active user
	 */
	public UserConfig getUserConfigInstance()
	{
		WorkspaceConfig wc = getWorkspaceConfigInstance();
		if (wc == null)
			return null;
		else
			return wc.getActiveUser();
	}
	
	/*
	 * WORKSPACE CONFIG : create, delete, enter
	 */
	
	/**
	 * A new workspace is created in the defined directory.
	 * All Necessary files and folders are created and the workspace is logically added to the globalConfig.
	 * @see WorkspaceConfig#loadWorkspace(File)
	 * @param dir
	 * @param name
	 * @throws IOException
	 */
	public void createWorkspace(File dir, String name) throws IOException
	{
		getGlobalConfigInstance().createWorkspace(dir, name);
	}
	
	public void deleteWorkspace(String wsName, boolean deleteFromDisk) throws SecurityException
	{
		File wsDir = getGlobalConfigInstance().getWorkspaceDirectory(wsName);
		getGlobalConfigInstance().removeWorkspace(wsName);
		if (deleteFromDisk)
			deleteFullyRecursive(wsDir);
	}
	
	/**
	 * @param wsDir
	 * @throws IllegalArgumentException if wsDir is not a legal workspace directory
	 */
	public void importWorkspace(File wsDir, String wsName) throws IllegalArgumentException
	{
		getGlobalConfigInstance().importWorkspace(wsDir, wsName);
	}
	
	/**
	 * Load the workspace
	 * <p>Always succeeds if workspaceName equals {@link WorkspaceConfig#VIRTUAL_WORKSPACE}
	 * @param workspaceName - the workspace to load and enter
	 * @throws IOException - if the old workspace could not be loaded
	 */
	public void enterWorkspace(String workspaceName) throws IOException
	{
		getGlobalConfigInstance().enterWorkspace(workspaceName);
	}
	
	/* *
	 * @throws IOException If workspace could not be saved.
	 * /
	public void leaveWorkspace() throws IOException
	{
		getGlobalConfig().leaveWorkspace();
	}*/
	
	
	/*
	 * USER CONFIG : create, delete, enter
	 */

	/**
	 * A new user is created in the current workspace.
	 * All Necessary files and folders are created and the workspace is logically added to the globalConfig.
	 * @param username
	 */
	public void createUser(String username)
	{	
		getWorkspaceConfigInstance().createUser(username);
	}
	
	/**
	 * @param username
	 * @param deleteFromDisk
	 */
	public void deleteUser(String username, boolean deleteFromDisk)
	{
		File location = StorableObject.getDirectory(getWorkspaceConfigInstance().getLocation(), username);
		getWorkspaceConfigInstance().removeUser(username);
		if (deleteFromDisk)
		{
			try{
				deleteFullyRecursive(location);
			}
			catch(SecurityException e) {
				System.out.println("Files not deleted: " + e.toString());
			}
		}
	}
	
	public void importUser(File userDir, String newUserName) throws IllegalArgumentException, IOException
	{
		getWorkspaceConfigInstance().importUser(userDir, newUserName);
	}
	
	/**
	 * @throws IOException If the old userConfig could not be stored. 
	 */
	public void enterUserSpace(String name) throws IOException
	{
		if (getWorkspaceConfigInstance() == null)
			throw new IllegalStateException("Must be in WorkspaceDirectory first to enter UserSpace.");
		
		getWorkspaceConfigInstance().enterUserSpace(name);
	}
	
	/*
	 * DIRECTORIES & FILE MANIPULATION
	 */

	public static File[] listDirectories(File dir)
	{			
		return dir.listFiles(new java.io.FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
	}

	/**
	 * A directory is considered a workspace directory,
	 * if it contains a file for {@link WorkspaceConfig}, as defined by {@link StorableObject#getFilePath(File, Class)}
	 * @param dir
	 * @return
	 */
	public static boolean isWorkspaceDirectory(File dir)
	{
		if (dir == null)
			return false;
		
		if(!dir.isDirectory())
			return false;
		
		File wcf = WorkspaceConfig.getFile(dir, WorkspaceConfig.class);
		if(!wcf.isFile())
			return false;
		
		return true;
	}
	
	/**
	 * A directory is considered a user directory,
	 * if it contains a file for {@link UserConfig},  as defined by {@link StorableObject#getFilePath(File, Class)}
	 * @param dir
	 * @return
	 */
	public static boolean isUserDirectory(File dir)
	{
		if (dir == null)
			return false;
		
		if(!dir.isDirectory())
			return false;
		
		File ucf = UserConfig.getFile(dir, UserConfig.class);
		if(!ucf.isFile())
			return false;
		
		return true;
	}

	/**
	 * If "from" denotes a file, then "to" should be a file too.
	 * The contents of file "from" are copied to file "to".
	 * "to" is created, if it does not exists, using mkdirs.
	 * 
	 * <p>If "from" denotes a directory, then "to" should be a directory too.
	 * The contents of directory "from" are copied recursively to directory "to".
	 * "to" is created, if it does not exists, using mkdirs.
	 * @param from - must exist
	 * @param to - must not exist
	 * @throws IOException 
	 */
	public static void copyFullyRecursive(File from, File to) throws IOException
	{
		if (!from.exists())
			throw new IllegalArgumentException("'from' (" + from.toString() + ") must exist.");
		
		if(from.isFile())
		{
			copyFile(from, to);
			return;
		}
		
		// else to is directory
		to.mkdirs();
	
		for(File src : from.listFiles())
		{
			File dest = new File(to.toString() + File.separator + src.getName());
			if (src.isFile())
			{
				copyFile(src, dest);
			}else if (src.isDirectory())
			{
				copyFullyRecursive(src, dest);
			}
		}
		
	}
	
	public static void copyFile(File from, File to) throws IOException
	{
		if (!from.isFile())
			throw new IllegalArgumentException("File 'from' (" + from.toString() + ") must exist.");
		
		if (to.exists())
		{
			if (!to.isFile())
				throw new IllegalArgumentException("File 'to' (" + from.toString() + ") must be a file.");
		}
		else
		{
			File parent = to.getParentFile();
			if (!parent.exists())
				to.getParentFile().mkdirs();
		}
		
		Utils.copyFile(from, to);
	}
	
	/**
	 * @param victim
	 * @throws SecurityException If one tries to delete some directory that is not under control of this application (Workspace or User)
	 */
	public static void deleteFullyRecursive(File victim) throws SecurityException
	{
		if (!victim.exists())
			return;
		
		if (victim.isFile())
		{
			victim.delete();
			return;
		}
		
		if (!isGlobalConfigDirectory(victim) && !isWorkspaceDirectory(victim) && !isUserDirectory(victim))
		{
			String title = AppSettings.getInstance().translate("error.security.violation.title");
			String message = AppSettings.getInstance().translate("error.attempt.delete.non.x4s.file");
			DialogMessenger.getInstance().dispatchError(title, message + ' ' + victim.toString());
			throw new SecurityException();
		}
		
		// Delete all sub-directories
		for (File f : victim.listFiles())
		{
			uncheckedRecursiveDelete(f);
			victim.delete();
		}
		
		// Delete directory itself
		victim.delete();
	}
	
	/**
	 * CAUTION : Don't use this unless you are sure that the directory you want to delete is a XLogo4Schools directory.
	 * Otherwise it could delete just everything.
	 * @param victim
	 */
	private static void uncheckedRecursiveDelete(File victim)
	{
		if (!victim.exists())
			return;
		
		if (victim.isFile())
		{
			victim.delete();
			return;
		}
		
		// Delete all sub-directories
		for (File f : victim.listFiles())
		{
			uncheckedRecursiveDelete(f);
			victim.delete();
		}
		
		// Delete directory itself
		victim.delete();
	}
	
	public static boolean isGlobalConfigDirectory(File dir)
	{
		if(!dir.isDirectory())
			return false;
		
		String name = dir.getName();
		if(!name.startsWith("X4S_"))
			return false;
		
		
		return true;
	}
}
