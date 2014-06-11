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

package xlogo.storage.workspace;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import xlogo.AppSettings;
import xlogo.Logo;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.StorableObject;
import xlogo.storage.WSManager;
import xlogo.storage.user.UserConfig;

/**
 * WorkspaceConfig maintains a workspace (i.e. a "class room") that consists of several projects or "Users".
 * It defines a common language for the project (English, French, ...) and a master password for the workspace owner.
 * The password is intended to allow certain settings only for the workspace owner. Therefore the corresponding methods will require a password.
 * <br> A directory on the file system is an XLogo4Schools workspace, if it contains a file named "X4S_WorkspaceConfig.ser",
 * no matter what it contains.
 * <br> If a workspace is made virtual, it is not stored on the file system. ({@link #createVirtualWorkspace()})
 * <p>
 * A user belongs to the workspace, if it is contained logically in the user list.
 * For a user to work correctly in its user space, a user directory {@code workspaceDir/username/}
 * and the file {@code workspaceDir/username/X4S_UserConfig.ser} are required.
 * As long as a user exists logically in the workspace, missing or corrupted files are recreated if needed.
 * If a user is deleted, it is deleted only logically, so it can be reintegrated later again.
 * @author Marko Zivkovic
 */
public class WorkspaceConfig extends StorableObject implements Serializable {

	private static final long serialVersionUID = -3554871695113998509L;

	/**
	 * Name of the virtual workspace
	 */
	public static final String VIRTUAL_WORKSPACE = "Guest Workspace (no automatic save)";

	protected WorkspaceConfig()
	{
		super();
		userList = new ArrayList<String>();
		language = Language.LANGUAGE_ENGLISH;
		font = new Font("dialog",Font.PLAIN,14); // TODO on access check if it is null.
		// TODO what if incompatible?
		syntaxHighlightingStyles = new SyntaxHighlightConfig();
		AppSettings.getInstance().setFont(font);
	}
	
	/**
	 * @return
	 * @throws IllegalStateException if this is not virtual and {@link #getLocation()} returns null
	 */
	public String getWorkspaceName() throws IllegalStateException
	{
		if (isVirtual())
			return VIRTUAL_WORKSPACE;
		
		File wsDir = getLocation();
		if (wsDir == null)
			throw new IllegalStateException("Name is not available because location is null.");
		return wsDir.getName();
	}
	
	/*
	 * Static constructors
	 */
	private static WorkspaceConfig virtualWS;
	
	/**
	 * A virtual user can enter the application in a virtual workspace without having an actual user account on the file system. Hence nothing will be stored.
	 * A regular user (not virtual) will have his own folder in a regular workspace on the file system and all his preferences and files are stored there.
	 * To create a regular workspace, use {@link #createNewWorkspace(File, String)},
	 * to load a regular workspace from the file system, user {@link #loadWorkspace(File)}}.
	 * @see #isVirtual()
	 * @return a virtual workspace
	 */
	public static WorkspaceConfig createVirtualWorkspace()
	{
		if (virtualWS == null)
		{
			virtualWS = new WorkspaceConfig();
			virtualWS.makeVirtual();
		}
		return virtualWS;
	}
	
	@Override
	protected void makeVirtual()
	{
		super.makeVirtual();
		userList = new ArrayList<String>();
		userList.add(UserConfig.VIRTUAL_USER);
		lastActiveUser = UserConfig.VIRTUAL_USER;
		try { enterUserSpace(UserConfig.VIRTUAL_USER); } catch (IOException e) { /* Does not happen */ }
	}
	
