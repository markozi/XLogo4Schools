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

package xlogo.storage.user;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import xlogo.interfaces.Observable;
import xlogo.interfaces.PropertyChangePublisher;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;

/**
 * This Config is user specific. It is stored in the {@link #workspaceLocation} in the user's folder.
 * @author Marko Zivkovic
 */
public class UserConfig implements Serializable, Observable<UserConfig.UserProperty> {
	
	private static final long	serialVersionUID	= 8897730869795295485L;
	
	/*
	 * Available Fonts
	 * TODO move them to a more meaningful place
	 */
	
	// This was initially in Panel_Font, which is not used anymore.
	public static final Font[]	fontes				= GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	
	public static int police_id(Font font) { // TODO linear search ... can be hashed
		for (int i = 0; i < fontes.length; i++) {
			if (fontes[i].getFontName().equals(font.getFontName()))
				return i;
		}
		return 0;
	}
	
	
	/*
	 * Constants
	 */
	
	/**
	 * Name of the virtual user
	 * @see #isVirtual()
	 * @see #createVirtualUser()
	 */
	public static final String	DEFAULT_USER				= "Default User";
	public static final File DEFAULT_LOCATION = WorkspaceConfig.DEFAULT_DIRECTORY;
	public static final File DEFAULT_DIRECTORY = new File(DEFAULT_LOCATION + File.separator + DEFAULT_USER);
	
	private static final String	SRC_DIR_NAME				= "src";
	private static final String	BACKUPS_DIR_NAME			= "backups";
	private static final String	CONTEST_DIR_NAME			= "contest";
	private static final String	COMMAND_LINE_RECORD_NAME	= "commandLineHistory";
	
	
	/*
	 * Transient Fields
	 */
	
	private transient File	userDir;
	
	public void setDirectory(File location) {
		this.userDir = location;
	}
	
	public File getDirectory() {
		return userDir;
	}
	
	/**
	 * The user's name is the name of the folder, where the user data is stored
	 * @return
	 */
	public String getUserName() {
		String result = null;
		if (getDirectory() != null)
			result = getDirectory().getName();
		return result;
	}
	
	/*
	 * File List
	 */
	
	/**
	 * @return ../WorkspaceDir/UserDir/src/ or null if {@link #isVirtual()}
	 */
	public File getSourceDirectory() {
		return new File(getDirectory().toString() + File.separator + SRC_DIR_NAME);
	}
	
	/**
	 * @param fileName - without extension
	 * @return
	 */
	public File getLogoFilePath(String fileName) {
		String path = getSourceDirectory().toString() + File.separator + fileName + GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}
	
	/**
	 * @return ../WorkspaceDir/UserDir/backups/ or null if {@link #isVirtual()}
	 */
	public File getBackupDirectory() {
		return new File(getDirectory().toString() + File.separator + BACKUPS_DIR_NAME);
	}
	
	/**
	 * @param fileName - without extension
	 * @return
	 */
	public File getFileBackupDir(String fileName) {
		String path = getBackupDirectory().toString() + File.separator + fileName;
		return new File(path);
	}
	
	public File getContestDir() {
		return new File(getDirectory().toString() + File.separator + CONTEST_DIR_NAME);
	}
	
	public File getContestFilePath(String fileName) {
		String path = getContestDir().toString() + File.separator + SRC_DIR_NAME + File.separator + fileName
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		return new File(path);
	}
	
	public File getContestFileDir(String fileName) {
		String path = getContestDir().toString() + File.separator + fileName;
		return new File(path);
	}
	
	/**
	 * @param fileName
	 * @return File descriptor for a backup file with the current timestamp
	 */
	public File getBackupFilePath(String fileName) {
		String path = getFileBackupDir(fileName) + File.separator + Utils.getTimeStamp()
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}
	
	/**
	 * @param fileName
	 * @return File descriptor for a contest/record file with the current timestamp
	 */
	public File getRecordFilePath(String fileName) {
		String path = getContestFileDir(fileName) + File.separator + Utils.getTimeStamp()
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		
		return new File(path);
	}
	
	public File getCommandLineContestFile() {
		String path = getContestDir().toString() + File.separator + COMMAND_LINE_RECORD_NAME
				+ GlobalConfig.LOGO_FILE_EXTENSION;
		return new File(path);
	}
	
	/**
	 * The list of files in the UserSpace.
	 */
	private ArrayList<String>	fileOrder	= new ArrayList<String>();
	
	public void addFile(String fileName) {
		if (!fileOrder.contains(fileName))
			fileOrder.add(fileName);
	}
	
