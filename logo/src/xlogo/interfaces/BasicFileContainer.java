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

import java.io.File;
import java.io.IOException;

public interface BasicFileContainer
{	
	public String[] getFileNames();
	
	public String readFile(String name); // Content

	public String getLastEditedFileName();

	public void createFile(String fileName) throws IOException;
	
	public String makeUniqueFileName(String base);
	
	public boolean existsFile(String name);
	
	public void writeFileText(String fileName, String content) throws IOException;
	
	public void storeFile(String fileName) throws IOException;
	
	public void importFile(File filePath) throws IOException;
	
	public void exportFile(String fileName, File dest) throws IOException;
	
	public void exportFile(String fileName, File location, String targetName) throws IOException;
	
	public void renameFile(String oldName, String newName);

	public void removeFile(String fileName);
	
	public void eraseAll();	
	
	public void openFile(String fileName);
	
	public void closeFile(String fileName);
	
	public String getOpenFileName();
	
	public boolean isFilesListEditable();

	//public boolean hasErrors(String fileName);

	public void addFileListener(FileContainerChangeListener listener);
	
	public void removeFileListener(FileContainerChangeListener listener);
	
	public interface FileContainerChangeListener
	{	
		/**
		 * After the file has been opened
		 * @param file name
		 */
		public void fileOpened(String file);
		
		/**
		 * After the file has been closed
		 * @param file name
		 */
		public void fileClosed(String file);
		
		/**
		 * After the file has been added
		 * @param file name
		 */
		public void fileAdded(String file);
		
		/**
		 * After the file has been removed
		 * @param file name
		 */
		public void fileRemoved(String file);
		
		/**
		 * After a file has been renamed
		 * @param oldName
		 * @param newName
		 */
		public void fileRenamed(String oldName, String newName);
		
		/**
		 * Applies to the whole container.
		 * @param editEnabled
		 */
		public void editRightsChanged(boolean editEnabled);
		
		/**
		 * After a file is updated,
		 * or after a context switch.
		 * @param fileName
		 */
		//public void errorsDetected(String fileName);
		
		/**
		 * Only fired if errors existed before a file was updated.
		 * @param fileName
		 */
		//public void errorsCorrected(String fileName);
	}
}
