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

package xlogo.kernel.userspace;

import java.util.HashMap;
import java.util.Set;

public class GlobalVariableTable
{
	/**
	 * All defined variables with their current value.
	 */
	protected HashMap<String, String>	globale;
	
	public GlobalVariableTable()
	{
		globale = new HashMap<String, String>();
	}
	
	public Set<String> getVariables()
	{
		return globale.keySet();
	}
	
	public String getValue(String var)
	{
		return globale.get(var.toLowerCase());
	}
	
	public void define(String var, String value)
	{
		globale.put(var.toLowerCase(), value);
	}
	
	public void deleteVariable(String st)
	{
		globale.remove(st.toLowerCase());
	}
	
	/**
	 * Delete all Variables from the workspace
	 */
	public void deleteAllVariables()
	{
		globale.clear();
	}
	
}
