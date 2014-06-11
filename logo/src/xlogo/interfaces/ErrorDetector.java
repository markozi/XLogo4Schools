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

package xlogo.interfaces;

import java.util.Collection;
import java.util.Map;

import xlogo.kernel.userspace.ProcedureErrorMessage;
import xlogo.kernel.userspace.procedures.Procedure;

public interface ErrorDetector
{
	public boolean hasErrors();
	
	public Collection<ProcedureErrorMessage> getAllErrors();
	
	public interface FileErrorCollector extends ErrorDetector
	{	
		public boolean hasErrors(String fileName);
		
		public Collection<String> getAllErroneousFiles();
		
		public void addErrorListener(ErrorListener listener);
		
		public void removeErrorListener(ErrorListener listener);
		
		public interface ErrorListener
		{
			/**
			 * This event is fired as soon as one or more errors are detected in a file
			 * @param fileName
			 */
			public void errorsDetected(String fileName);
			
			/**
			 * This event is fired as soon as all errors are removed from a document
			 * @param fileName
			 */
			public void allErrorsCorrected(String fileName);
		}
	}
	
	public interface AmbiguityDetector extends ErrorDetector
	{		
		/**
		 * @return Files whose procedures conflict with procedures of other files.
		 */
		public Collection<String> getAllConflictingFiles();
		
		/**
		 * @param fileName
		 * @return whether the specified file conflicts with some other file
		 */
		boolean hasAmbiguousProcedures(String fileName);
		
		/**
		 * @param name
		 * @return whether the specified procedure name is defined in more than one file.
		 */
		public boolean isProcedureAmbiguous(String procedureName);

		
		void addAmbiguityListener(AmbiguityListener listener);

		void removeAmbiguityListener(AmbiguityListener listener);

		
		public interface AmbiguityListener
		{
			public void ambiguityDetected(String procedureName, Map<String, Procedure> fileToProcedure);
			
			/**
			 * @param procedureName
			 * @param fileName When a single file has lost ambiguity for that specific procedure (might still have other ambiguities)
			 */
			public void ambiguityResolved(String procedureName, String fileName);
		}
	}
}
