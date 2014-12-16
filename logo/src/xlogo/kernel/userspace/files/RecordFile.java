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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Timer;

import xlogo.Logo;
import xlogo.interfaces.MessageBroadcaster;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.WSManager;
import xlogo.storage.user.UserConfig;
import xlogo.utils.Utils;

/**
 * This is a {@link LogoFile} which is used in contest/record mode.
 * @author Marko
 */
public class RecordFile extends LogoFile implements MessageBroadcaster
{
	private static final long	serialVersionUID	= -9137220313285199168L;
	
	private Timer timer; // the SWING Timer dispatchers on the EventDispatcher Thread => update GUI ok
	private Date started;
	private Date last;
	private long totalMillis;
	
	/**
	 * @param fileName
	 */
	protected RecordFile(String fileName)
	{
		super(fileName);
	}
	
	public static RecordFile createNewFile(String fileName) throws IOException
	{
		RecordFile file = new RecordFile(fileName);
		file.setupFileSystem();
		return file; 
	}
	
	
	/**
	 * @throws Exception 
	 * @throws NotImplementedException A virtual contest/record mode makes no sense.
	 */
	public static RecordFile createNewVirtualFile(UserConfig userConfig, String fileName) throws Exception
	{
		throw new Exception("Not implemented");
	}
	
	@Override
	protected void setupFileSystem() throws IOException
	{		
		File contestFileDir = getUserConfig().getContestFileDir(getPlainName());
		
		if (!contestFileDir.exists())
			contestFileDir.mkdirs();
	}
	
	@Override
	public File getFilePath()
	{
		return getUserConfig().getContestFilePath(getPlainName());
	}
	
	@Override
	public void store()
	{
		long now = Calendar.getInstance().getTime().getTime();
		recordFile(getTimeStampHeader(totalMillis, started.getTime(), now));
		//pauseRecord(); // This is already called by Context at open/close.
		// We actually never store normally, and we don't export these files.
	}
	
	/**
	 * Set the timer
	 */
	public void startRecord()
	{
		this.started = Calendar.getInstance().getTime();
		this.last = Calendar.getInstance().getTime();
		
		timer = new Timer(1000,
			new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					Date now = Calendar.getInstance().getTime();
					totalMillis += now.getTime() - last.getTime();
					last = now;
					
					String time = UserConfig.getMinSec(totalMillis);
					String fileName = getPlainName();
					
					for(MessageListener listener : timerEventListeners)
						listener.messageEvent(fileName, time);
				}
			}
		);
		timer.setRepeats(true);
		timer.start();
	}
	
	/**
	 * Stop the timer and record recent changes with time stamp in contest directory.
	 * (Make sure the recent changes from the editor are before calling this)
	 */
	public void pauseRecord()
	{
		timer.stop();
	}
	
	
	private void recordFile(final String header)
	{
		new Thread(new Runnable(){
			
			@Override
			public void run()
			{
				// Write to file's folder
				File recordFile = getUserConfig().getRecordFilePath(getPlainName());
				File recordFolder = recordFile.getParentFile();
				if (!recordFolder.exists())
					recordFolder.mkdirs();
				
				String content = header + getText();
				
				try
				{
					Utils.writeLogoFile(recordFile.toString(), content);
				}
				catch (IOException e)
				{
					DialogMessenger.getInstance().dispatchMessage(
							Logo.messages.getString("contest.error.title"),
							Logo.messages.getString("contest.error.could.not.record.file") + "\n\n " + e.toString());
				}
				
				// append to command line too ...
				PrintWriter out = null;
				File logoFile = WSManager.getUserConfig().getCommandLineContestFile();
				try
				{
					out = new PrintWriter(new BufferedWriter(new FileWriter(logoFile, true)));
					out.println("");
					out.println(getPlainName());
					out.println(content);
					out.println("\n");
				}
				catch (Exception e)
				{
					DialogMessenger.getInstance().dispatchMessage(Logo.messages.getString("contest.error.title"),
							Logo.messages.getString("contest.could.not.store") + "\n" + e.toString());
				}
				finally
				{
					if (out != null)
						out.close();
				}
			}
		}).run();
		
	}
	
	
	private String getTimeStampHeader(long totalTime, long lastEditStarted, long lastEditEnded)
	{
		String tot = UserConfig.getMinSec(totalTime);
		String lastStart = UserConfig.getTimeString(lastEditStarted);
		String now = UserConfig.getTimeString(lastEditEnded);
		
		return "# Total Time : " + tot + "\n# Edited from : " + lastStart + "\n# Until : " + now + "\n\n";
	}
	
	/*
	 * Timer Listeners
	 */
	
	private final ArrayList<MessageListener> timerEventListeners = new ArrayList<MessageListener>();
	
	@Override
	public void addBroadcastListener(MessageListener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("Listener must not be null.");
		timerEventListeners.add(listener);
		listener.messageEvent(getPlainName(), UserConfig.getMinSec(totalMillis));
	}

	@Override
	public void removeBroadcastListener(MessageListener listener)
	{
		timerEventListeners.remove(listener);
	}
	
}
