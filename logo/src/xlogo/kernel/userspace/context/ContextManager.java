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
import java.util.Stack;

import xlogo.interfaces.MessageBroadcaster;
import xlogo.interfaces.X4SModeSwitcher;

/**
 * One of the four main roles in the {@link xlogo.kernel.userspace.UserSpace} <br>
 * <b> The Contexts and Priorities and multiplicity</b>
 * 1. UserContext [1] : default context, lowest priority, cannot be killed.
 * 2. RecordContext [0..1] : medium priority, killing it will kill all networking contexts too.
 * 3. NetworkContext [0..*] : highest priority, can be stacked.
 * 
 * @author Marko
 */
public class ContextManager implements X4SModeSwitcher, ContextSwitcher, MessageBroadcaster
{
	private LogoContext currentContext;
	
	private final UserContext userContext = new UserContext();
	private RecordContext recordContext;
	private final Stack<NetworkContext> networkStack = new Stack<NetworkContext>();
	
	private MessageListener contextMessageListener;
	
	public ContextManager()
	{
		currentContext = userContext;
		initContextMessageListener();
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * CONTEXT PROVIDER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */

	/**
	 * After a context has been removed by stopXXXMode(), contextSwitch() will determine the next current context.
	 * It only peeks at the context collections, but does not modify them. Calling this does never harm,
	 * but after one of the context collections has been changed, it should be called.<br>
	 * It notifies ContextProviderListeners if the context has actually changed after calling this.
	 * <p>
	 * The policy for context switches is defines in the interface here : {@link X4SModeSwitcher}
	 * <p>
	 * Note, this should be called before the mode switch events, because we first want to completely context switch, before we change the mode.
	 * Generally always publish events after the event has actually occurred.
	 * 
	 */
	protected void contextSwitch()
	{
		LogoContext old = currentContext;
		
		if (networkStack.size() > 0)
			currentContext = networkStack.peek();
		else
			currentContext = recordContext != null ? recordContext : userContext;
		
		if(old != currentContext)
			notifyContextSwitched(currentContext);
	}
	
	/**
	 * returns the current context after the policy described in the in the interface {@link X4SModeSwitcher}
	 */
	@Override
	public LogoContext getContext()
	{
		return currentContext;
	}
	
	// Context switch : internal communication : no direct gui update, must not run on event dispatcher thread
	
	private final ArrayList<ContextSwitchListener> contextSwitchListeners = new ArrayList<ContextSwitcher.ContextSwitchListener>();
	
	@Override
	public void addContextSwitchListener(ContextSwitchListener listener)
	{
		contextSwitchListeners.add(listener);
	}

	@Override
	public void removeContextSwitchListener(ContextSwitchListener listener)
	{
		contextSwitchListeners.remove(listener);
	}
	
	public void notifyContextSwitched(LogoContext newContext)
	{
		for (ContextSwitchListener listener : contextSwitchListeners)
			listener.contextSwitched(newContext);
	}
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * MODE SWITCHER
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	@Override
	public String getSerializedContext()
	{
		return currentContext.toString();
	}

	@Override
	public boolean isUserMode()
	{
		return currentContext instanceof UserContext;
	}
	
	@Override
	public boolean isRecordMode()
	{
		return currentContext instanceof RecordContext;
	}
	
	@Override
	public boolean isNetworkMode()
	{
		return currentContext instanceof NetworkContext;
	}

	/**
	 * A record mode can only be started from user mode. In other words,
	 * a record mode cannot be started, if there is another <b>record or network</b> mode active.
	 * @throws IllegalStateException If current mode is not user mode.
	 * @throws IllegalArgumentException If the fileNames are not well formed, null, or ambiguous
	 * @throws IOException If the record files could not be created => recording is impossible => record mode is impossible
	 */
	@Override
	public void startRecordMode(String[] fileNames) throws IllegalArgumentException, IOException
	{	
		if (!isUserMode())
			throw new IllegalStateException();
		
		recordContext = new RecordContext(fileNames);
		recordContext.addBroadcastListener(contextMessageListener);
		
		contextSwitch();
		
		notifyContestModeStarted();
	}

	/**
	 * This will first regularly kill all network contexts, one after the other. Listeners will be notified for every kill. <br>
	 * Afterwards it will kill the current record context. <br>
	 * When this call returns, current mode is user mode.
	 * 
	 * @throws IllegalStateException If there is no recordContext available
	 */
	@Override
	public void stopRecordMode() throws IllegalStateException
	{
		if (recordContext == null)
			return; // might be a quick double klick that causes this

		while(isNetworkMode())
			popNetworkMode();
		
		recordContext = null;
		
		contextSwitch();
		
		notifyRecordModeStopped();
	}

	/**
	 * @throws IllegalArgumentException If serializedContext is corrupted
	 */
	@Override
	public void pushNetworkMode(String serializedContext) throws IllegalArgumentException
	{
		NetworkContext nc = new NetworkContext(serializedContext);
		networkStack.push(nc);
		
		contextSwitch();
		
		if (networkStack.size() == 1)
			notifyNetworkModeStarted();
	}

	/**
	 * This will kill the current network context.
	 * @throws IllegalStateException - if there is no network context to kill.
	 */
	@Override
	public void popNetworkMode() throws IllegalStateException
	{
		if (!isNetworkMode())
			throw new IllegalStateException("There is no network context to kill.");
			
		networkStack.pop();
		
		contextSwitch();
		
		if (!isNetworkMode())
			notifyNetworkModeStopped();
	}
	
	// Mode Change Listeners : update GUI => run on event dispatcher thread
	
	private ArrayList<ModeChangeListener>	modeChangeListeners	= new ArrayList<ModeChangeListener>();
	
	@Override
	public void addModeChangedListener(ModeChangeListener listener)
	{
		modeChangeListeners.add(listener);
	}

	@Override
	public void removeModeChangedListener(ModeChangeListener listener)
	{
		modeChangeListeners.remove(listener);
	}
	
	private void notifyContestModeStarted()
	{
		for (ModeChangeListener listener : modeChangeListeners)
			listener.recordModeStarted();
	}
	
	private void notifyRecordModeStopped()
	{
		for (ModeChangeListener listener : modeChangeListeners)
			listener.recordModeStopped();
	}
	
	private void notifyNetworkModeStarted()
	{
		for (ModeChangeListener listener : modeChangeListeners)
			listener.networkModeStarted();
	}
		
	private void notifyNetworkModeStopped()
	{
		for (ModeChangeListener listener : modeChangeListeners)
			listener.networkModeStopped();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * CONTEXT MESSAGE LISTENERS (used to broadcast clock events from record mode)
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	
	private void initContextMessageListener()
	{
		contextMessageListener = new MessageListener(){
			
			@Override
			public void messageEvent(String source, String message)
			{
				for (MessageListener listener : broadcastListeners)
					listener.messageEvent(source, message);
			}
		};
	}

	private final ArrayList<MessageListener> broadcastListeners = new ArrayList<MessageListener>();
	
	@Override
	public void addBroadcastListener(MessageListener listener)
	{
		broadcastListeners.add(listener);
	}
	
	@Override
	public void removeBroadcastListener(MessageListener listener)
	{
		broadcastListeners.remove(listener);
	}

}
