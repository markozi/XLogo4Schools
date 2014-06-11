/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Lo�c Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Z�rich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Lo�c Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were initially written by Lo�c Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

package xlogo.kernel;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import xlogo.utils.Utils;

public class MyFlowWriter extends MyFlow
{
	BufferedWriter	bfw;
	
	MyFlowWriter(MyFlow flow)
	{
		super(flow);
	}
	
	void append(String line) throws FileNotFoundException, IOException
	{
		if (null == bfw)
			bfw = new BufferedWriter(new FileWriter(getPath(), true));
		PrintWriter pw = new PrintWriter(bfw);
		pw.println(Utils.SortieTexte(line));
	}
	
	void write(String line) throws FileNotFoundException, IOException
	{
		if (null == bfw)
			bfw = new BufferedWriter(new FileWriter(getPath()));
		PrintWriter pw = new PrintWriter(bfw);
		pw.println(Utils.SortieTexte(line));
	}
	
	boolean isWriter()
	{
		return true;
	}
	
	void close() throws IOException
	{
		if (null != bfw)
			bfw.close();
	}
}
