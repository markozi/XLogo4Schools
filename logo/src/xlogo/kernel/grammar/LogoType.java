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

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * Licence : GPL
 * 
 * @author Loïc Le Coq
 */

package xlogo.kernel.grammar;

public abstract class LogoType
{
	
	/**
	 * If this token is a word ?
	 * 
	 * @return true for a word, false otherwise
	 */
	public boolean isWord()
	{
		return false;
	}
	
	/**
	 * If this token is a list?
	 * 
	 * @return true for a list, false otherwise
	 */
	public boolean isList()
	{
		return false;
	}
	
	/**
	 * If this token is a number?
	 * 
	 * @return true for a number, false otherwise
	 */
	public boolean isNumber()
	{
		return false;
	}
	
	/**
	 * If this token is a variable?
	 * 
	 * @return true for a variable, false otherwise
	 */
	public boolean isVariable()
	{
		return false;
	}
	
	/**
	 * If this token is a primitive?
	 * 
	 * @return true for a primitive, false otherwise
	 */
	public boolean isPrimitive()
	{
		return false;
	}
	
	/**
	 * If this token is a procedure?
	 * 
	 * @return true for a procedure, false otherwise
	 */
	public boolean isProcedure()
	{
		return false;
	}
	
	/**
	 * If this token is an exception?
	 * 
	 * @return true for an exception, false otherwise
	 */
	public boolean isException()
	{
		return false;
	}
	
	public boolean isNull()
	{
		return false;
	}
	
	public boolean isRightDelimiter()
	{
		return false;
	}
	
	/**
	 * Util for debugging
	 * 
	 * @return the type and value for LogoType
	 */
	abstract public String toDebug();
}
