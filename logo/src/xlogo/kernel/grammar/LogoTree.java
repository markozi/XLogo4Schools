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

package xlogo.kernel.grammar;

import java.util.Vector;

public class LogoTree
{
	private Vector<LogoTree>	children;
	private LogoTree			parent;
	private LogoType			value;
	private boolean				isRoot		= false;
	private boolean				isProcedure	= false;
	private boolean				isPrimitive	= false;
	private boolean				isLeaf		= false;
	
	LogoTree()
	{
		children = new Vector<LogoTree>();
	}
	
	protected void setParent(LogoTree lt)
	{
		this.parent = lt;
	}
	
	protected LogoTree getParent()
	{
		return parent;
	}
	
	protected boolean isRoot()
	{
		return isRoot;
	}
	
	protected void addChild(LogoTree child)
	{
		children.add(child);
	}
	
	protected void setValue(LogoType value)
	{
		this.value = value;
	}
	
	protected LogoType getValue()
	{
		return value;
	}
	
	protected boolean isLeaf()
	{
		return isLeaf;
	}
	
	LogoType evaluate()
	{
		Vector<LogoType> args = new Vector<LogoType>();
		for (int i = 0; i < children.size(); i++)
		{
			LogoTree child = children.get(i);
			if (child.isLeaf())
				args.add(child.getValue());
			else
				args.add(child.evaluate());
		}
		return null;
	}
}
