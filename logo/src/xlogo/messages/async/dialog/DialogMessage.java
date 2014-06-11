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

package xlogo.messages.async.dialog;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import xlogo.messages.async.AsyncMessage;

class DialogMessage extends AsyncMessage<JFrame>
{	
	private DialogMessageType type;
	
	public DialogMessage(DialogMessageType type, String title, String message)
	{
		super(title, message);
		this.type = type;
	}
	
	@Override
	public void displayMessage()
	{
		JOptionPane.showMessageDialog(getMedium(),
			getMessage(),
		    getTitle(),
		    type.getOptionPaneInt());
	}
	
	public enum DialogMessageType
	{
		ERROR(JOptionPane.ERROR_MESSAGE),
		WARNING(JOptionPane.WARNING_MESSAGE),
		PLAIN(JOptionPane.PLAIN_MESSAGE),
		QUESTION(JOptionPane.QUESTION_MESSAGE),
		INFORMATION(JOptionPane.INFORMATION_MESSAGE);
		
		private int msgType;
		
		private DialogMessageType(int optionPaneInt)
		{
			msgType = optionPaneInt;
		}
		
		public int getOptionPaneInt()
		{
			return msgType;
		}
	}
	
}
