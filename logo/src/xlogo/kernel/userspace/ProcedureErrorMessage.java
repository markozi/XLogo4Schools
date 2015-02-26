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

import java.util.ArrayList;
import java.util.Collection;

import xlogo.Logo;
import xlogo.kernel.Primitive;
import xlogo.kernel.userspace.procedures.ProcedureErrorType;

/**
 * This is used to report document structure errors
 * or ambiguity errors, either within or among files.
 * @author Marko Zivkovic
 *
 */
public class ProcedureErrorMessage
{
	ProcedureErrorType type;
	String procedureDescription;
	Collection <String> fileNames;
	
	/**
	 * The description may be either the name (if known) or the line, where the error was found.
	 * @param type
	 * @param procedureDescription
	 * @param fileName
	 */
	public ProcedureErrorMessage(ProcedureErrorType type, String procedureDescription, String fileName)
	{
		this.type = type;
		this.procedureDescription = procedureDescription;
		this.fileNames = new ArrayList<String>();
		this.fileNames.add(fileName);
	}
	
	/**
	 * This can be used for ambiguity messages
	 * @see #ProcedureErrorMessage(ProcedureErrorType, String, String)
	 */
	public ProcedureErrorMessage(ProcedureErrorType type, String procedureDescription, Collection<String> fileNames)
	{		
		this.type = type;
		this.procedureDescription = procedureDescription;
		this.fileNames = fileNames;
	}
	
	public ProcedureErrorType getErrorType()
	{
		return type;
	}
	
	public String getProcedureDescription()
	{
		return procedureDescription;
	}
	
	public Collection<String> getFileNames()
	{
		return fileNames;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for (String fileName : fileNames)
		{
			sb.append(fileName);
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length()-1);
		
		sb.append(procedureDescription);
		sb.append(": ");
		sb.append(Logo.messages.getString(type.getDescription())
				.replace("{to}", Primitive.TO)
				.replace("{end}", Primitive.END));
		
		return sb.toString();
	}

}