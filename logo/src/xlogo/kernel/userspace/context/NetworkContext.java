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

package xlogo.kernel.userspace.context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import xlogo.Logo;
import xlogo.kernel.userspace.files.LogoFile;

/**
 * The network context is very different compared to the other contexts. <br>
 * 1. It parses its contents from a serialized context string ({@link #toString()}) <br>
 * 2. Its files are created virtually ({@link xlogo.storage.Storable#isVirtual()}). They are gone when the network mode stops, leaving no trace. <br>
 * 3. {@link #fireFileEvents()}} and {@link fireProcedureEvents()}} suggest the managers to not fire events,
 * because the network files are not meant to be displayed in the editor.
 * In XLogo this effect was achieved by setting the property affichable of procedures.
 * @author Marko Zivkovic
 */
public class NetworkContext extends LogoContext
{
	
	public NetworkContext(String networkContext) throws IllegalArgumentException
	{
		super();
		setContext(networkContext);
	}
	
	/**
	 * This is used to include a remote context into XLogo4Schools and allow remote
	 * procedure calls via tcp. XLogo had this implemented in the Workspace class.
	 * <p>
	 * Initially, in remote mode, the workspace was temporarily replaced by a new one.
	 * To indicate that the workspace with its procedures should not be displayed
	 * in the editor, Loic had added setAffichable(false), meaning not displayable.<br>
	 * 
	 * Note that in XLogo, the workspace contained exactly one file. Therefore
	 * a simple swap in swap would do. However, in XLogo4Schools, we need more 
	 * state information, and some of the state must be preserved.
	 * For example, in network mode, I do not want the user's procedures
	 * to be removed from the procedure list. I just want to disable the list.<br>
	 * <p>
	 * With this Network context, no events will be fired that would indicate,
	 * that some procedures or files have changed. But the ContextChangedListeners
	 * will be notified that we are now in network mode, thus they can disable
	 * the files list.
	 * <p>
	 * <p>
	 * Note that in XLogo, the workspace text has to be set in the editor just
	 * because analysProcedure() reads the Logo source code from there.
	 * In the network mode, It was actually not necessary to have it in the editor,
	 * because they didn't intend do display the text anyway (setAffichable(false)).
	 * setWorkspace and toString have probably been added late, so they just "hacked"
	 * that new property in.
	 * The existing "architecture" did not allow a cleaner extension of the
	 * system, so they must have added "affichable" for this only purpose.
	 * <p>
	 * In the current implementation, "affichable" is completely removed. <br>
	 * I regulate this effect by either firing events or not.
	 * <p>
	 * Note, parsing of the context is inherited from XLogo. Its implementation is very optimistic.
	 * It assumes that only well-formed strings are provided. Therefore it throws no actual exceptions.
	 * I added IllegalArgumentException to provide a more general interface,
	 * and to prepare clients for the future (?) when this parsing is re-implemented.
	 * 
	 * @param app
	 * @param wp
	 * @author Marko Zivkovic, Loic
	 */
	protected void setContext(String wp) throws IllegalArgumentException
	{
		StringReader sr = new StringReader(wp);
		BufferedReader bfr = new BufferedReader(sr);
		try
		{
			String input = parseVariables(bfr);
			
			if (input != null)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(input);
				sb.append("\n");
				readerToBuilder(sb, bfr);
				
				String vFileName = "netowork_file";
				createFile(vFileName, "");
				LogoFile file = getFilesTable().get(vFileName);
				file.setText(sb.toString());
			}
		}
		catch (IOException e)
		{}
		finally
		{
			try
			{
				bfr.close();
			}
			catch (IOException e)
			{}
			sr.close();
		}
	}
	
	/**
	 * append all lines of br to sb
	 */
	private void readerToBuilder(StringBuilder sb, BufferedReader br) throws IOException
	{
		String line = null;
		while((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
	}
	
	private String parseVariables(BufferedReader bfr) throws IOException, IllegalArgumentException
	{
		String input = "";
		while ((input = bfr.readLine()) != null)
		{
			if (!input.startsWith(Logo.messages.getString("pour")))
			{
				String var = input.substring(1); // - expected
				String value = bfr.readLine();
				getGlobals().define(var, value);
			}
			else
				break;
		}
		return input;
	}

	/**
	 * Files created in network mode are virtual.
	 */
	@Override
	public void createFile(String fileName, String text) throws IllegalArgumentException
	{
		LogoFile vFile = LogoFile.createNewVirtualFile(fileName);
		getFilesTable().put(fileName, vFile);
		
		LogoFile file = LogoFile.createNewVirtualFile(fileName);
		file.setText(text);
		
		getFilesTable().put(fileName, file);
		
		installListeners(file);
	}
	
	/**
	 * @return In network mode : false : {@link FileContainerChangeListener} events will not be fired.
	 */
	public boolean fireFileEvents()
	{
		return false;
	}

	/**
	 * @return In network mode : false : {@link AmbiguityListener},
	 * {@link xlogo.interfaces.ExecutablesChangedListener},
	 * {@link xlogo.interfaces.ProceduresDefinedListener} events will not be fired.
	 */
	public boolean fireProcedureEvents()
	{
		return false;
	}

	/**
	 * The Network context suggest that the user should not be allowed to create files from the gui.
	 * @see LogoContext#isFilesListEditAllowed()
	 */
	@Override
	public boolean isFilesListEditAllowed()
	{
		return false;
	}
}
