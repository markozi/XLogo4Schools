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

package xlogo.gui.components;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

import xlogo.messages.async.AsyncMediumAdapter;
import xlogo.messages.async.AsyncMessage;
import xlogo.messages.async.AsyncMessenger;
import xlogo.messages.async.dialog.DialogMessenger;

public abstract class X4SFrame extends X4SGui{

	public X4SFrame() {
		super();
	}
	
	public abstract JFrame getFrame();
	
	public void showFrame()
	{
		setMessageManagerParent();
		getFrame().setVisible(true);
	}
	
	public void closeFrame()
	{
		getFrame().dispose();
	}
	
	/**
	 * Make this frame the parent for popups and dialogs
	 */
	protected void setMessageManagerParent()
	{
		DialogMessenger.getInstance().setMedium(new AsyncMediumAdapter<AsyncMessage<JFrame>, JFrame>(){
			public boolean isReady()
			{
				return getFrame().isDisplayable();
			}
			public JFrame getMedium()
			{
				return getFrame();
			}
			public void addMediumReadyListener(final AsyncMessenger messenger)
			{
				getFrame().addWindowStateListener(new WindowStateListener(){
					
					@Override
					public void windowStateChanged(WindowEvent e)
					{
						if (getFrame().isDisplayable())
							messenger.onMediumReady();
					}
				});
			}
		});
	}

}
