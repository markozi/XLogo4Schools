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

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xlogo.storage.WSManager;
import xlogo.storage.workspace.WorkspaceConfig;

public class ContestTab extends AbstractWorkspacePanel {
	
	JPanel component;
	JLabel workspaceLabel;
	JComboBox<String> workspaceSelection;
	JLabel nOfFilesLabel;
	JSpinner nOfFileSpinner;
	JLabel nOfBonusFilesLabel;
	JSpinner nOfBonusFileSpinner;
	
	public JComponent getComponent()
	{
		return component;
	}

	@Override
	protected JComboBox<String> getWorkspaceSelection() {
		return workspaceSelection;
	}
	
	@Override
	protected void initComponent()
	{
		int contestFiles = 6;
		int bonusFiles = 2;
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		if (wc != null){
			contestFiles = wc.getNOfContestFiles();
			bonusFiles = wc.getNOfContestBonusFiles();
		}
		
		component = new JPanel();
		
		workspaceLabel = new JLabel();
		workspaceSelection = new JComboBox<>();

		nOfFilesLabel = new JLabel();
	    nOfFileSpinner = new JSpinner(new SpinnerNumberModel(contestFiles, 0, 100, 1));
	    JComponent editor = new JSpinner.NumberEditor(nOfFileSpinner);
	    nOfFileSpinner.setEditor(editor);

		nOfBonusFilesLabel = new JLabel();
	    nOfBonusFileSpinner = new JSpinner(new SpinnerNumberModel(bonusFiles, 0, 100, 1));
	    JComponent editor2 = new JSpinner.NumberEditor(nOfBonusFileSpinner);
	    nOfBonusFileSpinner.setEditor(editor2);

	    if (wc != null){
	    	enableComponents();
	    } else {
	    	disableComponents();
	    }
	    
		populateWorkspaceList();
	}

	@Override
	protected void layoutComponent()
	{
		workspaceSelection.setMinimumSize(new Dimension(150,25));
		workspaceSelection.setMaximumSize(new Dimension(150,25));

		nOfFileSpinner.setMinimumSize(new Dimension(25,25));
		nOfFileSpinner.setMaximumSize(new Dimension(50,25));
		
		nOfBonusFileSpinner.setMinimumSize(new Dimension(25,25));
		nOfBonusFileSpinner.setMaximumSize(new Dimension(50,25));
		
		component.add(workspaceLabel);
		component.add(workspaceSelection);
		component.add(nOfFilesLabel);
		component.add(nOfFileSpinner);
		component.add(nOfBonusFilesLabel);
		component.add(nOfBonusFileSpinner);
		
		GroupLayout groupLayout = new GroupLayout(component);
		component.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		groupLayout.setVerticalGroup(
			groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(workspaceLabel)
						.addComponent(workspaceSelection)
					)
				.addGroup(groupLayout.createParallelGroup()
					.addComponent(nOfFilesLabel)
					.addComponent(nOfFileSpinner)
				)
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(nOfBonusFilesLabel)
						.addComponent(nOfBonusFileSpinner)
					)
		);

		groupLayout.setHorizontalGroup(
			groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup()
						.addComponent(workspaceLabel)
						.addComponent(nOfFilesLabel)
						.addComponent(nOfBonusFilesLabel)
					)
				.addGroup(groupLayout.createParallelGroup()
					.addComponent(workspaceSelection)
					.addComponent(nOfFileSpinner)
					.addComponent(nOfBonusFileSpinner)
				)
		);
	}

	@Override
	protected void setText()
	{
		workspaceLabel.setText(translate("ws.settings.workspace"));
		nOfFilesLabel.setText(translate("contest.number.of.files"));
		nOfBonusFilesLabel.setText(translate("contest.number.of.bonus.files"));
	}

	@Override
	protected void initEventListeners()
	{
		super.initEventListeners();
		
		nOfFileSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				if (wc != null) {
					wc.setNOfContestFiles((Integer) nOfFileSpinner.getValue());
				}
			}
		});
		
		nOfBonusFileSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				if (wc != null){
					wc.setNOfContestBonusFiles((Integer) nOfBonusFileSpinner.getValue());
				}
				
			}
		});
	}
	
	@Override
	public void stopEventListeners()
	{
		super.stopEventListeners();
		
	}

	@Override
	protected void setValues() {
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		if (wc == null) {
			nOfFileSpinner.setValue(0);
			nOfBonusFileSpinner.setValue(0);
			disableComponents();
			return;
		} else {
			enableComponents();
		}
		nOfFileSpinner.setValue(wc.getNOfContestFiles());
		nOfBonusFileSpinner.setValue(wc.getNOfContestBonusFiles());
	}

	@Override
	protected void enableComponents() {
		workspaceSelection.setEnabled(true);
		nOfFileSpinner.setEnabled(true);
		nOfBonusFileSpinner.setEnabled(true);
	}

	@Override
	protected void disableComponents() {
		workspaceSelection.setEnabled(false);
		nOfFileSpinner.setEnabled(false);
		nOfBonusFileSpinner.setEnabled(false);
	}

}
