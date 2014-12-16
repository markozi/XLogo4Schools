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
 * Contents of this file were adapted by Marko Zivkovic
 * 
 * The original authors:
 * 
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of Oracle or the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xlogo.gui.components;

import java.awt.*;

import javax.swing.*;

import xlogo.utils.Utils;

public class TurtleComboBox extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6921667779684536164L;
	ImageIcon[]	images;
	String[]	petStrings	= { "preview0", "preview1", "preview2", "preview3", "preview4", "preview5", "preview6" };
	
	JComboBox petList;
	/*
	 * Despite its use of EmptyBorder, this panel makes a fine content
	 * pane because the empty border just increases the panel's size
	 * and is "painted" on top of the panel's normal background.  In
	 * other words, the JPanel fills its entire background if it's
	 * opaque (which it is by default); adding a border doesn't change
	 * that.
	 */
	public TurtleComboBox()
	{
		super(new BorderLayout());
		
		//Load the pet images and create an array of indexes.
		images = new ImageIcon[petStrings.length];
		Integer[] intArray = new Integer[petStrings.length];
		for (int i = 0; i < petStrings.length; i++)
		{
			intArray[i] = new Integer(i);
			images[i] = createImageIcon(petStrings[i] + ".png");
			if (images[i] != null)
			{
				images[i].setDescription(petStrings[i]);
			}
		}
		
		//Create the combo box.
		petList = new JComboBox(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(50, 50));
		petList.setRenderer(renderer);
		petList.setMaximumRowCount(7);
		
		//Lay out the demo.
		add(petList, BorderLayout.PAGE_START);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	public JComboBox getComboBox()
	{
		return petList;
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path)
	{
		try{
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(path)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}	
	
	class ComboBoxRenderer extends JLabel implements ListCellRenderer
	{
		
		private static final long	serialVersionUID	= -2208613325470559104L;
		
		public ComboBoxRenderer()
		{
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}
		
		/*
		 * This method finds the image and text corresponding
		 * to the selected value and returns the label, set up
		 * to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)
			int selectedIndex = ((Integer) value).intValue();
			
			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			//Set the icon and text.  If icon was null, say so.
			ImageIcon icon = images[selectedIndex];
			setIcon(icon);
			
			return this;
		}
	}
}
