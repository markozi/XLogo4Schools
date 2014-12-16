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

package xlogo.gui.welcome;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.io.IOException;

import xlogo.gui.components.X4SFrame;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;
import xlogo.utils.WebPage;
import xlogo.Application;

/**
 * This was initially called {@code Selection_Langue} and it was only displayed when the Application was opened for the very first time.
 * Now this has become {@code WelcomeScreen}, as it was enhanced with more options than just language selection:
 * <li> User Account Selection / Creation </li>
 * <li> Storage Location (master password required) </li>
 * @author Marko
 */
public class WelcomeScreen extends X4SFrame {
	
	JFrame						frame;
	
	private JLabel				label;
	
	private JLabel				workspace;
	private JLabel				username;
	
	private JComboBox			workspaceSelection;
	private JComboBox			userSelection;
	
	private JButton				openWorkspaceSettingsBtn;
	private JButton				enterButton;
	
	private JButton				infoButton;
	private JButton				gplButton;
	
	private JPanel				panel;
	private GroupLayout			groupLayout;
	
	private ActionListener		onApplicationEnterListener;
	private ActionListener		onWorkspaceListChangeListener;
	private ActionListener		onEnterWorkspaceListener;
	
	private WorkspaceSettings	workspaceSettings;
	
	/**
	 * 
	 * @param listener to be informed when the user is ready to enter the application
	 */
	public WelcomeScreen(ActionListener onApplicationEnterListener) {
		this.onApplicationEnterListener = onApplicationEnterListener;
	}
	
	@Override
	public JFrame getFrame() {
		return frame;
	}
	
