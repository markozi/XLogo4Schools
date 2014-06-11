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
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.kernel.userspace;

import java.util.ArrayList;
import java.util.Collection;

import xlogo.Logo;
import xlogo.interfaces.BroadcasterErrorFileContainer;
import xlogo.interfaces.X4SModeSwitcher;
import xlogo.interfaces.ErrorDetector.FileErrorCollector;
import xlogo.kernel.gui.GuiMap;
import xlogo.kernel.userspace.context.ContextManager;
import xlogo.kernel.userspace.context.LogoContext;
import xlogo.kernel.userspace.files.LogoFileContainer;
import xlogo.kernel.userspace.files.LogoFilesManager;
import xlogo.kernel.userspace.procedures.ExecutablesProvider;
import xlogo.kernel.userspace.procedures.Procedure;
import xlogo.kernel.userspace.procedures.ProceduresManager;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is a facade for what was before called Workspace in XLogo, and much more.
 * XLogo's Workspace had:
 * <p>
 * - All Defined Procedures <br>
 * - All Global variables <br>
 * - All GUI Objects (generated by a Logo program)<br>
 * - All Property lists
 * <p>
 * The new "Workspace" does not resemble the old workspace at all, although it still provides access to these symbol tables.
 * <p>
 * New in XLogo4Schools: multiple Files. Procedures must be unique within all files of a context.
 * If multiple procedures with the same name are defined <br>
 * - within one file : The file will not be usable, until errors and ambiguities are fixed (like in XLogo), <br>
 * - among multiple files : All of these files will not be executable, <br> //TODO or the ambiguous procedures will not be executed
 * unless the ambiguity is resolved.
 * <p>
 * New are also the Contexts for user mode, contest mode, network mode.
 * The contest mode is completely new. In XLogo, Network mode was implemented by replacing the whole workspace.
 * Now only the Contexts with symbol tables are replaced. Why? Because we have an event driven architecture now.
 * If the whole workspace (referring to XLogo's workspace, including event dispatchers) would be replaced, then all subscribers to the workspace would have to be re-mapped.
 * By keeping the LogoFileManager and the ProceduresManager, subscribers to the user space (which are redirected to the managers) must not be re-mapped when a context switch happens.
 * <p>
 * (Option for future: Procedures can be qualified with
 * {@code fileName.procedureName}. But : A dot is not a legal character for a
 * name anymore)
 * 
 * @author Marko Zivkovic
 * 
 */
public class UserSpace implements X4SModeSwitcher, LogoFileContainer, BroadcasterErrorFileContainer,
		ExecutablesProvider, FileErrorCollector
{

	/**
	 * This is included for every call that is delegated. This way all errors are caught and if we're lucky,
	 * The application can still be used or terminated normally. At least the user has discovered a new bug and he or she
	 * can report it.
	 * 
	 * TODO write a log file?
	 * 
	 * @param e
	 */
	public static void showErrorDialog(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		DialogMessenger.getInstance().dispatchError(Logo.messages.getString(MessageKeys.WS_ERROR_TITLE),
				sw.toString());
	}
	
	private final ContextManager	contextManager;	// Switches context when modes change, notifies the other managers. & causes mode and clock events
	private final LogoFilesManager	filesManager;		// handles file specific requests & causes file events
	private final ProceduresManager	proceduresManager;	// handles procedure specific requests & causes defined/undefined events
	private final ErrorManager		errorManager;		// handles error specific requests & causes error events
														
	public UserSpace()
	{
		contextManager = new ContextManager();
		proceduresManager = new ProceduresManager(contextManager);
		filesManager = new LogoFilesManager(contextManager);
		errorManager = new ErrorManager(filesManager, proceduresManager);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * X4S MODE SWITCHER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	public String getSerializedContext()
	{
		try
		{
			return contextManager.getSerializedContext();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return "";
	}
	
	@Override
	public boolean isNetworkMode()
	{
		try
		{
			return contextManager.isNetworkMode();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public boolean isRecordMode()
	{
		try
		{
			return contextManager.isRecordMode();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public boolean isUserMode()
	{
		try
		{
			return contextManager.isUserMode();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return true;
	}
	
	@Override
	public void startRecordMode(String[] fileNames) throws IllegalArgumentException, IOException
	{
		try
		{
			contextManager.startRecordMode(fileNames);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void stopRecordMode()
	{
		try
		{
			contextManager.stopRecordMode();
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	public void pushNetworkMode(String networkContext)
	{
		try
		{
			contextManager.pushNetworkMode(networkContext);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void popNetworkMode()
	{
		try
		{
			contextManager.popNetworkMode();
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void addModeChangedListener(ModeChangeListener listener)
	{
		try
		{
			contextManager.addModeChangedListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeModeChangedListener(ModeChangeListener listener)
	{
		try
		{
			contextManager.removeModeChangedListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void addBroadcastListener(MessageListener listener)
	{
		try
		{
			contextManager.addBroadcastListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeBroadcastListener(MessageListener listener)
	{
		try
		{
			contextManager.removeBroadcastListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * LOGO FILE CONTAINER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	public String[] getFileNames()
	{
		try
		{
			return filesManager.getFileNames();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new String[0];
	}
	
	@Override
	public String readFile(String name)
	{
		try
		{
			return filesManager.readFile(name);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return "";
	}
	
	@Override
	public String getOpenFileName()
	{
		try
		{
			return filesManager.getOpenFileName();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return null;
	}
	
	@Override
	public boolean existsFile(String name)
	{
		try
		{
			return filesManager.existsFile(name);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public void createFile(String fileName) throws IOException
	{
		try
		{
			filesManager.createFile(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeFile(String fileName)
	{
		try
		{
			filesManager.removeFile(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/**
	 * The file is expected to exist on the file system and to have a .lgo extension
	 * @param filePath
	 * @throws IOException 
	 */
	public void importFile(File filePath) throws IOException
	{
		try
		{
			filesManager.importFile(filePath);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/**
	 * @throws IOException 
	 * @see {@link LogoFilesManager#exportFile(String, File)}
	 */
	public void exportFile(String fileName, File dest) throws IOException
	{
		try
		{
			filesManager.exportFile(fileName, dest);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/**
	 * @throws IOException
	 * @see {@link LogoFilesManager#exportFile(String, File, String)}
	 */
	public void exportFile(String fileName, File location, String targetName) throws IOException
	{
		try
		{
			filesManager.exportFile(fileName, location, targetName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public String makeUniqueFileName(String base)
	{
		try
		{
			return filesManager.makeUniqueFileName(base);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return null;
	}
	
	@Override
	public void renameFile(String oldName, String newName)
	{
		try
		{
			filesManager.renameFile(oldName, newName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void openFile(String fileName)
	{
		try
		{
			filesManager.openFile(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void closeFile(String fileName)
	{
		try
		{
			filesManager.closeFile(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void writeFileText(String fileName, String content) throws IOException
	{
		try
		{
			filesManager.writeFileText(fileName, content);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void storeFile(String fileName) throws IOException
	{
		try
		{
			filesManager.storeFile(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void addFileListener(FileContainerChangeListener listener)
	{
		try
		{
			filesManager.addFileListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeFileListener(FileContainerChangeListener listener)
	{
		try
		{
			filesManager.removeFileListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	public String getLastEditedFileName()
	{
		try
		{
			return filesManager.getLastEditedFileName();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return null;
	}
	
	public boolean isFilesListEditable()
	{
		try
		{
			return filesManager.isFilesListEditable();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return true;
	}
	
	public boolean hasErrors(String fileName)
	{
		try
		{
			return filesManager.hasErrors(fileName) || proceduresManager.hasAmbiguousProcedures(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public void editAll()
	{
		try
		{
			filesManager.editAll();
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/**
	 * Resets the UserSpace by deleting all files (and procedures), all
	 * variables and all property lists.
	 * <p>
	 * Note : deleting all files will cause a chain of events.
	 */
	public void eraseAll()
	{
		try
		{
			filesManager.eraseAll(); // should remove all files and procedures and fire the right events
			LogoContext context = contextManager.getContext();
			context.getGlobals().deleteAllVariables();
			context.getPropertyLists().deleteAllPropertyLists();
			context.getGuiMap().clear();
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * PROCEDURE CONTAINER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * This is the implementation of the Logo Command define.
	 * Its effect has changed since we have multiple files now, but the semantics are preserved.
	 * Logo programs will behave exactly as before. <p>
	 * If the procedure is ambiguous, cannot decide which one to redefine => IllegalArgumentException <br>
	 * If the procedure is already defined once, that definition will be redefined in its original LogoFile. <br>
	 * If the procedure is not yet defined, a new File will be created "Generated Procedures" if does not yet exist,
	 * and the new procedure will be put there.
	 * 
	 * @throws IllegalArgumentException if the procedure's name is ambiguous in the current context: I Don't know which one to redefine.
	 * @throws IllegalArgumentException if the procedure is not executable or it's owner does not exist in this context.
	 * @throws IOException - if the file "gerated procedure" could not be created in the context.
	 */
	@Override
	public void defineProcedure(Procedure procedure) throws IOException
	{
		try
		{
			String procedureName = procedure.getName();
			if (proceduresManager.isProcedureAmbiguous(procedureName))
				throw new IllegalArgumentException("Attempt to redefine ambiguous procedure.");
			
			if (!isExecutable(procedureName))
			{
				String fileName = Logo.messages.getString("ws.generated.procedure");
				if (!existsFile(fileName))
					createFile(fileName);
			}
			
			proceduresManager.defineProcedure(procedure);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public Collection<Procedure> getExecutables()
	{
		try
		{
			return proceduresManager.getExecutables();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<Procedure>();
	}
	
	@Override
	public Procedure getExecutable(String procedureName)
	{
		try
		{
			return proceduresManager.getExecutable(procedureName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return null;
	}
	
	@Override
	public boolean isExecutable(String procedureName)
	{
		try
		{
			return proceduresManager.isExecutable(procedureName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public void eraseProcedure(String procedureName)
	{
		try
		{
			proceduresManager.eraseProcedure(procedureName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * The following symbol tables do not need a manager and no events
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	public GuiMap getGuiMap()
	{
		try
		{
			return contextManager.getContext().getGuiMap();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new GuiMap();
	}
	
	public PropertyListTable getPropertyLists()
	{
		try
		{
			return contextManager.getContext().getPropertyLists();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new PropertyListTable();
	}
	
	public GlobalVariableTable getGlobals()
	{
		try
		{
			return contextManager.getContext().getGlobals();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new GlobalVariableTable();
	}
	
	@Override
	public Collection<String> getAllProcedureNames()
	{
		try
		{
			return proceduresManager.getAllProcedureNames();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<String>();
	}
	
	@Override
	public Collection<String> getAllProcedureNames(String fileName)
	{
		try
		{
			return proceduresManager.getAllProcedureNames(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<String>();
	}
	
	@Override
	public String getProcedureOwner(String procedureName)
	{
		try
		{
			return proceduresManager.getProcedureOwner(procedureName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return null;
	}
	
	@Override
	public void addProcedureMapListener(ProcedureMapListener listener)
	{
		try
		{
			proceduresManager.addProcedureMapListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeProcedureMapListener(ProcedureMapListener listener)
	{
		try
		{
			proceduresManager.removeProcedureMapListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	/*
	 * Errors & Ambiguities
	 */
	
	/**
	 * conflicting w.r.t. ambiguity of their procedures
	 */
	@Override
	public Collection<String> getAllConflictingFiles()
	{
		try
		{
			return proceduresManager.getAllConflictingFiles();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<String>();
	}
	
	@Override
	public boolean hasAmbiguousProcedures(String fileName)
	{
		try
		{
			return proceduresManager.hasAmbiguousProcedures(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public boolean isProcedureAmbiguous(String procedureName)
	{
		try
		{
			return proceduresManager.isProcedureAmbiguous(procedureName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public void addAmbiguityListener(AmbiguityListener listener)
	{
		try
		{
			proceduresManager.addAmbiguityListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeAmbiguityListener(AmbiguityListener listener)
	{
		try
		{
			proceduresManager.removeAmbiguityListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public boolean hasErrors()
	{
		try
		{
			return errorManager.hasErrors();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return false;
	}
	
	@Override
	public Collection<ProcedureErrorMessage> getAllErrors()
	{
		try
		{
			return errorManager.getAllErrors();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<ProcedureErrorMessage>();
	}
	
	@Override
	public Collection<String> getAllErroneousFiles()
	{
		try
		{
			return errorManager.getAllErroneousFiles();
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<String>();
		
	}
	
	@Override
	public void addErrorListener(ErrorListener listener)
	{
		try
		{
			errorManager.addErrorListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	@Override
	public void removeErrorListener(ErrorListener listener)
	{
		try
		{
			errorManager.removeErrorListener(listener);
		}
		catch (Exception e)	{ showErrorDialog(e); }
	}
	
	public Collection<String> getErrorMessages(String fileName)
	{
		try
		{
			return errorManager.getErrorMessages(fileName);
		}
		catch (Exception e)	{ showErrorDialog(e); }
		return new ArrayList<String>();
	}
	
}
