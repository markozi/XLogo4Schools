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

package xlogo.storage;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.AppSettings;
import xlogo.Logo;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.global.GlobalConfig.GlobalProperty;
import xlogo.storage.user.UserConfig;
import xlogo.storage.user.UserConfig.UserProperty;
import xlogo.storage.user.UserConfigJSONSerializer;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.storage.workspace.WorkspaceConfig.WorkspaceProperty;
import xlogo.storage.workspace.WorkspaceConfigJSONSerializer;
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
	private static Logger		logger				= LogManager.getLogger(WSManager.class.getSimpleName());
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Singleton
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	private static WSManager	instance;
	private static boolean isConstructingSingleton = false;
	
	public static WSManager getInstance() {
		if (instance == null) {
			if (isConstructingSingleton){
				throw new IllegalStateException("Recursive Singleton Creation.");
			}
			isConstructingSingleton = true;
			instance = new WSManager();
			isConstructingSingleton = false;
		}
		return instance;
	}

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Config Access
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * This is a shortcut for {@code WSManager.getInstance().getWorkspaceConfigInstance()}
	 * This is usually not null {@link WSManager},
	 * but it is for a short time while a workspace is being switched or the program fails to enter a workspace.
	 * @return
	 */
	public static WorkspaceConfig getWorkspaceConfig() {
		return getInstance().getWorkspaceConfigInstance();
	}
	
	/**
	 * This is a shortcut for {@code WSManager.getInstance().getGlobalConfigInstance()}
	 * This is never null by definition of {@link WSManager}
	 * @return
	 */
	public static GlobalConfig getGlobalConfig() {
		return getInstance().getGlobalConfigInstance();
	}
	
	/**
	 * This is a shortcut for {@code WSManager.getInstance().getUserConfigInstance()}
	 * <p> Note that this might be null, if no user has entered his user space.
	 * @return
	 */
	public static UserConfig getUserConfig() {
		return getInstance().getUserConfigInstance();
	}
	
	/**
	 * @return the instance of the GlobalConfig
	 */
	public GlobalConfig getGlobalConfigInstance() {
		return globalConfig.get();
	}
	
	/**
	 * @return the active workspace
	 */
	public WorkspaceConfig getWorkspaceConfigInstance() {
		if (getGlobalConfigInstance().getCurrentWorkspace() != null){
			return getGlobalConfigInstance().getCurrentWorkspace().get();
		}
		return null;
	}
	
	/**
	 * @return the active user
	 */
	public UserConfig getUserConfigInstance() {
		WorkspaceConfig wc = getWorkspaceConfigInstance();
		if (wc == null)
			return null;
		else
			return wc.getActiveUser().get();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Initialization
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private USBWorkspaceManager usbManager;
	private StorableObject<GlobalConfig, GlobalProperty>	globalConfig;
	private boolean hasEnteredApplication = false;
	
	private WSManager() {
		usbManager = new USBWorkspaceManager(new USBWorkspaceManager.WorkspaceContainer(){
			@Override
			public void add(StorableObject<WorkspaceConfig, WorkspaceProperty> usbwc) {
				GlobalConfig gc = getGlobalConfigInstance();
				if (!hasEnteredApplication){
					gc.addWorkspace(usbwc);
					enterWorkspace(gc, usbwc);
				}
			}
			
			@Override
			public void remove(StorableObject<WorkspaceConfig, WorkspaceProperty> usbwc) {
				GlobalConfig gc = getGlobalConfigInstance();
				if (hasEnteredApplication){
					if (usbwc.equals(gc.getCurrentWorkspace())){
						// TODO translate
						DialogMessenger.getInstance().dispatchError(
								"USB Stick Removed", 
								"I will not be able to remember your changes. Please reinsert your USB stick.");
					}
					// else ignore
				} else {
					gc.removeWorkspace(usbwc.get().getWorkspaceName());
					enterInitialWorkspace(gc);
				}
			}
		});
		
		globalConfig = new StorableObject<GlobalConfig, GlobalProperty>(GlobalConfig.class,
				GlobalConfig.DEFAULT_LOCATION).withCreationInitializer(new StorableObject.Initializer<GlobalConfig>(){
			@Override
			public void init(GlobalConfig gc) {
				StorableObject<WorkspaceConfig,WorkspaceProperty> wc = createWorkspace(gc, WorkspaceConfig.DEFAULT_DIRECTORY);
				createUser(wc, UserConfig.DEFAULT_DIRECTORY);
				initGc(gc);
			}
			
		}).withLoadInitializer(new StorableObject.Initializer<GlobalConfig>(){
			@Override
			public void init(GlobalConfig gc) {
				initGc(gc);
			}
		});
		try {
			globalConfig = globalConfig.createOrLoad();
		}
		catch (Exception e) {
			DialogMessenger.getInstance().dispatchError("Unable to Initilize Global Configuration", e.toString());
		}
	}
	
	protected void initGc(GlobalConfig gc){
		usbManager.init();
		gc.cleanUpWorkspaces();
		StorableObject<WorkspaceConfig,WorkspaceProperty> wc = enterInitialWorkspace(gc);
		if (wc != null && wc.get() != null){
			enterInitialUserSpace(wc);
		}
	}
	
	public void enterApplication(){
		hasEnteredApplication = true;
	}
	
	/**
	 * This is used to have a workspace ready at the beginning, without any user interaction.
	 * <p>
	 * Tries to enter workspaces with the following priority.
	 * 1. Last used workspace (if any)
	 * 2. Default workspace, if there is no last used workspace
	 * 3. Virtual Workspace, if entering or creating the default workspace failed for some reason.
	 */
	protected StorableObject<WorkspaceConfig,WorkspaceProperty> enterInitialWorkspace(GlobalConfig gc) {
		logger.trace("Entering initial workspace.");
		
		if (gc.getAllWorkspaces().length == 0){
			logger.warn("No workspaces available.");
			return null;
		}
		
		String initialWs = usbManager.getFirstUSBWorkspace(gc.getAllWorkspaces());
		if (initialWs != null) {
			File wsDir = usbManager.getWorkspaceDirectory(initialWs);
			StorableObject<WorkspaceConfig, WorkspaceProperty> wc = WorkspaceConfigJSONSerializer.createOrLoad(wsDir, true);
			return enterWorkspace(gc, wc);
		}
		
		initialWs = gc.getLastUsedWorkspace();
		
		if (initialWs == null) {
			initialWs = gc.getAllWorkspaces()[0];
		}
		return enterWorkspace(gc, initialWs);
	}
	
	public void enterInitialUserSpace(StorableObject<WorkspaceConfig,WorkspaceProperty> wc) {
		String user = wc.get().getLastActiveUser();
		
		if (user != null && wc.get().existsUserLogically(user)){
			enterUserSpace(user);
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * WORKSPACE CONFIG : create, delete, enter
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * A new workspace is created in the defined directory.
	 * All Necessary files and folders are created and the workspace is logically added to the globalConfig.
	 * @see WorkspaceConfig#loadWorkspace(File)
	 * @param location
	 * @param name
	 */
	public void createWorkspace(File location, String name) {
		File wsDir = StorableObject.getDirectory(location, name);
		GlobalConfig gc = getGlobalConfig();
		createWorkspace(gc, wsDir);
		enterInitialWorkspace(gc);
	}
	
	/**
	 * 
	 * @param gc - The global config, where the new workspace should be registered
	 * @param wsDir - the workspace directory
	 * @return
	 */
	protected StorableObject<WorkspaceConfig, WorkspaceProperty> createWorkspace(GlobalConfig gc, File wsDir){
		StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> wc;
		wc = WorkspaceConfigJSONSerializer.createOrLoad(wsDir);
		if (wc != null){
			gc.addWorkspace(wc);
			enterWorkspace(gc, wsDir.getName());
		}
		return wc;
	}
	
	public void deleteWorkspace(String wsName, boolean deleteFromDisk) {
		GlobalConfig gc = getGlobalConfigInstance();
		File wsDir = gc.getWorkspaceDirectory(wsName);
		gc.leaveWorkspace();
		gc.removeWorkspace(wsName);
		if (deleteFromDisk)
			deleteFullyRecursive(wsDir);
		enterInitialWorkspace(gc);
	}
	
	/**
	 * @param wsDir
	 * @throws IllegalArgumentException if wsDir is not a legal workspace directory
	 */
	public void importWorkspace(File workspaceDir, String workspaceName) {
		logger.trace("Importing workspace '" + workspaceName + "' from " + workspaceDir.getAbsolutePath());
		if (!isWorkspaceDirectory(workspaceDir)) {
			DialogMessenger.getInstance().dispatchError(Logo.messages.getString(MessageKeys.WS_ERROR_TITLE),
					workspaceDir + " " + Logo.messages.getString(MessageKeys.WS_NOT_A_WORKSPACE_DIRECTORY));
			return;
		}
		getGlobalConfigInstance().addWorkspace(workspaceName, workspaceDir.getParent());
		enterWorkspace(workspaceName);
	}
	
	/**
	 * Load the workspace
	 * <p>Always succeeds if workspaceName equals {@link WorkspaceConfig#VIRTUAL_WORKSPACE}
	 * @param workspaceName - the workspace to load and enter
	 * @throws IOException - if the old workspace could not be loaded
	 */
	public void enterWorkspace(String workspaceName) {
		GlobalConfig gc = getGlobalConfigInstance();
		enterWorkspace(gc, workspaceName);
	}
	
	protected StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> enterWorkspace(GlobalConfig gc, String workspaceName) {
		StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> wc = gc.getCurrentWorkspace();
		if(wc != null && workspaceName.equals(wc.get().getWorkspaceName())){
			logger.trace("I'm already in workspace: " + workspaceName);
			return gc.getCurrentWorkspace();
		}
		wc = null;

		if (usbManager.isUSBDrive(workspaceName)) {
			logger.trace("Retrieving USB workspace: " + workspaceName);
			wc = usbManager.createOrLoad(workspaceName);
		}
		
		if (wc == null){
			File wsDir = gc.getWorkspaceDirectory(workspaceName);
			
			if (wsDir == null){
				logger.error("Can't find workspace " + workspaceName);
				return gc.getCurrentWorkspace();
			}
			wc = WorkspaceConfigJSONSerializer.createOrLoad(wsDir);
		}
		
		if (wc == null){
			logger.error("Can't enter workspace because creation or laod failed for " + workspaceName);
			return gc.getCurrentWorkspace();
		}
		
		return enterWorkspace(gc, wc);
	}

	protected StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> enterWorkspace(GlobalConfig gc, StorableObject<WorkspaceConfig, WorkspaceProperty> wc) {
		gc.enterWorkspace(wc);		
		enterInitialUserSpace(wc);
		return gc.getCurrentWorkspace();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *  USER CONFIG : create, delete, enter
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * A new user is created in the current workspace.
	 * All Necessary files and folders are created and the workspace is logically added to the globalConfig.
	 * @param username
	 */
	public void createUser(String username) {
		StorableObject<WorkspaceConfig, WorkspaceProperty> wc = globalConfig.get().getCurrentWorkspace();
		if (wc == null || wc.get() == null){
			throw new IllegalStateException("Cannot create a user directory outside of workspaces. Use createUser(File dir) for special cases.");
		}
		File userDir = StorableObject.getDirectory(wc.getLocation(), username);
		createUser(wc, userDir);
	}
	
	private void createUser(StorableObject<WorkspaceConfig,WorkspaceProperty> wc, File userDir){
		StorableObject<UserConfig, UserConfig.UserProperty> duc = new StorableObject<UserConfig,UserProperty>(UserConfig.class, userDir);
		try {
			duc = duc.createOrLoad();
		}
		catch (Exception e) { }
		if (!duc.isPersisted()){
			logger.warn("Could not persist user files.");
		}
		duc.get().setDirectory(userDir);
		wc.get().addUser(duc);
		enterUserSpace(wc.get(), duc);
	}
	
	/**
	 * @param username
	 * @param deleteFromDisk
	 */
	public void deleteUser(String username, boolean deleteFromDisk) {
		File location = StorableObject.getDirectory(getWorkspaceConfigInstance().getDirectory(), username);
		getWorkspaceConfigInstance().removeUser(username);
		if (deleteFromDisk) {
			try {
				deleteFullyRecursive(location);
			}
			catch (SecurityException e) {
				System.out.println("Files not deleted: " + e.toString());
			}
		}
	}
		
	/**
	 * Import a user directory from anywhere in the file system to this workspace.
	 * All files in the user directory are copied. Already existing files might get overwritten.
	 * <p> This has no effect if this is virtual.
	 * @param srcUserDir - a legal user directory anywhere on the file system
	 * @param destUsername - Existing files of targetUser are overwritten. If targetUser does not exist, it will be created first.
	 * @throws IllegalArgumentException
	 * @throws IOException 
	 * @see WSManager#isUserDirectory(File)
	 */
	public void importUser(File srcUserDir, String destUsername) throws IllegalArgumentException, IOException {
		logger.trace("Importing user '" + destUsername + "' from " + srcUserDir.getAbsolutePath());
		
		if (!isUserDirectory(srcUserDir))
			throw new IllegalArgumentException("Target directory is not a user directory.");
		
		createUser(destUsername);
		File wsDir = getWorkspaceConfig().getDirectory();
		File targetUserDir = StorableObject.getDirectory(wsDir, destUsername);
		
		copyFullyRecursive(srcUserDir, targetUserDir);

		enterUserSpace(destUsername);
	}
	
	/**
	 * @throws IOException If the old userConfig could not be stored. 
	 */
	public void enterUserSpace(String name) {
		WorkspaceConfig wc = getWorkspaceConfigInstance();
		if (wc == null)
			throw new IllegalStateException("Must be in WorkspaceDirectory first to enter UserSpace.");
		enterUserSpace(wc, name);
	}
	
	protected void enterUserSpace(WorkspaceConfig wc, String username) {
		File userDir = StorableObject.getDirectory(wc.getDirectory(), username);
		StorableObject<UserConfig, UserProperty> uc = UserConfigJSONSerializer.createOrLoad(userDir);
		if (uc != null){
			enterUserSpace(wc, uc);
		} else {
			DialogMessenger.getInstance().dispatchError("Workspace Error", "Cannot enter workspace.");
		}
	}
	
	protected void enterUserSpace(WorkspaceConfig wc, StorableObject<UserConfig, UserProperty> uc) {
		wc.enterUserSpace(uc);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *  SHORTCUTS
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	public void storeAllSettings() {
		globalConfig.store();
		StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> swc = globalConfig.get().getCurrentWorkspace();
		if (swc == null) { return; }
		swc.store();
		StorableObject<UserConfig, UserProperty> suc = swc.get().getActiveUser();
		if (suc == null) { return; }
		suc.store();
	}
	
	/**
	 * Make sure all threads are stopped and open files saved and closed.
	 */
	public void stopEverything(){
		usbManager.stop();
		storeAllSettings();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *  DIRECTORIES & FILE MANIPULATION
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static File[] listDirectories(File dir) {
		File[] dirs = dir.listFiles(new java.io.FileFilter(){
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (dirs == null) {
			dirs = new File[0];
		}
		return dirs;
	}
	
	/**
	 * A directory is considered a workspace directory,
	 * if it contains a file for {@link WorkspaceConfig}, as defined by {@link StorableObject#getFilePath(File, Class)}
	 * @param dir
	 * @return
	 */
	public static boolean isWorkspaceDirectory(File dir) {
		if (dir == null)
			return false;
		
		if (!dir.isDirectory())
			return false;
		
		File wcf = StorableObject.getFilePath(dir, WorkspaceConfig.class);
		if (!wcf.isFile())
			return false;
		
		return true;
	}
	
	/**
	 * A directory is considered a user directory,
	 * if it contains a file for {@link UserConfig},  as defined by {@link StorableObject#getFilePath(File, Class)}
	 * @param dir
	 * @return
	 */
	public static boolean isUserDirectory(File dir) {
		if (dir == null)
			return false;
		
		if (!dir.isDirectory())
			return false;
		
		File ucf = StorableObject.getFilePath(dir, UserConfig.class);
		if (!ucf.isFile())
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
	public static void copyFullyRecursive(File from, File to) throws IOException {
		if (!from.exists())
			throw new IllegalArgumentException("'from' (" + from.toString() + ") must exist.");
		
		if (from.isFile()) {
			copyFile(from, to);
			return;
		}
		
		// else to is directory
		to.mkdirs();
		
		for (File src : from.listFiles()) {
			File dest = new File(to.toString() + File.separator + src.getName());
			if (src.isFile()) {
				copyFile(src, dest);
			}
			else if (src.isDirectory()) {
				copyFullyRecursive(src, dest);
			}
		}
		
	}
	
	public static void copyFile(File from, File to) throws IOException {
		if (!from.isFile())
			throw new IllegalArgumentException("File 'from' (" + from.toString() + ") must exist.");
		
		if (to.exists()) {
			if (!to.isFile())
				throw new IllegalArgumentException("File 'to' (" + from.toString() + ") must be a file.");
		}
		else {
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
	public static void deleteFullyRecursive(File victim) throws SecurityException {
		if (!victim.exists())
			return;
		
		if (victim.isFile()) {
			victim.delete();
			return;
		}
		
		if (!isGlobalConfigDirectory(victim) && !isWorkspaceDirectory(victim) && !isUserDirectory(victim)) {
			String title = AppSettings.getInstance().translate("error.security.violation.title");
			String message = AppSettings.getInstance().translate("error.attempt.delete.non.x4s.file");
			DialogMessenger.getInstance().dispatchError(title, message + ' ' + victim.toString());
			throw new SecurityException();
		}
		
		// Delete all sub-directories
		for (File f : victim.listFiles()) {
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
	private static void uncheckedRecursiveDelete(File victim) {
		if (!victim.exists())
			return;
		
		if (victim.isFile()) {
			victim.delete();
			return;
		}
		
		// Delete all sub-directories
		for (File f : victim.listFiles()) {
			uncheckedRecursiveDelete(f);
			victim.delete();
		}
		
		// Delete directory itself
		victim.delete();
	}
	
	public static boolean isGlobalConfigDirectory(File dir) {
		if (!dir.isDirectory())
			return false;
		
		String name = dir.getName();
		if (!name.startsWith("X4S_"))
			return false;
		
		return true;
	}
}
