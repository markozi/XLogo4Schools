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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import xlogo.Logo;
import xlogo.interfaces.ErrorDetector.FileErrorCollector;
import xlogo.kernel.userspace.ProcedureErrorMessage;
import xlogo.kernel.userspace.context.ContextSwitcher;
import xlogo.kernel.userspace.context.LogoContext;
import xlogo.kernel.userspace.context.ContextSwitcher.ContextSwitchListener;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.Storable;
import xlogo.storage.global.GlobalConfig;

/**
 * This Manager is completely new, because XLogo did not support multiple files. <br>
 * During the requirements analysis, we have decided to maintain a global scope for procedures.
 * That means a procedure defined in file A is visible in file B.
 * <p>
 * If we find during testing that the global scope is confusing for children and it leads to many ambiguity conflicts,
 * then the current architecture allows to easily switch to file-wide scope. Instead of retrieving executables from the context's procedure table,
 * we can directly retrieve them from the currently open/active file.
 * 
 * @author Marko Zivkovic
 */
public class LogoFilesManager implements LogoFileContainer, FileErrorCollector
{
	private final ContextSwitcher contextProvider;
	private LogoContext	context;

	private final ArrayList<FileContainerChangeListener>	fileListeners	= new ArrayList<FileContainerChangeListener>();
	
	public LogoFilesManager(ContextSwitcher contextProvider)
	{
		this.contextProvider = contextProvider;
		initContextSwitchListener();
		setContext(contextProvider.getContext());
	}

	private void initContextSwitchListener()
	{
		contextProvider.addContextSwitchListener(new ContextSwitchListener(){
			@Override
			public void contextSwitched(LogoContext newContext)
			{
				setContext(newContext);
			}
		});
	}

	private void setContext(LogoContext newContext) 
	{	
		LogoContext old = context;
		context = newContext;
		
		LogoFile openFile = newContext.getOpenFile();
		if (openFile != null)
			closeFile(openFile.getPlainName());
		
		if (newContext.fireFileEvents()) // Example : Network context does not change GUI, only internal change => no events
		{
			if (old != null && old.fireFileEvents())
				for(LogoFile file : old.getFilesTable().values())
					notifyFileRemoved(file.getPlainName());
			
			for (String fileName : newContext.getFileOrder())
			{
				notifyFileAdded(fileName);
				if (context.getFilesTable().get(fileName).hasErrors())
					notifyErrorsDetected(fileName);
			}
		}
		
		if (old == null || old.isFilesListEditAllowed() != newContext.isFilesListEditAllowed())
			notifyRightsChanged();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * X4S Specific features and Logo command implementations
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * The implementation of the Logo command {@code editall} or {@code edall} <br>
	 * In XLogo4Schools, we cannot open all files simultaneously to show all procedures. Instead, editall opens the file that was edited last.
	 */
	public void editAll()
	{
		String fileName = getLastEditedFileName();
		if (fileName == null)
			return;
		openFile(fileName);
	}
	
	@Override
	public void importFile(File filePath) throws IOException
	{
		String name = filePath.getName().substring(0,
				filePath.getName().length() 
				- GlobalConfig.LOGO_FILE_EXTENSION.length());
		
		if(existsFile(name))
			name = makeUniqueFileName(name);
		context.importFile(filePath, name);
		notifyFileAdded(name);
	}
	
	/**
	 * If file is a directory, the exported file will be named fileName.
	 * Otherwise the Logo-file will be exported to the file specified by dest
	 * @param fileName
	 * @param dest
	 * @throws IOException
	 */
	public void exportFile(String fileName, File dest) throws IOException
	{
		
		if (dest.isDirectory())
			exportFile(fileName, dest, fileName);
		else
		{
			File parent = dest.getParentFile();
			String targetName = dest.getName();
			exportFile(fileName, parent, targetName);
		}
	}
	
	/**
	 * @param fileName - of a file in the current context
	 * @param location - an existing directory on the file system
	 * @param targetName - the exported file's name
	 * @throws IOException
	 */
	public void exportFile(String fileName, File location, String targetName) throws IOException
	{
		LogoFile file = context.getFilesTable().get(fileName);
		
		if(file == null)
			throw new IllegalArgumentException("The specified fileName does not exist in the context.");
		
		if (!location.isDirectory())
			throw new IllegalArgumentException("The specified location does not exist : " + location.toString());

		String extendedName = targetName;
		
		if(extendedName == null || extendedName.length() == 0)
			extendedName = fileName;
		
		String extension = GlobalConfig.LOGO_FILE_EXTENSION;
		
		if(!extendedName.endsWith(extension))
			extendedName += extension;

		File target = new File(location.toString() + File.separator + extendedName);
		file.storeCopyToFile(target);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * LOGO FILE CONTAINER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
	 */	

	@Override
	public String[] getFileNames()
	{
		return context.getFileOrder();
	}
	
	@Override
	public void createFile(String fileName) throws IOException
	{
		context.createFile(fileName, "");
		notifyFileAdded(fileName);
	}

	@Override
	public void writeFileText(String fileName, String content)
	{
		LogoFile file = context.getFilesTable().get(fileName);
		
		if (file == null)
			throw new IllegalStateException("Attempt to write to inexistent file.");
		
		boolean hadErrors = file.hasErrors();
		
		file.setText(content);
		
		if (file.hasErrors())
			notifyErrorsDetected(fileName); // notify anyway
		else if (hadErrors)
			notifyErrorsCorrected(fileName);
	}

	@Override
	public void storeFile(String fileName) throws IOException
	{
		context.getFilesTable().get(fileName).store();
	}
	
	/**
	 * The file is also deleted from the file system
	 */
	@Override
	public void removeFile(String fileName)
	{
		LogoFile file = context.getFilesTable().get(fileName);
		file.delete();
		context.getFilesTable().remove(fileName);
		notifyFileRemoved(fileName);
	}

	/**
	 * Deletes all files from the context and removes them from the context's tables. <br>
	 * Note: The events caused be deleting the files should cause all the procedures to disappear from the tables as well.
	 * [But the files manager doesn't care about procedures]
	 */
	@Override
	public void eraseAll()
	{
		Collection<LogoFile> files = context.getFilesTable().values();
		
		while (!files.isEmpty())
		{
			LogoFile nextVictim = null;
			for (LogoFile file : files)
			{
				nextVictim = file;
				break;
			}
			nextVictim.delete();
			context.getFilesTable().remove(nextVictim.getPlainName());
			notifyFileRemoved(nextVictim.getPlainName());
		}
		context.getFilesTable().clear();		
	}
	
	@Override
	public boolean existsFile(String name)
	{
		return context.getFilesTable().containsKey(name);
	}

	@Override
	public String readFile(String name)
	{
		return context.getFilesTable().get(name).getText();
	}
	
	/**
	 * Please make sure the renaming makes sense, otherwise an IllegalStateException is thrown at you.
	 */
	@Override
	public void renameFile(String oldName, String newName)
	{
		if (oldName.equals(newName))
			return;
		
		if(!existsFile(oldName))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.RENAME_INEXISTENT_FILE));
			return;
		}
		