	/**
	 * @param dir
	 * @param workspaceName
	 * @return
	 * @throws IOException
	 */
	public static WorkspaceConfig createNewWorkspace(File dir, String workspaceName) throws IOException
	{
		if (!Storable.checkLegalName(workspaceName))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE), 
					Logo.messages.getString(MessageKeys.ILLEGAL_NAME));
			return null;
		}
		
		File wsd = getDirectory(dir, workspaceName);
		boolean existed = wsd.exists();
		if (!existed)
		{
			wsd.mkdirs();
		}
		// wsd exists and it is empty
		WorkspaceConfig wsc = new WorkspaceConfig();
		wsc.setLocation(wsd);
		//if(existed && WSManager.isUserDirectory(wsd))
		//	wsc.refreshUserList();
		wsc.store();
		return wsc;
	}

	/**
	 * @see #loadWorkspace(File)
	 * @param dir - location of the workspace
	 * @param workspaceName
	 * @return
	 * @throws IOException
	 */
	public static WorkspaceConfig loadWorkspace(File dir, String workspaceName) throws IOException
	{
		File wsc = getDirectory(dir, workspaceName);
		return loadWorkspace(wsc);
	}
	
	/**
	 * @param workspaceDir
	 * @return Load an existing workspace from the file system.
	 * If workspaceDir specifies a {@link WorkspaceConfig#VIRTUAL_WORKSPACE}, the virtual workspace is returned instead.
	 * @throws IOException
	 */
	public static WorkspaceConfig loadWorkspace(File workspaceDir) throws IOException
	{
		if(workspaceDir.getName().equals(WorkspaceConfig.VIRTUAL_WORKSPACE))
		{
			return createVirtualWorkspace();
		}
		
		File wsf = getFile(workspaceDir, WorkspaceConfig.class);
		
		WorkspaceConfig wsc;
		try {
			wsc = (WorkspaceConfig) WorkspaceConfig.loadObject(wsf);
			wsc.setLocation(workspaceDir);
			return wsc;
		} catch (ClassNotFoundException e) {
			return null; // this won't happen
		}
	}
	
	/*
	 * User list
	 */
	
	/**
	 * @see #getUserList()
	 */
	private ArrayList<String> userList;
	
	/**
	 * The names of the logical users in the workspace
	 * @return
	 */
	public String[] getUserList()
	{		
		String[] users = new String[userList.size()];
		return userList.toArray(users);
	}
	
	public File getUserDirectroy(String username)
	{
		if (!existsUserLogically(username) || isVirtual())
			return null;
		
		return getDirectory(getLocation(), username);
	}
	
	/**
	 * Create new user directory and UserConfig file in this workspace on the file system, and add it logically to the userList.
	 * If either existed, only the non-existing parts are created.
	 * To create a user in a virtual workspace will have no effect, but an error message is printed.
	 * <p> Has no effect if this is virtual.
	 * @param username
	 */
	public void createUser(String username)
	{
		if (!Storable.checkLegalName(username))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE), 
					Logo.messages.getString(MessageKeys.ILLEGAL_NAME));
			return;
		}
		
		if(isVirtual())
		{
			DialogMessenger.getInstance().dispatchError("Workspace Error", "Attempt to create new user to virtual workspace.");
			return;
		}
		
		File userDir = getDirectory(getLocation(), username);
		
		if(!userDir.mkdirs())
		{
			DialogMessenger.getInstance().dispatchError("Workspace Error","Could not make required directories: " + userDir.toString());
			return;
		}
		
		if (!existsUserLogically(username))
		{
			userList.add(username);
			makeDirty();
		}

		// Make new user logically existent in workspace config file
		try { store(); } catch (IOException e) {
			DialogMessenger.getInstance().dispatchError("Workspace Error","Could not store workspace.");
		}
		if(!existsUserPhysically(username))
			UserConfig.createNewUser(this, username);
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
	public void importUser(File srcUserDir, String destUsername) throws IllegalArgumentException, IOException
	{
		if (isVirtual())
			return;
		
		if (!WSManager.isUserDirectory(srcUserDir))
			throw new IllegalArgumentException();
		
		createUser(destUsername);
		File targetUserDir = getDirectory(getLocation(), destUsername);
		
		WSManager.copyFullyRecursive(srcUserDir, targetUserDir);
	}
	
	/**
	 * @param userName will be removed logically only
	 */
	public void removeUser(String userName)
	{		
		if(existsUserLogically(userName))
			makeDirty();
		
		userList.remove(userName);
	}
	
	/**
	 * @param username - if this exists not logically in the workspace, null is returned.
	 * @return a {@link UserConfig} generated from the file system. If this is a virtual workspace, a virtual user is created instead.
	 * @throws IOException if the UserConfig could not be loaded
	 * @see UserConfig#loadUser(File, String)
	 */
	public UserConfig loadUser(String username) throws IOException
	{
		if(!existsUserLogically(username))
		{
			DialogMessenger.getInstance().dispatchError("Workspace Error","Attempt to load inexistent user: " + username + ". Try to import this user.");
			return null;
		}
		
		if(isVirtual())
			return UserConfig.createVirtualUser();
		
		// exists logically and is not virtual
		
		if(!existsUserPhysically(username))
		{
			// but it does exist logically => it must have been corrupted externally.
			// => restore it.
			if(!getLocation().mkdirs())
			{
				DialogMessenger.getInstance().dispatchError("Workspace Error","Could not make required directories.");
				return null;
			}
			// user creation requires existence of the workspace on file system
			try { store(); } catch (IOException e) {
				DialogMessenger.getInstance().dispatchError("Workspace Error","Could not store workspace.");
			}
			return UserConfig.createNewUser(this, username);
		}
		// exists physically
		return UserConfig.loadUser(this, username);
	}
	
	/**
	 * Produces a list of user names by reading the contents of the current workspace directory.
	 * The users in this list may contain users that have been deleted logically before.
	 * @return
	 */
	public ArrayList<String> getPhysicalUserList()
	{
		if(isVirtual())
			return new ArrayList<String>();

		ArrayList<String> users = new ArrayList<String>();
		
		if (WSManager.isWorkspaceDirectory(getLocation()))
		{
			DialogMessenger.getInstance().dispatchError("Workspace Error", "Current workspace was probably deleted. I will recreate it.");
			try { store(); } catch (IOException e) {
				DialogMessenger.getInstance().dispatchError("Workspace Error", "I could not recreate the Workspace. Try to delete the Workspace and recreate it manually.");
				return users;
			}
		}

		
		for(File dir : WSManager.listDirectories(getLocation()))
		{
			if(WSManager.isUserDirectory(dir))
			{
				users.add(dir.getName());
			}
		}
		return users;
	}
	
	/**
	 * A user exists logically, if its name is known by the workspace.
	 * @param userName
	 * @return
	 */
	public boolean existsUserLogically(String username)
	{
		return userList.contains(username);
	}
	
	/**
	 * A user exists physically, if a folder with the user's name exists in this workspace and if it contains a UserConfig file.
	 * @param username
	 * @return
	 * @see WSManager#isUserDirectory(File)
	 */
	public boolean existsUserPhysically(String username)
	{
		File userDir = getDirectory(getLocation(), username);
		return WSManager.isUserDirectory(userDir);
	}
	
	/*
	 * last active user
	 */
	
	private String lastActiveUser;
	
	/**
	 * @return name of the last active user
	 */
	public String getLastActiveUser()
	{
		return lastActiveUser;
	}
	
	/**
	 * Succeeds if the user exists
	 * @param workspace
	 */
	public void setLastActiveUser(String username)
	{
		if(existsUserLogically(username) && !username.equals(lastActiveUser))
		{
			lastActiveUser = new String(username);
			makeDirty();
		}
	}
		
	/*
	 * active user
	 */
	
	private transient UserConfig activeUser;
	
	public UserConfig getActiveUser()
	{
		return activeUser;
	}
	
	/**
	 * @throws IOException If the old userConfig could not be stored. 
	 */
	public void enterUserSpace(String username) throws IOException
	{
		if(activeUser != null)
		{
			leaveUserSpace();
		}
		
		if (isVirtual())
			activeUser = UserConfig.createVirtualUser();
		else
			activeUser = UserConfig.loadUser(this, username);
	
		setLastActiveUser(username);
	}
	
	/**
	 * @throws IOException If userConfig could not be stored. 
	 */
	public void leaveUserSpace() throws IOException
	{
		if (activeUser.isDirty())
			activeUser.store();
		activeUser = null;
	}	

	/*
	 * Version control
	 */
	
	/**
	 * How many old versions of a file should be kept, in addition to the most recent one?
	 * Default is infinite.
	 */
	private NumberOfBackups numberOfBackups = NumberOfBackups.INFINITE;
	
	/**
	 * @see #numberOfBackups
	 */
	public NumberOfBackups getNumberOfBackups()
	{
		return numberOfBackups;
	}
	
	/**
	 * @see #numberOfBackups
	 */
	public void setNumberOfBackups(NumberOfBackups n)
	{
		numberOfBackups = n;
		makeDirty();
	}
	
	/*
	 * Workspace language
	 */

	/**
	 * The language to be used within this workspace
	 */
	public Language language;
	
	public void setLanguage(Language language)
	{
		this.language = language;
		AppSettings.getInstance().setLanguage(language);
		makeDirty();
	}
	
	public Language getLanguage()
	{
		return language;
	}
	
	/*
	 * Allow users (children) to create new user accounts in workspaces?
	 */
	
	private boolean allowUserCreation = true;
	
	public void setAllowUserCreation(boolean allowed)
	{
		this.allowUserCreation = allowed;
		makeDirty();
	}
	
	public boolean isUserCreationAllowed()
	{
		return allowUserCreation && !isVirtual();
	}

	/*
	 * Contest //TODO create options in workspace settings
	 */
	
	private ContestConfig contestConfig;
	
	protected ContestConfig getContestSettings()
	{
		return contestConfig;
	}
	
	public int getNOfContestFiles()
	{
		if (contestConfig == null)
			contestConfig = new ContestConfig();
		return getContestSettings().getNOfContestFiles();
	}

	public void setNOfContestFiles(int nOfContestFiles)
	{
		getContestSettings().setNOfContestFiles(nOfContestFiles);
	}

	public int getNOfContestBonusFiles()
	{
		return getContestSettings().getNOfContestBonusFiles();
	}

	public void setNOfContestBonusFiles(int nOfContestBonusFiles)
	{
		getContestSettings().setNOfContestBonusFiles(nOfContestBonusFiles);
	}
	
	/*
	 * Syntax Highlighting
	 */
	private SyntaxHighlightConfig syntaxHighlightingStyles; // TODO = new SyntaxHighlightStyles();

	/**
	 * This font is the default font for all menus ... in XLogo Application
	 */
	private Font font;// TODO =new Font("dialog",Font.PLAIN,14);

	public SyntaxHighlightConfig getSyntaxHighlightStyles()
	{
		if (syntaxHighlightingStyles == null)
		{
			syntaxHighlightingStyles = new SyntaxHighlightConfig();
			makeDirty();
		}
		return syntaxHighlightingStyles;
	}
	
	public void setSyntaxHighlightConfig(SyntaxHighlightConfig syntaxHighlightingStyles)
	{
		this.syntaxHighlightingStyles = syntaxHighlightingStyles;
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(syntaxHighlightingStyles);
	}
	
	public int getPrimitiveColor() {
		return getSyntaxHighlightStyles().getPrimitiveColor();
	}


	public void setPrimitiveColor(int primitiveColor) {
		getSyntaxHighlightStyles().setPrimitiveColor(primitiveColor);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getPrimitiveStyle() {
		return getSyntaxHighlightStyles().getPrimitiveStyle();
	}

	public void setPrimitiveStyle(int primitiveStyle) {
		getSyntaxHighlightStyles().setPrimitiveStyle(primitiveStyle);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getOperatorColor() {
		return getSyntaxHighlightStyles().getOperatorColor();
	}

	public void setOperandColor(int operatorColor) {
		getSyntaxHighlightStyles().setOperatorColor(operatorColor);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getOperatorStyle() {
		return getSyntaxHighlightStyles().getOperatorStyle();
	}

	public void setOperandStyle(int operatorStyle) {
		getSyntaxHighlightStyles().setOperatorStyle(operatorStyle);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getCommentColor() {
		return getSyntaxHighlightStyles().getCommentColor();
	}

	public void setCommentColor(int commentColor) {
		getSyntaxHighlightStyles().setCommentColor(commentColor);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getCommentStyle() {
		return getSyntaxHighlightStyles().getCommentStyle();
	}

	public void setCommentStyle(int commentStyle) {
		getSyntaxHighlightStyles().setCommentStyle(commentStyle);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getBraceColor() {
		return getSyntaxHighlightStyles().getBraceColor();
	}

	public void setBraceColor(int braceColor) {
		getSyntaxHighlightStyles().setBraceColor(braceColor);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public int getBraceStyle() {
		return getSyntaxHighlightStyles().getBraceStyle();
	}

	public void setBraceStyle(int braceStyle) {
		getSyntaxHighlightStyles().setBraceStyle(braceStyle);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}

	public boolean isSyntaxHighlightingEnabled() {
		return getSyntaxHighlightStyles().isColorEnabled();
	}

	public void setSyntaxHighlightingEnabled(boolean colorEnabled) {
		getSyntaxHighlightStyles().setColorEnabled(colorEnabled);
		makeDirty();
		AppSettings.getInstance().setSyntaxHighlightingStyles(getSyntaxHighlightStyles());
	}
	
	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		makeDirty();
		AppSettings.getInstance().setFont(font);
	}
	
}
