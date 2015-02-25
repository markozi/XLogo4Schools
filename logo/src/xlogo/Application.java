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
 * Contents of this file were initially written by Loic Le Coq,
 * a lot of modifications, extensions, refactorings might been applied by Marko Zivkovic
 */

/** Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq */
package xlogo;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Stack;

import xlogo.interfaces.BasicFileContainer.FileContainerChangeListener;
import xlogo.interfaces.ErrorDetector.FileErrorCollector.ErrorListener;
import xlogo.interfaces.MessageBroadcaster.MessageListener;
import xlogo.interfaces.ProcedureMapper.ProcedureMapListener;
import xlogo.interfaces.X4SModeSwitcher.ModeChangeListener;
import xlogo.kernel.DrawPanel;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;
import xlogo.utils.WriteImage;
import xlogo.gui.components.ProcedureSearch;
import xlogo.gui.components.TurtleComboBox;
import xlogo.gui.components.ProcedureSearch.ProcedureSearchRequestListener;
import xlogo.gui.components.X4SFrame;
import xlogo.gui.components.fileslist.FilesList;
import xlogo.gui.components.fileslist.FilesListEventListener;
import xlogo.gui.*;
import xlogo.kernel.Affichage;
import xlogo.kernel.Kernel;
import xlogo.kernel.network.NetworkServer;
import xlogo.kernel.perspective.Viewer3D;
import xlogo.kernel.userspace.UserSpace;
import xlogo.kernel.userspace.procedures.Procedure;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.messages.async.history.HistoryMessenger;

/**
 * @author Marko
 * @author Loic Le Coq
 */
public class Application extends X4SFrame {
	private static final int			BG_COLOR		= 0xB3BCEA;
	public static final String			appName			= "XLogo4Schools";
	
	private static Stack<String>		pile_historique;
	private int							index_historique;
	
	private MenuListener				menulistener;
	
	public boolean						error;
	boolean								stop;
	
	public Affichage					affichage;
	private Sound_Player				son;
	private Touche						touche;
	private Popup						jpop;
	
	// pref Box
	/**
	 * To display 3D View
	 */
	private Viewer3D					viewer3d;
	
	// Interpreter and drawer
	private Kernel						kernel;
	
	private UserSpace					userSpace;
	private UserConfig	uc;
	
	/*
	 * My Layout
	 */
	
	private JFrame						mainFrame;
	private JPanel						filesAndProcedures;
	private JPanel						commandOrEditor;
	private FilesList					filesList;
	private ProcedureSearch				procedureSearch;
	
	// drawingOrEditor@Drawing
	private JPanel						commandCard;
	private ZoneCommande				commandLine;
	private JLabel						recordTimerLabel;
	private JButton						stopButton;
	private JButton						menuButton;
	public JSplitPane					drawingAndHistory;
	
	private JPanel						drawingAndExtras;
	private HistoryPanel				history;
	
	public JScrollPane					scrollArea;
	private DrawPanel					drawPanel;
	private JPanel						extrasPanel;
	private JSlider						speedSlider;
	private TurtleComboBox				turtleCombo;
	
	// drawingOrEditor@Editor
	private Editor						editor;
	
	// Extras Menu
	private JPopupMenu					extras;
	
	private static final String			COMMAND_CARD_ID	= "command_card";
	private static final String			EDITOR_CARD_ID	= "editor_card";
	
	/** Builds the main frame */
	public Application() {
		super();
		showWelcomeMessage();
		focusCommandLine();
	}
	
