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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import xlogo.gui.components.X4SComponent;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
/**
 * @since June 10th 2014
 * @author Marko
 */
public class GlobalTab extends X4SComponent {
	
	private JPanel component;
	
	private JCheckBox askPasswordCb;
	private JLabel passwordLabel;
	private JLabel retypeLabel;
	private JPasswordField passwordField;
	private JPasswordField retypeField;
	private JButton save1;
	
	private String authentification;
	
	public GlobalTab() {
		super();
	}
	
	public void authenticate(String authentification)
	{
		this.authentification = authentification;
		
		boolean hasPw = authentification != null;
		askPasswordCb.setSelected(hasPw);
		showOrHidePw(hasPw);
		passwordField.setText(authentification);
		retypeField.setText(authentification);
	}
	
	public JComponent getComponent()
	{
		return component;
	}

	@Override
	protected void initComponent()
	{	
		component = new JPanel();
		
		askPasswordCb = new JCheckBox("Protect these settings with a password.");
		passwordLabel = new JLabel("Password");
		retypeLabel = new JLabel("Retype Password");
		passwordField = new JPasswordField();
		retypeField = new JPasswordField();
		save1 = new JButton("Save Password");
	}
	
	@Override
	protected void layoutComponent()
	{
		passwordField.setMinimumSize(new Dimension(200, 25));
		retypeField.setMinimumSize(new Dimension(200, 25));
		passwordField.setMaximumSize(new Dimension(200, 25));
		retypeField.setMaximumSize(new Dimension(200, 25));
		
		component.add(askPasswordCb);
		component.add(passwordLabel);
		component.add(passwordField);
		component.add(retypeLabel);
		component.add(retypeField);
		component.add(save1);
		
		GroupLayout groupLayout = new GroupLayout(component);
		component.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup()
					.addComponent(askPasswordCb)
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(passwordLabel)
						.addComponent(passwordField))
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(retypeLabel)
						.addComponent(retypeField))
					.addComponent(save1)
					);
		
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup()
					.addComponent(askPasswordCb)
					.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup()
							.addComponent(passwordLabel)
							.addComponent(retypeLabel)
							)
						.addGroup(groupLayout.createParallelGroup()
							.addComponent(passwordField)
							.addComponent(retypeField)
							)
					)
					.addComponent(save1)
				);
	}

	@Override
	protected void initEventListeners()
	{
		askPasswordCb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				showOrHidePw(selected);
				passwordField.setText(null);
				retypeField.setText(null);
				if (!selected)
					WSManager.getInstance().getGlobalConfigInstance().setNewPassword(authentification, null);
			}
		});
		
		save1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					savePassword();
				} catch (IOException e) {
					DialogMessenger.getInstance().dispatchMessage(
							translate("ws.error.title"),
							translate("ws.settings.cannot.store.pw") + e.toString());
				}
			}
		});
	}
	
	@Override
	public void stopEventListeners()
	{
		super.stopEventListeners();
		
	}
	
	private void showOrHidePw(boolean show)
	{
		askPasswordCb.setSelected(show);
		passwordLabel.setVisible(show);
		passwordField.setVisible(show);
		retypeLabel.setVisible(show);
		retypeField.setVisible(show);
		save1.setVisible(show);
	}
	
	private void savePassword() throws IOException
	{
		GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
		
		if (askPasswordCb.isSelected())
		{
			String pw1 = new String(passwordField.getPassword());
			String pw2 = new String(retypeField.getPassword());
			if (pw1.equals(pw2))
			{
				gc.setNewPassword(authentification, pw1);
				authentification = pw1;
				gc.store();
			}
			else
			{
				DialogMessenger.getInstance().dispatchMessage(
						translate("i.am.sorry"),
						translate("ws.settings.pw.must.be.equal"));
			}
		}else // checkbox not selected
		{
			gc.setNewPassword(authentification, null);
			authentification = null;
			gc.store();
		}
	}
	
	@Override
	protected void setText()
	{
		askPasswordCb.setText(translate("ws.settings.require_password"));
		passwordLabel.setText(translate("ws.settings.password"));
		retypeLabel.setText(translate("ws.settings.retype.password"));
		save1.setText(translate("ws.settings.save.password"));
	}

}
