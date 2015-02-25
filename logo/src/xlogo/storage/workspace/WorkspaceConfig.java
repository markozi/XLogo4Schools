/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * 
 * Contact Information: marko88zivkovic at gmail dot com
 * 
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

package xlogo.storage.workspace;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.interfaces.Observable;
import xlogo.interfaces.PropertyChangePublisher;
import xlogo.storage.StorableObject;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.UserConfig;
import xlogo.storage.user.UserConfig.UserProperty;

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
public class WorkspaceConfig implements Serializable, Observable<WorkspaceConfig.WorkspaceProperty> {
	
	private static final long	serialVersionUID		= -3554871695113998509L;
	
	/*
	 * Constants
	 */
	
	public static final String	USB_DEFAULT_WORKSPACE	= "XLogo4Schools";
	public static final String	USER_DEFAULT_WORKSPACE	= "XLogo4Schools-Workspace";
	public static final int			MAX_EMPTY_FILES		= 4;
	
	public static final File DEFAULT_LOCATION = GlobalConfig.DEFAULT_LOCATION;
	public static final File DEFAULT_DIRECTORY = new File(DEFAULT_LOCATION + File.separator + USER_DEFAULT_WORKSPACE);
	
	public static final Font DEFAULT_FONT = new Font("dialog", Font.PLAIN, 14);
	
	private static Logger		logger					= LogManager.getLogger(WorkspaceConfig.class.getSimpleName());
	
	/*
	 * Transient Fields
	 */
	
	private transient File wsDir;
	
	public File getDirectory() {
		return wsDir;
	}

	public void setDirectory(File wsDir) {
		this.wsDir = wsDir;
	}
	
	public String getWorkspaceName(){
		File wsDir = getDirectory();
		if (wsDir == null)
			throw new IllegalStateException("Name is not available because location is null.");
		return wsDir.getName();
	}
	
	/*
	 * Persistent Fields
	 */
	
	private ArrayList<String>		userList			= new ArrayList<String>();
	private String					lastActiveUser;
	private NumberOfBackups			numberOfBackups		= NumberOfBackups.INFINITE;
	private Language				language;
	private boolean					allowUserCreation	= true;
	private ContestConfig			contestSettings; // Contest //TODO create options in workspace settings
	private SyntaxHighlightConfig	syntaxHighlightingStyles;	// TODO = new SyntaxHighlightStyles();
	private Font					font; // This font is the default font for all menus ... in XLogo Application
					
	/*
	 * Constructor
	 */
	
	public WorkspaceConfig() {
		super();
		userList = new ArrayList<String>();
	}
	
		
	public File getUserDirectroy(String username) {
		if (!existsUserLogically(username))
			return null;
		
		return StorableObject.getDirectory(getDirectory(), username);
	}
			
	/**
	 * Produces a list of user names by reading the contents of the current workspace directory.
	 * The users in this list may contain users that have been deleted logically before.
	 * @return
	 */
	public ArrayList<String> getPhysicalUserList() {
		
		ArrayList<String> users = new ArrayList<String>();
		
		for (File dir : WSManager.listDirectories(getDirectory())) {
			if (WSManager.isUserDirectory(dir)) {
				users.add(dir.getName());
			}
		}
		return users;
	}
		
	/*
	 * active user
	 */
	
	private transient StorableObject<UserConfig, UserProperty>	activeUser;
	
	public StorableObject<UserConfig, UserProperty> getActiveUser() {
		return activeUser;
	}
	
	public void enterInitialUserSpace() throws IOException{
		String user = getLastActiveUser();
		if (user != null && userList.contains(user)){
			enterUserSpace(user);
		}
	}
	
	/**
	 * @throws IOException If the old userConfig could not be stored. 
	 */
	public void enterUserSpace(String username) throws IOException {
		enterUserSpace(retrieveUserSpace(username));
	}
	
	public void enterUserSpace(StorableObject<UserConfig, UserProperty> userConfig) throws IOException {
		if(userConfig == activeUser){
			return;
		}
		
		if (activeUser != null) {
			leaveUserSpace();
		}
		logger.trace("Entering user space: " + userConfig.get().getUserName());
		
		activeUser = userConfig;
		setLastActiveUser(userConfig.get().getUserName());
	}
	
	/**
	 * @throws IOException If userConfig could not be stored. 
	 */
	public void leaveUserSpace() throws IOException {
		logger.trace("Leaving user space: " + activeUser.get().getUserName());
		if (activeUser.isDirty())
			activeUser.store();
		activeUser = null;
	}
	
	
	protected StorableObject<UserConfig, UserProperty> retrieveUserSpace(String username){
		StorableObject<UserConfig, UserProperty> uc = getCachedUserSpace(username);
		if (uc == null && getDirectory() != null){
			uc = WSManager.getUser(getDirectory(), username);
			cacheUserSpace(username, uc);
		}
		
		return uc;
	}
	
