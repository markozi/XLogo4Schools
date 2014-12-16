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

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.text.Highlighter;

import java.awt.event.*;

import javax.swing.JFrame;

import xlogo.Logo;
import xlogo.storage.WSManager;
public class ReplaceFrame extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	private final String FIND="find";
	private final String REPLACE="replace";
	private final String REPLACEALL="replaceall";
	private final String FIND_REPLACE="find_replace";
	
	private JButton find,replace,findReplace,replaceAll;
	private JRadioButton backward, forward;
	private JPanel buttonPanel;
	private JComboBox comboFind,comboReplace;
	private ButtonGroup bg;
	private JLabel labelFind, labelReplace,labelResult;
	private Searchable jtc;
	Highlighter.HighlightPainter cyanPainter;

	public ReplaceFrame(JFrame jf,Searchable jtc){
		super(jf);
		this.jtc=jtc;
		initGui();
	}	

	public void actionPerformed(ActionEvent e){
		String cmd=e.getActionCommand();
		if (cmd.equals(FIND)){
			find();

		}
		else if (cmd.equals(REPLACE)){
			replace();
		}
		else if (cmd.equals(FIND_REPLACE)){
			replace();
			find();
		}
		else if (cmd.equals(REPLACEALL)){
			replaceAll();
		}
		
	}
	private void replaceAll(){
		String substitute="";
		try {
			substitute=comboReplace.getSelectedItem().toString();	
			addCombo(substitute,comboReplace);
		}
		catch(NullPointerException e2){}
		String element=comboFind.getSelectedItem().toString();
		addCombo(element,comboFind);
		jtc.replaceAll(element, substitute);
		replace.setEnabled(false);
		findReplace.setEnabled(false);
		
	}
	private void replace(){
		String element=comboReplace.getSelectedItem().toString();
		addCombo(element,comboReplace);
		jtc.replace(element,forward.isSelected());
		replace.setEnabled(false);
		findReplace.setEnabled(false);	
	}
	private void find(){
		String element=comboFind.getSelectedItem().toString();
		// Add the element to the combobox
		addCombo(element,comboFind);
		boolean b=jtc.find(element,forward.isSelected());
		if (b) 	{
			replace.setEnabled(true);
			findReplace.setEnabled(true);
			// Found
			labelResult.setText("");
		}
		else {
			replace.setEnabled(false);
			findReplace.setEnabled(false);
			// Not found
			labelResult.setText(Logo.messages.getString("string_not_found"));
		}		
	}
	protected void processWindowEvent(WindowEvent e){
		super.processWindowEvent(e);
		if (e.getID()==WindowEvent.WINDOW_CLOSING){
			jtc.removeHighlight();
		}
	}
	private void addCombo(String element,JComboBox combo){
		boolean b=false;
		for (int i=0;i<combo.getItemCount();i++){
			if (combo.getItemAt(i).equals(element)) {
				b=true;
				break;
			}
		}
		if (!b){
			combo.insertItemAt(element, 0);
			int n=combo.getItemCount();
			if (n>10){
				combo.removeItemAt(n-1);
			}
		}
	}
	private void setFont(){
		Font font = WSManager.getWorkspaceConfig().getFont();
		find.setFont(font);
		replace.setFont(font);
		findReplace.setFont(font);
		replaceAll.setFont(font);
		backward.setFont(font);
		forward.setFont(font);
		comboFind.setFont(font);
		comboReplace.setFont(font);
		labelFind.setFont(font);
		labelReplace.setFont(font);
		labelResult.setFont(font);

		
	}
	protected void setText(){
		setFont();
		backward=new JRadioButton(Logo.messages.getString("backward"));
		forward=new JRadioButton(Logo.messages.getString("forward"));
		TitledBorder tb=BorderFactory.createTitledBorder(Logo.messages.getString("direction"));
		tb.setTitleFont(WSManager.getWorkspaceConfig().getFont());
		buttonPanel.setBorder(tb);
		find=new JButton(Logo.messages.getString("find"));
		replace=new JButton(Logo.messages.getString("replace"));
		findReplace=new JButton(Logo.messages.getString("find_replace"));
		replaceAll=new JButton(Logo.messages.getString("replaceall"));
		labelFind=new JLabel(Logo.messages.getString("find")+" :");
		labelReplace=new JLabel(Logo.messages.getString("replacewith"));
		setTitle(Logo.messages.getString("find_replace"));
	}
	private void initGui(){
		
		setTitle(Logo.messages.getString("find_replace"));
		// Init the RadioButton for the direction search
		backward=new JRadioButton(Logo.messages.getString("backward"));
		forward=new JRadioButton(Logo.messages.getString("forward"));
		forward.setSelected(true);
		bg=new ButtonGroup();
		bg.add(forward);
		bg.add(backward);
		buttonPanel=new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
		buttonPanel.add(forward);
		buttonPanel.add(backward);
		TitledBorder tb=BorderFactory.createTitledBorder(Logo.messages.getString("direction"));
		tb.setTitleFont(WSManager.getWorkspaceConfig().getFont());
		buttonPanel.setBorder(tb);
		// Init Buttons
		find=new JButton(Logo.messages.getString("find"));
		replace=new JButton(Logo.messages.getString("replace"));
		findReplace=new JButton(Logo.messages.getString("find_replace"));
		replaceAll=new JButton(Logo.messages.getString("replaceall"));
		findReplace.setEnabled(false);
		replace.setEnabled(false);
		find.addActionListener(this);
		findReplace.addActionListener(this);
		replaceAll.addActionListener(this);
		replace.addActionListener(this);
		find.setActionCommand(FIND);
		replace.setActionCommand(REPLACE);
		findReplace.setActionCommand(FIND_REPLACE);
		replaceAll.setActionCommand(REPLACEALL);
		
		// Init JLabel and JCombobox
		labelFind=new JLabel(Logo.messages.getString("find")+" :");
		labelReplace=new JLabel(Logo.messages.getString("replacewith"));
		labelResult=new JLabel();

		comboFind=new JComboBox();
		comboReplace=new JComboBox();
		comboFind.setEditable(true);
		comboReplace.setEditable(true);
		setFont();
		getContentPane().setLayout(new GridBagLayout());
		
		// Draw all
		
		getContentPane().add(labelFind, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(comboFind, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(labelReplace, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(comboReplace, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(labelResult, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(find, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(replace, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(findReplace, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(replaceAll, new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
	}
}
