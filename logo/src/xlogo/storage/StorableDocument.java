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

package xlogo.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.WorkspaceConfig;

public abstract class StorableDocument extends Storable
{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8218323197066522297L;
	
	private static Logger logger = LogManager.getLogger(StorableDocument.class.getSimpleName());

	/**
	 * Contents of the file
	 */
	private String					text;
	
	/**
	 * DEFINE TIME
	 */
	private Calendar lastSync;
	
	/**
	 * When this was created or saved the last time using {@link #store()}.
	 * Other store methods will not affect the time.
	 * @return
	 */
	public Calendar getLastSync()
	{
		return lastSync;
	}
	
	public StorableDocument()
	{
		super();
		text = "";
		synced();
	}
	
	/**
	 * If this is not virtual, store the file in the source folder of the UserSpace, <br>
	 * and it also stores a copy in the backup folder, if this is required by {@link WorkspaceConfig#getNumberOfBackups()}.
	 */
	@Override
	public void store() throws IOException
	{
		synced();
		if (isVirtual())
			return;
		
		File file = getFilePath();
		logger.trace("Storing document: " + file.getAbsolutePath());
		
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		storeCopyToFile(file);
	}
	
	protected void synced()
	{
		lastSync = Calendar.getInstance();
	}

	/**
	 * 
	 * This is the counterpart to {@link #openFromAnyFile(UserConfig, File)} <br>
	 * Save (export) to any file on the file system.
	 * This works, even if the file is declared virtual.
	 */
	@Override
	public void storeCopyToFile(File file) throws IOException, IllegalArgumentException
	{
		logger.trace("Storing copy of " + getFileName() + " to " + file.getAbsolutePath());
		try
		{
			mkParentDirs(file);
			FileOutputStream f = new FileOutputStream(file);
			BufferedOutputStream b = new BufferedOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(b, "UTF8");
			osw.write(getText());
			osw.close();
			b.close();
			f.close();
			
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * Deletes the current file path
	 */
	public void delete()
	{
		if (isVirtual())
			return;
		File file = getFilePath();
		if (file != null && file.exists())
			file.delete();
	}
	
	public String getText()
	{
		if (text == null)
			text = generateText();
		return text;
	}
	
	/**
	 * This is invoked in {@link getText()} when the current text string is currently not defined. <br>
	 * Implement this to map your data to a string. This is the counterpart to {@link #parseText(BufferedReader)}.
	 * @return
	 */
	protected abstract String generateText();
	
	/**
	 * Setting text will invalidate the current text.
	 * The new text is then parsed to the concrete document structure.
	 * If possible, use {@link #setTextFromReader(BufferedReader)} for performance reasons.
	 * @param text
	 */
	public void setText(String text)
	{
		invalidateText();
		if(text == null)
			return;
		
		String replIndent = text.replaceAll("\t", "    ");
		StringReader sr = new StringReader(replIndent);
		BufferedReader br = new BufferedReader(sr);
		parseText(br);
	}
	
	/**
	 * Setting text will invalidate the current text.
	 * The new text is then parsed to the concrete document structure.
	 * @param br
	 */
	public void setTextFromReader(BufferedReader br)
	{
		invalidateText();
		parseText(br);
	}
	
	protected abstract void parseText(BufferedReader text);
	
	/**
	 * @return Whether the set text could be parsed without errors.
	 */
	public abstract boolean hasErrors();
	
	/**
	 * Call this whenever the internal object structure has changed and it should be serialized first, using {@link #generateText()},
	 * when {@link #getText()} is called the next time.
	 */
	protected void invalidateText()
	{
		this.text = null;
	}
}
