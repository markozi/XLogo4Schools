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

package xlogo.gui.welcome.settings.tabs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xlogo.Logo;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.Language;
import xlogo.storage.workspace.NumberOfBackups;
import xlogo.storage.workspace.WorkspaceConfig;

public class WorkspaceTab extends AbstractWorkspacePanel{

	JPanel component;
	
	JLabel workspaceLabel;
	JLabel wsLocationLabel;
	JLabel wsLanguageLabel;
	JLabel wsBackupLabel;
	JLabel userLabel;
	
	JButton addWorkspaceBtn;
	JButton addUserBtn;

	JButton removeWorkspaceBtn;
	JButton removeUserBtn;
	
	JButton importWorkspaceBtn;
	JButton importUserBtn;
	
	JComboBox workspaceSelection;
	JComboBox userSelection;
	JLabel wsLocation;
	JFileChooser wsLocationChooser;
	JComboBox languageSelection;
	JComboBox nOfBackupsSelecteion;
	JCheckBox userCreatable;
	
	public WorkspaceTab() {
		super();
	}
	
	@Override
	public JComponent getComponent()
	{
		return component;
	}

	@Override
	protected JComboBox getWorkspaceSelection() {
		return workspaceSelection;
	}

	@Override
	protected void initComponent()
	{
		component = new JPanel();
		
		workspaceLabel = new JLabel("Workspace: ");
		wsLocationLabel = new JLabel("Location: ");
		wsLanguageLabel = new JLabel("Language: ");
		wsBackupLabel = new JLabel("Number of Backups: ");
		userLabel = new JLabel("User: ");
		
		addWorkspaceBtn = new JButton("Add");
		addUserBtn = new JButton("Add");

		removeWorkspaceBtn = new JButton("Remove");
		removeUserBtn = new JButton("Remove");
		
		importWorkspaceBtn = new JButton("Import");
		importUserBtn = new JButton("Import");
		
		workspaceSelection = new JComboBox();
		userSelection = new JComboBox();
		wsLocation = new JLabel();
		wsLocationChooser = new JFileChooser();
		languageSelection = new JComboBox(Language.values());
		nOfBackupsSelecteion = new JComboBox(NumberOfBackups.values());
		userCreatable = new JCheckBox("Allow the users to create new user accounts?");
		
		populateWorkspaceList();
		setValues();
	}

