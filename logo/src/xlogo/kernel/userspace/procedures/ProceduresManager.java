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

package xlogo.kernel.userspace.procedures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import xlogo.kernel.userspace.ProcedureErrorMessage;
import xlogo.kernel.userspace.context.ContextSwitcher;
import xlogo.kernel.userspace.context.LogoContext;
import xlogo.kernel.userspace.context.ContextSwitcher.ContextSwitchListener;
import xlogo.kernel.userspace.files.LogoFile;
import xlogo.kernel.userspace.procedures.Procedure.State;

/**
 * This class maintains the procedure table of all files in a context, and it reports ambiguity among them.
 * @author Marko Zivkovic
 */
public class ProceduresManager implements ExecutablesProvider
{
	private LogoContext context;

	private final ArrayList<AmbiguityListener>  ambiguityListeners = new ArrayList<AmbiguityListener>();

	private ProcedureMapListener	procedureMapListener;
	
	public ProceduresManager(ContextSwitcher contextProvider)
	{
		initProcedureMapListener();
		installContextSwitchListener(contextProvider);
		setContext(contextProvider.getContext());
	}
	
	private void setContext(LogoContext newContext)
	{	
		LogoContext old = this.context;
		this.context = newContext;
		
		if (old != null)
			old.removeProcedureMapListener(procedureMapListener);
		
		mapProcedures(newContext);

		/* Record Mode => new files => events
		// Network Mode => only virtual files, invisible => no events
		// that was achieved with the boolean "affichable" (displayable) in XLogo
		if (!newContext.fireProcedureEvents())
		{
			
			return;
		}
		*/
		if (old != null && old.fireProcedureEvents() && newContext.fireProcedureEvents()) // TODO changed
			for(LogoFile file : old.getFilesTable().values())
				notifyDeleted(file.getPlainName(), file.getAllProcedureNames());

		// Note : context will immediately fire event to notify what his contents are.
		// Then these events are forwarded here, in procedureMapListener.
		newContext.addProcedureMapListener(procedureMapListener);
	}
	
	private void mapProcedures(LogoContext context)
	{
		context.getProcedureTable().clear();
		for (LogoFile file : context.getFilesTable().values())
			for (Procedure procedure : file.getExecutables())
				addProcedure(procedure);
	}
	
	private void installContextSwitchListener(ContextSwitcher provider)
	{
		provider.addContextSwitchListener(new ContextSwitchListener(){
			
			@Override
			public void contextSwitched(LogoContext newContext)
			{
				setContext(newContext);
			}
		});
	}
	
	private void initProcedureMapListener()
	{
		// This one listens for changes in files.
		procedureMapListener = new ProcedureMapListener()
		{	
			/**
			 * This event is received when a document is assigned a new text and it contains no errors,
			 * or when this ProcedureManager is assigned a new context that already contains files.
			 */
			@Override
			public void defined(String fileName, Collection<String> procedures)
			{
				for (String procedureName : procedures)
					addProcedure(fileName, procedureName);

				if (procedures.size() > 0)
					notifyDefined(fileName, procedures);
				
				for (String procedureName : procedures)
				{
					Map<String, Procedure> fileNameToProcedure = context.getProcedureTable().get(procedureName);
					if (fileNameToProcedure.size() > 1)
					{
						notifyAmbiguityDetected(procedureName, context.getProcedureTable().get(procedureName));
					}
				}
				
			}
			
			/**
			 * This event is received when the Logo command "define" (re-)defined a command in a file (which already exists!)
			 */
			@Override
			public void defined(String fileName, String procedureName)
			{
				addProcedure(fileName, procedureName);

				notifyDefined(fileName, procedureName);
				
				// Check ambiguity
				Map<String, Procedure> fileNameToProcedure = context.getProcedureTable().get(procedureName);
				if (fileNameToProcedure.size() > 1)
				{
					notifyAmbiguityDetected(procedureName, fileNameToProcedure);
					return;
				}
			}

			/**
			 * This event is received when a file is deleted or when it is assigned a new text in which old procedures are missing.
			 */
			@Override
			public void undefined(String fileName, Collection<String> procedures)
			{
				for (String procedure : procedures)
					undefined(fileName, procedure);
			}
			
			/**
			 * This event is received when the Logo command "eraseProcedure" removes a single procedure from a document
			 * Depending on how many procedures with the same name are left across files after this procedure is removed,
			 * this will fire ExecutableChanged events or AmbiguityResolved events.
			 */
			@Override
			public void undefined(String fileName, String procedureName)
			{
				Map<String,HashMap<String, Procedure>> procedureTable = context.getProcedureTable();
				
				Map<String, Procedure> fileToProc = procedureTable.get(procedureName);
				
				if (fileToProc == null)
					return;
				
				// remove from fileToProc entry
				fileToProc.remove(fileName);

				notifyDeleted(fileName, procedureName);
				
				if (fileToProc.size() == 0)
					procedureTable.remove(procedureName);
				else
					notifyAmbiguityResolved(procedureName, fileName);
													
				if (fileToProc.size() == 1)
				{
					String winnerFile = null;
					for (String wf : fileToProc.keySet())
						winnerFile = wf; // size == 1 => only 1 iteration
					
					notifyAmbiguityResolved(procedureName, winnerFile);
				}
			}

			@Override
			public void ownerRenamed(String oldName, String newName)
			{
				// Note : very important that this event is catched after filemanager has renamed files
				// very critical. but implementation guarantees this order.
				LogoFile file = context.getFilesTable().get(newName);
				// Rename keys in procedureTable
				Map<String, Procedure> fileToProc;
				for (Procedure proc : file.getExecutables())
				{
					fileToProc = context.getProcedureTable().get(proc.getName());
					fileToProc.remove(oldName);
					fileToProc.put(newName, proc);
				}
			}
		};
	}

