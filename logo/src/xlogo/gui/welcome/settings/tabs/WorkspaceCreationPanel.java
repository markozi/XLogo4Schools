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

package xlogo.gui.welcome.settings.tabs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import xlogo.gui.components.X4SComponent;
import xlogo.storage.WSManager;

class WorkspaceCreationPanel extends X4SComponent
{
	
	private JPanel component;
	
	public JLabel wsNameLabel;
	public JLabel locationLabel;
	public JTextField wsNameField;
	public JTextField locationField;
	public JButton openFilechooserBtn;
	
	public JComponent getComponent()
	{
		return component;
	}

	@Override
	protected void initComponent() {
		component = new JPanel();
		
		wsNameLabel = new JLabel("Workspace Name");
		locationLabel = new JLabel("Location");
		wsNameField = new JTextField();
		locationField = new JTextField();
		openFilechooserBtn = new JButton("Browse");
		
		locationField.setText(WSManager.getInstance().getGlobalConfigInstance().getLocation().toString());
		locationField.setEditable(false);		
	}

	@Override
	protected void layoutComponent() {
		component.add(wsNameLabel);
		component.add(locationLabel);
		component.add(wsNameField);
		component.add(locationField);
		component.add(openFilechooserBtn);
		
		locationField.setMinimumSize(new Dimension(250,15));
		
		GroupLayout groupLayout = new GroupLayout(component);
		component.setLayout(groupLayout);
		
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setAutoCreateGaps(true);
		
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(wsNameLabel)
						.addComponent(wsNameField)
						)
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(locationLabel)
						.addComponent(locationField)
						.addComponent(openFilechooserBtn)
						)
			);
		
		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(wsNameLabel)
						.addComponent(locationLabel)
						)
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(wsNameField)
						.addGroup(groupLayout.createSequentialGroup()
								.addComponent(locationField)
								.addComponent(openFilechooserBtn))
						)
			);
		
	}
	
	@Override
	protected void initEventListeners()
	{
		openFilechooserBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File location = getUserSelectedDirectory();
				if (location != null)
					locationField.setText(location.toString());
			}
		});
	}
	
	@Override
	public void stopEventListeners()
	{
		super.stopEventListeners();
		
	}
	
	@Override
	protected void setText()
	{
		wsNameLabel.setText(translate("ws.creation.panel.name"));
		locationLabel.setText(translate("ws.creation.panel.location"));
		openFilechooserBtn.setText(translate("ws.creation.panel.browse"));
	}
}