		if (existsFile(newName))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.WS_FILENAME_EXISTS_ALREADY));
			return;
		}

		if (newName == null || newName.length() == 0)
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.EMPTY_NAME));
			return;
		}
		
		if (!Storable.checkLegalName(newName))
		{
			DialogMessenger.getInstance().dispatchError(
					Logo.messages.getString(MessageKeys.NAME_ERROR_TITLE),
					Logo.messages.getString(MessageKeys.ILLEGAL_NAME) + " : " + newName);
			return;
		}

		context.renameFile(oldName, newName);
		notifyFileRenamed(oldName, newName);
	}
	
	@Override
	public String makeUniqueFileName(String base)
	{
		int i = 0;
		String name = null;
		do
		{
			name = base + i;
			i++;
		} while (existsFile(name));
		return name;
	}
	
	/**
	 * @throws IllegalArgumentException if the specified file does not exist in the current context.
	 */
	@Override
	public void openFile(String fileName)
	{
		if(!existsFile(fileName))
			throw new IllegalStateException("The specified file to open does not exist in the current context.");
		
		LogoFile openFile = context.getOpenFile();
		if(openFile != null)
			closeFile(openFile.getPlainName());
		
		context.openFile(fileName);
		notifyFileOpened(fileName);
	}
	
	/**
	 * This can handle only one open file.
	 * If the wrong filename is closed, nothing happens<p>
	 * @throws IllegalStateException
	 */
	@Override
	public void closeFile(String fileName)
	{
		LogoFile openFile = context.getOpenFile();
		if (openFile == null || !openFile.getPlainName().equals(fileName))
			throw new IllegalStateException("Attempting to close a file that was not opened.");
		context.closeFile();
		notifyFileClosed(openFile.getPlainName());
	}
	
	/**
	 * returns null if no file is open.
	 */
	@Override
	public String getOpenFileName()
	{
		LogoFile file = context.getOpenFile();
		if (file == null)
			return null;
		return file.getPlainName();
	}
	
	public boolean isFilesListEditable()
	{
		return context.isFilesListEditAllowed();
	}
		
	/**
	 * the name of the file that was edited last in this context.
	 */
	@Override
	public String getLastEditedFileName()
	{
		Calendar latest = Calendar.getInstance();
		latest.setTimeInMillis(0);
		
		LogoFile result = null;
		for (LogoFile file : context.getFilesTable().values())
		{
			Calendar fileDefinedAt = file.getLastSync();
			if (latest.before(fileDefinedAt))
			{
				result = file;
				latest = fileDefinedAt;
			}
		}
		if (result == null)
			return null;
		
		return result.getPlainName();
	}
		
	// Change listeners : these event update the gui, they must run on the event dispatcher thread
		
	@Override
	public void addFileListener(FileContainerChangeListener listener)
	{
		if (listener == null)
			throw new IllegalArgumentException("listener must not be null.");
		fileListeners.add(listener);
	}
	
	@Override
	public void removeFileListener(FileContainerChangeListener listener)
	{
		fileListeners.remove(listener);
	}
	
	private void notifyFileAdded(final String fileName)
	{
		if (!context.fireFileEvents())
			return;

		for (FileContainerChangeListener listener : fileListeners)
			listener.fileAdded(fileName);
	}
	
	private void notifyFileRemoved(final String fileName)
	{
		if (!context.fireFileEvents())
			return;

		for (FileContainerChangeListener listener : fileListeners)
			listener.fileRemoved(fileName);
	}
	
	private void notifyFileRenamed(final String oldName, final String newName)
	{
		if (!context.fireFileEvents())
			return;
		
		for (FileContainerChangeListener listener : fileListeners)
			listener.fileRenamed(oldName, newName);
	}
	
	private void notifyFileOpened(final String fileName)
	{
		if (!context.fireFileEvents())
			return;
		
		for (FileContainerChangeListener listener : fileListeners)
			listener.fileOpened(fileName);
	}
	
	private void notifyFileClosed(final String fileName)
	{
		if (!context.fireFileEvents())
			return;

		for (FileContainerChangeListener listener : fileListeners)
			listener.fileClosed(fileName);
	}
	
	private void notifyRightsChanged()
	{
		for (FileContainerChangeListener listener : fileListeners)
			listener.editRightsChanged(context.isFilesListEditAllowed());
	}
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * ERROR COLLECTOR : these events do not update the gui directly, they must not run on the event dispatcher thread
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	public Collection<String> getAllErroneousFiles()
	{
		ArrayList<String> erroneousFiles = new ArrayList<String>();
		
		for(LogoFile file : context.getFilesTable().values())
			if(file.hasErrors())
				erroneousFiles.add(file.getPlainName());
		
		return erroneousFiles;
	}
	
	@Override
	public boolean hasErrors()
	{
		for(LogoFile file : context.getFilesTable().values())
			if(file.hasErrors())
				return true;
		return false;
	}
	
	@Override
	public boolean hasErrors(String fileName)
	{
		LogoFile file = context.getFilesTable().get(fileName);
		if (file == null)
			throw new IllegalStateException("The specified fileName does not exist in this context.");
		
		return file.hasErrors();
	}

	@Override
	public Collection<ProcedureErrorMessage> getAllErrors()
	{
		ArrayList<ProcedureErrorMessage> allErrors = new ArrayList<ProcedureErrorMessage>();
		for (LogoFile file : context.getFilesTable().values())
			allErrors.addAll(file.getAllErrors());
		return allErrors;
	}

	// Error listeners
	
	private final ArrayList<ErrorListener> errorListeners = new ArrayList<ErrorListener>();	

	@Override
	public void addErrorListener(ErrorListener listener)
	{
		errorListeners.add(listener);
	}

	@Override
	public void removeErrorListener(ErrorListener listener)
	{
		errorListeners.add(listener);
	}
	
		
	private void notifyErrorsDetected(String fileName)
	{
		if (!context.fireFileEvents())
			return;
		for (ErrorListener listener : errorListeners)
			listener.errorsDetected(fileName);
	}
	
	private void notifyErrorsCorrected(String fileName)
	{
		if (!context.fireFileEvents())
			return;
		for (ErrorListener listener : errorListeners)
			listener.allErrorsCorrected(fileName);
	}

	
}
