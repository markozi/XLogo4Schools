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

package xlogo.gui.welcome;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import xlogo.gui.components.X4SFrame;
import xlogo.gui.welcome.settings.tabs.SyntaxHighlightingTab;
import xlogo.gui.welcome.settings.tabs.ContestTab;
import xlogo.gui.welcome.settings.tabs.GlobalTab;
import xlogo.gui.welcome.settings.tabs.WorkspaceTab;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.WSManager;
import xlogo.utils.Utils;

public class WorkspaceSettings extends X4SFrame {
	
	private JFrame frame;

	// TABS
	JTabbedPane tabs;
	GlobalTab globalTab;
	WorkspaceTab workspaceTab;
	SyntaxHighlightingTab appearanceTab;
	ContestTab contestTab;
	
	/**
	 * Used to communicate with the frame which opened this one.
	 */
	private ActionListener listener;
	
	public WorkspaceSettings(ActionListener listener)
	{
		super();
		this.listener = listener;
	}
	
	public void showFrame(String authentification)
	{
		globalTab.authenticate(authentification);
		frame.setVisible(true);
	}
	
	@Override
	public JFrame getFrame() {
		return frame;
	}

	@Override
	protected void initComponent() {
		frame = new JFrame(){
			private static final long serialVersionUID = 7057009528231153055L;
			
			@Override
			public void dispose()
			{
				try {
					WSManager.getInstance().getGlobalConfigInstance().store();
					WSManager.getInstance().getWorkspaceConfigInstance().store();
				} catch (IOException e) {
					DialogMessenger.getInstance().dispatchMessage(
							translate("ws.error.title"),
							translate("storage.could.not.store.gc"));
				}
				
				listener.actionPerformed(null);
				super.dispose();
				System.gc();
			}
		};
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("Icon_x4s.png"))); // TODO need new icon?
		
		tabs = new JTabbedPane();
		globalTab = new GlobalTab();
		workspaceTab = new WorkspaceTab();
		appearanceTab = new SyntaxHighlightingTab();
		contestTab = new ContestTab();
		
		setMessageManagerParent();
	}

	@Override
	protected void layoutComponent() {
		frame.setResizable(false);

		tabs.addTab("Global", null, globalTab.getComponent(), "Global Settings");
		tabs.addTab("Workspace", null, workspaceTab.getComponent(), "Workspace Settings");
		tabs.addTab("Appearance", null, appearanceTab.getComponent(), "Workspace Appearance");
		tabs.addTab("Contest", null, contestTab.getComponent(), "Contest Settings");
		
		frame.getContentPane().add(tabs);
	}

	@Override
	protected void setText()
	{
		frame.setTitle(translate("ws.settings.title"));
		tabs.setTitleAt(0, translate("ws.settings.global"));
		tabs.setToolTipTextAt(0, translate("ws.settings.global.settings"));
		tabs.setTitleAt(1, translate("ws.settings.workspace"));
		tabs.setToolTipTextAt(1, translate("ws.settings.global.settings"));
		tabs.setTitleAt(2, translate("ws.settings.syntax")); // TODO make translation
		tabs.setToolTipTextAt(2, translate("ws.settings.global.settings"));
		tabs.setTitleAt(3, translate("ws.settings.contest"));
		tabs.setToolTipTextAt(3, translate("ws.settings.global.settings"));
		frame.pack();
	}
	
	@Override
	protected void initEventListeners()
	{
		
	}
	
	@Override
	public void stopEventListeners()
	{
		stopListenForLanguageChangeEvents();
		globalTab.stopEventListeners();
		workspaceTab.stopEventListeners();
		appearanceTab.stopEventListeners();
		contestTab.stopEventListeners();
	}
		
}
