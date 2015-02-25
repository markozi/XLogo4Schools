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

package xlogo.kernel.userspace.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.Logo;
import xlogo.interfaces.ErrorDetector;
import xlogo.interfaces.ProcedureMapper;
import xlogo.kernel.userspace.ProcedureErrorMessage;
import xlogo.kernel.userspace.procedures.ExecutablesContainer;
import xlogo.kernel.userspace.procedures.Procedure;
import xlogo.kernel.userspace.procedures.Procedure.State;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.StorableDocument;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.NumberOfBackups;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;

/**
 * This class holds the text file a user entered in the editor.
 * It analyzes the text and maintains a symbol table for all defined procedures that live within it.
 * <p>
 * The file does never store itself implicitly, except for when it is created using {@link #createNewFile(String)} or renamed using {@link #setPlainName(String)}
 * In every other case, {@link #store()}} or {@link #storeCopyToFile(File)}} must be invoked explicitly.
 * <p>
 * The file's text can be set using {@link #setTextFromReader(BufferedReader)}} (preferred) or {@link #setText(String)}}.
 * Both will try to parse the signature of all procedures using the constructor of {@link xlogo.kernel.userspace.procedures.Procedure}
 * 
 * @author Marko Zivkovic, (Loic Le Coq's parsing of procedures is not recognizable anymore.)
 * 
 */
public class LogoFile extends StorableDocument implements ExecutablesContainer, ProcedureMapper, ErrorDetector
{

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1117062836862782516L;
	
	private static Logger logger = LogManager.getLogger(LogoFile.class.getSimpleName());
	
	/**
	 * UserConfig of the owner of this file
	 */
	private UserConfig				userConfig;
	
	/**
	 * Contains only executable procedures
	 */
	private Map<String, Procedure>	executables;
	
	/**
	 * Contains all procedures, no matter what the state is.
	 * The order of the list is relevant to reproduce the editor text after the Logo command 'eraseprocedure'
	 * (after {@link #deleteProcedure(String)}})
	 */
	private ArrayList<Procedure> allProcedures;
	
	/**
	 * A flag that indicated whether the last parsing ended with errors or ambiguities
	 */
	private boolean hasError;
	
	/*
	 * CONSTRUCTOR & STATIC CONSTRUCTORS, FILE LOADERS
	 */
	
	/**
	 * The LogoFile automatically sets its location to the current user's src directory, if that user is not virtual.
	 * @param fileName
	 * @throws IllegalArgumentException see : {@link Storable#setFileName()}
	 */
	protected LogoFile(String fileName) throws IllegalArgumentException
	{
		super();
		this.userConfig = WSManager.getUserConfig();
		setPlainName(fileName);
		executables = new HashMap<String, Procedure>();
		allProcedures = new ArrayList<Procedure>();
	}
	
	public static LogoFile createNewVirtualFile(String fileName)
	{
		logger.trace("Creating new virtual file " + fileName);
		LogoFile file = null;
		try
		{
			file = new LogoFile(fileName);
			file.makeVirtual();
		}
		catch (IllegalArgumentException ignore) { }
		return file;
	}
	/**
	 * Create a new file and store it in the user's source directory.
	 * @throws IOException 
	 * @throws IllegalArgumentException
	 */
	public static LogoFile createNewFile(String fileName) throws IOException, IllegalArgumentException
	{
		logger.trace("Creating new file " + fileName);
		
		LogoFile file = new LogoFile(fileName);
		file.setupFileSystem();
		return file;
	}
	
	/**
	 * Load the specified file from the user's source directory and parse procedure structures.
	 * @param fileName - without extension
	 * @return
	 * @throws IOException
	 */
	public static LogoFile loadFile(String fileName) throws IOException
	{
		logger.trace("Loading " + fileName + " from current workspace");
		
		UserConfig userConfig = WSManager.getUserConfig();
		File path = userConfig.getLogoFilePath(fileName);
		String text = Utils.readLogoFile(path.toString());
		LogoFile file = new LogoFile(fileName);
		file.setText(text);
		return file;
	}
		
