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
 * Contents of this file were initially written by Loïc Le Coq,
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
public class SearchFrame extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	private final String FIND="find";
	
	private JButton find;
	private JRadioButton backward, forward;
	private JPanel buttonPanel;
	private JComboBox comboFind;
	private ButtonGroup bg;
	private JLabel labelFind,labelResult;
	private Searchable jtc;
	Highlighter.HighlightPainter cyanPainter;

	public SearchFrame(JFrame jf,Searchable jtc){
		super(jf);
		this.jtc=jtc;
		initGui();
	}	

	public void actionPerformed(ActionEvent e){
		String cmd=e.getActionCommand();
		if (cmd.equals(FIND)){
			find();

		}
	}
	private void find(){
		String element=comboFind.getSelectedItem().toString();
		// Add the element to the combobox
		addCombo(element,comboFind);
		boolean b=jtc.find(element,forward.isSelected());
		if (b) 	{
			// Found
			labelResult.setText("");
		}
		else {
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
	protected void setText(){
		Font font = WSManager.getWorkspaceConfig().getFont();
		backward.setFont(font);
		forward.setFont(font);
		find.setFont(font);
		labelFind.setFont(font);
		setFont(font);
		backward=new JRadioButton(Logo.messages.getString("backward"));
		forward=new JRadioButton(Logo.messages.getString("forward"));
		TitledBorder tb=BorderFactory.createTitledBorder(Logo.messages.getString("direction"));
		tb.setTitleFont(font);
		buttonPanel.setBorder(tb);
		find=new JButton(Logo.messages.getString("find"));
		labelFind=new JLabel(Logo.messages.getString("find")+" :");
		setTitle(Logo.messages.getString("find_replace"));
	}
	private void initGui(){
		Font font = WSManager.getWorkspaceConfig().getFont();
		
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
		tb.setTitleFont(font);
		buttonPanel.setBorder(tb);
		
		// Init Buttons
		find=new JButton(Logo.messages.getString("find"));
		find.addActionListener(this);
		find.setActionCommand(FIND);
		
		// Init JLabel and JCombobox
		labelFind=new JLabel(Logo.messages.getString("find")+" :");
		labelResult=new JLabel();

		comboFind=new JComboBox();
		comboFind.setEditable(true);

		backward.setFont(font);
		forward.setFont(font);
		find.setFont(font);
		labelFind.setFont(font);
		setFont(font);
		
		getContentPane().setLayout(new GridBagLayout());
		// Draw all
		
		getContentPane().add(labelFind, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(comboFind, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(labelResult, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 10, 10, 10), 0, 0));
		getContentPane().add(find, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(
						10, 10, 10, 10), 0, 0));
	}
}
