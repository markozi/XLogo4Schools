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

package xlogo.messages.async.history;

import xlogo.gui.HistoryPanel;
import xlogo.messages.async.AbstractAsyncMessenger;
import xlogo.messages.async.AsyncMessage;
import xlogo.messages.async.history.HistoryMessage.HistoryMessageType;

/**
 * This class helps to decouple various old XLogo classes from {@link Application} and {@link HistoryPanel}
 * @author Marko Zivkovic
 * @superclass {@link AbstractAsyncMessenger}
 */
public class HistoryMessenger extends AbstractAsyncMessenger<AsyncMessage<HistoryWriter>, HistoryWriter>
{	
	/**
	 * Implementation for thread-safe singleton found here : http://www.theserverside.de/singleton-pattern-in-java/  [accessed 14.8.2013]
	 */
	private static HistoryMessenger instance = new HistoryMessenger();
	
	private HistoryMessenger() {}
	
	public static HistoryMessenger getInstance()
	{
		return instance;
	}
	
	/**
	 * Normal Logo style : syntax highlighter
	 */
	@Override
	public void dispatchMessage(String message)
	{
		deliverMessage(new HistoryMessage(HistoryMessageType.NORMAL, message));
	}
	
	/**
	 * Printed in red.
	 * @param message
	 */
	public void dispatchError(String message)
	{
		deliverMessage(new HistoryMessage(HistoryMessageType.ERROR, message));
	}
	
	/**
	 * Printed in blue
	 * @param message
	 */
	public void dispatchComment(String message)
	{
		deliverMessage(new HistoryMessage(HistoryMessageType.COMMENT, message));
	}
	
	public void dispatchLogoOutput(String message)
	{
		deliverMessage(new HistoryMessage(HistoryMessageType.LOGO_OUTPUT, message));
	}
}