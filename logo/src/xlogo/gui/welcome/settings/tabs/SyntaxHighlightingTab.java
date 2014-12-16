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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import xlogo.AppSettings;
import xlogo.StyledDocument.DocumentLogo;
import xlogo.gui.components.ColorStyleSelectionPanel;
import xlogo.storage.WSManager;
import xlogo.storage.workspace.SyntaxHighlightConfig;
import xlogo.storage.workspace.WorkspaceConfig;

public class SyntaxHighlightingTab extends AbstractWorkspacePanel{
	private JPanel component;
	
	private JLabel workspaceLabel;
	private JComboBox workspaceSelection;
	private ColorStyleSelectionPanel commentStyleSelection;
	private ColorStyleSelectionPanel braceStyleSelection;
	private ColorStyleSelectionPanel primitiveStyleSelection;
	private ColorStyleSelectionPanel operandStyleSelection;
	private JCheckBox activateHighlightingCheckBox;
	private JLabel activateHighlightingLabel;
	private JButton restoreDefaultsButton;
	private DocumentLogo previewLogoDocument;
	private JTextPane previewTextPane;

	private ActionListener syntaxHighlightChangeListener;
	
	public SyntaxHighlightingTab() {
		super();
	}
	
	public JComponent getComponent()
	{
		return component;
	}
	
	protected void initComponent()
	{
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		//Font font = wc.getFont();
		component = new JPanel();
		workspaceLabel = new JLabel();
		workspaceSelection = new JComboBox();
		commentStyleSelection=new ColorStyleSelectionPanel(wc.getCommentColor(), wc.getCommentStyle(), translate("pref.highlight.comment"));
		braceStyleSelection=new ColorStyleSelectionPanel(wc.getBraceColor(), wc.getBraceStyle(), translate("pref.highlight.parenthesis"));
		primitiveStyleSelection=new ColorStyleSelectionPanel(wc.getPrimitiveColor(), wc.getPrimitiveStyle(), translate("pref.highlight.primitive"));
		operandStyleSelection=new ColorStyleSelectionPanel(wc.getOperatorColor(), wc.getOperatorStyle(), translate("pref.highlight.operand"));

		previewTextPane=new JTextPane();
		activateHighlightingCheckBox = new JCheckBox();
		activateHighlightingLabel=new JLabel();
		restoreDefaultsButton=new JButton();
		
		//activateHighlightingLabel.setFont(font);
		//restoreDefaultsButton.setFont(font);
        previewTextPane.setOpaque(true);
        previewTextPane.setBackground(Color.white);
        previewLogoDocument=new DocumentLogo();
        previewTextPane.setDocument(previewLogoDocument);
        previewLogoDocument.setColore_Parenthese(true);

		populateWorkspaceList();
		setValues();
	}
	