	public void removeFile(String fileName) {
		fileOrder.remove(fileName);
	}
	
	public void renameFile(String oldName, String newName) {
		int i = fileOrder.indexOf(oldName);
		if (i < 0)
			return;
		fileOrder.remove(i);
		fileOrder.add(i, newName);
	}
	
	public ArrayList<String> getFileOrder() {
		return fileOrder;
	}
	
	// The below properties are essentially copied from the old XLogo
	// they might get (re)moved or changed
	
	/**
	 * Drawing Quality
	 */
	private DrawQuality			quality				= DrawQuality.HIGH;
	
	/** 
	 * This integer represents the selected looknfeel for the appplication
	 */
	private LookAndFeel			looknfeel			= LookAndFeel.JAVA;
	
	/**
	 * This integer represents the drawing area width
	 */
	private int					imageWidth			= Math.max(1000, Toolkit.getDefaultToolkit().getScreenSize().width);
	/**
	 * This integer represents the drawing area height
	 */
	private int					imageHeight			= Math.max(1000, Toolkit.getDefaultToolkit().getScreenSize().height);
	/**
	 * Integer that represents the active turtle's shape
	 */
	private int					activeTurtle		= 0;
	/**
	 * Maximum allowed pen size 
	 */
	private int					maxPenWidth			= -1;
	/**
	 * This boolean indicates if the drawing area has to be cleaned when the editor is left.
	 */
	private boolean				eraseImage			= false;
	/**
	 * This boolean indicates if variables are deleted when closing the editor.
	 */
	private boolean				clearVariables		= false;
	/**
	 * Max value for the turtles number 
	 */
	private int					maxTurtles			= 16;
	/**
	 * Default screen color: This color is used when the primitive "clearscreen" is used.
	 */
	private Color				screencolor			= Color.WHITE;
	/**
	 * Default pen color: This color is used when the primitive "clearscreen" is used.
	 */
	private Color				pencolor			= Color.BLACK;
	
	/**
	 * This represents the pen shape
	 */
	private PenShape			penShape			= PenShape.SQUARE;
	
	/**
	 * This integer represents the turtle's speed for drawing <br>
	 * Slow: 100
	 * Fast: 0
	 */
	private int					turtleSpeed			= 0;
	/**
	 * This String contains the command to execute on Startup. <br>
	 * Configured in the dialog box "startup files"
	 */
	private String				a_executer			= "";
	/** Marko Zivkovic : this is used for a few Logo Commands that operate with files <br>
	 * Loic:
	 * The default folder for the user when the application starts.<br>
	 * This folder corresponds to the last opened or saved file in format lgo // Marko : not true anymore
	 */
	private String				defaultFolder		= Utils.rajoute_backslash(System.getProperty("user.home"));
	
	/**
	 * This boolean indicates if the grid is enabled
	 */
	private boolean				drawGrid			= false;
	/**
	 * This integer represents the X distance for the grid
	 */
	private int					XGrid				= 20;
	/**
	 * This integer represents the Y distance for the grid
	 */
	private int					YGrid				= 20;
	/**
	 * This integer represents the grid Color
	 */
	private int					gridColor			= Color.DARK_GRAY.getRGB();
	/**
	 * This boolean indicates if the X axis is enabled
	 */
	private boolean				drawXAxis			= false;
	/**
	 * This boolean indicates if the Y axis is enabled
	 */
	private boolean				drawYAxis			= false;
	/**
	 * This integer represents the axis Color
	 */
	private int					axisColor			= new Color(255, 0, 102).getRGB();
	/**
	 * This integer represents the X distance between two divisions on the X Axis
	 */
	private int					XAxis				= 30;
	/**
	 * This integer represents the X distance between two divisions on the Y Axis
	 */
	private int					YAxis				= 30;
	
	/** 
	 * This long represents the hour of XLogo starting
	 */
	private long				heure_demarrage;
	/**
	 * Color for the border around drawing area
	 */
	private Color				borderColor			= null;
	/** 
	 * The Image is for the border around drawing area 
	 */
	private String				borderImageSelected	= "background.png";
	
	/** 
	 * This Vector contains all images added by the user for image Border
	 */
	private ArrayList<String>	borderExternalImage	= new ArrayList<String>();
	/** 
	 * The default image defined by default that are included in XLogo
	 */
	private String[]			borderInternalImage	= { "background.png" };
	/** 
	 * This String represents the main command accessible with the button play in the toolbar
	 */
	private String				mainCommand			= "";
	/**
	 * This boolean indicates if Xlogo must launch the main Command on XLogo startup
	 * It overrides the String a_executer
	 */
	private boolean				autoLaunch			= false;
	
