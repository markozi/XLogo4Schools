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
 * modifications, extensions, refactorings migh have been applied by Marko Zivkovic 
 */

package xlogo.gui.translation;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import xlogo.Logo;
import xlogo.utils.Utils;
public class BottomPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private TranslateXLogo tx;
	private JTabbedPane jt;
	private MyTable messageTable;
	private MyTable primTable;
	private String id;
	private String action;
	private JButton searchButton;
	 private ImageIcon ichercher=Utils.dimensionne_image("chercher.png",this);

	protected BottomPanel(TranslateXLogo tx,String action, String id){
		this.tx=tx;
		this.action=action;
		this.id=id;
		initGui();
	}
	private void initGui(){
		setLayout(new BorderLayout());
		jt= new JTabbedPane();
		messageTable=new MyTable(tx,action,id,"langage");
		primTable=new MyTable(tx,action,id,"primitives");
		jt.add(primTable,Logo.messages.getString("primitives"));
		jt.add(messageTable,Logo.messages.getString("messages"));
		javax.swing.JScrollPane scroll=new javax.swing.JScrollPane(jt);
		
		add(scroll,BorderLayout.CENTER);
		searchButton=new JButton(ichercher);
		searchButton.setToolTipText(Logo.messages.getString("find"));
		searchButton.addActionListener(tx);
		searchButton.setActionCommand(TranslateXLogo.SEARCH);
		searchButton.setSize(new java.awt.Dimension(100,50));
		add(searchButton,BorderLayout.EAST);
	}
	protected String getPrimValue(int a, int b){
		return primTable.getValue(a, b);
	}
	protected String getMessageValue(int a, int b){
		String st=messageTable.getValue(a, b);
		return st;
	}
	protected MyTable getMessageTable(){
		return this.messageTable;
	}
	protected MyTable getPrimTable(){
		return this.primTable;
	}
	protected MyTable getVisibleTable(){
		if (jt.getSelectedIndex()==0) return primTable;
		return messageTable;
	}
}
