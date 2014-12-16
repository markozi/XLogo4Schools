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
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.kernel.userspace;

import java.util.HashMap;
import java.util.Set;

public class PropertyListTable
{

	/**
	 * For all Property Lists
	 */
	private HashMap<String, HashMap<String, String>>	propList;
	
	public PropertyListTable()
	{
		propList = new HashMap<String, HashMap<String,String>>();
	}
	

	/**
	 * This method adds in the property List called "name" a value for the
	 * corresponding key
	 * 
	 * @param name
	 *            The property List 's name
	 * @param key
	 *            The key for the value to add
	 * @param value
	 *            The value to add
	 */
	public void addPropList(String name, String key, String value)
	{
		if (!propList.containsKey(name))
		{
			propList.put(name, new HashMap<String, String>());
		}
		propList.get(name).put(key, value);
	}
	
	/**
	 * This method removes a Property List
	 * 
	 * @param name
	 *            The property List 's name
	 */
	public void removePropList(String name)
	{
		if (propList.containsKey(name))
		{
			propList.remove(name);
		}
	}
	
	/**
	 * This method removes a couple (key, value) from a Property List
	 * 
	 * @param name
	 *            The property List 's name
	 * @param key
	 *            The key to delete
	 */
	public void removePropList(String name, String key)
	{
		if (propList.containsKey(name))
		{
			if (propList.get(name).containsKey(key))
				propList.get(name).remove(key);
			if (propList.get(name).isEmpty())
				propList.remove(name);
		}
	}
	
	/**
	 * This method returns a list that contains all couple key value
	 * 
	 * @param name
	 *            The Property List's name
	 * @return A list with all keys-values
	 */
	public String displayPropList(String name)
	{
		if (propList.containsKey(name))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("[ ");
			Set<String> set = propList.get(name).keySet();
			for (String key : set)
			{
				sb.append(key);
				sb.append(" ");
				String value = propList.get(name).get(key);
				if (value.startsWith("\""))
					value = value.substring(1);
				sb.append(value);
				sb.append(" ");
			}
			sb.append("] ");
			return sb.toString();
		}
		else
			return "[ ] ";
	}
	
	/**
	 * This method return a value from a Property List
	 * 
	 * @param name
	 *            The Property List's name
	 * @param key
	 *            The key for the chosen value
	 * @return The value for this key
	 */
	public String getPropList(String name, String key)
	{
		if (!propList.containsKey(name)) { return "[ ]"; }
		if (!propList.get(name).containsKey(key))
			return "[ ]";
		return propList.get(name).get(key);
	}
	
	/**
	 * Returns all defined Property List names
	 * 
	 * @return A list with all Property List names
	 */
	public Set<String> getPropListKeys()
	{
		return propList.keySet();
	}
	
	/**
	 * Delete all property Lists from the workspace
	 */
	public void deleteAllPropertyLists()
	{
		propList.clear();
	}
}