	/**
	 * User Cache
	 * 
	 * UserConfigs that have already been created or loaded from disk.
	 */
	private transient Map<String, StorableObject<UserConfig, UserProperty>> cachedUserSpaces;
	
	private StorableObject<UserConfig, UserProperty> getCachedUserSpace(String username) {
		if (cachedUserSpaces == null){
			cachedUserSpaces= new TreeMap<String, StorableObject<UserConfig, UserProperty>>();
		}
		return cachedUserSpaces.get(username);
	}
	
	private void cacheUserSpace(String username, StorableObject<UserConfig, UserProperty> wsc){
		if (cachedUserSpaces == null){
			cachedUserSpaces= new TreeMap<String, StorableObject<UserConfig, UserProperty>>();
		}
		cachedUserSpaces.put(username, wsc);
	}
	
	/*
	 * User list
	 */

	public void addUser(String username) {
		if (userList.contains(username)){
			logger.warn("Adding user name that is already present in user list. Ignore adding.");
			return;
		}
		userList.add(username);
		publisher.publishEvent(WorkspaceProperty.USER_LIST);
	}
	
	public void addUser(StorableObject<UserConfig, UserConfig.UserProperty> uc){
		String name = uc.get().getUserName();
		cacheUserSpace(name, uc);
		addUser(name);
	}
	
	/**
	 * @param userName will be removed logically only
	 * @throws IOException 
	 */
	public void removeUser(String username){
		logger.trace("Removing user: " + username);
		if (existsUserLogically(username)){
			userList.remove(username);
			cachedUserSpaces.remove(username);
			publisher.publishEvent(WorkspaceProperty.USER_LIST);
		} else {
			userList.remove(username);
			cachedUserSpaces.remove(username);
		}
		
		if (activeUser != null && activeUser.get().getUserName().equals(username)){
			activeUser = null;
			setLastActiveUser(null);
		}
	}
	
	/*
	 * last active user
	 */

	/**
	 * @return name of the last active user
	 */
	public String getLastActiveUser() {
		if (lastActiveUser == null) {
			if (userList.size() > 0) {
				lastActiveUser = userList.get(0);
			}
		}
		return lastActiveUser;
	}
	
	/**
	 * Succeeds if the user exists
	 * @param workspace
	 */
	public void setLastActiveUser(String username) {
		if (existsUserLogically(username) && !username.equals(getLastActiveUser())) {
			lastActiveUser = new String(username);
			publisher.publishEvent(WorkspaceProperty.LAST_ACTIVE_USER);
		}
	}
	
	/**
	 * A user exists logically, if its name is known by the workspace.
	 * @param userName
	 * @return
	 */
	public boolean existsUserLogically(String username) {
		return userList.contains(username);
	}
	
	public String[] getUserList() {
		String[] users = new String[userList.size()];
		return userList.toArray(users);
	}
	
	/*
	 * Version control
	 */

	public NumberOfBackups getNumberOfBackups() {
		return numberOfBackups;
	}

	public void setNumberOfBackups(NumberOfBackups n) {
		if (this.numberOfBackups == n) { return; }
		numberOfBackups = n;
		publisher.publishEvent(WorkspaceProperty.N_OF_BACKUPS);
	}
	
	/*
	 * Workspace language
	 */

	public void setLanguage(Language language) {
		if (this.language == language) { return; }
		this.language = language;
		publisher.publishEvent(WorkspaceProperty.LANGUAGE);
	}

	public Language getLanguage() {
		if (language == null)
			return Language.LANGUAGE_ENGLISH;
		return language;
	}
	
	/*
	 * Allow users (children) to create new user accounts in workspaces?
	 */
	public void setUserCreationAllowed(boolean allowed) {
		if (this.allowUserCreation == allowed) { return; }
		this.allowUserCreation = allowed;
		publisher.publishEvent(WorkspaceProperty.ALLOW_USER_CREATION);
	}

	public boolean isUserCreationAllowed() {
		return allowUserCreation;
	}
	
	public ContestConfig getContestSettings() {
		if (contestSettings == null)
			contestSettings = new ContestConfig();
		return contestSettings;
	}
	
	public void setContestSettings(ContestConfig contestConfig) {
		if (this.contestSettings == contestConfig) { return; }
		this.contestSettings = contestConfig;
		publisher.publishEvent(WorkspaceProperty.CONTEST);
	}

	public int getNOfContestFiles() {
		return getContestSettings().getNOfContestFiles();
	}

	public void setNOfContestFiles(int nOfContestFiles) {
		if (this.getNOfContestFiles() == nOfContestFiles) { return; }
		getContestSettings().setNOfContestFiles(nOfContestFiles);
		publisher.publishEvent(WorkspaceProperty.CONTEST);
	}

	public int getNOfContestBonusFiles() {
		return getContestSettings().getNOfContestBonusFiles();
	}

