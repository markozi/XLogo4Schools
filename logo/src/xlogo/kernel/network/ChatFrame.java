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

package xlogo.kernel.network;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;

import java.awt.event.*;
import java.awt.Dimension;
import java.io.PrintWriter;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.BorderLayout;

import xlogo.Application;
import xlogo.storage.WSManager;
import xlogo.utils.Utils;
import xlogo.Logo;

public class ChatFrame extends JFrame implements ActionListener
{
	private static final long		serialVersionUID	= 1L;
	
	private MutableAttributeSet		local;
	
	private MutableAttributeSet		distant;
	
	private JTextPane				textPane;
	
	private DefaultStyledDocument	dsd;
	
	private JScrollPane				scroll;
	
	private JTextField				textField;
	
	private Application				app;
	
	private PrintWriter				out;
	
	protected ChatFrame(PrintWriter out, Application app)
	{
		this.app = app;
		this.out = out;
		initStyle();
		initGui();
		textField.addActionListener(this);
	}
	
	private void initStyle()
	{
		Font font = WSManager.getWorkspaceConfig().getFont();
		local = new SimpleAttributeSet();
		StyleConstants.setFontFamily(local, font.getFamily());
		StyleConstants.setForeground(local, Color.black);
		StyleConstants.setFontSize(local, font.getSize());
		
		distant = new SimpleAttributeSet();
		StyleConstants.setFontFamily(distant, font.getFamily());
		StyleConstants.setForeground(distant, Color.RED);
		StyleConstants.setFontSize(distant, font.getSize());
	}
	
	private void initGui()
	{
		setTitle(Logo.messages.getString("chat"));
		setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("icone.png")));
		dsd = new DefaultStyledDocument();
		textPane = new JTextPane();
		textPane.setDocument(dsd);
		textPane.setEditable(false);
		textPane.setBackground(new Color(255, 255, 220));
		this.getContentPane().setLayout(new BorderLayout());
		textField = new JTextField();
		java.awt.FontMetrics fm = app.getFrame().getGraphics().getFontMetrics(WSManager.getWorkspaceConfig().getFont());
		int width = fm.stringWidth(Logo.messages.getString("stop_chat")) + 30;
		if (width < 200)
			width = 200;
		
		textPane.setPreferredSize(new Dimension(width, 300));
		textField.setPreferredSize(new Dimension(width, WSManager.getWorkspaceConfig().getFont().getSize() + 10));
		scroll = new JScrollPane(textPane);
		getContentPane().add(scroll, BorderLayout.CENTER);
		getContentPane().add(textField, BorderLayout.SOUTH);
		textPane.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
		textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pack();
		setVisible(true);
	}
	
	protected void append(String sty, String st)
	{
		st += "\n";
		try
		{
			if (sty.equals("local"))
				dsd.insertString(dsd.getLength(), st, local);
			if (sty.equals("distant"))
				dsd.insertString(dsd.getLength(), ">" + st, distant);
			textPane.setCaretPosition(dsd.getLength());
			
		}
		catch (BadLocationException e)
		{}
	}
	
	protected void processWindowEvent(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			out.println(NetworkServer.END_OF_FILE);
			dispose();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String txt = textField.getText();
		append("local", txt);
		textField.setText("");
		out.println(txt);
	}
}
