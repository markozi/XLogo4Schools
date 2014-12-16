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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import xlogo.interfaces.ErrorDetector.AmbiguityDetector.AmbiguityListener;
import xlogo.interfaces.ErrorDetector.FileErrorCollector;
import xlogo.kernel.userspace.procedures.Procedure;

public class ErrorManager implements FileErrorCollector
{
	private final FileErrorCollector fileErrorDetector;
	private final AmbiguityDetector ambiguityDetector;
	
	private final HashMap<String, Void> errorFiles = new HashMap<String, Void>();
	private final HashMap<String, Collection<String>> ambiguousProcToFiles = new HashMap<String, Collection<String>>();
	
	private final ArrayList<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
	
	public ErrorManager(FileErrorCollector fileErrorDetector, AmbiguityDetector ambiguityDetector)
	{
		this.fileErrorDetector = fileErrorDetector;
		this.ambiguityDetector = ambiguityDetector;
		
		initListeners();
	}
	
	private void initListeners()
	{
		fileErrorDetector.addErrorListener(new ErrorListener(){
			
			@Override
			public void errorsDetected(String fileName)
			{
				errorFiles.put(fileName, null);
				notifyErrorDetected(fileName);
			}
			
			@Override
			public void allErrorsCorrected(String fileName)
			{
				errorFiles.remove(fileName);
				
				for (Collection<String> fileNames : ambiguousProcToFiles.values())
				{
					if (fileNames.contains(fileName))
						return;
				}
				// no more errors or ambiguities found
				notifyAllErrorsCorrected(fileName);
			}
		});
		
		ambiguityDetector.addAmbiguityListener(new AmbiguityListener(){
			
			@Override
			public void ambiguityResolved(String procedureName, String fileName)
			{
				Collection<String> ambigFiles = ambiguousProcToFiles.get(procedureName);
				
				ambigFiles.remove(fileName);
				
				if (ambigFiles.size() == 0)
					ambiguousProcToFiles.remove(procedureName);
								
				// [this check is not necessary. if it was ambiguous, it did not have errors.]
				//if (errorFiles.containsKey(fileName))
				//	return;
				// No more file errors 
				
				for (Collection<String> fileNames : ambiguousProcToFiles.values())
				{
					if (fileNames.contains(fileName))
						return;
				}
				// No more ambiguities for file
				
				notifyAllErrorsCorrected(fileName);
			}

			@Override
			public void ambiguityDetected(String procedureName, Map<String, Procedure> fileToProcedure)
			{
				ambiguousProcToFiles.put(procedureName, new ArrayList<String>(fileToProcedure.keySet()));
				for (String fileName : fileToProcedure.keySet())
					notifyErrorDetected(fileName);
			}
		});
	}

	@Override
	public boolean hasErrors()
	{
		return errorFiles.size() > 0 || ambiguousProcToFiles.size() > 0;
	}

	@Override
	public boolean hasErrors(String fileName)
	{
		if (errorFiles.containsKey(fileName))
			return true;
		
		for (Collection<String> conflictingFiles : ambiguousProcToFiles.values())
			if (conflictingFiles.contains(fileName))
				return true;
		return false;
	}
	
	@Override
	public Collection<ProcedureErrorMessage> getAllErrors()
	{
		ArrayList<ProcedureErrorMessage> allErrorMessages = new ArrayList<ProcedureErrorMessage>();
		// Not the most efficient impl possible
		allErrorMessages.addAll(fileErrorDetector.getAllErrors());
		allErrorMessages.addAll(ambiguityDetector.getAllErrors());
		return allErrorMessages;
	}

	@Override
	public Collection<String> getAllErroneousFiles()
	{
		TreeMap<String, Void> allErrorFiles = new TreeMap<String, Void>();
		
		for(String fileName : errorFiles.keySet())
			allErrorFiles.put(fileName, null);
		
		for(Collection<String> files : ambiguousProcToFiles.values())
			for(String fileName : files)
				allErrorFiles.put(fileName, null);
		
		return allErrorFiles.keySet();
	}

	public Collection<String> getErrorMessages(String fileName)
	{
		ArrayList<String> messages = new ArrayList<String>();
		
		for(ProcedureErrorMessage pem : getAllErrors())
			if(pem.fileNames.contains(fileName))
				messages.add(pem.toString());
				
		return messages;
	}
	
	// These error event will directly update the gui => run on event dispatcher thread
	
	@Override
	public void addErrorListener(ErrorListener listener)
	{
		errorListeners.add(listener);
	}

	@Override
	public void removeErrorListener(ErrorListener listener)
	{
		errorListeners.remove(listener);
	}
	
	private void notifyErrorDetected(final String fileName)
	{
		for (ErrorListener listener : errorListeners)
			listener.errorsDetected(fileName);
	}	
	
	private void notifyAllErrorsCorrected(final String fileName)
	{
		for (ErrorListener listener : errorListeners)
			listener.allErrorsCorrected(fileName);
	}
	
}
