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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import xlogo.MenuListener;
import xlogo.utils.Utils;

public class MyToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	private MenuListener menulistener;
	  private ImageIcon izoomin=Utils.dimensionne_image("zoomin.png",this);
	  private ImageIcon izoomout=Utils.dimensionne_image("zoomout.png",this);
	  private ImageIcon icopier=Utils.dimensionne_image("editcopy.png",this);
	  private ImageIcon icoller=Utils.dimensionne_image("editpaste.png",this);
	  private ImageIcon icouper=Utils.dimensionne_image("editcut.png",this);
	  private ImageIcon iplay=Utils.dimensionne_image("play.png",this);
	  //private ImageIcon iturtleProp=new ImageIcon(Utils.dimensionne_image("turtleProp.png", this));
	  private JButton zoomin=new JButton(izoomin);
	  private JButton zoomout=new JButton(izoomout);
	  private JButton copier=new JButton(icopier);
	  private JButton coller=new JButton(icoller);
	  private JButton couper=new JButton(icouper);
	  private JButton play=new JButton(iplay);
	  //private JButton turtleProp=new JButton(iturtleProp);
	  
	  
	public MyToolBar(MenuListener menulistener){
		super(JToolBar.VERTICAL);
		this.menulistener=menulistener;
		initGui();
	}
	
	private void initGui(){
		zoomin.addActionListener(menulistener);
		zoomin.setActionCommand(MenuListener.ZOOMIN);
		zoomout.addActionListener(menulistener);
		zoomout.setActionCommand(MenuListener.ZOOMOUT);
		copier.addActionListener(menulistener);
		copier.setActionCommand(MenuListener.EDIT_COPY);
		couper.addActionListener(menulistener);
		couper.setActionCommand(MenuListener.EDIT_CUT);
		coller.addActionListener(menulistener);
		coller.setActionCommand(MenuListener.EDIT_PASTE);
		play.addActionListener(menulistener);
		play.setActionCommand(MenuListener.PLAY);
/*		slider= new JSlider(JSlider.VERTICAL);
		slider.setValue(slider.getMaximum()-Config.turtleSpeed);
		//Create the label table
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer( 0 ), new JLabel("Slow") );
		labelTable.put( new Integer( 100 ), new JLabel("Fast") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
		/*	slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);

		slider.setSnapToTicks(true);
		slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
			    JSlider source = (JSlider)e.getSource();
			    int value=source.getValue();
			    Config.turtleSpeed=source.getMaximum()-value;			
			}
		});
		int width=Toolkit.getDefaultToolkit().getScreenSize().width;
		width=32*width/1024;
		slider.setMinimumSize(new java.awt.Dimension(width,200));
		slider.setMaximumSize(new java.awt.Dimension(width,200));
		*/
		add(zoomin);
		addSeparator();
		add(zoomout);
		addSeparator();
		add(couper);
		addSeparator();
		add(copier);
		addSeparator();
		add(coller);
		addSeparator();
		add(play);
		addSeparator();
//		add(slider);
	}
	public void enabledPlay(boolean b){
		play.setEnabled(b);
	}
	 /**
	  * Enables or disables the zoom buttons
	  * @param b The boolean  
	  */
	public void setZoomEnabled(boolean b){
		zoomin.setEnabled(b);
		zoomout.setEnabled(b);
//		repaint();
	}
}
