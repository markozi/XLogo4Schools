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

package xlogo.messages.async.dialog;

import javax.swing.JFrame;

import xlogo.messages.async.AbstractAsyncMessenger;
import xlogo.messages.async.AsyncMessage;
import xlogo.messages.async.dialog.DialogMessage.DialogMessageType;

/**
 * The MessageManager is a singleton class that can be globally used to display error messages in dialogs.
 *  
 * @author Marko Zivkovic
 *
 */
public class DialogMessenger extends AbstractAsyncMessenger<AsyncMessage<JFrame>, JFrame>
{
	/**
	 * Implementation for thread-safe singleton found here : http://www.theserverside.de/singleton-pattern-in-java/  [accessed 14.8.2013]
	 */
	private static DialogMessenger instance = new DialogMessenger();
	
	public static DialogMessenger getInstance()
	{
		return instance;
	}
	
	protected DialogMessenger()
	{
		super();
	}
	
	@Override
	public void dispatchMessage(String message)
	{
		deliverMessage(new DialogMessage(DialogMessageType.PLAIN, "", message));
	}
	
	public void dispatchMessage(String title, String message)
	{
		deliverMessage(new DialogMessage(DialogMessageType.PLAIN, title, message));
	}
	
	public void dispatchError(String title, String message)
	{
		deliverMessage(new DialogMessage(DialogMessageType.ERROR, title, message));
	}
}