	@Override
	protected void initComponent() {
		frame = new JFrame(){
			
			private static final long	serialVersionUID	= -6730403281163492211L;
			
			@Override
			public void dispose() {
				try {
					WSManager.getInstance().getGlobalConfigInstance().store();
				}
				catch (IOException e) {
					DialogMessenger.getInstance().dispatchMessage(translate("ws.error.title"),
							translate("storage.could.not.store.gc"));
				}
				
				super.dispose();
			}
		};
		// Window
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("Icon_x4s.png")));
		frame.setTitle("XLogo4Schools");
		
		workspace = new JLabel("Workspace");
		username = new JLabel("User");
		
		workspaceSelection = new JComboBox();
		userSelection = new JComboBox();
		
		openWorkspaceSettingsBtn = new JButton("Settings");
		enterButton = new JButton("Enter");
		
		infoButton = new JButton();
		gplButton = new JButton();
		
		panel = new JPanel();
		
		// The XLogo4Schools logo
		//ImageIcon logo = Utils.dimensionne_image("Logo_xlogo4schools.png", this);
		infoButton.setIcon(createImageIcon("info_icon.png", "Info", 40, 40));
		gplButton.setIcon(createImageIcon("gnu_gpl.png", "GPL", 40, 40));
		label = new JLabel(createImageIcon("Logo_xlogo4schools.png", "XLogo4Schools", 250, 40));
		
		populateWorkspaceList();
		populateUserList();
	}
	
	@Override
	protected void layoutComponent() {
		frame.getContentPane().add(panel);
		
		frame.setResizable(false);
		infoButton.setBorder(null);
		gplButton.setBorder(null);
		
		infoButton.setOpaque(false);
		gplButton.setOpaque(false);
		
		panel.add(workspace);
		panel.add(username);
		panel.add(workspaceSelection);
		panel.add(userSelection);
		panel.add(openWorkspaceSettingsBtn);
		panel.add(enterButton);
		panel.add(infoButton);
		panel.add(gplButton);
		
		workspaceSelection.setMinimumSize(new Dimension(200, 25));
		userSelection.setMinimumSize(new Dimension(200, 25));
		workspaceSelection.setMaximumSize(new Dimension(200, 25));
		userSelection.setMaximumSize(new Dimension(200, 25));
		
		groupLayout = new GroupLayout(panel);
		panel.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		groupLayout.setVerticalGroup(groupLayout
				.createSequentialGroup()
				.addGroup(
						groupLayout.createParallelGroup().addComponent(gplButton).addComponent(infoButton)
								.addComponent(label))
				.addGroup(
						groupLayout.createParallelGroup().addComponent(workspace).addComponent(workspaceSelection)
								.addComponent(openWorkspaceSettingsBtn))
				.addGroup(
						groupLayout.createParallelGroup().addComponent(username).addComponent(userSelection)
								.addComponent(enterButton)));
		
		groupLayout.setHorizontalGroup(groupLayout
				.createParallelGroup()
				.addGroup(
						groupLayout.createSequentialGroup().addComponent(label).addComponent(gplButton)
								.addComponent(infoButton))
				.addGroup(
						groupLayout
								.createSequentialGroup()
								.addGroup(
										groupLayout.createParallelGroup().addComponent(workspace)
												.addComponent(username))
								.addGroup(
										groupLayout.createParallelGroup().addComponent(workspaceSelection)
												.addComponent(userSelection))
								.addGroup(
										groupLayout.createParallelGroup().addComponent(openWorkspaceSettingsBtn)
												.addComponent(enterButton))));
	}
	
	private boolean ignoreGuiEvents = false;
	
	@Override
	protected void initEventListeners() {
		
		GlobalConfig gc = WSManager.getGlobalConfig();
		
		onEnterWorkspaceListener = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ignoreGuiEvents = true;
				populateWorkspaceList();
				populateUserList();
				ignoreGuiEvents = false;
				
			}
		};
		
		gc.addEnterWorkspaceListener(onEnterWorkspaceListener);
						
		onWorkspaceListChangeListener = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ignoreGuiEvents = true;
				populateWorkspaceList();
				populateUserList();
				ignoreGuiEvents = false;
			}
		};
		
		WSManager.getGlobalConfig().addWorkspaceListChangeListener(onWorkspaceListChangeListener);
		
		workspaceSelection.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if (ignoreGuiEvents) {
					return;
				}
				String workspace = (String) workspaceSelection.getSelectedItem();
				enterWorkspace(workspace);
			}
		});
		// Open workspace settings button
		openWorkspaceSettingsBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showWorkspaceSettings();
			}
		});
		
		// Select user combo box
		final JTextComponent tc = (JTextComponent) userSelection.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(new DocumentListener(){
			public void removeUpdate(DocumentEvent arg0) {
				enableOrDisableEnter();
			}
			
			public void insertUpdate(DocumentEvent arg0) {
				enableOrDisableEnter();
			}
			
			public void changedUpdate(DocumentEvent arg0) {
				enableOrDisableEnter();
			}
			
			private void enableOrDisableEnter() {
				String username = tc.getText();
				enterButton.setEnabled(username != null && username.length() != 0);
			}
		});
		
		userSelection.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String username = (String) userSelection.getSelectedItem();
				enterButton.setEnabled(username != null && username.length() != 0);
			}
		});
		
		// Enter user space button
		enterButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						enterApplication();
					}
				}).start();
			}
		});
		
		gplButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						showGPL();
					}
				}).start();
			}
		});
		
		infoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						showInfo();
					}
				}).start();
			}
		});
	}
	
	private void populateWorkspaceList() {
		GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
		String[] workspaces = gc.getAllWorkspaces();
		workspaceSelection.setModel(new DefaultComboBoxModel(workspaces));
		selectCurrentWorkspace();
	}
	
	private void selectCurrentWorkspace(){
		GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
		workspaceSelection.setSelectedItem(gc.getLastUsedWorkspace());
	}
	
	private void populateUserList() {
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		String[] users = wc.getUserList();
		userSelection.setModel(new DefaultComboBoxModel(users));
		String lastUser = wc.getLastActiveUser();
		userSelection.setSelectedItem(lastUser);
		enterButton.setEnabled(lastUser != null && lastUser.length() > 0);
		userSelection.setEditable(wc.isUserCreationAllowed());
	}
	
	protected void enterWorkspace(String workspaceName) {
		try {
			WSManager.getInstance().enterWorkspace(workspaceName);
			populateUserList();
		}
		catch (IOException e) {
			DialogMessenger.getInstance().dispatchMessage(translate("ws.error.title"),
					translate("ws.settings.could.not.enter.wp") + "\n\n" + e.toString());
		}
	}
	
	private synchronized void showWorkspaceSettings() {
		Runnable runnable = new Runnable(){
			public void run() {
				String authentification = null;
				GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
				if (gc.isPasswordRequired()) {
					authentification = showPasswordPopup();
					if (authentification == null)
						return; // user cancelled the process
						
					if (!gc.authenticate(new String(authentification))) {
						// Could not authenticate => cancel
						DialogMessenger.getInstance().dispatchMessage(translate("i.am.sorry"),
								translate("welcome.wrong.pw"));
						return;
					}
				}
				
				frame.setEnabled(false);
				getWorkspaceSettings().showFrame(authentification);
			}
		};
		
		new Thread(runnable).start();
	}
	
	private synchronized WorkspaceSettings getWorkspaceSettings() {
		if (workspaceSettings == null) {
			workspaceSettings = new WorkspaceSettings(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					// When the window is closed, repopulate workspace and user lists, update language, enable this window again.
					setMessageManagerParent();
					setText();
					populateUserList();
					frame.setEnabled(true);
				}
			});
		}
		return workspaceSettings;
	}
	
	protected String showPasswordPopup() {
		JPasswordField passwordField = new JPasswordField();
		int option = JOptionPane.showConfirmDialog(frame, passwordField, translate("welcome.enter.pw"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if (option == JOptionPane.OK_OPTION) { return new String(passwordField.getPassword()); }
		return null;
	}
	
	public void enterApplication() {
		String username = (String) userSelection.getSelectedItem();
		
		if ((username == null) || (username.length() == 0))
			return; // this should not happen since the enter button is disabled
			
		if (!Storable.checkLegalName(username)) {
			DialogMessenger.getInstance().dispatchError(translate(MessageKeys.NAME_ERROR_TITLE),
					translate(MessageKeys.ILLEGAL_NAME));
			return;
		}
		
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		if (!wc.existsUserLogically(username))
			wc.createUser(username);
		
		try {
			WSManager.getInstance().enterUserSpace(username);
		}
		catch (IOException e) {
			DialogMessenger.getInstance().dispatchMessage(translate("ws.error.title"),
					translate("welcome.could.not.enter.user") + "\n" + e.toString());
			return;
		}
		cleanupAfterEnter();
	}
	
	private void cleanupAfterEnter() {
		if (workspaceSettings != null){
			workspaceSettings.stopEventListeners();
			// TODO remove each reference to workspace settings
			workspaceSettings = null;
		}
		WSManager.getGlobalConfig().removeEnterWorkspaceListener(onEnterWorkspaceListener);
		WSManager.getGlobalConfig().removeWorkspaceListChangeListener(onWorkspaceListChangeListener);
		try {
			WSManager.getWorkspaceConfig().store();
		}
		catch (IOException ignore) { }
		onApplicationEnterListener.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	@Override
	public void setText() {
		workspace.setText(translate("welcome.workspace"));
		username.setText(translate("welcome.username"));
		openWorkspaceSettingsBtn.setText(translate("welcome.settings"));
		enterButton.setText(translate("welcome.enter"));
		frame.setTitle(translate("welcome.title"));
		frame.pack();
	}
	
	/**
	 * Like in XLogo, almost unmodified.
	 * It is displayed in the language of the currently selected workspace.
	 */
	private void showGPL() {
		JFrame frame = new JFrame(translate("menu.help.licence"));
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(WebPage.class.getResource("Logo_xlogo4schools.png")));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(500, 500);
		WebPage editorPane = new WebPage();
		editorPane.setEditable(false);
		
		String langCode = WSManager.getWorkspaceConfig().getLanguage().getLanguageCode();
		
		String path = "gpl/gpl-" + langCode + ".html";
		
		java.net.URL helpURL = Application.class.getResource(path);
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			}
			catch (IOException e1) {
				System.err.println("Attempted to read a bad URL: " + helpURL);
			}
		}
		else {
			System.err.println("Couldn't find file: " + path);
		}
		
		// Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		frame.getContentPane().add(editorScrollPane);
		frame.setVisible(true);
	}
	
	private void showInfo() {
		JFrame frame = new JFrame(translate("welcome.info.title"));
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(WebPage.class.getResource("Icon_x4s.png")));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(500, 500);
		WebPage editorPane = new WebPage();
		editorPane.setEditable(false);
		
		String path = "gpl/x4s_info.html";
		
		java.net.URL helpURL = Application.class.getResource(path);
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			}
			catch (IOException e1) {
				System.err.println("Attempted to read a bad URL: " + helpURL);
			}
		}
		else {
			System.err.println("Couldn't find file: " + path);
		}
		
		// Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		frame.getContentPane().add(editorScrollPane);
		frame.setVisible(true);
	}
	
	/*
	 * Helper
	 */
	
	private ImageIcon createImageIcon(String path, String description, int width, int heigth) {
		Image img = Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(path));
		return new ImageIcon(img.getScaledInstance(width, heigth, Image.SCALE_SMOOTH));
	}
}