	public JFrame getFrame() {
		return mainFrame;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * INIT
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void initComponent() {
		pile_historique = new Stack<String>();
		
		son = new Sound_Player(this);
		touche = new Touche();
		
		mainFrame = new JFrame();
		filesAndProcedures = new JPanel();
		
		commandCard = new JPanel();
		commandLine = new ZoneCommande(this);
		recordTimerLabel = new JLabel();
		stopButton = new JButton();
		menuButton = new JButton();
		drawingAndHistory = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		drawingAndExtras = new JPanel();
		history = new HistoryPanel(this);
		
		extrasPanel = new JPanel();
		speedSlider = new JSlider(JSlider.VERTICAL);
		
		editor = new Editor(this);
		
		extras = new JPopupMenu();
		
		try {
			uc = WSManager.getUserConfig();
			
			userSpace = new UserSpace();
			kernel = new Kernel(this, userSpace);
			kernel.initInterprete();
			
			menulistener = new MenuListener(this);
			jpop = new Popup(menulistener, commandLine);
			
			setTheme();
			initFrame();
			JPanel contentPane = (JPanel) mainFrame.getContentPane();
			JPanel toplevel = new JPanel();
			toplevel.setLayout(new BorderLayout());
			
			initFilesList();
			
			procedureSearch = new ProcedureSearch(userSpace);
			filesAndProcedures = new JPanel(new GridBagLayout());
			
			toplevel.add(filesAndProcedures, BorderLayout.WEST);
			commandOrEditor = new JPanel(new CardLayout());
			toplevel.add(commandOrEditor, BorderLayout.CENTER);
			
			commandOrEditor.add(commandCard, COMMAND_CARD_ID);
			commandOrEditor.add(editor.getComponent(), EDITOR_CARD_ID);
			//commandOrEditor.setPreferredSize(new Dimension(d.width-200, d.height));
			showCommandCard();
			
			scrollArea = new JScrollPane();
			DrawPanel.dessin = new BufferedImage(uc.getImageWidth(), uc.getImageHeight(), BufferedImage.TYPE_INT_RGB);
			drawPanel = new DrawPanel(this);
			
			initMenu();
			
			contentPane.add(toplevel);
			showFrame();
			initDrawingZone();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initFrame() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setSize(new Dimension(d.width, d.height * 9 / 10));
		mainFrame.setTitle("XLogo4Schools" + " - " + uc.getUserName());
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("Icon_x4s.png")));
		mainFrame.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowActivated(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
			
			public void windowClosed(WindowEvent e) {
			}
		});
	}
	
	private void initFilesList() {
		filesList = new FilesList();
		boolean isEditable = userSpace.isFilesListEditable();
		filesList.setEditable(isEditable);
		for (String fileName : userSpace.getFileNames()) {
			boolean hasErrors = userSpace.hasErrors(fileName);
			filesList.addFile(fileName, isEditable, hasErrors);
		}
	}
	
	private void initDrawingZone() {
		// on centre la tortue
		// Centering turtle
		Dimension dim = scrollArea.getViewport().getViewRect().getSize();
		Point p = new Point(Math.abs(uc.getImageWidth() / 2 - dim.width / 2), Math.abs(uc.getImageHeight() / 2
				- dim.height / 2));
		scrollArea.getViewport().setViewPosition(p);
		
		MediaTracker tracker = new MediaTracker(getFrame());
		tracker.addImage(DrawPanel.dessin, 0);
		try {
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {}
		//drawPanel.getGraphics().drawImage(DrawPanel.dessin,0,0,mainFrame);
		scrollArea.validate();
		
		setCommandLine(false);
		genere_primitive();
		uc.setHeure_demarrage(Calendar.getInstance().getTimeInMillis());
		setCommandLine(true);
		focusCommandLine();
		resizeDrawingZone();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * THEME
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void setTheme() {
		Font font = WSManager.getWorkspaceConfig().getFont();
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					UIManager.put("defaultFont", font);
					//UIManager.put("defaultFont", new Font(Font.SANS_SERIF, 0, 18));
					return;
				}
			}
			
			switch (uc.getLooknfeel()) {
				case JAVA:
					//MetalLookAndFeel.setCurrentTheme(uc.getColorTheme().getTheme());
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					break;
				default:
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			
		}
		catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception ignore) {}
		}
		UIManager.put("defaultFont", font);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * LAYOUT
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void layoutComponent() {
		layoutFilesAndProcedures();
		layoutCommandCard();
		layoutDrawingAndExtras();
		layoutDrawingArea();
		layoutExtras();
		drawPanel.getGraphics().drawImage(DrawPanel.dessin, 0, 0, mainFrame);
	}
	
	private void layoutFilesAndProcedures() {
		
		// Note : the following JPanel saved my day ...
		// thanks to this The files list is finally position at the top :-)
		JPanel layoutHelper = new JPanel(new BorderLayout());
		layoutHelper.add(filesList.getComponent(), BorderLayout.NORTH);
		
		JScrollPane scroller = new JScrollPane(layoutHelper);
		scroller.getViewport().setBackground(new Color(BG_COLOR));
		layoutHelper.setBackground(new Color(BG_COLOR));
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		filesAndProcedures.add(procedureSearch, c);
		
		c.gridx = 0;
		c.gridy = 1;
		
		c.weighty = 1;
		c.weightx = 1;
		
		c.fill = GridBagConstraints.BOTH;
		
		c.anchor = GridBagConstraints.NORTHWEST;
		
		filesAndProcedures.add(scroller, c);
		
	}
	
	private void layoutCommandCard() {
		
		int preferredHeight = WSManager.getWorkspaceConfig().getFont().getSize() * 15 / 10;
		
		stopButton.setIcon(createImageIcon("stop.png", 20, 20));
		menuButton.setIcon(createImageIcon("menubtn.png", 20, 20));
		
		stopButton.setMaximumSize(new Dimension(preferredHeight, preferredHeight));
		menuButton.setMaximumSize(new Dimension(preferredHeight, preferredHeight));
		commandCard.setBackground(Color.white);
		Font clockFont = new Font(null, Font.BOLD, 20);
		
		recordTimerLabel.setOpaque(true);
		recordTimerLabel.setBackground(Color.white);
		recordTimerLabel.setFont(clockFont);
		recordTimerLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		
		commandCard.add(recordTimerLabel);
		commandCard.add(commandLine);
		commandCard.add(drawingAndHistory);
		commandLine.setAlignmentX(JFrame.CENTER_ALIGNMENT);
		
		drawingAndHistory.setTopComponent(drawingAndExtras);
		drawingAndHistory.setBottomComponent(history);
		drawingAndHistory.setResizeWeight(0.8);
		
		GroupLayout commandCardLayout = new GroupLayout(commandCard);
		commandCard.setLayout(commandCardLayout);
		commandLine.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		
		commandCardLayout.setAutoCreateContainerGaps(false);
		commandCardLayout.setAutoCreateGaps(false);
		
		commandCardLayout.setVerticalGroup(commandCardLayout
				.createSequentialGroup()
				.addGroup(
						commandCardLayout.createParallelGroup().addComponent(commandLine)
								// TODO GAP?
								.addComponent(recordTimerLabel).addGap(2).addComponent(stopButton).addGap(2)
								.addComponent(menuButton)).addComponent(drawingAndHistory));
		
		commandCardLayout.setHorizontalGroup(commandCardLayout
				.createParallelGroup()
				.addGroup(
						commandCardLayout.createSequentialGroup().addComponent(commandLine)
								.addComponent(recordTimerLabel).addComponent(stopButton).addComponent(menuButton))
				.addComponent(drawingAndHistory));
	}
	
	private void layoutDrawingAndExtras() {
		history.setMinimumSize(new Dimension(600, 40));
		drawingAndExtras.setBackground(new Color(BG_COLOR));
		
		drawingAndExtras.add(scrollArea);
		drawingAndExtras.add(extrasPanel);
		
		GroupLayout drawingAndExtrasLayout = new GroupLayout(drawingAndExtras);
		drawingAndExtras.setLayout(drawingAndExtrasLayout);
		
		drawingAndExtrasLayout.setAutoCreateContainerGaps(false);
		drawingAndExtrasLayout.setAutoCreateGaps(false);
		
		drawingAndExtrasLayout.setVerticalGroup(drawingAndExtrasLayout.createParallelGroup().addComponent(scrollArea)
				.addComponent(extrasPanel));
		
		drawingAndExtrasLayout.setHorizontalGroup(drawingAndExtrasLayout.createSequentialGroup()
				.addComponent(scrollArea).addComponent(extrasPanel));
	}
	
	private void layoutDrawingArea() {
		drawPanel.setSize(new Dimension((int) (uc.getImageWidth() * DrawPanel.zoom),
				(int) (uc.getImageHeight() * DrawPanel.zoom)));
		scrollArea.getViewport().add(drawPanel);
		scrollArea.getHorizontalScrollBar().setBlockIncrement(5);
		scrollArea.getVerticalScrollBar().setBlockIncrement(5);
		scrollArea.setPreferredSize(drawPanel.getPreferredSize());
	}
	
	private void layoutExtras() {
		extrasPanel.setBackground(new Color(BG_COLOR));
		extrasPanel.setLayout(new BoxLayout(extrasPanel, BoxLayout.Y_AXIS));
		speedSlider.setMaximumSize(new Dimension(20, 200));
		speedSlider.setValue(speedSlider.getMaximum() - uc.getTurtleSpeed());
		
		turtleCombo = new TurtleComboBox();
		extrasPanel.add(speedSlider);
		extrasPanel.add(turtleCombo);
		turtleCombo.setBackground(new Color(BG_COLOR));
		
		turtleCombo.getComboBox().setSelectedIndex(uc.getActiveTurtle());
		
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * EXTRAS MENU
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private JMenuItem	startContestMenuItem;
	private JMenuItem	stopContestMenuItem;
	private JMenuItem	importMenuItem;
	private JMenuItem	exportMenuItem;
	private JMenuItem	saveImage;
	
	public void initMenu() {
		//Create the popup menu.
		extras = new JPopupMenu();
		
		startContestMenuItem = new JMenuItem(translate(MessageKeys.CONTEST_MODE_START));
		stopContestMenuItem = new JMenuItem(translate(MessageKeys.CONTEST_MODE_STOP));
		extras.add(startContestMenuItem, 0);
		
		startContestMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				int nOfFiles = wc.getNOfContestFiles();
				int nOfBonusFiles = wc.getNOfContestBonusFiles();
				
				String[] contestFileNames = new String[nOfFiles + nOfBonusFiles];
				
				String nameBase = translate("contest.mode.filename") + " ";
				for (int i = 0; i < nOfFiles; i++)
					contestFileNames[i] = nameBase + (i + 1);
				
				nameBase = translate("contest.mode.bonus.filename") + " ";
				
				for (int i = 0; i < nOfBonusFiles; i++)
					contestFileNames[nOfFiles + i] = nameBase + (i + 1);
				try {
					userSpace.startRecordMode(contestFileNames);
				}
				catch (IOException e1) {
					DialogMessenger.getInstance().dispatchMessage(translate("contest.error.title"),
							translate("contest.could.not.create") + "\n" + e.toString());
					return;
				}
				commandLine.requestFocus();
			}
		});
		stopContestMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				userSpace.stopRecordMode();
				commandLine.requestFocus();
			}
		});
		
		importMenuItem = new JMenuItem(translate(MessageKeys.US_IMPORT));
		extras.add(importMenuItem);
		importMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
				fc.setFileFilter(new FileFilter(){
					
					@Override
					public String getDescription() {
						return GlobalConfig.LOGO_FILE_EXTENSION;
					}
					
					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(GlobalConfig.LOGO_FILE_EXTENSION);
					}
				});
				;
				
				int returnVal = fc.showOpenDialog(getFrame());
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						userSpace.importFile(file);
					}
					catch (IOException e1) {
						DialogMessenger.getInstance().dispatchError(MessageKeys.GENERAL_ERROR_TITLE, e1.toString());
					}
				}
				commandLine.requestFocus();
			}
		});
		
		exportMenuItem = new JMenuItem(translate(MessageKeys.US_EXPORT));
		extras.add(exportMenuItem);
		exportMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				String fileName = (String) JOptionPane.showInputDialog(getFrame(), translate(MessageKeys.US_EXPORT_MSG)
						+ "\n", translate(MessageKeys.US_EXPORT), JOptionPane.PLAIN_MESSAGE, null,
						userSpace.getFileNames(), "ham");
				
				//If a string was returned, say so.
				if (fileName == null || fileName.length() == 0)
					return;
				
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
				fc.setFileFilter(new FileFilter(){
					
					@Override
					public String getDescription() {
						return GlobalConfig.LOGO_FILE_EXTENSION;
					}
					
					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(GlobalConfig.LOGO_FILE_EXTENSION);
					}
				});
				;
				
				int returnVal = fc.showOpenDialog(getFrame());
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						userSpace.exportFile(fileName, file);
					}
					catch (IOException e1) {
						DialogMessenger.getInstance().dispatchError(MessageKeys.GENERAL_ERROR_TITLE,
								"Could not export file : \n " + e1.toString());
					}
				}
				commandLine.requestFocus();
			}
		});
		
		saveImage = new JMenuItem(translate(MessageKeys.US_SAVE_IMAGE));
		extras.add(saveImage);
		saveImage.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				WriteImage writeImage = new WriteImage(getFrame(), getDrawPanel().getSelectionImage());
				int value = writeImage.chooseFile();
				if (value == JFileChooser.APPROVE_OPTION) {
					writeImage.start();
				}
				commandLine.requestFocus();
			}
		});
	}
	
	private ImageIcon createImageIcon(String path, int width, int heigth) {
		Image img = Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(path));
		return new ImageIcon(img.getScaledInstance(width, heigth, Image.SCALE_SMOOTH));
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * CHANGE LISTENERS
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * This glues together models and the GUI controllers.
	 */
	@Override
	protected void initEventListeners() {
		
		commandLine.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					jpop.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		commandLine.addKeyListener(touche);
		
		speedSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				int value = source.getValue();
				uc.setTurtleSpeed(source.getMaximum() - value);
				commandLine.requestFocus();
			}
		});
		
		turtleCombo.getComboBox().addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = turtleCombo.getComboBox().getSelectedIndex();
				uc.setActiveTurtle(i);
				getKernel().getActiveTurtle().setShape(uc.getActiveTurtle());
				getKernel().change_image_tortue("tortue" + i + ".png");
				focusCommandLine();
			}
		});
		
		stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				error = true;
				if (NetworkServer.isActive) {
					NetworkServer.stopServer();
				}
				setCommandLine(true);
				commandLine.requestFocus();
			}
		});
		
		menuButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				extras.show(menuButton, 0, 0);
			}
		});
		
		userSpace.addProcedureMapListener(new ProcedureMapListener(){
			@Override
			public void ownerRenamed(String oldName, String newName) {
				// ignore
			}
			
			@Override
			public void undefined(final String fileName, final String procedure) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						HistoryMessenger.getInstance().dispatchComment(
								fileName + " : " + translate(MessageKeys.HIST_MSG_PROCEDURES_UNDEFINED) + " "
										+ procedure + ".\n");
					}
				});
			}
			
			@Override
			public void undefined(final String fileName, final Collection<String> procedures) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						StringBuilder sb = new StringBuilder();
						for (String procedureName : procedures) {
							sb.append(procedureName);
							sb.append(", ");
						}
						sb.delete(sb.length() - 2, sb.length() - 1);
						
						HistoryMessenger.getInstance().dispatchComment(
								fileName + " : " + translate(MessageKeys.HIST_MSG_PROCEDURES_UNDEFINED) + " "
										+ sb.toString() + ".\n");
					}
				});
			}
			
			@Override
			public void defined(final String fileName, final String procedure) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						HistoryMessenger.getInstance().dispatchComment(
								fileName + " : " + translate("definir") + " " + procedure + ".\n");
					}
				});
			}
			
			@Override
			public void defined(final String fileName, final Collection<String> procedures) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						if (procedures.size() == 0)
							return;
						
						StringBuilder sb = new StringBuilder();
						for (String procedureName : procedures) {
							sb.append(procedureName);
							sb.append(", ");
						}
						sb.delete(sb.length() - 2, sb.length());
						
						HistoryMessenger.getInstance().dispatchComment(
								fileName + " : " + translate("definir") + " " + sb.toString() + ".\n");
					}
				});
			}
		});
		
		userSpace.addFileListener(new FileContainerChangeListener(){
			public void fileOpened(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						showEditorCard();
						editor.setText(userSpace.readFile(fileName));
						mainFrame.setTitle(appName + " - " + uc.getUserName() + " - " + fileName);
						filesList.openFile(fileName);
					}
				});
			}
			
			public void fileClosed(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						try {
							if (userSpace.existsFile(fileName)) // It is possibly deleted, and the editor is therefore closed.
							{
								userSpace.writeFileText(fileName, editor.getText());
								userSpace.storeFile(fileName);
							}
						}
						catch (IOException e) {
							DialogMessenger.getInstance().dispatchError(translate("general.error.title"),
									translate("ws.automatic.save.failed"));
						}
						showCommandCard();
						focusCommandLine();
						//commandLine.requestFocus();
						mainFrame.setTitle(appName + " - " + uc.getUserName());
						filesList.closeFile(fileName);
					}
				});
			}
			
			@Override
			public void fileAdded(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.addFile(fileName, userSpace.isFilesListEditable(), userSpace.hasErrors(fileName));
					}
				});
			}
			
			@Override
			public void fileRemoved(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.removeFile(fileName);
					}
				});
			}
			
			@Override
			public void fileRenamed(final String oldName, final String newName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.renameFile(oldName, newName);
						focusEditor();
					}
				});
			}
			
			@Override
			public void editRightsChanged(final boolean editEnabled) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.setEditable(editEnabled);
					}
				});
			}
			
		});
		
		userSpace.addErrorListener(new ErrorListener(){
			@Override
			public void errorsDetected(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.markError(fileName, true);
						// TODO: Think about not showing this message
						HistoryMessenger.getInstance().dispatchError(
								fileName + " " + translate(MessageKeys.FILE_CONTAINS_ERRORS) + "\n");
						
						for (String msg : userSpace.getErrorMessages(fileName))
							HistoryMessenger.getInstance().dispatchError(msg + "\n");
					}
				});
			}
			
			@Override
			public void allErrorsCorrected(final String fileName) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.markError(fileName, false);
					}
				});
			}
		});
		
		userSpace.addModeChangedListener(new ModeChangeListener(){
			@Override
			public void recordModeStopped() {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						extras.remove(stopContestMenuItem);
						extras.add(startContestMenuItem, 0);
						importMenuItem.setEnabled(true);
						exportMenuItem.setEnabled(true);
						recordTimer.stop();
						recordTimerLabel.setText(null);
					}
				});
			}
			
			private Timer	recordTimer;
			
			@Override
			public void recordModeStarted() {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						extras.remove(startContestMenuItem);
						extras.add(stopContestMenuItem, 0);
						importMenuItem.setEnabled(false);
						exportMenuItem.setEnabled(false);
						recordTimer = new Timer(1000, new ActionListener(){
							private Date	start	= Calendar.getInstance().getTime();
							
							@Override
							public void actionPerformed(ActionEvent e) {
								
								Date now = Calendar.getInstance().getTime();
								
								long diff = now.getTime() - start.getTime();
								
								recordTimerLabel.setText(Utils.getMinSec(diff));
							}
						});
						recordTimer.start();
					}
				});
			}
			
			@Override
			public void networkModeStopped() {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.getComponent().setEnabled(true);
						extras.setEnabled(true);
					}
				});
			}
			
			@Override
			public void networkModeStarted() {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.getComponent().setEnabled(false);
						extras.setEnabled(false);
					}
				});
			}
		});
		
		procedureSearch.addSearchRequestListener(new ProcedureSearchRequestListener(){
			public void requestProcedureSearch(String procedureName) {
				displayProcedure(procedureName);
			}
		});
		
		filesList.addEventListener(new FilesListEventListener(){
			
			@Override
			public void onFileDeleteRequest(String fileName) {
				userSpace.closeFile(fileName);
				userSpace.removeFile(fileName);
			}
			
			@Override
			public void onFileRenameRequest(String oldName, String newName) {
				userSpace.renameFile(oldName, newName);
			}
			
			@Override
			public void onFileCreateRequest() {
				String newFileName = AppSettings.getInstance().translate("new.file");
				String fileName = userSpace.makeUniqueFileName(newFileName);
				try {
					if (userSpace.hasTooManyEmptyFiles()) {
						String msg = AppSettings.getInstance().translate("message.too.many.empty.files");
						DialogMessenger.getInstance().dispatchMessage(msg);
						return;
					}
					userSpace.createFile(fileName);
					userSpace.openFile(fileName);
					filesList.editFile(fileName);
				}
				catch (Exception e) {
					String title = AppSettings.getInstance().translate("ws.error.title");
					String msg = AppSettings.getInstance().translate("ws.error.could.not.create.logo.file");
					DialogMessenger.getInstance().dispatchError(Logo.messages.getString(title),
							Logo.messages.getString(msg) + "\n" + e.toString());
				}
			}
			
			@Override
			public void onFileOpened(String fileName) {
				userSpace.openFile(fileName);
			}
			
			@Override
			public void onFileClosed(String fileName) {
				userSpace.closeFile(fileName);
			}
		});
		
		userSpace.addBroadcastListener(new MessageListener(){
			
			@Override
			public void messageEvent(final String fileName, final String message) {
				runOnGuiThread(new Runnable(){
					@Override
					public void run() {
						filesList.setItemMessage(fileName, message);
					}
				});
			}
		});
	}
	
	public static void runOnGuiThread(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
			return;
		}
		
		try {
			SwingUtilities.invokeAndWait(runnable);
		}
		catch (InterruptedException e) {
			runOnGuiThread(runnable);
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * COMMANDS
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public void showWelcomeMessage() {
		HistoryMessenger.getInstance().dispatchComment(
				translate(MessageKeys.APP_HELLO) + " " + uc.getUserName() + "!\n");
		
		StringBuilder sb = new StringBuilder();
		for (String procedureName : userSpace.getAllProcedureNames()) {
			sb.append(procedureName);
			sb.append(", ");
		}
		if (sb.length() == 0)
			return;
		
		sb.delete(sb.length() - 2, sb.length());
		
		HistoryMessenger.getInstance().dispatchComment(translate("definir") + " " + sb.toString() + ".\n");
	}
	
	public void displayProcedure(String procedureName) {
		Procedure proc = userSpace.getExecutable(procedureName);
		if (proc != null)
			displayProcedure(proc);
		else {
			HistoryMessenger.getInstance().dispatchError(
					procedureName + " : " + translate(MessageKeys.EDITOR_DISPLAY_PROC_NOT_FOUND) + "\n");
		}
	}
	
	public void displayProcedure(Procedure proc) {
		String openFile = userSpace.getOpenFileName();
		
		if (openFile != null) {
			try {
				userSpace.writeFileText(openFile, editor.getText());
				userSpace.storeFile(openFile);
				userSpace.closeFile(openFile);
			}
			catch (IOException e) {}
		}
		
		String fileName = proc.getOwnerName();
		if (userSpace.existsFile(fileName)) {
			userSpace.openFile(fileName);
			editor.displayProcedure(proc.getName());
		}
	}
	
	public void showCommandCard() {
		CardLayout cardLayout = (CardLayout) commandOrEditor.getLayout();
		cardLayout.show(commandOrEditor, COMMAND_CARD_ID);
	}
	
	public void showEditorCard() {
		CardLayout cardLayout = (CardLayout) commandOrEditor.getLayout();
		cardLayout.show(commandOrEditor, EDITOR_CARD_ID);
	}
	
	public void focusCommandLine() {
		commandLine.requestFocus();
	}
	
	public void focusEditor() {
		editor.requestFocus();
	}
	
	public void closeWindow() {
		WSManager storageManager = WSManager.getInstance();
		try {
			String openFile = userSpace.getOpenFileName();
			if (openFile != null) {
				userSpace.writeFileText(openFile, editor.getText());
				userSpace.storeFile(openFile);
			}
			storageManager.storeAllSettings();
			System.exit(0);
		}
		catch (Exception e1) {
			String message = translate(MessageKeys.US_COULD_NOT_STORE_RECENT_DATA) + "\n\n" + e1.toString() + "\n\n"
					+ translate("quitter?");
			
			String[] choix = { translate(MessageKeys.DIALOG_YES), translate(MessageKeys.DIALOG_NO) };
			int retval = JOptionPane.showOptionDialog(mainFrame, message, translate("general.error.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, choix, choix[0]);
			
			if (retval == JOptionPane.OK_OPTION)
				System.exit(0);
		}
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * BELOW IS MOSTLY XLOGO LEGACY CODE
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Called by the constructor or when language has been modified
	 */
	public void setText() {
		stopButton.setToolTipText(translate("interrompre_execution"));
		
		// Texte interne à utiliser pour JFileChooser et JColorChooser
		UIManager.put("FileChooser.cancelButtonText", translate("pref.cancel"));
		UIManager.put("FileChooser.cancelButtonToolTipText", translate("pref.cancel"));
		UIManager.put("FileChooser.fileNameLabelText", translate("nom_du_fichier"));
		UIManager.put("FileChooser.upFolderToolTipText", translate("dossier_parent"));
		UIManager.put("FileChooser.lookInLabelText", translate("rechercher_dans"));
		
		UIManager.put("FileChooser.newFolderToolTipText", translate("nouveau_dossier"));
		UIManager.put("FileChooser.homeFolderToolTipText", translate("repertoire_accueil"));
		UIManager.put("FileChooser.filesOfTypeLabelText", translate("fichier_du_type"));
		UIManager.put("FileChooser.helpButtonText", translate("menu.help"));
		
		history.updateText();
		jpop.setText();
	}
	
	// Ce qu'il se passe en validant dans la zone de texte
	/**
	 * When the user types "Enter" in the Command Line
	 * @author Loic Le Coq
	 */
	public void commande_actionPerformed() {
		// System.out.println("commandeTotal :"+Runtime.getRuntime().totalMemory()/1024/1024+" Free "+Runtime.getRuntime().freeMemory()/1024/1024);
		// Si une parenthese était sélectionnée, on désactive la
		// décoloration
		commandLine.setActive(false);
		// System.out.println(commande.getCaret().isVisible());
		if (stop)
			stop = false;
		String texte = commandLine.getText();
		if (!texte.equals("") && commandLine.isEditable()) {
			if (touche.tape) {
				touche.tape = false;
				pile_historique.pop();
			}
			if (pile_historique.size() > 49)
				pile_historique.remove(0);
			pile_historique.push(texte); // On rajoute ce texte à l'historique
			index_historique = pile_historique.size(); // on réajuste l'index
														// de
			// l'historique
			
			HistoryMessenger.getInstance().dispatchMessage(texte + "\n");
			
			// On enlève les éventuels commentaires
			int a = texte.indexOf("#");
			
			// LogoParser lp=new LogoParser(texte);
			while (a != -1) {
				if (a == 0) {
					texte = "";
					break;
				}
				else if (!texte.substring(a - 1, a).equals("\\")) {
					texte = texte.substring(0, a);
					break;
				}
				a = texte.indexOf("#", a + 1);
			}
			
			if (userSpace.isRecordMode())
				recordCommandLine(texte);
			
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			startInterpretation(Utils.decoupe(texte));
			
			// On efface la ligne de commande
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					commandLine.setText("");
				}
			});
			
		}
	}
	
	/**
	 * @author Marko Zivkovic
	 * @param text
	 */
	public void recordCommandLine(final String text) {
		new Thread(new Runnable(){
			
			@Override
			public void run() {
				PrintWriter out = null;
				File logoFile = uc.getCommandLineContestFile();
				String line = Utils.getTimeStamp() + " : " + text;
				try {
					out = new PrintWriter(new BufferedWriter(new FileWriter(logoFile, true)));
					out.println(line);
				}
				catch (Exception e) {
					DialogMessenger.getInstance().dispatchMessage(translate("contest.error.title"),
							translate("contest.could.not.store") + "\n" + e.toString());
				}
				finally {
					if (out != null)
						out.close();
				}
			}
		}).run();
	}
	
	/**
	 * Launch the Thread Affichage with the instructions "st"
	 * 
	 * @param st
	 *            List of instructions
	 * @author Loic Le Coq
	 * @author Marko Zivkovic - renamed (it was affichage_Start)
	 */
	public void startInterpretation(StringBuffer st) {
		affichage = new Affichage(this, st);
		affichage.start();
	}
	
	/**
	 * Get Method for Sound Player
	 * 
	 * @return The Sound Player
	 */
	public Sound_Player getSon() {
		return son;
	}
	
	/**
	 * Resize the dawing area
	 */
	public void resizeDrawingZone() {
		if (null != affichage) {
			affichage.setPause(true);
		}
		// resize the drawing image
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				DrawPanel.dessin = new BufferedImage(uc.getImageWidth(), uc.getImageHeight(),
						BufferedImage.TYPE_INT_RGB);
				// System.out.println("Total :"+Runtime.getRuntime().totalMemory()/1024+" max "+Runtime.getRuntime().maxMemory()/1024+" Free "+Runtime.getRuntime().freeMemory()/1024);
				MediaTracker tracker = new MediaTracker(drawPanel);
				tracker.addImage(DrawPanel.dessin, 0);
				try {
					tracker.waitForID(0);
				}
				catch (InterruptedException e) {}
				// ardoise1.getGraphics().drawImage(Ardoise.dessin,0,0,ardoise1);
				
				drawPanel.setPreferredSize(new Dimension(uc.getImageWidth(), uc.getImageHeight()));
				drawPanel.revalidate();
				kernel.initGraphics();
				// ardoise.repaint();
				//calculateMargin(); TODOD maybe return this
				
				Dimension d = scrollArea.getViewport().getViewRect().getSize();
				Point p = new Point(Math.abs((uc.getImageWidth() - d.width) / 2),
						Math.abs((uc.getImageHeight() - d.height) / 2));
				scrollArea.getViewport().setViewPosition(p);
				if (null != affichage)
					affichage.setPause(false);
			}
			
		});
		
	}
	
	/**
	 * Return the drawing area
	 * 
	 * @return The drawing area
	 */
	public DrawPanel getDrawPanel() {
		return drawPanel;
	}
	
	/**
	 * Notice if the command line is editable.
	 * 
	 * @return true if Command Line is editable, false otherwise
	 */
	public boolean commande_isEditable() {
		return commandLine.isEditable();
	}
	
	/**
	 * Set the text in the command Line
	 * 
	 * @param txt
	 *            The text to write
	 */
	public void setCommandText(String txt) {
		commandLine.setText(txt);
	}
	
	/**
	 * Get History panel
	 * 
	 * @return The HistoryPanel
	 */
	public HistoryPanel getHistoryPanel() {
		return history;
	}
	
	/**
	 * Enable or disable the command line and the play button
	 */
	public void setCommandLine(boolean b) {
		if (b) {
			if (SwingUtilities.isEventDispatchThread()) {
				commandLine.setEditable(true);
				commandLine.setBackground(Color.WHITE);
			}
			else {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						commandLine.setEditable(true);
						// commande.requestFocus();
						commandLine.setBackground(Color.WHITE);
					}
				});
			}
		}
		else {
			commandLine.setEditable(false);
			commandLine.setBackground(new Color(250, 232, 217));
		}
	}
	
	/**
	 * This method copy the selected Text in the command line
	 */
	protected void copy() {
		commandLine.copy();
	}
	
	/**
	 * This methos cut the selected Text in the command line
	 */
	protected void cut() {
		commandLine.cut();
	}
	
	/**
	 * This methos paste the selected Text into the command line
	 */
	protected void paste() {
		commandLine.paste();
	}
	
	/**
	 * This methos selects all the Text in the command line
	 */
	protected void select_all() {
		commandLine.selectAll();
	}
	
	/**
	 * This method creates all primitives.
	 * In XLogo4Schools, this method does not initialize the startup files
	 * anymore, because we don't have startup files.
	 * 
	 * @author Marko Zivkovic
	 */
	protected void genere_primitive() {
		kernel.initPrimitive();
	}
	
	/**
	 * Set the last key pressed to the key corresponding to integer i
	 * 
	 * @param i
	 *            The key code
	 */
	public void setCar(int i) {
		touche.setCar(i);
	}
	
	/**
	 * Returns an int that corresponds to the last key pressed.
	 * 
	 * @return the int representing the last key pressed
	 */
	public int getCar() {
		return touche.getCar();
	}
	
	/**
	 * This boolean indicates if the viewer3D is visible
	 * 
	 * @return true or false
	 */
	public boolean viewer3DVisible() {
		if (null != viewer3d)
			return viewer3d.isVisible();
		return false;
	}
	
	/**
	 * Initialize the 3D Viewer
	 */
	public void initViewer3D() {
		if (null == viewer3d) {
			viewer3d = new Viewer3D(drawPanel.getWorld3D(), drawPanel.getBackgroundColor());
		}
		
	}
	
	public Viewer3D getViewer3D() {
		return viewer3d;
	}
	
	/**
	 * Open the Viewer3D Frame
	 */
	public void viewerOpen() {
		if (null == viewer3d) {
			viewer3d = new Viewer3D(drawPanel.getWorld3D(), drawPanel.getBackgroundColor());
		}
		else {
			viewer3d.setVisible(false);
		}
		viewer3d.insertBranch();
		viewer3d.setVisible(true);
		viewer3d.requestFocus();
	}
	
	/**
	 * Returns the current kernel
	 * 
	 * @return The Kernel Object associated to main frame
	 */
	public Kernel getKernel() {
		return kernel;
	}
	
	/**
	 * 
	 * @author loic This class is the Controller for the Command Line<br>
	 *         It looks for key event, Upper and Lower Arrow for History<br>
	 *         And all other Characters
	 */
	class Touche extends KeyAdapter {
		int				car		= -1;
		
		private boolean	tape	= false;
		
		public void setCar(int i) {
			car = i;
		}
		
		public int getCar() {
			return car;
		}
		
		public void keyPressed(KeyEvent e) {
			int ch = e.getKeyChar();
			int code = e.getKeyCode();
			if (commandLine.isEditable()) {
				if (code == KeyEvent.VK_UP) {
					if (index_historique > 0) {
						if (index_historique == pile_historique.size()) {
							tape = true;
							pile_historique.push(commandLine.getText());
						}
						index_historique--;
						commandLine.setText(pile_historique.get(index_historique));
					}
					else
						index_historique = 0;
				}
				else if (code == KeyEvent.VK_DOWN) {
					if (index_historique < pile_historique.size() - 1) {
						index_historique++;
						commandLine.setText(pile_historique.get(index_historique));
					}
					else
						index_historique = pile_historique.size() - 1;
				}
			}
			else {
				if (ch != 65535)
					car = ch;
				else
					car = -code;
			}
		}
	}
}