	/**
	 * TCP Port for robotics and network flows
	 */
	private int					tcpPort				= 1948;
	
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
		publisher.publishEvent(UserProperty.QUALITY);
	}
	
	public LookAndFeel getLooknfeel() {
		return looknfeel;
	}
	
	public void setLooknfeel(LookAndFeel looknfeel) {
		this.looknfeel = looknfeel;
		publisher.publishEvent(UserProperty.LOOKNFEEL);
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
		publisher.publishEvent(UserProperty.IMAGE_WIDTH);
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
		publisher.publishEvent(UserProperty.IMAGE_HEIGHT);
	}
	
	public int getActiveTurtle() {
		return activeTurtle;
	}
	
	public void setActiveTurtle(int activeTurtle) {
		this.activeTurtle = activeTurtle;
		publisher.publishEvent(UserProperty.ACTIVE_TURTLE);
	}
	
	public int getMaxPenWidth() {
		return maxPenWidth;
	}
	
	public void setMaxPenWidth(int maxPenWidth) {
		this.maxPenWidth = maxPenWidth;
		publisher.publishEvent(UserProperty.MAX_PEN_WIDTH);
	}
	
	public boolean isEraseImage() {
		return eraseImage;
	}
	
	public void setEraseImage(boolean eraseImage) {
		this.eraseImage = eraseImage;
		publisher.publishEvent(UserProperty.ERASE_IMAGE);
	}
	
	public boolean isClearVariables() {
		return clearVariables;
	}
	
	public void setClearVariables(boolean clearVariables) {
		this.clearVariables = clearVariables;
		publisher.publishEvent(UserProperty.CLEAR_VARIABLES);
	}
	
	public int getMaxTurtles() {
		return maxTurtles;
	}
	
	public void setMaxTurtles(int maxTurtles) {
		this.maxTurtles = maxTurtles;
		publisher.publishEvent(UserProperty.MAX_TURTLES);
	}
	
	public Color getScreencolor() {
		return screencolor;
	}
	
	public void setScreencolor(Color screencolor) {
		this.screencolor = screencolor;
		publisher.publishEvent(UserProperty.SCREENCOLOR);
	}
	
	public Color getPencolor() {
		return pencolor;
	}
	
	public void setPencolor(Color pencolor) {
		this.pencolor = pencolor;
		publisher.publishEvent(UserProperty.PENCOLOR);
	}
	
	public PenShape getPenShape() {
		return penShape;
	}
	
	public void setPenShape(PenShape penShape) {
		this.penShape = penShape;
		publisher.publishEvent(UserProperty.PEN_SHAPE);
	}
	
	public int getTurtleSpeed() {
		return turtleSpeed;
	}
	
	public void setTurtleSpeed(int turtleSpeed) {
		this.turtleSpeed = turtleSpeed;
		publisher.publishEvent(UserProperty.TURTLE_SPEED);
	}
	
	public String getA_executer() {
		return a_executer;
	}
	
	public void setA_executer(String a_executer) {
		this.a_executer = a_executer;
		publisher.publishEvent(UserProperty.A_EXECUTER);
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
		publisher.publishEvent(UserProperty.DEFAULT_FOLDER);
	}
	
	public boolean isDrawGrid() {
		return drawGrid;
	}
	
	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
		publisher.publishEvent(UserProperty.DRAW_GRID);
	}
	
	public int getXGrid() {
		return XGrid;
	}
	
	public void setXGrid(int xGrid) {
		XGrid = xGrid;
		publisher.publishEvent(UserProperty.X_GRID);
	}
	
	public int getYGrid() {
		return YGrid;
	}
	
	public void setYGrid(int yGrid) {
		YGrid = yGrid;
		publisher.publishEvent(UserProperty.Y_GRID);
	}
	
	public int getGridColor() {
		return gridColor;
	}
	
	public void setGridColor(int gridColor) {
		this.gridColor = gridColor;
		publisher.publishEvent(UserProperty.GRID_COLOR);
	}
	
	public boolean isDrawXAxis() {
		return drawXAxis;
	}
	
	public void setDrawXAxis(boolean drawXAxis) {
		this.drawXAxis = drawXAxis;
		publisher.publishEvent(UserProperty.DRAW_X_AXIS);
	}
	
	public boolean isDrawYAxis() {
		return drawYAxis;
	}
	
	public void setDrawYAxis(boolean drawYAxis) {
		this.drawYAxis = drawYAxis;
		publisher.publishEvent(UserProperty.DRAW_Y_AXIS);
	}
	
	public int getAxisColor() {
		return axisColor;
	}
	
	public void setAxisColor(int axisColor) {
		this.axisColor = axisColor;
		publisher.publishEvent(UserProperty.AXIS_COLOR);
	}
	
	public int getXAxis() {
		return XAxis;
	}
	
	public void setXAxis(int xAxis) {
		XAxis = xAxis;
		publisher.publishEvent(UserProperty.X_AXIS);
	}
	
	public int getYAxis() {
		return YAxis;
	}
	
	public void setYAxis(int yAxis) {
		YAxis = yAxis;
		publisher.publishEvent(UserProperty.Y_AXIS);
	}
	
	public long getHeure_demarrage() {
		return heure_demarrage;
	}
	
	public void setHeure_demarrage(long heure_demarrage) {
		this.heure_demarrage = heure_demarrage;
		publisher.publishEvent(UserProperty.HEURE_DEMARRAGE);
	}
	
	public Color getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		publisher.publishEvent(UserProperty.BORDER_COLOR);
	}
	
	public String getBorderImageSelected() {
		return borderImageSelected;
	}
	
	public void setBorderImageSelected(String borderImageSelected) {
		this.borderImageSelected = borderImageSelected;
		publisher.publishEvent(UserProperty.BORDER_IMAGE_SELECTED);
	}
	
	public ArrayList<String> getBorderExternalImage() {
		return borderExternalImage;
	}
	
	public void setBorderExternalImage(ArrayList<String> borderExternalImage) {
		this.borderExternalImage = borderExternalImage;
		publisher.publishEvent(UserProperty.BORDER_EXTERNAL_IMAGE);
	}
	
	public String[] getBorderInternalImage() {
		return borderInternalImage;
	}
	
	public void setBorderInternalImage(String[] borderInternalImage) {
		this.borderInternalImage = borderInternalImage;
		publisher.publishEvent(UserProperty.BORDER_INTERNAL_IMAGE);
	}
	
	public String getMainCommand() {
		return mainCommand;
	}
	
	public void setMainCommand(String mainCommand) {
		this.mainCommand = mainCommand;
		publisher.publishEvent(UserProperty.MAIN_COMMAND);
	}
	
	public boolean isAutoLaunch() {
		return autoLaunch;
	}
	
	public void setAutoLaunch(boolean autoLaunch) {
		this.autoLaunch = autoLaunch;
		publisher.publishEvent(UserProperty.AUTO_LAUNCH);
	}
	
	public int getTcpPort() {
		return tcpPort;
	}
	
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
		publisher.publishEvent(UserProperty.TCP_PORT);
	}
	
	/*
	 * Property Change Listeners
	 */
	
	public enum UserProperty {
	QUALITY,
	LOOKNFEEL,
	IMAGE_WIDTH,
	IMAGE_HEIGHT,
	ACTIVE_TURTLE,
	MAX_PEN_WIDTH,
	ERASE_IMAGE,
	CLEAR_VARIABLES,
	MAX_TURTLES,
	SCREENCOLOR,
	PENCOLOR,
	PEN_SHAPE,
	TURTLE_SPEED,
	A_EXECUTER,
	DEFAULT_FOLDER,
	DRAW_GRID,
	X_GRID,
	Y_GRID,
	GRID_COLOR,
	DRAW_X_AXIS,
	DRAW_Y_AXIS,
	AXIS_COLOR,
	X_AXIS,
	Y_AXIS,
	HEURE_DEMARRAGE,
	BORDER_COLOR,
	BORDER_IMAGE_SELECTED,
	BORDER_EXTERNAL_IMAGE,
	BORDER_INTERNAL_IMAGE,
	MAIN_COMMAND,
	AUTO_LAUNCH,
	TCP_PORT;
	}
	
	private transient PropertyChangePublisher<UserProperty>	publisher = new PropertyChangePublisher<>();
	
	@Override
	public void addPropertyChangeListener(UserProperty property, PropertyChangeListener listener) {
		if (publisher == null){
			publisher = new PropertyChangePublisher<>();
		}
		publisher.addPropertyChangeListener(property, listener);
	}
	
	@Override
	public void removePropertyChangeListener(UserProperty property, PropertyChangeListener listener) {
		publisher.removePropertyChangeListener(property, listener);
	}
	
}