	public void setNOfContestBonusFiles(int nOfContestBonusFiles) {
		if (this.getNOfContestBonusFiles() == nOfContestBonusFiles) { return; }
		getContestSettings().setNOfContestBonusFiles(nOfContestBonusFiles);
		publisher.publishEvent(WorkspaceProperty.CONTEST);
	}
	
	public int getMaxEmptyFiles() {
		return MAX_EMPTY_FILES;
	}
	
	public SyntaxHighlightConfig getSyntaxHighlightStyles() {
		if (syntaxHighlightingStyles == null) {
			syntaxHighlightingStyles = new SyntaxHighlightConfig();
		}
		return syntaxHighlightingStyles;
	}
	
	public void setSyntaxHighlightConfig(SyntaxHighlightConfig syntaxHighlightingStyles) {
		if (this.syntaxHighlightingStyles == syntaxHighlightingStyles) { return; }
		this.syntaxHighlightingStyles = syntaxHighlightingStyles;
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getPrimitiveColor() {
		return getSyntaxHighlightStyles().getPrimitiveColor();
	}
	
	public void setPrimitiveColor(int primitiveColor) {
		if (this.getPrimitiveColor() == primitiveColor) { return; }
		getSyntaxHighlightStyles().setPrimitiveColor(primitiveColor);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getPrimitiveStyle() {
		return getSyntaxHighlightStyles().getPrimitiveStyle();
	}
	
	public void setPrimitiveStyle(int primitiveStyle) {
		if (this.getPrimitiveStyle() == primitiveStyle) { return; }
		getSyntaxHighlightStyles().setPrimitiveStyle(primitiveStyle);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getOperandColor() {
		return getSyntaxHighlightStyles().getOperandColor();
	}
	
	public void setOperandColor(int operandColor) {
		if (this.getOperandColor() == operandColor) { return; }
		getSyntaxHighlightStyles().setOperandColor(operandColor);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getOperandStyle() {
		return getSyntaxHighlightStyles().getOperandStyle();
	}
	
	public void setOperandStyle(int operandStyle) {
		if (this.getOperandStyle() == operandStyle) { return; }
		getSyntaxHighlightStyles().setOperandStyle(operandStyle);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getCommentColor() {
		return getSyntaxHighlightStyles().getCommentColor();
	}
	
	public void setCommentColor(int commentColor) {
		if (this.getCommentColor() == commentColor) { return; }
		getSyntaxHighlightStyles().setCommentColor(commentColor);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getCommentStyle() {
		return getSyntaxHighlightStyles().getCommentStyle();
	}
	
	public void setCommentStyle(int commentStyle) {
		if (this.getCommentStyle() == commentStyle) { return; }
		getSyntaxHighlightStyles().setCommentStyle(commentStyle);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getBraceColor() {
		return getSyntaxHighlightStyles().getBraceColor();
	}
	
	public void setBraceColor(int braceColor) {
		if (this.getBraceColor() == braceColor) { return; }
		getSyntaxHighlightStyles().setBraceColor(braceColor);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public int getBraceStyle() {
		return getSyntaxHighlightStyles().getBraceStyle();
	}
	
	public void setBraceStyle(int braceStyle) {
		if (this.getBraceStyle() == braceStyle) { return; }
		getSyntaxHighlightStyles().setBraceStyle(braceStyle);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public boolean isSyntaxHighlightingEnabled() {
		return getSyntaxHighlightStyles().isColorEnabled();
	}
	
	public void setSyntaxHighlightingEnabled(boolean colorEnabled) {
		if (this.isSyntaxHighlightingEnabled() == colorEnabled) { return; }
		getSyntaxHighlightStyles().setColorEnabled(colorEnabled);
		publisher.publishEvent(WorkspaceProperty.SYNTAX_HIGHLIGHTING);
	}
	
	public Font getFont() {
		if (font == null) {
			font = DEFAULT_FONT;
		}
		return font;
	}
	
	public void setFont(Font font) {
		if (this.font == font) { return; }
		this.font = font;
		publisher.publishEvent(WorkspaceProperty.FONT);
	}


	
	/*
	 * Property Change Listeners
	 */
	
	public enum WorkspaceProperty {
		FONT, SYNTAX_HIGHLIGHTING, ALLOW_USER_CREATION, LANGUAGE, N_OF_BACKUPS, LAST_ACTIVE_USER, USER_LIST, CONTEST;
	}

	private transient PropertyChangePublisher<WorkspaceProperty> publisher = new PropertyChangePublisher<>();
	
	@Override
	public void addPropertyChangeListener(WorkspaceProperty property, PropertyChangeListener listener) {
		if (publisher == null){
			publisher = new PropertyChangePublisher<>();
		}
		publisher.addPropertyChangeListener(property, listener);
	}
	
	@Override
	public void removePropertyChangeListener(WorkspaceProperty property, PropertyChangeListener listener) {
		publisher.removePropertyChangeListener(property, listener);
	}

}