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

package xlogo.kernel.gui;

import javax.swing.JButton;

import xlogo.kernel.Interprete;

import java.awt.Font;
import java.awt.event.*;

import xlogo.Application;
import xlogo.storage.WSManager;
import xlogo.utils.Utils;

public class GuiButton extends GuiComponent
{
	
	private StringBuffer	action;
	
	private Application		app;
	
	public GuiButton(String id, String text, Application app)
	{
		Font font = WSManager.getWorkspaceConfig().getFont();
		super.setId(id);
		guiObject = new JButton(Utils.SortieTexte(text));
		this.app = app;
		java.awt.FontMetrics fm = app.getFrame().getGraphics().getFontMetrics(font);
		originalWidth = fm.stringWidth(((JButton) (getGuiObject())).getText()) + 50;
		originalHeight = font.getSize() + 10;
		setSize(originalWidth, originalHeight);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (!app.commande_isEditable())
		{
			Interprete.actionInstruction.append(action);
		}
		else
		{
			app.startInterpretation(action);
		}
	}
	
	public boolean isButton()
	{
		return true;
	}
	
	public boolean isMenu()
	{
		return false;
	}
	
	/**
	 * @param action
	 * @uml.property name="action"
	 */
	public void setAction(StringBuffer action)
	{
		this.action = action;
	}
	
}
