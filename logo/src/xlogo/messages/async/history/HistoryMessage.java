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

package xlogo.messages.async.history;

import xlogo.messages.async.AsyncMessage;

class HistoryMessage extends AsyncMessage<HistoryWriter>
{

	public HistoryMessage(HistoryMessageType messageType, String message)
	{
		super(messageType.toString(), message);
	}

	@Override
	public void displayMessage()
	{
		getMedium().writeMessage(getTitle(), getMessage());
	}
	
	
	public enum HistoryMessageType
	{
		/*
		 * Note: String values inherited from XLogo, such that HistoryPanel must not be changed too much (for now)
		 */
		
		/**
		 * Syntax highlighted
		 */
		NORMAL("normal"),
		ERROR("erreur"),
		/**
		 * When the user leaves the editor "You defined ..."
		 */
		COMMENT("commentaire"),
		/**
		 * Commands write or print
		 */
		LOGO_OUTPUT("perso");
		
		String type;
		
		private HistoryMessageType(String type)
		{
			this.type = type;
		}
		
		public String toString()
		{
			return type;
		}
	}
}
