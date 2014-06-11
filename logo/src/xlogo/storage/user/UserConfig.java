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

package xlogo.storage.user;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import xlogo.Logo;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.StorableObject;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;

/**
 * This Config is user specific. It is stored in the {@link #workspaceLocation} in the user's folder.
 * @author Marko Zivkovic
 */
public class UserConfig extends StorableObject implements Serializable
{
	private static final long serialVersionUID = 8897730869795295485L;
	
	// This was initially in Panel_Font, which is not used anymore.
	public static final Font[] fontes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	
	static public int police_id(Font font)
	{
		for (int i = 0; i < fontes.length; i++)
		{
			if (fontes[i].getFontName().equals(font.getFontName()))
				return i;
		}
		return 0;
	}

	
	/**
	 * Name of the virtual user
	 * @see #isVirtual()
	 * @see #createVirtualUser()
	 */
	public static final String VIRTUAL_USER = "Guest";

	private static final String SRC_DIR_NAME = "src";
	private static final String BACKUPS_DIR_NAME = "backups";
	private static final String CONTEST_DIR_NAME = "contest";
	private static final String COMMAND_LINE_RECORD_NAME = "commandLineHistory";
	
	protected UserConfig()
	{
		super();
	}
		
	/*
	 * Static constructors
	 */
	
	/**
	 * A virtual user can enter the application in a virtual workspace without having an actual user account on the file system. Hence nothing will be stored.
	 * A regular user (not virtual) will have his own folder in a regular workspace on the file system and all his preferences and files are stored there.
	 * To create a regular user, use {@link #createNewUser(File, String)},
	 * to load a regular user from the file system, user {@link #loadUser(File, String)}}.
	 * @see #isVirtual()
	 * @return a virtual workspace
	 */
	public static UserConfig createVirtualUser()
	{
		UserConfig vuc = new UserConfig();
		vuc.makeVirtual();
		return vuc;
	}
	
	/**
	 * Loads the UserConfig from the specified user, or creates it, if it's missing or corrupted.
	 * If workspace specifies a virtual workspace, a virtual user is created instead. In that case the error manager is informed.
	 * @param workspace
	 * @param username
	 * @return the loaded UserConfig
	 * @throws IOException 
	 */
	public static UserConfig loadUser(WorkspaceConfig workspace, String username)
	{
		if(workspace.isVirtual())
			return createVirtualUser();
		
		if (!WSManager.isWorkspaceDirectory(workspace.getLocation()))
			throw new IllegalArgumentException(workspace.toString() + " is not a valid Workspace.");
		
		File userDirectory = getDirectory(workspace.getLocation(), username);
		File userConfigFile = getFile(userDirectory, UserConfig.class);
		
		UserConfig userConfig = null;
		
		if(userConfigFile.exists())
		{
			try
			{
				userConfig = (UserConfig) loadObject(userConfigFile);
				userConfig.setLocation(userDirectory);
			}catch(ClassNotFoundException e1) { } // this won't happen
			catch(IOException e2) {
				DialogMessenger.getInstance().dispatchError("Workspace Error", "Could not load user config file: " + e2.toString());
			}
		}
		
		// If file is missing, it will be created again.
		if (userConfig == null)
			userConfig = createNewUser(workspace, username);
		
		return userConfig;
	}
	
