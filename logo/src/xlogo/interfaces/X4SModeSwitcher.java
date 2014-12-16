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

package xlogo.interfaces;

import java.io.IOException;

/**
 * X4S USER SPACE MODES<br>
 * 1. User Mode : [lowest priority] (default, a.k.a. root context) cannot be killed, only 1 <br>
 * 2. Contest Mode : [medium priority] only 0..1 can exist. If this is killed, all current network contexts are killed too.<br>
 * 3. Network Mode : [highest priority] 0..n can exist, stacked on each other, 1 active at a time.
 * <p>
 * General Policy: <br>
 * 1. Only one context is active at a time. <br>
 * 2. Priority = if there exists a context which has higher priority, then this context's symbol tables and properties are active. <br>
 * 3. If there exist multiple contexts of the same priority, then the last created is active. <br>
 * 
 * @author Marko Zivkovic
 */
public interface X4SModeSwitcher
{
	public String getSerializedContext();

	public boolean isNetworkMode();

	public boolean isRecordMode();

	public boolean isUserMode();

	
	public void pushNetworkMode(String serializedContext) throws IllegalArgumentException;

	public void popNetworkMode();

	
	public void startRecordMode(String[] fileNames) throws IllegalArgumentException, IllegalStateException, IOException;

	public void stopRecordMode();
	
	
	public void addModeChangedListener(ModeChangeListener listener);
	
	public void removeModeChangedListener(ModeChangeListener listener);
			
	
	public interface ModeChangeListener
	{
		public void recordModeStarted();
		
		public void recordModeStopped();
		
		public void networkModeStarted();
		
		public void networkModeStopped();
	}
	
}
