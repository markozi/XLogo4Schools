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

import xlogo.kernel.LogoError;

import java.util.HashMap;

import xlogo.Logo;

public class GuiMap extends HashMap<String, GuiComponent>
{
	private static final long	serialVersionUID	= 1L;
	
	public GuiMap()
	{
	}
	
	public void put(GuiComponent gc) throws LogoError
	{
		if (this.containsKey(gc.getId()))
		{
			throw new LogoError(Logo.messages.getString("gui_exists") + " " + gc.getId());
		}
		else
			this.put(gc.getId(), gc);
	}
	
	public GuiComponent get(Object key)
	{
		String k = key.toString().toLowerCase();
		return super.get(k);
	}
	
	/*
	 * public void remove(GuiComponent gc) throws myException{
	 * if (this.containsKey(gc.getId())){
	 * this.remove(gc.getId());
	 * }
	 * else{
	 * throw new
	 * myException(app,Logo.messages.getString("no_gui")+" "+gc.getId());
	 * }
	 * }
	 */
}
