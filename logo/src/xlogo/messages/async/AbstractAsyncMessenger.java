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

package xlogo.messages.async;

import java.util.concurrent.ConcurrentLinkedQueue;

import xlogo.Application;

/**
 * <p> This class was implemented with the purpose to decouple various parts of XLogo4Schools from the Application-class. <br>
 * Before, almost every part had to have a reference to {@link Application}. Without the reference to the current frame, no errors or other messages could be displayed.
 * <p>
 * This class also allows to dispatch messages before a Medium (such as JFrame for dialogs or HistoryPanel for history messages) is set up. Therefore it maintains an internal queue of messages.
 * As soon as the medium is set up and ready, the queue will be emptied by displaying all the messages, one after the other.
 * <p>
 * This class is thread-safe.
 */
public abstract class AbstractAsyncMessenger<M extends AsyncMessage<T>, T> implements AsyncMessenger
{
	AsyncMediumAdapter<M, T> mediumAdapter;
	private ConcurrentLinkedQueue<M> messageQueue;
	private boolean isWorking;
	
	public AbstractAsyncMessenger()
	{
		messageQueue = new ConcurrentLinkedQueue<M>();
	}
	
	/**
	 * @return whether medium is set (not null) and ready
	 */
	protected synchronized boolean isMediumReady()
	{
		return mediumAdapter != null && mediumAdapter.isReady();
	}
	
	/**
	 * @return Whether the worker thread is currently not working off the message
	 */
	protected synchronized boolean isReady()
	{
		return isMediumReady() && !isWorking;
	}
	
	private synchronized void setIsWorking(boolean isWorking)
	{
		this.isWorking = isWorking;
	}
	
	public synchronized void setMedium(AsyncMediumAdapter<M, T> medium)
	{
		this.mediumAdapter = medium;
		
		if (medium == null)
			return;
		
		if (isReady())
			workOffQueue();
		else
			medium.addMediumReadyListener(this);
	}
	
	/**
	 * This should be called by
	 */
	public void onMediumReady()
	{
		workOffQueue();
	}
	
	protected void installReadyEventListener(AsyncMediumAdapter<M, T> medium)
	{
		medium.addMediumReadyListener(this);
	}
	
	protected synchronized AsyncMediumAdapter<M, T> getMedium()
	{
		return mediumAdapter;
	}
			
	protected void deliverMessage(M message)
	{
		messageQueue.add(message);
		if (isReady())
			workOffQueue();
	}
	
	protected synchronized void workOffQueue()
	{
		setIsWorking(true);
		
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				while(!messageQueue.isEmpty() && isMediumReady())
				{
					M msg = messageQueue.peek();
					prepareMessage(msg);
					msg.displayMessage();
					messageQueue.remove();
				}
				
				setIsWorking(false);
			}
		}).run();
	}
	
	/**
	 * @param message
	 */
	protected void prepareMessage(M message){
		message.setMedium(mediumAdapter.getMedium());
	}
	
}
