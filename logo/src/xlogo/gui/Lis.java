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
/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author Loïc Le Coq
 */
// Frame for the primitive "read"


import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.HeadlessException;
import javax.swing.JButton;
import java.awt.event.*;
import java.awt.*;
import xlogo.utils.Utils;
import xlogo.Logo;
public class Lis extends JFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
  private JTextField texte=new JTextField();
  private JButton ok=new JButton(Logo.messages.getString("pref.ok"));
  public Lis() throws HeadlessException {
  }
  public Lis(String titre,int longueur){
    setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("icone.png")));
    getContentPane().setLayout(new BorderLayout());
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().add(ok,BorderLayout.EAST);
    getContentPane().add(texte,BorderLayout.CENTER);
    texte.setPreferredSize(new Dimension(longueur,50));
    ok.setPreferredSize(new Dimension(75,50));
    texte.addActionListener(this);
    ok.addActionListener(this);
    pack();
    setTitle(titre);
	Dimension d=Toolkit.getDefaultToolkit().getScreenSize().getSize();
	int x=(int)(d.getWidth()/2-longueur/2);
	int y=(int)(d.getHeight()/2-25);
	setLocation(x,y);
    setVisible(true);
    texte.requestFocus();
  }
  public void actionPerformed(ActionEvent e){
    setVisible(false);
  }
  public String getText(){
  	return texte.getText();
  }
}