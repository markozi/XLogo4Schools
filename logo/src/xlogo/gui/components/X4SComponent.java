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
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.gui.components;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Base Class for well structured GUI components.
 * includes commonly used features for displaying popups and retrieving user input per dialogs.
 * @author Marko
 *
 */
public abstract class X4SComponent extends X4SGui{

	public X4SComponent() {
		super();
	}
	
	public abstract JComponent getComponent();
		
	protected String getUserText(String message, String title)
	{
		return (String) JOptionPane.showInputDialog(
                getComponent(),
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
	}
	
	protected boolean getUserYesOrNo(String message, String title)
	{
		int ans = JOptionPane.showConfirmDialog(getComponent(), message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		return ans == JOptionPane.YES_OPTION;
	}
	
	protected File getUserSelectedDirectory()
	{
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(getComponent());
		
		if(returnVal == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile();
		else
			return null;
	}

}
