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
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

package xlogo.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import xlogo.storage.WSManager;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;
import xlogo.Application;
import xlogo.kernel.userspace.UserSpace;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.Logo;

/**
 * Title : XLogo Description : XLogo is an interpreter for the Logo programming
 * language
 * <p>
 * <p>
 * Changes made in July 2013 by Marko Zivkovic: All the procedure analyzing is
 * moved to class 'Workspace' which was then renamed to {@link UserSpace}.
 * Reason: This is a GUI component and it should not do much model manipulation
 * things. As often in XLogo this was an issue of weak separation of concerns,
 * leading to bad modifiability, extensibility, changeability, and
 * maintainability. Better have the Workspace deal with all the workspace
 * management. For the editor, it's enough to edit text, do syntax highlighting,
 * and provide the input text to some other specialized class.
 * 
 * @author Loïc Le Coq, Marko Zivkovic
 * 
 */

/*
 * The main class for the Editor windows
 */
public class Editor implements ActionListener
{
	private JPanel				mainPanel	= new JPanel();
	private JPanel				menu		= new JPanel();
	private JButton				chercher, undo, redo;
	private JScrollPane			scroll;
	private EditorTextZone		textZone;
	// private ZoneEdition zonedition;
	
	private Application			app;
	private ReplaceFrame		sf;
	UserConfig					uc;
	