	/**
	 * @see #addProcedure(Procedure)
	 */
	protected void addProcedure(String fileName, String procedureName) throws IllegalArgumentException
	{
		Procedure procedure = context.getFilesTable().get(fileName).getExecutable(procedureName.toLowerCase());	
		addProcedure(procedure);
	}
	
	/**
	 * Adds the procedure to the procedureTable without firing events.
	 * Use {@link #defineProcedure(Procedure)}} if you want to notify listeners
	 * @param procedure
	 * @throws IllegalArgumentException
	 */
	protected void addProcedure(Procedure procedure) throws IllegalArgumentException
	{
		String procedureName = procedure.getName();
		
		if (procedure.getState() != State.EXECUTABLE && procedure.getState() != State.AMBIGUOUS_NAME)
			throw new IllegalArgumentException("Only executable procedures should be added to the context.");
				
		HashMap<String, HashMap<String, Procedure>> procedureTable = context.getProcedureTable();
		HashMap<String, Procedure> fileToProc = procedureTable.get(procedureName);
		
		if (fileToProc == null) // The procedure name is not yet defined in the context. 
		{
			// Must create fileToProc entry in procedureTable
			fileToProc = new HashMap<String, Procedure>();
			procedureTable.put(procedureName, fileToProc);
		}

		fileToProc.put(procedure.getOwnerName(), procedure);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * EXECUTABLES CONTAINER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * @param {@link Procedure#getOwnerName()} is supposed to be already mapped in the filesTable, and the procedure should be executable
	 * @throws IllegalArgumentException if the procedure's owner name is not mapped;
	 * @see {@link LogoFile#defineProcedure(Procedure)}
	 */
	@Override
	public void defineProcedure(Procedure procedure)
	{
		LogoFile file = context.getFilesTable().get(procedure.getOwnerName());
		if (file == null)
			throw new IllegalArgumentException("The file name \"" 
				+ procedure.getOwnerName() 
				+ "\" specified in procedure is not mapped to a file.");
		
		//addProcedure(procedure);
		file.defineProcedure(procedure); // this will cause the necessary event cascade
	}
	
	/**
	 * Erase
	 */
	@Override
	public void eraseProcedure(String procedureName)
	{
		String lower = procedureName.toLowerCase();
		while(context.getProcedureTable().containsKey(lower))
		{
			// contains key => there must be one
			Procedure nextVictim = null;
			for (Procedure proc : context.getProcedureTable().get(lower).values())
			{
				// iterate only once : otherwise concurrent modification exception
				nextVictim = proc;
				break;
			}
			context.getFilesTable().get(nextVictim.getOwnerName()).eraseProcedure(nextVictim.getName());
		}
	}
	
	/**
	 * Executable procedures of all files. Ambiguous procedures are not included. <br>
	 * This list should not be used to do computations on it, because it must be created every time.
	 * Its purpose is to feed GUI lists
	 */
	@Override
	public ArrayList<Procedure> getExecutables()
	{
		ArrayList<Procedure> executables = new ArrayList<Procedure>();
		for(Map<String, Procedure> fileToProc : context.getProcedureTable().values())
			if (fileToProc.size() == 1) // only one exists
				for(Procedure proc : fileToProc.values())
					executables.add(proc); // only one is added
		return executables;
	}
	
	/**
	 * @return The specified procedure if and only if there is exactly one
	 * procedure definition for this name in the context, otherwise null.
	 */
	@Override
	public Procedure getExecutable(String procedureName)
	{
		Map<String, Procedure> fileToProc = context.getProcedureTable().get(procedureName.toLowerCase());
		
		if (fileToProc == null || fileToProc.size() > 1)
			return null;
		// There is exactly 1 procedure
		for (Procedure proc : fileToProc.values())
			return proc;
		// Code won't reach here
		return null;
	}

	/**
	 * @return true if and only if there exists exactly one procedure definition for the specified name in this context
	 */
	@Override
	public boolean isExecutable(String procedureName)
	{
		Map<String, Procedure> fileToProc = context.getProcedureTable().get(procedureName.toLowerCase());
		return (fileToProc != null && fileToProc.size() == 1);
	}
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * PROCEDURE MAPPER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * Get all executable procedure names
	 * @see #getExecutables()
	 */
	@Override
	public Collection<String> getAllProcedureNames()
	{
		ArrayList<String> executables = new ArrayList<String>();
		for(Map<String, Procedure> fileToProc : context.getProcedureTable().values())
			if (fileToProc.size() == 1) // only one exists
				for(Procedure proc : fileToProc.values())
					executables.add(proc.getName()); // only one is added
		return executables;
	}

	/**
	 * Get all procedures of that file, if its procedures are not conflicting with other files' procedures.
	 */
	@Override
	public Collection<String> getAllProcedureNames(String fileName)
	{
		if (!hasAmbiguousProcedures(fileName))
			return context.getFilesTable().get(fileName).getAllProcedureNames();
		return null;
	}
	
	/**
	 * @return null if procedure does not exist or if it is ambiguous
	 */
	@Override
	public String getProcedureOwner(String procedureName)
	{		
		Procedure procedure = getExecutable(procedureName.toLowerCase());
		return procedure == null ? null : procedure.getOwnerName();
	}
	
	// Procedure Map Listeners : update gui => run on event dispatcher thread
	
	private final ArrayList<ProcedureMapListener>	procedureMapListeners	= new ArrayList<ProcedureMapListener>();

	@Override
	public void addProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListeners.add(listener);
	}

	@Override
	public void removeProcedureMapListener(ProcedureMapListener listener)
	{
		procedureMapListeners.remove(listener);
	}
		
	protected void notifyDefined(final String fileName, final Collection<String> procedures)
	{
		if (!context.fireProcedureEvents())
			return;
		if (procedures.size() == 0)
			return;
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.defined(fileName, procedures);
	}
	
	protected void notifyDefined(final String fileName, final String procedure)
	{
		if (!context.fireProcedureEvents())
			return;
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.defined(fileName, procedure);
		
	}
	
	protected void notifyDeleted(final String fileName, final Collection<String> procedures)
	{
		if (!context.fireProcedureEvents())
			return;
		if (procedures.size() == 0)
			return;

		for (ProcedureMapListener listener : procedureMapListeners)
			listener.undefined(fileName, procedures);
	}
	
	protected void notifyDeleted(final String fileName, final String procedure)
	{
		if (!context.fireProcedureEvents())
			return;
		for (ProcedureMapListener listener : procedureMapListeners)
			listener.undefined(fileName, procedure);
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * AMBIGUITY DETECTOR
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	/**
	 * @return Whether there exists a procedure name that is defined in more
	 *         than one file in the UserSpace
	 */
	@Override
	public boolean hasErrors()
	{
		for (Entry<String, ? extends Map<String, Procedure>> entry : context.getProcedureTable().entrySet())
		{
			if (entry.getValue().size() > 1)
				return true;
		}
		return false;
	}

	/**
	 * Returns all ambiguity problems
	 */
	@Override
	public Collection<ProcedureErrorMessage> getAllErrors()
	{
		ArrayList<ProcedureErrorMessage> ambiguities = new ArrayList<ProcedureErrorMessage>();
		
		for (Entry<String, HashMap<String, Procedure>> entry : context.getProcedureTable().entrySet())
		{
			HashMap<String, Procedure> fileToProc = entry.getValue();
			
			if (fileToProc.size() < 2)
				continue;
			
			ambiguities.add(
				new ProcedureErrorMessage(
					ProcedureErrorType.AMBIGUOUS,
					entry.getKey(),
					fileToProc.keySet()
				)
			);
		}
		
		return ambiguities;
	}

	@Override
	public Collection<String> getAllConflictingFiles()
	{
		ArrayList<String> conflictingFiles = new ArrayList<String>();
		
		for (Entry<String, HashMap<String, Procedure>> entry : context.getProcedureTable().entrySet())
		{
			HashMap<String, Procedure> fileToProc = entry.getValue();
			
			if (fileToProc.size() < 2)
				continue;
			
			conflictingFiles.addAll(fileToProc.keySet());
		}
		
		return conflictingFiles;
	}
	
	@Override
	public boolean hasAmbiguousProcedures(String fileName)
	{
		for (Procedure proc : context.getFilesTable().get(fileName).getExecutables())
		{
			if(isProcedureAmbiguous(proc.getName()))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isProcedureAmbiguous(String name)
	{
		Map<String, Procedure> ambigs = context.getProcedureTable().get(name.toLowerCase());
		if (ambigs == null)
			return false;
		return ambigs.size() > 1;
	}

	
	@Override
	public void addAmbiguityListener(AmbiguityListener listener)
	{
		ambiguityListeners.add(listener);
	}
	
	@Override
	public void removeAmbiguityListener(AmbiguityListener listener)
	{
		ambiguityListeners.remove(listener);
	}
	
	private void notifyAmbiguityDetected(String procedureName, Map<String, Procedure> fileToProcedure)
	{
		if(!context.fireProcedureEvents())
			return;
		for (AmbiguityListener listener : ambiguityListeners)
			listener.ambiguityDetected(procedureName, fileToProcedure);
	}
	
	private void notifyAmbiguityResolved(String procedureName, String winnerFile)
	{
		if(!context.fireProcedureEvents())
			return;
		for (AmbiguityListener listener : ambiguityListeners)
			listener.ambiguityResolved(procedureName, winnerFile);
	}

	


}
