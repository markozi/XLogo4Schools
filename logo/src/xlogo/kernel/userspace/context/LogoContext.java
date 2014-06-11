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

package xlogo.kernel.userspace.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xlogo.Logo;
import xlogo.interfaces.ProcedureMapper.ProcedureMapListener;
import xlogo.kernel.gui.GuiMap;
import xlogo.kernel.userspace.GlobalVariableTable;
import xlogo.kernel.userspace.PropertyListTable;
import xlogo.kernel.userspace.files.LogoFile;
import xlogo.kernel.userspace.procedures.Procedure;
import xlogo.storage.global.GlobalConfig;
/**
 * A LogoContext contains all the symbol tables for execution of Logo programs <p>
 * Parts of this resemble the old Workspace class, but this has a new purpose and it is used differently.
 * In XLogo4Schools, a LogoContext is only a container for a Logo environment - all the symbol tables.
 * <p>
 * Note: To be consistent, the execution stack should be included in the context too, but this would (for now) need too much work
 * to refactor the existing interpreter. It is for sure something that should be done in the future, when the entire interpreter is refactored.
 * 
 * @author Marko Zivkovic - 
 * @author Loïc Le Coq - methods inherited from XLogo are marked with author-tag
 */
public class LogoContext
{	
	private LogoFile openFile;
	
	private final GlobalVariableTable							globals				= new GlobalVariableTable();
	
	private final PropertyListTable								propertyLists		= new PropertyListTable();
	
	private final Map<String, LogoFile>							files				= new HashMap<String, LogoFile>();
	
	private final HashMap<String, HashMap<String, Procedure>>	procedureTable		= new HashMap<String, HashMap<String, Procedure>>();
	
	// private Map<String, Procedure> executables = new HashMap<String,
	// Procedure>();
	
	private final GuiMap										guiMap				= new GuiMap();
			
	public LogoContext() { }

	/*
	 * Symbol table getters.
	 */
	
	/**
	 * All the files in this context
	 * @return
	 */
	public Map<String, LogoFile>	 getFilesTable()
	{
		return files;
	}
	
	/**
	 * All Executable procedures from all files. Procedures with equal names,
	 * from different files, will be included in the same list. <br>
	 * This Map is used to keep track of all defined procedures and to resolve
	 * name conflicts.
	 */
	public HashMap<String, HashMap<String, Procedure>> getProcedureTable()
	{
		return procedureTable;
	}
	
	/**
	 * Global Logo variables
	 * @return
	 */

	public GlobalVariableTable getGlobals()
	{
		return globals;
	}
	
	/**
	 * Logo property lists
	 * @return
	 */
	public PropertyListTable getPropertyLists()
	{
		return propertyLists;
	}
	
	/**
	 * For all Gui Objects (Buttons, ComboBoxes...)
	 */
	public GuiMap getGuiMap()
	{
		return guiMap;
	}
	
	/*
	 * Context dependent operations
	 */
	
	public String[] getFileOrder()
	{
		return files.keySet().toArray(new String[files.size()]);
	}
	
	public void openFile(String fileName)
	{
		openFile = files.get(fileName);
	}
	
	public void closeFile()
	{
		openFile = null;
	}
	
	public LogoFile getOpenFile()
	{
		return openFile;
	}
	
	/**
	 * This is the preferred method to create a file within a context, because different contexts prefer different LogoFile configurations.
	 * @throws IllegalStateException if fileName already exists in the files table
	 * @throws IOException If the file could not be created on the file system and the text not written.
	 */
	public void createFile(String fileName, String text) throws IOException
	{
		if (files.containsKey(fileName))
			throw new IllegalStateException("Attempt to create already existing file.");
		
		LogoFile file = LogoFile.createNewFile(fileName);
		file.setText(text);
		file.store();
		
		files.put(fileName, file);
		installListeners(file);
	}
	