	protected void layoutComponent()
	{
		component.add(workspaceLabel);
		component.add(workspaceSelection);
        component.add(activateHighlightingCheckBox);
        component.add(activateHighlightingLabel);
        component.add(commentStyleSelection.getComponent());
        component.add(primitiveStyleSelection.getComponent());
        component.add(operandStyleSelection.getComponent());
        component.add(braceStyleSelection.getComponent());
        component.add(previewTextPane);
        component.add(restoreDefaultsButton);
        
		GroupLayout groupLayout = new GroupLayout(component);
		component.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup()
							.addComponent(workspaceLabel)
							.addComponent(workspaceSelection))
					.addGroup(groupLayout.createParallelGroup()
						.addComponent(activateHighlightingLabel)
						.addComponent(activateHighlightingCheckBox))
					.addComponent(commentStyleSelection.getComponent())
					.addComponent(primitiveStyleSelection.getComponent())
					.addComponent(operandStyleSelection.getComponent())
					.addComponent(braceStyleSelection.getComponent())
					.addComponent(previewTextPane)
					.addComponent(restoreDefaultsButton)
				);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup()
					.addGroup(groupLayout.createSequentialGroup()
							.addComponent(workspaceLabel)
							.addComponent(workspaceSelection))
					.addGroup(groupLayout.createSequentialGroup()
							.addComponent(activateHighlightingLabel)
							.addComponent(activateHighlightingCheckBox))
					.addComponent(commentStyleSelection.getComponent())
					.addComponent(primitiveStyleSelection.getComponent())
					.addComponent(operandStyleSelection.getComponent())
					.addComponent(braceStyleSelection.getComponent())
					.addComponent(previewTextPane)
					.addComponent(restoreDefaultsButton)
				);
	}
	
	protected void setText()
	{
		workspaceLabel.setText(translate("ws.settings.workspace"));
		previewTextPane.setText(translate("pref.highlight.example"));
		commentStyleSelection.setTitle(translate("pref.highlight.comment"));
		braceStyleSelection.setTitle(translate("pref.highlight.parenthesis"));
		primitiveStyleSelection.setTitle(translate("pref.highlight.primitive"));
		operandStyleSelection.setTitle(translate("pref.highlight.operand"));
		activateHighlightingLabel.setText(translate("pref.highlight.enabled"));
		restoreDefaultsButton.setText(translate("pref.highlight.init"));
		previewTextPane.setText(translate("pref.highlight.example"));
	}
	
	@Override
	protected void initEventListeners()
	{
		super.initEventListeners();
		activateHighlightingCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					WSManager.getWorkspaceConfig().setSyntaxHighlightingEnabled(activateHighlightingCheckBox.isSelected());
				}
			});
		restoreDefaultsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SyntaxHighlightConfig syntaxStyles = new SyntaxHighlightConfig();
					WSManager.getWorkspaceConfig().setSyntaxHighlightConfig(syntaxStyles);
					setValues();
				}
			});
		AppSettings.getInstance().addSyntaxHighlightStyleChangeListener(
				syntaxHighlightChangeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSyntaxHighlightingPreview();
			}
		});
		
		operandStyleSelection.addStyleChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				wc.setOperandColor(operandStyleSelection.color());
				wc.setOperandStyle(operandStyleSelection.style());
			}
		});
		
		braceStyleSelection.addStyleChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				wc.setBraceColor(braceStyleSelection.color());
				wc.setBraceStyle(braceStyleSelection.style());
			}
		});
		
		primitiveStyleSelection.addStyleChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				wc.setPrimitiveColor(primitiveStyleSelection.color());
				wc.setPrimitiveStyle(primitiveStyleSelection.style());
			}
		});
		
		commentStyleSelection.addStyleChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				WorkspaceConfig wc = WSManager.getWorkspaceConfig();
				wc.setCommentColor(commentStyleSelection.color());
				wc.setCommentStyle(commentStyleSelection.style());
			}
		});
	}
	
	@Override
	public void stopEventListeners()
	{
		super.stopEventListeners();
		AppSettings.getInstance().removeSyntaxHighlightStyleChangeListener(syntaxHighlightChangeListener);
		
	}

	@Override
	protected JComboBox getWorkspaceSelection() {
		return workspaceSelection;
	}

	@Override
	protected void setValues() {
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		commentStyleSelection.setColorAndStyle(wc.getCommentColor(), wc.getCommentStyle());
		braceStyleSelection.setColorAndStyle(wc.getBraceColor(), wc.getBraceStyle());
		primitiveStyleSelection.setColorAndStyle(wc.getPrimitiveColor(), wc.getPrimitiveStyle());
		operandStyleSelection.setColorAndStyle(wc.getOperatorColor(), wc.getOperatorStyle());
		
		updateSyntaxHighlightingPreview();
	}
	
	protected void updateSyntaxHighlightingPreview()
	{
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();

		boolean isHighlightingEnabled = wc.isSyntaxHighlightingEnabled();
		activateHighlightingCheckBox.setSelected(isHighlightingEnabled);
		commentStyleSelection.setEnabled(isHighlightingEnabled);
		primitiveStyleSelection.setEnabled(isHighlightingEnabled);
		braceStyleSelection.setEnabled(isHighlightingEnabled);
		operandStyleSelection.setEnabled(isHighlightingEnabled);
		restoreDefaultsButton.setEnabled(isHighlightingEnabled);
		previewLogoDocument.setColoration(isHighlightingEnabled);
		
		previewLogoDocument.initStyles(
				wc.getCommentColor(), wc.getCommentStyle(),
				wc.getPrimitiveColor(), wc.getPrimitiveStyle(),
				wc.getBraceColor(), wc.getBraceStyle(),
				wc.getOperatorColor(), wc.getOperatorStyle());
		
		previewTextPane.setText(translate("pref.highlight.example"));
	}

	@Override
	protected void enableComponents() {
		
	}

	@Override
	protected void disableComponents() {
		
	}

}
