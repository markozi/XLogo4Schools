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

import java.io.IOException;
import java.util.ArrayList;

import xlogo.interfaces.MessageBroadcaster;
import xlogo.kernel.userspace.files.RecordFile;

/**
 * The context for contest/record mode.
 * When creating a new RecordContext, a number of RecordFiles are created, for each name provided in the constructor
 * <p>
 * Note that a RecordContext only works correctly with RecordFiles.
 * Therefore FileManagers should always use context.createFile(); to add new files, and never put them directly into the files table.
 * <p>
 * Besides, RecordContext suggests to not create new files ({@link #isFilesListEditAllowed()}),
 * but files can still be created by using Logo commands, such as define or load.
 * 
 * @author Marko
 */
public class RecordContext extends LogoContext implements MessageBroadcaster
{
	private String[] fileOrder;
	
	private MessageListener fileTimerListener;
	private ArrayList<MessageListener> timerEventListeners = new ArrayList<MessageListener>();
	
	public RecordContext(final String[] recordModeFileNames) throws IOException
	{
		super();
		this.fileOrder = recordModeFileNames;
		initFileTimerListener();
		setupRecordFiles();
	}
	
	protected void setupRecordFiles() throws IOException
	{	
		for(String fileName : fileOrder)
			createFile(fileName, "");
	}
	
	private void initFileTimerListener()
	{
		fileTimerListener = new MessageListener(){
			
			@Override
			public void messageEvent(String source, String message)
			{
				for (MessageListener listener : timerEventListeners)
					listener.messageEvent(source, message);
			}
		};
	}
	
	@Override
	public void createFile(String fileName, String text) throws IOException
	{
		RecordFile file = RecordFile.createNewFile(fileName);

		if (text != null && text.length() > 0)
		{
			file.setText(text);
			file.store();
		}

		installListeners(file);
		file.addBroadcastListener(fileTimerListener);
		
		getFilesTable().put(fileName, file);
	}	
	
	public void openFile(String fileName)
	{
		super.openFile(fileName);
		RecordFile file = (RecordFile) getFilesTable().get(fileName);
		file.startRecord();
	}
	
	@Override
	public void closeFile()
	{
		RecordFile file = (RecordFile) getOpenFile();
		file.pauseRecord();
		super.closeFile();
	}
	
	@Override
	public String[] getFileOrder()
	{
		// TODO must extend file order for command "define" [otherwise constant number of files in record mode]
		// - although it's not really correct, no visible consequences in app => no priority ... program works just as well
		return fileOrder; 
	}
	
	/**
	 * The Record context suggest that the user should not be allowed to create files from the gui.
	 * @see LogoContext#isFilesListEditAllowed()
	 */
	@Override
	public boolean isFilesListEditAllowed()
	{
		return false;
	}

	
	@Override
	public void addBroadcastListener(MessageListener listener)
	{
		timerEventListeners.add(listener);
	}

	@Override
	public void removeBroadcastListener(MessageListener listener)
	{
		timerEventListeners.add(listener);
	}


}
