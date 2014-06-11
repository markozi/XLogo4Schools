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

package xlogo.kernel.gui;

import javax.swing.JComboBox;

import xlogo.storage.WSManager;
import xlogo.utils.Utils;

import java.util.StringTokenizer;

import xlogo.kernel.Interprete;

import java.awt.event.*;

import xlogo.Application;

public class GuiMenu extends GuiComponent
{
	private Application		app;
	private String[]		item;
	private StringBuffer[]	action;
	
	public GuiMenu(String id, String text, Application app)
	{
		this.app = app;
		setId(id);
		StringTokenizer st = new StringTokenizer(text);
		item = new String[st.countTokens()];
		action = new StringBuffer[st.countTokens()];
		int i = 0;
		originalWidth = 0;
		while (st.hasMoreTokens())
		{
			item[i] = Utils.SortieTexte(st.nextToken());
			java.awt.FontMetrics fm = app.getFrame().getGraphics()
					.getFontMetrics(WSManager.getWorkspaceConfig().getFont());
			originalWidth = Math.max(originalWidth, fm.stringWidth(item[i]));
			i++;
		}
		originalWidth += 50;
		guiObject = new JComboBox(item);
		originalHeight = WSManager.getWorkspaceConfig().getFont().getSize() + 10;
		setSize(originalWidth, originalHeight);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		// System.out.println("coucou");
		int select = ((JComboBox) guiObject).getSelectedIndex();
		if (!app.commande_isEditable())
		{
			Interprete.actionInstruction.append(action[select]);
		}
		else
		{
			app.startInterpretation(action[select]);
		}
	}
	
	public boolean isButton()
	{
		return false;
	}
	
	public boolean isMenu()
	{
		return true;
	}
	
	public void setAction(StringBuffer action, int id)
	{
		this.action[id] = action;
	}
}