	public void importFile(File path, String newFileName) throws IOException
	{
		if (files.containsKey(newFileName))
			throw new IllegalStateException("Attempt to create already existing file.");
		
		if (!path.isFile())
			throw new IllegalArgumentException("The specified file does not exist : " + path.toString());
		
		String extension = GlobalConfig.LOGO_FILE_EXTENSION;
		if(!path.getName().endsWith(extension))
			throw new IllegalArgumentException("Only accept " + extension + " files, but received : " + path.toString());

		String fileName = path.getName();
		fileName = path.getName().substring(0, fileName.length() - extension.length());
		
		LogoFile file = LogoFile.importFile(path, newFileName);
		files.put(newFileName, file);
		installListeners(file);
	}
	
	/**
	 * Note: does not perform checks on validity of file names.
	 * @param oldName
	 * @param newName
	 */
	public void renameFile(String oldName, String newName)
	{
		if (oldName.equals(newName))
			return;
		
		LogoFile file = files.get(oldName);
		
		// must first re-map in files table because file.setFileName fires events
		files.remove(oldName);
		files.put(newName, file);
		
		file.setFileName(newName);
	}
	
	private final ArrayList<ProcedureMapListener> procedureMapListener = new ArrayList<ProcedureMapListener>();

	/**
	 * This should be used when a file is created or added to this context.
	 * Listeners that were previously added with {@link #addProcedureMapListener(ProcedureMapListener)} will be installed.
	 * @param file
	 */
	public void installListeners(LogoFile file)
	{
		for (ProcedureMapListener listener : procedureMapListener)
			file.addProcedureMapListener(listener);
	}
	
	/**
	 * This helps the Procedure Manager to register to ProcedureMap events from the logo files.
	 * The ProcedureManager does not need to be notified explicitly when a new file is created.
	 * @see #installListeners(LogoFile)
	 * @param listener
	 */
	public void addProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListener.add(listener);
		for (LogoFile file : files.values())
			file.addProcedureMapListener(listener);
	}
	
	public void removeProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListener.remove(listener);
		for (LogoFile file : files.values())
			file.removeProcedureMapListener(listener);
	}
	
	/*
	 * MISC
	 */
	
	/**
	 * That's the String that is sent via TCP, and interpreted by the receiver in {@link NetworkContext},
	 * using {@link #setWorkspace(String)}
	 * @author Loïc Le Coq
	 * @author Marko Zivkovic
	 * refactored using the new data structures.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		for (String key : getGlobals().getVariables())
		{
			sb.append("-");
			sb.append(key);
			sb.append("\n");
			sb.append(getGlobals().getValue(key));
			sb.append("\n");
		}
		
		for (HashMap<String, Procedure> fileToProc : procedureTable.values())
		{
			if (fileToProc.size() != 1)
				continue;
			
			Procedure procedure = null;
			for (Procedure uniqueProc : fileToProc.values())
				procedure = uniqueProc; // retrieve the only procedure in fileToProc
			
			sb.append(Logo.messages.getString("pour") + " " + procedure.name);
			for (int j = 0; j < procedure.nbparametre; j++)
			{
				sb.append(" :" + procedure.variable.get(j));
			}
			sb.append("\n");
			sb.append(procedure.instruction);
			sb.append(Logo.messages.getString("fin"));
			sb.append("\n\n");
		}
		return (new String(sb));
	}
	
	/**
	 * @return true : {@link FileContainerChangeListener} should be fired.
	 */
	public boolean fireFileEvents()
	{
		return true;
	}

	/**
	 * @return true : {@link AmbiguityListener},
	 * {@link ExecutablesChangedListener},
	 * {@link ProceduresDefinedListener} should be fired.
	 */
	public boolean fireProcedureEvents()
	{
		return true;
	}
	
	/**
	 * Default is true. Other contexts might override this.
	 * @return Whether it is allowed to create, delete, or rename files.
	 * @note : this is only a suggestion for the files manager and the gui, modification of the table is still possible
	 */
	public boolean isFilesListEditAllowed()
	{
		return true;
	}
}