	private KeyListener logoTextAnalyzerTrigger;
	//private ArrayList<ProcedureErrorMessage> errors = new ArrayList<ProcedureErrorMessage>();

	
	public Editor(Application app)
	{
		super();
		mainPanel.setOpaque(true);
		this.app = app;
		uc = WSManager.getUserConfig();
				
		try
		{
			initGui();
		}
		catch (Exception e)
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.GENERAL_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.ERROR_WHILE_CREATING_EDITOR));
		}
		
	}
	
	public String getText()
	{
		return textZone.getText();
	}
	
	public JComponent getComponent()
	{
		return mainPanel;
	}
	
	/**
	 * Set the text of this component and displays it. <br>
	 * If the Editor was already open, it will store the old file and then
	 * display the new file.
	 * 
	 * @param file
	 */
	public void setText(String text)
	{
		textZone.clearText();
		setEditorStyledText(text);
		textZone.requestFocus();
	}
		
	/**
	 * @author Marko Zivkovic
	 */
	public void displayProcedure(String procedureName)
	{
		String to = Logo.messages.getString("pour");
		if (!textZone.find(to + " " + procedureName, false))
			textZone.find(to + " " + procedureName, true);
		
	}
		
	private void initGui() throws Exception
	{	
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		
		// Init All other components
		scroll = new JScrollPane();
		if (wc.isSyntaxHighlightingEnabled())
		{
			textZone = new EditorTextPane(this);
			textZone.getTextComponent().addKeyListener(logoTextAnalyzerTrigger);
		}
		else
			textZone = new EditorTextArea(this);
		
		sf = new ReplaceFrame(app.getFrame(), textZone);
		
		scroll.setPreferredSize(new Dimension(500, 500));
		scroll.getViewport().add(textZone.getTextComponent(), null);
		sf = new ReplaceFrame(app.getFrame(), textZone);
		
		applyLayout();
		initToolbar();
	}
	
	private void applyLayout()
	{
		GroupLayout groupLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(groupLayout);
		
		groupLayout.setAutoCreateContainerGaps(true);
		groupLayout.setAutoCreateGaps(true);
		
		groupLayout.setVerticalGroup(
				groupLayout.createSequentialGroup()
					.addComponent(menu)
					.addComponent(scroll));
		
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup()
					.addComponent(menu)
					.addComponent(scroll));
		
		FlowLayout flowLayout = new FlowLayout();
		menu.setLayout(flowLayout);
		mainPanel.add(menu);
		mainPanel.add(scroll);
	}
	
	/*
	 * Below everything is inherited from XLogo, except for minor changes due to refactoring
	 */
	
	
	private void initToolbar()
	{
		menu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		
		chercher = getIconButton("chercher.png","find");
		undo = getIconButton("undo.png","editor.undo");
		redo = getIconButton("redo.png","editor.redo");
		
		undo.setEnabled(false);
		redo.setEnabled(false);
		
		menu.add(chercher);
		menu.add(undo);
		menu.add(redo);
		
	}
	
	private JButton getIconButton(String iconName, String description)
	{
		JButton btn = new JButton();
		if (iconName != null)
			btn.setIcon(Utils.dimensionne_image(iconName, mainPanel));
		
		btn.setToolTipText(Logo.messages.getString(description));
		btn.setActionCommand(Logo.messages.getString(description));
		btn.addActionListener(this);
		return btn;
	}
	
	public void actionPerformed(ActionEvent e)
	{		
		String cmd = e.getActionCommand();
		if (cmd.equals(Logo.messages.getString("find")))
		{
			if (!sf.isVisible())
			{
				sf.setSize(350, 350);
				sf.setVisible(true);
			}
		}
		// Undo Action
		else if (cmd.equals(Logo.messages.getString("editor.undo")))
		{
			textZone.getUndoManager().undo();
			updateUndoRedoButtons();
		}
		// Redo Action
		else if (cmd.equals(Logo.messages.getString("editor.redo")))
		{
			textZone.getUndoManager().redo();
			updateUndoRedoButtons();
		}
	}
	
	// Change Syntax Highlighting for the editor
	public void initStyles(int c_comment, int sty_comment, int c_primitive, int sty_primitive, int c_parenthese,
			int sty_parenthese, int c_operande, int sty_operande)
	{
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		
		if (textZone.supportHighlighting())
		{
			((EditorTextPane) textZone).getDsd().initStyles(wc.getCommentColor(), wc.getCommentStyle(),
					wc.getPrimitiveColor(), wc.getPrimitiveStyle(), wc.getBraceColor(),
					wc.getBraceStyle(), wc.getOperandColor(), wc.getOperandStyle());
		}
	}
	
	// Enable or disable Syntax Highlighting
	/*
	 * public void setColoration(boolean b){ if (textZone.supportHighlighting())
	 * ((EditorTextPane)textZone).getDsd().setColoration(b); }
	 */
	public void setEditorFont(Font f)
	{
		textZone.setFont(f);
	}
	
	/**
	 * Erase all text
	 */
	public void clearText()
	{
		textZone.clearText();
	}
	
	/**
	 * Convert the textZone from a JTextArea to a JTextPane To allow Syntax
	 * Highlighting
	 */
	public void toTextPane()
	{
		textZone.getTextComponent().removeKeyListener(logoTextAnalyzerTrigger);
		scroll.getViewport().removeAll();// .remove(textZone.getTextComponent());
		String s = textZone.getText();
		textZone = new EditorTextPane(this);
		sf = new ReplaceFrame(app.getFrame(), textZone);
		textZone.ecris(s);
		textZone.getTextComponent().addKeyListener(logoTextAnalyzerTrigger);
		scroll.getViewport().add(textZone.getTextComponent());
		scroll.revalidate();
	}
	
	/**
	 * Convert the textZone from a JTextPane to a JTextArea Cause could be that:
	 * - Syntax Highlighting is disabled - Large text to display in the editor
	 */
	public void toTextArea()
	{
		/*
		 * Marko : for large text or disabled highlighting, we also don't want error messages to be produced while typing.
		 * 
		 */
		textZone.getTextComponent().removeKeyListener(logoTextAnalyzerTrigger); 
		String s = textZone.getText();
		scroll.getViewport().removeAll();// .remove(textZone.getTextComponent());
		textZone = new EditorTextArea(this);
		sf = new ReplaceFrame(app.getFrame(), textZone);
		textZone.ecris(s);
		scroll.getViewport().add(textZone.getTextComponent());
		scroll.revalidate();
	}
	
	/**
	 * Inserts the text at the current caret position.
	 * 
	 * @param txt
	 */
	public void setEditorStyledText(String txt)
	{
		if (txt.length() < 100000)
		{
			textZone.ecris(txt);
		}
		else
		{
			if (textZone instanceof EditorTextPane)
			{
				WSManager.getWorkspaceConfig().setSyntaxHighlightingEnabled(false);
				toTextArea();
				textZone.ecris(txt);
			}
			else
				textZone.ecris(txt);
		}
	}
	
	/**
	 * append a procedure to the end of the document.
	 * 
	 * @param program
	 * @author Marko Zivkovic
	 */
	public void append(String procedure)
	{
		if (procedure.length() < 100000)
		{
			textZone.append(procedure);
		}
		else
		{
			if (textZone instanceof EditorTextPane)
			{
				WSManager.getWorkspaceConfig().setSyntaxHighlightingEnabled(false);
				toTextArea();
				textZone.append(procedure);
			}
			else
				textZone.append(procedure);
		}
	}
	
	public void requestFocus()
	{
		textZone.requestFocus();
	}
		
	public void discardAllEdits()
	{
		textZone.getUndoManager().discardAllEdits();
		updateUndoRedoButtons();
	}
	
	protected void updateUndoRedoButtons()
	{
		if (textZone.getUndoManager().canRedo())
			redo.setEnabled(true);
		else
			redo.setEnabled(false);
		if (textZone.getUndoManager().canUndo())
			undo.setEnabled(true);
		else
			undo.setEnabled(false);
	}

		
}