	/**
	 * If workspace is valid, then a new userSpace is created within that workspace.
	 * All necessary files and directories are created.
	 * <p>
	 * If a UserConfig file for the specified user already exists, it will be overwritten.
	 * If an error occurs while creating the file, the userConfig is just returned without saving to file.
	 * TODO In that case, the global ErrorManager is informed.
	 * @param workspace - must exist physically
	 * @param username
	 * @return the created UserConfig - or null, if one of the arguments was not legal
	 */
	public static UserConfig createNewUser(WorkspaceConfig workspace, String username)
	{
		if(workspace.isVirtual())
			return createVirtualUser();
		
		if (!WSManager.isWorkspaceDirectory(workspace.getLocation()))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.WS_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.WS_DOES_NOT_EXIST));
			return null;
		}
		
		if(!Storable.checkLegalName(username))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.ILLEGAL_NAME));
			return null;
		}
		
		File userDirectory = getDirectory(workspace.getLocation(), username);
		
		if(!userDirectory.exists())
		{
			userDirectory.mkdirs();
		}
			
		// userDirectory exists

		//File userFile = getFile(userDirectory, UserConfig.class);
		UserConfig userConfig = new UserConfig();
		try {
			userConfig.setLocation(userDirectory);
			userConfig.store();
		} catch (IOException e) {
			// Best effort. Continue without saving to disk.
			System.out.print("Could not create UserConfig file: " + e.toString());
		}
		
		return userConfig;
	}
	
	public String getUserName()
	{
		if (isVirtual())
			return VIRTUAL_USER;
		String result = null;
		if(getLocation() != null)
			result = getLocation().getName();
		return result;
	}
	
	/*
	 * File List
	 */
	
	/**
	 * @return ../WorkspaceDir/UserDir/src/ or null if {@link #isVirtual()}
	 */
	public File getSourceDirectory()
	{
		if (isVirtual())
			return null;
		return new File(getLocation().toString() + File.separator + SRC_DIR_NAME);
	}
	
	/**
	 * @param fileName - without extension
	 * @return
	 */
	public File getLogoFilePath(String fileName)
	{
		if(isVirtual())
			return null;
		
		String path = getSourceDirectory().toString()
				+ File.separator + fileName 
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}

	/**
	 * @return ../WorkspaceDir/UserDir/backups/ or null if {@link #isVirtual()}
	 */
	public File getBackupDirectory()
	{
		if (isVirtual())
			return null;
		return new File(getLocation().toString() + File.separator + BACKUPS_DIR_NAME);
	}
	
	/**
	 * @param fileName - without extension
	 * @return
	 */
	public File getFileBackupDir(String fileName)
	{
		if (isVirtual())
			return null;
		String path = getBackupDirectory().toString()
				+ File.separator + fileName;
		return new File(path);
	}
	
	public File getContestDir()
	{
		if (isVirtual())
			return null;
		return new File(getLocation().toString() + File.separator + CONTEST_DIR_NAME);
	}
	
	public File getContestFilePath(String fileName)
	{
		if (isVirtual())
			return null;
		String path = getContestDir().toString()
				+ File.separator + SRC_DIR_NAME
				+ File.separator + fileName
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		return new File(path);
	}
	
	public File getContestFileDir(String fileName)
	{
		if (isVirtual())
			return null;
		String path = getContestDir().toString()
				+ File.separator + fileName;
		return new File(path);
	}
	
	/**
	 * @param fileName
	 * @return File descriptor for a backup file with the current timestamp
	 */
	public File getBackupFilePath(String fileName)
	{
		String path = getFileBackupDir(fileName)
				+ File.separator + getTimeStamp()
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}
	
	/**
	 * @param fileName
	 * @return File descriptor for a contest/record file with the current timestamp
	 */
	public File getRecordFilePath(String fileName)
	{
		String path = getContestFileDir(fileName)
				+ File.separator + getTimeStamp()
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}
	
	public File getCommandLineContestFile()
	{
		if (isVirtual())
			return null;
		String path = getContestDir().toString()
				+ File.separator + COMMAND_LINE_RECORD_NAME
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		return new File(path);
	}
	
	public static final String dateTimeFormat = "yyyy-MM-dd-HH-mm-ss";
	public static final String timeFormat = "HH:mm:ss";
	public static final String minSecFormat = "mm:ss";
	
	public static String getTimeStamp()
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(dateTimeFormat);		
		Calendar cal = Calendar.getInstance();
		return sdf.format(cal.getTime());	
	}
	
	/**
	 * @param millis
	 * @return yyyy-MM-dd-HH-mm-ss
	 */
	public static String getDateTimeString(long millis)
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(dateTimeFormat);
		return sdf.format(new Date(millis));
	}
	
	/**
	 * @param millis
	 * @return HH:mm:ss
	 */
	public static String getTimeString(long millis)
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(timeFormat);
		return sdf.format(new Date(millis));
	}
	
	public static String getMinSec(long millis)
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(minSecFormat);
		return sdf.format(new Date(millis));
	}
		
	/**
	 * The list of files UserSpace in the workspace.
	 */
	private ArrayList<String> fileOrder = new ArrayList<String>();
	
	public void addFile(String fileName)
	{
		if (!fileOrder.contains(fileName))
			fileOrder.add(fileName);
	}
	
	public void removeFile(String fileName)
	{
		fileOrder.remove(fileName);
	}
	
	public void renameFile(String oldName, String newName)
	{
		int i = fileOrder.indexOf(oldName);
		if (i < 0)
			return;
		fileOrder.remove(i);
		fileOrder.add(i, newName);
	}
	
	public ArrayList<String> getFileOrder()
	{
		return fileOrder;
	}

	// The below properties are essentially copied from the old XLogo
	// they might get (re)moved or changed

	final String version="0.9.96pre 27/06/12"; // TODO not needed? new version?

	/**
	 * Drawing Quality
	 */
	private DrawQuality quality= DrawQuality.HIGH;

	/** 
	 * This integer represents the selected looknfeel for the appplication
	 */
	private LookAndFeel looknfeel=LookAndFeel.JAVA;

	/**
	 * This integer represents the drawing area width
	 */
	private int imageWidth= Math.max(1000, Toolkit.getDefaultToolkit().getScreenSize().width);
	/**
	 * This integer represents the drawing area height
	 */
	private int imageHeight= Math.max(1000, Toolkit.getDefaultToolkit().getScreenSize().height);
	/**
	 * Integer that represents the active turtle's shape
	 */
	private int activeTurtle=0;
	/**
	 * Maximum allowed pen size 
	 */
	private int maxPenWidth=-1;
	/**
	 * This boolean indicates if the drawing area has to be cleaned when the editor is left.
	 */
	private boolean eraseImage = false;
	/**
	 * This boolean indicates if variables are deleted when closing the editor.
	 */
	private boolean clearVariables = false; 
	/**
	 * Max value for the turtles number 
	 */
	private int maxTurtles  = 16;
	/**
	 * Default screen color: This color is used when the primitive "clearscreen" is used.
	 */
	private Color screencolor=Color.WHITE;
	/**
	 * Default pen color: This color is used when the primitive "clearscreen" is used.
	 */
	private Color pencolor=Color.BLACK;


	/**
	 * This represents the pen shape
	 */
	private PenShape penShape = PenShape.SQUARE;

	/**
	 * This integer represents the turtle's speed for drawing <br>
	 * Slow: 100
	 * Fast: 0
	 */
	private int turtleSpeed=0;
	/**
	 * This String contains the command to execute on Startup. <br>
	 * Configured in the dialog box "startup files"
	 */	
	private String a_executer="";
	/** Marko Zivkovic : this is used for a few Logo Commands that operate with files <br>
	 * Loic:
	 * The default folder for the user when the application starts.<br>
	 * This folder corresponds to the last opened or saved file in format lgo // Marko : not true anymore
	 */
	private String defaultFolder=Utils.rajoute_backslash(System.getProperty("user.home"));
	
	/**
	 * This boolean indicates if the grid is enabled
	 */
	private boolean drawGrid=false;
	/**
	 * This integer represents the X distance for the grid
	 */
	private int XGrid=20;
	/**
	 * This integer represents the Y distance for the grid
	 */
	private int YGrid=20;
	/**
	 * This integer represents the grid Color
	 */
	private int gridColor=Color.DARK_GRAY.getRGB();
	/**
	 * This boolean indicates if the X axis is enabled
	 */
	private boolean drawXAxis=false;
	/**
	 * This boolean indicates if the Y axis is enabled
	 */
	private boolean drawYAxis=false;
	/**
	 * This integer represents the axis Color
	 */
	private int axisColor=new Color(255,0,102).getRGB();
	/**
	 * This integer represents the X distance between two divisions on the X Axis
	 */
	private int XAxis=30;
	/**
	 * This integer represents the X distance between two divisions on the Y Axis
	 */
	private int YAxis=30;

	/** 
	 * This long represents the hour of XLogo starting
	 */
	private long heure_demarrage;
	/**
	 * Color for the border around drawing area
	 */
	private Color borderColor=null;
	/** 
	 * The Image is for the border around drawing area 
	 */
	private String borderImageSelected="background.png";

	/** 
	 * This Vector contains all images added by the user for image Border
	 */
	private ArrayList<String> borderExternalImage=new ArrayList<String>();
	/** 
	 * The default image defined by default that are included in XLogo
	 */
	private String[] borderInternalImage={"background.png"};
	/** 
	 * This String represents the main command accessible with the button play in the toolbar
	 */
	private String mainCommand="";
	/**
	 * This boolean indicates if Xlogo must launch the main Command on XLogo startup
	 * It overrides the String a_executer
	 */
	private boolean autoLaunch=false;


	/**
	 * TCP Port for robotics and network flows
	 */
	private int tcpPort = 1948;
	
	public int searchInternalImage(String st) {
		for (int i = 0; i < borderInternalImage.length; i++) {
			if (st.equals(borderInternalImage[i]))
				return i;
		}
		return -1;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * Generated Getters & Setters
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public DrawQuality getQuality() {
		return quality;
	}


	public void setQuality(DrawQuality quality) {
		this.quality = quality;
		makeDirty();
	}

	public LookAndFeel getLooknfeel() {
		return looknfeel;
	}

	public void setLooknfeel(LookAndFeel looknfeel) {
		this.looknfeel = looknfeel;
		makeDirty();
	}


	public int getImageWidth() {
		return imageWidth;
	}


	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
		makeDirty();
	}


	public int getImageHeight() {
		return imageHeight;
	}


	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
		makeDirty();
	}

	public int getActiveTurtle() {
		return activeTurtle;
	}


	public void setActiveTurtle(int activeTurtle) {
		this.activeTurtle = activeTurtle;
		makeDirty();
	}


	public int getMaxPenWidth() {
		return maxPenWidth;
	}


	public void setMaxPenWidth(int maxPenWidth) {
		this.maxPenWidth = maxPenWidth;
		makeDirty();
	}


	public boolean isEraseImage() {
		return eraseImage;
	}


	public void setEraseImage(boolean eraseImage) {
		this.eraseImage = eraseImage;
		makeDirty();
	}


	public boolean isClearVariables() {
		return clearVariables;
	}


	public void setClearVariables(boolean clearVariables) {
		this.clearVariables = clearVariables;
		makeDirty();
	}


	public int getMaxTurtles() {
		return maxTurtles;
	}


	public void setMaxTurtles(int maxTurtles) {
		this.maxTurtles = maxTurtles;
		makeDirty();
	}


	public Color getScreencolor() {
		return screencolor;
	}


	public void setScreencolor(Color screencolor) {
		this.screencolor = screencolor;
		makeDirty();
	}


	public Color getPencolor() {
		return pencolor;
	}


	public void setPencolor(Color pencolor) {
		this.pencolor = pencolor;
		makeDirty();
	}


	public PenShape getPenShape() {
		return penShape;
	}


	public void setPenShape(PenShape penShape) {
		this.penShape = penShape;
		makeDirty();
	}


	public int getTurtleSpeed() {
		return turtleSpeed;
	}


	public void setTurtleSpeed(int turtleSpeed) {
		this.turtleSpeed = turtleSpeed;
		makeDirty();
	}


	public String getA_executer() {
		return a_executer;
	}


	public void setA_executer(String a_executer) {
		this.a_executer = a_executer;
		makeDirty();
	}

	/**
	 * Default : User source directory
	 * @return the current defaultDirectory
	 * @author Marko Zivkovic
	 */
	public String getDefaultFolder() {
		if (defaultFolder == null)
			return getSourceDirectory().toString();
		return defaultFolder;
	}

	
	public void setDefaultFolder(String defaultFolder) {
		this.defaultFolder = defaultFolder;
		makeDirty();
	}

	


	public boolean isDrawGrid() {
		return drawGrid;
	}


	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
		makeDirty();
	}


	public int getXGrid() {
		return XGrid;
	}


	public void setXGrid(int xGrid) {
		XGrid = xGrid;
		makeDirty();
	}


	public int getYGrid() {
		return YGrid;
	}


	public void setYGrid(int yGrid) {
		YGrid = yGrid;
		makeDirty();
	}


	public int getGridColor() {
		return gridColor;
	}


	public void setGridColor(int gridColor) {
		this.gridColor = gridColor;
		makeDirty();
	}


	public boolean isDrawXAxis() {
		return drawXAxis;
	}


	public void setDrawXAxis(boolean drawXAxis) {
		this.drawXAxis = drawXAxis;
		makeDirty();
	}


	public boolean isDrawYAxis() {
		return drawYAxis;
	}


	public void setDrawYAxis(boolean drawYAxis) {
		this.drawYAxis = drawYAxis;
		makeDirty();
	}


	public int getAxisColor() {
		return axisColor;
	}


	public void setAxisColor(int axisColor) {
		this.axisColor = axisColor;
		makeDirty();
	}


	public int getXAxis() {
		return XAxis;
	}


	public void setXAxis(int xAxis) {
		XAxis = xAxis;
		makeDirty();
	}


	public int getYAxis() {
		return YAxis;
	}


	public void setYAxis(int yAxis) {
		YAxis = yAxis;
		makeDirty();
	}


	public long getHeure_demarrage() {
		return heure_demarrage;
	}


	public void setHeure_demarrage(long heure_demarrage) {
		this.heure_demarrage = heure_demarrage;
		makeDirty();
	}

	
	public Color getBorderColor() {
		return borderColor;
	}


	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		makeDirty();
	}


	public String getBorderImageSelected() {
		return borderImageSelected;
	}


	public void setBorderImageSelected(String borderImageSelected) {
		this.borderImageSelected = borderImageSelected;
		makeDirty();
	}


	public ArrayList<String> getBorderExternalImage() {
		return borderExternalImage;
	}


	public void setBorderExternalImage(ArrayList<String> borderExternalImage) {
		this.borderExternalImage = borderExternalImage;
		makeDirty();
	}


	public String[] getBorderInternalImage() {
		return borderInternalImage;
	}


	public void setBorderInternalImage(String[] borderInternalImage) {
		this.borderInternalImage = borderInternalImage;
		makeDirty();
	}


	public String getMainCommand() {
		return mainCommand;
	}


	public void setMainCommand(String mainCommand) {
		this.mainCommand = mainCommand;
		makeDirty();
	}


	public boolean isAutoLaunch() {
		return autoLaunch;
	}


	public void setAutoLaunch(boolean autoLaunch) {
		this.autoLaunch = autoLaunch;
		makeDirty();
	}

	public int getTcpPort() {
		return tcpPort;
	}


	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
		makeDirty();
	}
	
}