	/**
	 * Open any file on the file system and integrate it in the UserSpace.
	 * The file will be stored and made visible under the specified newFileName
	 * @param file
	 * @param newFileName
	 * @throws IOException 
	 */
	public static LogoFile importFile(File path, String newFileName) throws IOException
	{
		logger.trace("Importing " + newFileName + " from " +  path.getAbsolutePath() + "into current workspace");
		
		String text = Utils.readLogoFile(path.toString());
		LogoFile file = new LogoFile(newFileName);
		file.setText(text);
		file.store();
		return file;
	}
	
	protected UserConfig getUserConfig()
	{
		return userConfig;
	}
	/**
	 * This assumes that the file name is well formed. No additional checks are performed
	 * Rename this LogoFile and the file on the file system, if it exists there. Notify all FileChangeListeners.
	 * This accepts name with or without .lgo extension.
	 * @param newFileName - without extension
	 */
	@Override
	public void setPlainName(String newFileName)
	{
		logger.trace("Renaming file " + getPlainName() + " to " + newFileName);
		
		if (newFileName == null || newFileName.length() == 0)
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.EMPTY_NAME));
			return;
		}
		
		if (!Storable.checkLegalName(newFileName))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.ILLEGAL_NAME) + " : " + newFileName);
			return;
		}
		
		String oldPlainName = getPlainName();
		super.setPlainName(newFileName);
		String newPlainName = getPlainName();
		
		if (oldPlainName != null)
			notifyRenamed(oldPlainName, newPlainName);
	}	
	
	@Override
	public String getFileNameExtension()
	{
		return GlobalConfig.LOGO_FILE_EXTENSION;
	}
	
	private void notifyRenamed(String oldName, String newName)
	{
		for(ProcedureMapListener listener : procedureMapListeners)
			listener.ownerRenamed(oldName, newName);
	}
			
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * STORE & LOAD FILE
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * If this is not virtual, create this file on the file system and create a backup folder for it.
	 * @throws IOException 
	 */
	protected void setupFileSystem() throws IOException
	{
		if (isVirtual())
			return;
		
		File source = getFilePath();
		File backupFolder = userConfig.getFileBackupDir(getPlainName());
		
		if (!source.getParentFile().exists())
			source.getParentFile().mkdirs();
		
		if (!backupFolder.exists())
			backupFolder.mkdirs();
		
		storeCopyToFile(source);
	}
	
	/**
	 * If this is not virtual, store the file in the source folder of the UserSpace, <br>
	 * and another copy in the backup folder, if this is required by {@link WorkspaceConfig#getNumberOfBackups()}.
	 */
	@Override
	public void store()
	{
		super.store();
		doBackup();
	}
	
	@Override
	public void delete()
	{
		logger.trace("Deleting file " + getPlainName());
		
		super.delete();
		Collection<String> procedures = new ArrayList<String>(executables.keySet());
		executables.clear();
		allProcedures.clear();
		notifyDeleted(procedures);
	}
	
		
	/**
	 * Store a backup copy of this file.
	 * If the number of maximally allowed backups is exceeded,
	 * delete the oldest copies until the number of backups equals the limit
	 * defined by {@link WorkspaceConfig#getNumberOfBackups()}}
	 * @throws IOException
	 */
	private void doBackup()
	{
		if (isVirtual())
			return;
		logger.trace("Creating backup file of current version of " + getPlainName());
		
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		NumberOfBackups nob = wc.getNumberOfBackups();

		File backupFile = userConfig.getBackupFilePath(getPlainName());
		File backupFolder = backupFile.getParentFile();
		if (!backupFolder.exists())
			backupFolder.mkdirs();
		
		if (nob != NumberOfBackups.NO_BACKUPS) {
			try {
				storeCopyToFile(backupFile);
			}
			catch (Exception ignore) { }
		}
		
		if (nob == NumberOfBackups.INFINITE)
			return;
		
		int max = nob.getNumber(); // max is >= 0
		// Assume no outer manipulation of that directory
		File[] backups = backupFolder.listFiles();
		
		int actual = backups.length;
		if (actual <= max)
			return;
		
		// must delete the oldest backups
		Arrays.sort(backups, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return f2.getName().compareTo(f1.getName().toString());
			}
		});
		
		while (actual > max)
		{
			actual--;
			backups[actual].delete();
		}
	}
	
	/**
	 * The file path of this LogoFile in the source directory.
	 */
	@Override
	public File getFilePath()
	{
		if (super.getFilePath() != null)
			return super.getFilePath();
		return userConfig.getLogoFilePath(getPlainName());
	}
	
	@Override
	public File getLocation()
	{
		if (super.getLocation() != null)
			return super.getLocation();
		return userConfig.getSourceDirectory();
	}

	/**
	 * More efficient test without generating entire text
	 */
	@Override
	public boolean isEmpty(){
		return allProcedures.isEmpty() || (allProcedures.size() == 1 && allProcedures.get(0).getText().isEmpty());
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * SERIALIZATION AND DESERIALIZATION
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	protected String generateText()
	{
		StringBuilder text = new StringBuilder();
		
		for(Procedure proc : allProcedures)
		{
			text.append(proc.getText());
			text.append("\n");
		}
		if (text.length() > 0){
			text.deleteCharAt(text.length() - 1);
		}
		
		return text.toString();
	}
	
	/**
	 * Changes made 21.6.2013 and July 2013
	 * <p>
	 * In XLogo, this was {@code Editor.analyseprocedure()}
	 * <p><p>
	 * <b>Refactored:</b><br>
	 * Initially all that happens in 
	 * <li> {@link #setText(String)}
	 * <li> {@link #parseText(BufferedReader)}
	 * <li> {@link #nextProcedure(BufferedReader)}
	 * <li> {@link Procedure#Procedure() }
	 * <li> {@link #untilEnd() }<br>
	 * was composed into one big and unreadable procedure {@code Editor.analyseprocedure()}.
	 * Note that the Editor (a GUI Controller) was responsible to parse text.
	 * {@code Editor.analyseprocedure()} took the text it was analyzing from the Editor's text component directly.
	 * Thus, whenever a procedure was programmatically defined, or if a workspace was received from the network,
	 * the text had to be written to the editor first before {@code Editor.analyseprocedure()} was called.<p>
	 * Note that in the networking case, the received text was never meant to be displayed.
	 * In that case the Editor served merely as a temporary container such that analyseProcedure() could read the text from it.
	 * This was the only reason why the property "affichable" (displayable) was added to so many classes.
	 * 
	 * <p><p>
	 * <b>New Mechanism:</b><br>
	 * In XLogo, as soon as an error was found in the document, an exception was thrown and displayed to the user. <br>
	 * The new approach is to first split the document wherever a line starts with a token 'end'.
	 * <p>
	 * [#belongs to procedure 1<br>
	 * ... <br>
	 * end][#belongs to procedure 2 <br>
	 * ... <br>
	 * end] ...
	 * <p>
	 * These parts of the document are given to the constructor {@code Procedure#Procedure(String)},
	 * so the procedure can maintain its own state
	 * <br>
	 * Based on the type of errors, a Procedure can now detect several errors at a time and report them.
	 * The LogoFile can then report all errors that have been collected from its procedures.
	 * This approach allows to give more precise error messages to the user.
	 * Example: It is now possible to say which procedure is missing an 'end'
	 * <br>
	 * In the new implementation, a Procedure is not necessarily executable.
	 * Whether it is executable, can be read from its state {@link Procedure.State}.
	 * Its state can be
	 * <li> UNINITIALIZED
	 * <li> EXECUTABLE
	 * <li> COMMENT_ONLY (for white space and comments at the end of the document)
	 * <li> ERROR
	 * <li> AMBIGUOUS_NAME <br>
	 * <p>
	 * Only EXECUTABLE procedures are included in the procedureTable of the {@link xlogo.kernel.userspace.context.LogoContext},
	 * but all procedures are maintained by LogoFile.
	 * @param str
	 * @throws DocumentStructureException 
	 * @throws IOException
	 */
	@Override
	protected void parseText(BufferedReader br)
	{
		/*
		 *  Keep old procedures before reset of procedure tables.
		 *  procedures that remain in the end, will count as deleted.
		 *  procedures that existed before, but have errors now, count as deleted.
		 */
		HashMap<String, Void> deleted = new HashMap<String, Void>();
		for (Procedure proc : executables.values())
			deleted.put(proc.getName(), null);
		
		/*
		 * Must notify that all old executables are deleted as soon as a single procedure has an error,
		 * We want the whole file to be not executable when there exists an error
		 */
		Collection<String> oldExecutables = new ArrayList<String>(executables.keySet());
		
		/*
		 * We don't want the procedures to become ordered by creation time in the editor.
		 * The Logo command "define" was affected by this change, hence it was adapted to work as before.
		 * <p>
		 * Because we delete all the procedures from the tables [unlike XLogo] every time before reading the file, 
		 * the procedures will be stored in the order in which the user defined them last.
		 */
		resetProcedureTables(); // Added by Marko Zivkovic, 21.6.2013
		
		// When the file is empty, it has no errors...
		hasError = false;
		
		try
		{
			while (br.ready())	// next procedure
			{
				Procedure proc;
				String procedureText = untilEnd(br);
				if (procedureText.equals(""))
					break;
				proc = new Procedure(procedureText);
				proc.setOwnerName(getPlainName());
				
				if (proc.getState() == State.EXECUTABLE)
				{
					deleted.remove(proc.getName());
				}
				addProcedure(proc);
				
				if(proc.getState() == State.ERROR || proc.getState() == State.AMBIGUOUS_NAME)
					hasError = true;
			}
		}
		catch (IOException e){} // This should not happen, because no actual IO happens
		finally { try { br.close(); } catch (IOException e) { } }

		if(hasError)
		{
			notifyDeleted(oldExecutables);
			return;
		}
		
		if (deleted.size() > 0)
			notifyDeleted(deleted.keySet());
		
		if (executables.size() > 0)
			notifyDefined(executables.keySet());
	}
	
	/**
	 * @return String until the token 'end' is found on a line, or until the end of the BufferedReader
	 * @throws IOException
	 */
	private static String untilEnd(BufferedReader br) throws IOException
	{
		String end = Logo.messages.getString("fin").toLowerCase();
		StringBuffer text = new StringBuffer();
		String line;
		
		while (br.ready())
		{
			line = br.readLine();
			if (line == null)
				break;
			else if (line.trim().toLowerCase().equals(end))
			{
				text.append(end);
				break;
			}
			else
				text.append(line + "\n");
		}

		return text.toString();
	}

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * PROCEDURE CONTAINER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * Note: does not notify <br>
	 * Delete all procedures
	 */
	private void resetProcedureTables()
	{
		allProcedures.clear();
		executables.clear();
		invalidateText();
		setText(null);
	}
	
	/** 
	 * Implementation of the Logo Command "define". <br>
	 * FileChangeListeners are notified.
	 * <p>
	 * In XLogo, the command define had no effect, when an error was detected while parsing.
	 * The same is true here, because an IllegalStateException is thrown if procedure is not executable.
	 * <p>
	 * In XLogo4Schools, to preserve semantics, we create a {@link Procedure} using its normal constructor and then check for errors.
	 * If errors exist in the procedure text, the procedure should not be defined in its destined file either.
	 * The responsibility whether a procedure is added to a file lies therefore in the interpreter.
	 * <p>
	 * Existing procedures with the same name are just redefined, as in XLogo.
	 * <p>
	 * @param procedure Expects an executable procedure.
	 * @throws IllegalStateException - if procedure is not Executable or its name is ambiguous in this file.
	 */
	@Override 
	public void defineProcedure(Procedure procedure)
	{
		if (procedure.getState() != State.EXECUTABLE)
			throw new IllegalStateException("Attempt to define procedure which is not executable.");
	
		Procedure other = executables.get(procedure.name);

		invalidateText();
		
		if (other != null)
		{
			if (other.getState() == State.AMBIGUOUS_NAME)
				throw new IllegalStateException("Attempt to redefine ambiguous procedure.");
			
			other.redefine(procedure);
			
		}else
		{
			allProcedures.add(procedure);
			executables.put(procedure.name, procedure);
		}
		notifyDefined(procedure.getName());
	}
	
	/**
	 * This is for the Logo command 'eraseprocedure'
	 * @param name
	 * @throws IllegalArgumentException
	 */
	@Override
	public void eraseProcedure(String name)
	{
		Procedure proc = getExecutable(name);
		if(proc == null)
			throw new IllegalStateException("Attempt to erase procedure which exists not.");
		allProcedures.remove(proc);
		executables.remove(name);
		invalidateText();
		notifyDeleted(proc.getName());
	}
	
	/**
	 * Note: Does not notify listeners! <br>
	 * Semantics: If more than one procedures with the same name are defined in a document,
	 * all are marked ambiguous. The first one is kept in the executables list to track ambiguity.
	 * @param pr
	 */
	protected void addProcedure(Procedure pr)
	{
		if (pr.getState() == State.EXECUTABLE)
		{
			Procedure other = executables.get(pr.name);
			
			if(other != null)
			{
				other.makeAmbiguous();
				pr.makeAmbiguous();
			}
			else
				executables.put(pr.name, pr);
		}
		allProcedures.add(pr);
		invalidateText();
	}

	@Override
	public Procedure getExecutable(String name)
	{
		return executables.get(name);
	}
		
	/**
	 * @param name
	 * @return Whether an executable procedure with the specified name exists
	 */
	@Override
	public boolean isExecutable(String name)
	{
		return executables.get(name) != null;
	}
	
	@Override
	public Collection<Procedure> getExecutables()
	{
		if (hasErrors())
			return new ArrayList<Procedure>();
		return executables.values();
	}
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * ERROR DETECTOR
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	public boolean hasErrors()
	{
		return hasError;
	}
	
	public Collection<ProcedureErrorMessage> getAllErrors()
	{
		ArrayList<ProcedureErrorMessage> allErrors = new ArrayList<ProcedureErrorMessage>();
		for(Procedure proc : allProcedures)
		{
			for (xlogo.kernel.userspace.procedures.ProcedureErrorType e : proc.getErrors())
			{
				String description = proc.getName();
				if (description == null)
				{
					description = proc.getText().length() < 100 ? 
							proc.getText() : 
							proc.getText().substring(0, 100) + "...";
				}
				
				allErrors.add(new ProcedureErrorMessage(e, description, getPlainName()));
			}
		}
		return allErrors;
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * PROCEDURE MAPPER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */

	/**
	 * Only executables.
	 * If the file has errors, no procedure is returned.
	 */
	@Override
	public Collection<String> getAllProcedureNames()
	{
		if (hasErrors())
			return  new ArrayList<String>();
		
		ArrayList<String> procedureNames = new ArrayList<String>();
		
		for (Procedure p : executables.values())
			procedureNames.add(p.getName());
		
		return procedureNames;
	}
	
	@Override
	public Collection<String> getAllProcedureNames(String fileName)
	{
		if (fileName.equals(getPlainName()))
			return getAllProcedureNames();
		return null;
	}

	/**
	 * Behaves similar like contains(). If the procedure is in the file's executable list, then returns this file's plainName. Otherwise null.
	 */
	@Override
	public String getProcedureOwner(String procedureName)
	{
		if (executables.containsKey(procedureName))
			return getPlainName();
		return null;
	}
	
	// Procedure Map Listeners

	private final ArrayList<ProcedureMapListener>	procedureMapListeners	= new ArrayList<ProcedureMapListener>();

	@Override
	public void addProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListeners.add(listener);
		
		if(executables.size() > 0)
			notifyDefined(executables.keySet()); // TODO hmmm
	}

	@Override
	public void removeProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListeners.remove(listener);
	}
		
	protected void notifyDefined(Collection<String> procedures)
	{
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.defined(getPlainName(), procedures);
	}
	
	protected void notifyDefined(String procedure)
	{
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.defined(getPlainName(), procedure);
	}
	
	protected void notifyDeleted(Collection<String> collection)
	{
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.undefined(getPlainName(), collection);
	}
	
	protected void notifyDeleted(String procedure)
	{
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.undefined(getPlainName(), procedure);
	}
		
}
