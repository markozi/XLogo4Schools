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
import java.awt.FlowLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import xlogo.Logo;
public class TopPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private TranslateXLogo tx;
	private JTextArea area;
	private JButton sendButton;
	protected TopPanel(TranslateXLogo tx){
		this.tx=tx;
		initGui();
	}
	private void initGui(){
		setLayout(new FlowLayout());
		area=new JTextArea(Logo.messages.getString("translatemessage"));
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		sendButton=new JButton(Logo.messages.getString("pref.ok"));

		
		area.setEditable(false);
		sendButton.addActionListener(tx);
		sendButton.setActionCommand(TranslateXLogo.SEND);
	
		
		area.setSize(new Dimension(400,100));
		sendButton.setSize(new Dimension(50,30));
		add(area);
		add(sendButton);
	}
}
