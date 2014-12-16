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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import xlogo.Logo;
import xlogo.kernel.userspace.files.LogoFile;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.UserConfig;

public class UserContext extends LogoContext
{

	/**
	 * Load and parse all the files in current user's source directory. <br>
	 * This happens only once, in the constructor. <br>
	 * Refresh is currently not planned, but it would be easy to add.
	 * 
	 * @param userDir
	 */
	public UserContext()
	{
		loadUserFiles();
	}
	
	private void loadUserFiles()
	{
		UserConfig userConfig = WSManager.getUserConfig();
		
		if (userConfig.isVirtual())
			return;
		
		File sourceDir = userConfig.getSourceDirectory();
		if (!sourceDir.exists())
			sourceDir.mkdirs();
		
		if (!sourceDir.isDirectory())
		{
			DialogMessenger.getInstance().dispatchMessage(Logo.messages.getString("ws.error.title"),
					Logo.messages.getString("ws.error.userdir.not.dir"));
			return;
		}
		
		StringBuilder ioErrors = new StringBuilder();
		
		for (String fileName : getLogoFileNamesFromDirectory(sourceDir))
		{
			String name = fileName.substring(0, fileName.length() - GlobalConfig.LOGO_FILE_EXTENSION.length());
			userConfig.addFile(name);
			try
			{
				LogoFile file = LogoFile.loadFile(name);
				getFilesTable().put(file.getPlainName(), file);
				
			}
			catch (IOException e)
			{
				ioErrors.append(e.toString());
				ioErrors.append("\n\n");
			}
		}
		
		// must remove files from fileOrder that could not be found anymore.
		for (String fileName : new ArrayList<String>(userConfig.getFileOrder()))
		{
			if (!getFilesTable().containsKey(fileName))
				userConfig.getFileOrder().remove(fileName);
		}
		
		if (ioErrors.length() > 0)
		{
			DialogMessenger.getInstance().dispatchMessage(Logo.messages.getString("ws.error.title"),
					Logo.messages.getString("ws.error.could.not.load.logo.files") + "\n" + ioErrors.toString());
		}
	}
	
	/**
	 * Caller must make sure that newName does not already exist.
	 */
	@Override
	public void renameFile(String oldName, String newName)
	{
		super.renameFile(oldName, newName);
		WSManager.getUserConfig().renameFile(oldName, newName);
	}
	
	@Override
	public void createFile(String fileName, String text) throws IOException
	{
		/*
		 * Eager creation of files in file order list in user config.
		 */		
		
		if (!WSManager.getUserConfig().isVirtual())
			super.createFile(fileName, text);
		else
		{
			LogoFile file = LogoFile.createNewVirtualFile(fileName);
			file.setText(text);
			getFilesTable().put(fileName, file);
			installListeners(file);
		}
		WSManager.getUserConfig().addFile(fileName);
	}
	
	@Override
	public void importFile(File path, String newFileName)
	{
		try
		{
			super.importFile(path, newFileName);
		}
		catch (IOException e)
		{
			DialogMessenger.getInstance().dispatchError(MessageKeys.GENERAL_ERROR_TITLE, "Could not import file : \n" + e.toString());
		}
		WSManager.getUserConfig().addFile(newFileName);
	}
	
	private String[] getLogoFileNamesFromDirectory(File dir)
	{
		return dir.list(new FilenameFilter(){
			public boolean accept(File file, String name)
			{
				return name.endsWith(".lgo");
			}
		});
	}
	
	@Override
	public String[] getFileOrder()
	{	
		/*
		 * Lazy deletion from file order list in user config.
		 */		
		ArrayList<String> list = new ArrayList<String>(WSManager.getUserConfig().getFileOrder());
		Map<String,LogoFile> filesTable = getFilesTable();
		
		if (filesTable.size() != list.size())
		{
			Iterator<String> iter = list.iterator();
			String current;
			
			while(iter.hasNext())
			{
				current = iter.next();
				if(!filesTable.containsKey(current))
					iter.remove();
			}
		}
				
		return list.toArray(new String[list.size()]); 
	}
	
	

}