	@Override
	protected void layoutComponent()
	{
		workspaceSelection.setMinimumSize(new Dimension(150,25));
		workspaceSelection.setMaximumSize(new Dimension(150,25));
		userSelection.setMinimumSize(new Dimension(150,25));
		userSelection.setMaximumSize(new Dimension(150,25));
		languageSelection.setMinimumSize(new Dimension(150,25));
		languageSelection.setMaximumSize(new Dimension(150,25));
		nOfBackupsSelecteion.setMinimumSize(new Dimension(75,25));
		nOfBackupsSelecteion.setMaximumSize(new Dimension(75,25));
		
		component.add(workspaceLabel);
		component.add(wsLocationLabel);
		component.add(wsLanguageLabel);
		component.add(wsBackupLabel);
		component.add(userLabel);
		
		component.add(addWorkspaceBtn);
		component.add(addUserBtn);
		component.add(removeWorkspaceBtn);
		component.add(removeUserBtn);
		component.add(importWorkspaceBtn);
		component.add(importUserBtn);

		component.add(workspaceSelection);
		component.add(userSelection);
		component.add(wsLocation);
		component.add(languageSelection);
		component.add(nOfBackupsSelecteion);
		
		GroupLayout groupLayout = new GroupLayout(component);
		component.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(workspaceLabel)
						.addComponent(workspaceSelection)
						.addComponent(addWorkspaceBtn)
						.addComponent(removeWorkspaceBtn)
						.addComponent(importWorkspaceBtn))
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(wsLocationLabel)
						.addComponent(wsLocation))
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(userLabel)
						.addComponent(userSelection)
						.addComponent(addUserBtn)
						.addComponent(removeUserBtn)
						.addComponent(importUserBtn))
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(wsLanguageLabel)
						.addComponent(languageSelection))
					.addGroup(groupLayout.createParallelGroup()
							.addComponent(wsBackupLabel)
							.addComponent(nOfBackupsSelecteion))
					.addGroup(groupLayout.createParallelGroup()
							.addComponent(userCreatable))
					);
		
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup()
					.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup()
							.addComponent(workspaceLabel)
							.addComponent(userLabel)
							.addComponent(wsLocationLabel)
							.addComponent(wsLanguageLabel)
							.addComponent(wsBackupLabel)
							)
						.addGroup(groupLayout.createParallelGroup()
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup()
										.addComponent(workspaceSelection)
										.addComponent(userSelection)
									)
								.addGroup(groupLayout.createParallelGroup()
										.addComponent(addWorkspaceBtn)
										.addComponent(addUserBtn)
									)
								.addGroup(groupLayout.createParallelGroup()
										.addComponent(removeWorkspaceBtn)
										.addComponent(removeUserBtn)
									)
								.addGroup(groupLayout.createParallelGroup()
										.addComponent(importWorkspaceBtn)
										.addComponent(importUserBtn)
									)	
								)
							.addComponent(wsLocation)
							.addComponent(languageSelection)
							.addComponent(nOfBackupsSelecteion)		
							)
						)
					.addComponent(userCreatable)
					);
	}
	
	@Override
	protected void initEventListeners()
	{
		/*
		 * WORKSPACE [SELECT in super class]; ADD; REMOVE; IMPORT
		 */
		super.initEventListeners();
			
		addWorkspaceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						addWorkspace();
					}
				}).run();
			}
		});
		
		removeWorkspaceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						deleteWorkspace();
					}
				}).run();
			}
		});
		
		importWorkspaceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						importWorkspace();
					}
				}).run();
			}
		});
		
		/*
		 * USER ADD; REMOVE; IMPORT
		 */
		
		addUserBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						addUser();
					}
				}).run();
			}
		});
		
		removeUserBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						removeUser();
					}
				}).run();
			}
		});
		
		importUserBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						importUser();
					}
				}).run();
			}
		});
		
		/*
		 * LANGUAGE
		 */
		
		languageSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						Language lang = (Language) languageSelection.getSelectedItem();
						changeLanguage(lang);
					}
				}).run();
			}
		});
		
		/*
		 * BACKUP VERSIONS
		 */
		
		nOfBackupsSelecteion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				NumberOfBackups n = (NumberOfBackups) nOfBackupsSelecteion.getSelectedItem();
				WSManager.getInstance().getWorkspaceConfigInstance().setNumberOfBackups(n);
			}
		});
		
		/*
		 * USER CREATION
		 */
		
		userCreatable.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean isAllowed = userCreatable.isSelected();
				WSManager.getInstance().getWorkspaceConfigInstance().setAllowUserCreation(isAllowed);
			}
		});
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * WORKSPACE : ADD, REMOVE, IMPORT
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * On creation or when a workspace is set, the input elements must be set according to the current workspace properties.
	 */
	protected void setValues()
	{
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
		
		// location text
		if (wc == null)
		{
			String wsName = (String) workspaceSelection.getSelectedItem();
			wsLocation.setText(gc.getWorkspaceDirectory(wsName).toString());
			wsLocationLabel.setText(Logo.messages.getString("ws.settings.damaged"));
			disableComponents();
			return;
		}else if(wc.isVirtual())
			wsLocation.setText(Logo.messages.getString("ws.settings.virtual.ws.not.stored"));
		else
			wsLocation.setText(wc.getLocation().toString());
		
		// user list
		populateUserList();
		
		// Language
		languageSelection.setSelectedItem(wc.getLanguage());
		changeLanguage(wc.getLanguage());
		
		// Backups
		nOfBackupsSelecteion.setSelectedItem(wc.getNumberOfBackups());
		
		// User Creation
		userCreatable.setSelected(wc.isUserCreationAllowed());
	}
	
	/**
	 * Disable controls that depend on the workspace and that cannot be used with a virtual workspace
	 * or a workspace that could not be loaded
	 */
	protected void disableComponents()
	{
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		removeWorkspaceBtn.setEnabled(wc == null);
		userSelection.setEnabled(false);
		addUserBtn.setEnabled(false);
		removeUserBtn.setEnabled(false);
		importUserBtn.setEnabled(false);
		nOfBackupsSelecteion.setEnabled(false);
		userCreatable.setEnabled(false);
	}
	
	@Override
	/**
	 * Enable if Workspace is successfully entered and if it is not virtual.
	 */
	protected void enableComponents()
	{
		removeWorkspaceBtn.setEnabled(true);
		userSelection.setEnabled(true);
		addUserBtn.setEnabled(true);
		removeUserBtn.setEnabled(true);
		importUserBtn.setEnabled(true);
		nOfBackupsSelecteion.setEnabled(true);
		userCreatable.setEnabled(true);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * USER : ADD, REMOVE, IMPORT
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	private void populateUserList()
	{
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		String[] users = wc.getUserList();
		userSelection.setModel(new DefaultComboBoxModel(users));
		try {
			wc.enterInitialUserSpace();
			String lastUser = wc.getLastActiveUser();
			userSelection.setSelectedItem(lastUser);
		}
		catch (IOException ignore) {
		}
	}
	
	private void addUser()
	{
		String username =  getUserText(Logo.messages.getString("ws.settings.enter.user.name"), Logo.messages.getString("ws.settings.create.new.user"));

		if ((username == null) || (username.length() == 0))
			return;
		
		if (WSManager.getInstance().getWorkspaceConfigInstance().existsUserLogically(username))
		{
			DialogMessenger.getInstance().dispatchMessage(
					Logo.messages.getString("ws.error.title"),
					Logo.messages.getString("ws.settings.user.exists.already"));
			return;
		}
		
		WSManager.getInstance().createUser(username);
		populateUserList();
	}
	
	private void removeUser()
	{
		WSManager wsManager = WSManager.getInstance();
		WorkspaceConfig wc = wsManager.getWorkspaceConfigInstance();

		String username = (String) userSelection.getSelectedItem();
		if (username == null)
			return;
		String userDirectory = wc.getUserDirectroy(username).toString();
		String message = 
				Logo.messages.getString("ws.settings.want.delete.dir.1") 
				+ userDirectory 
				+ Logo.messages.getString("ws.settings.want.delete.dir.1");
		
		boolean ans = getUserYesOrNo(message, Logo.messages.getString("ws.settings.remove.user"));
		
		WSManager.getInstance().deleteUser(username, ans);
		
		populateUserList();
	}
	
	private void importUser()
	{
		File dir = getUserSelectedDirectory();
		if (dir == null)
			return;

		if (!WSManager.isUserDirectory(dir))
		{
			DialogMessenger.getInstance().dispatchMessage(
					Logo.messages.getString("i.am.sorry"),
					dir.toString() + Logo.messages.getString("ws.settings.not.legal.user.dir"));
			return;
		}
		
		String newName = dir.getName();
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		if (dir.equals(wc.getUserDirectroy(newName)))
		{
			DialogMessenger.getInstance().dispatchMessage("This user was already in the list.");
			return;
		}

		while (wc.existsUserLogically(newName) || !Storable.checkLegalName(newName))
		{
			String msg = wc.existsUserLogically(newName) ? 
					"The user name " + newName + " already exists. Please choose a new name."
					: "The chosen name contains illegal characters. Please choose a new name.";
			
			newName = getUserText(msg, "Name Conflict");
			if (newName == null)
				return;
		}
		
		try {
			WSManager.getInstance().importUser(dir, newName);
		} catch (Exception e) {
			DialogMessenger.getInstance().dispatchMessage(
					Logo.messages.getString("ws.error.title"),
					Logo.messages.getString("ws.settings.could.not.import.user") + e.toString());
		}
		populateUserList();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * START : LANGUAGE
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void changeLanguage(Language lang) {
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		
		if (wc.getLanguage() != lang)
		{
			wc.setLanguage(lang);
		}
	}
	
	protected void setText()
	{
		workspaceLabel.setText(Logo.messages.getString("ws.settings.workspace"));
		wsLocationLabel.setText(Logo.messages.getString("ws.settings.location"));
		wsLanguageLabel.setText(Logo.messages.getString("ws.settings.language"));
		wsBackupLabel.setText(Logo.messages.getString("ws.settings.backups"));
		userLabel.setText(Logo.messages.getString("ws.settings.user"));
		addWorkspaceBtn.setText(Logo.messages.getString("ws.settings.add"));
		addUserBtn.setText(Logo.messages.getString("ws.settings.add"));
		removeWorkspaceBtn.setText(Logo.messages.getString("ws.settings.remove"));
		removeUserBtn.setText(Logo.messages.getString("ws.settings.remove"));
		importWorkspaceBtn.setText(Logo.messages.getString("ws.settings.import"));
		importUserBtn.setText(Logo.messages.getString("ws.settings.import"));
		userCreatable.setText(Logo.messages.getString("ws.settings.enable.user.account.creation"));
	}
}
