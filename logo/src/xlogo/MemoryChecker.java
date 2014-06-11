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
 * Contents of this file were initially written by Loïc Le Coq,
 * a lot of modifications, extensions, refactorings have been applied by Marko Zivkovic 
 */

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author LoÃ¯c Le Coq
 **/
package xlogo;

import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.global.GlobalConfig;

/**
 * This class is a thread that prevents from memory Overflow <br>
 * Those problems could happen when a program loops indefinitely<br>
 * Eg:<br>
 * <br>
 * <tt>
 * To bad<br>
 * fd 1 rt 1 <br>
 * bad<br>
 * this lines will explode memory<br>
 * end <br>
 * </tt>
 * 
 * @author loic
 * 
 * @author Marko Zivkovic - Extensive tests and provocation of the memory usage error message have shown
 * that in XLogo4Schools, using a maximum 128MB heap, the memory threshold is never reached
 * (Or very, very late in a gigantic recursion).
 * 
 */
public class MemoryChecker extends Thread
{
	/**
	 * The main frame
	 */
	private Application	cadre;
	/**
	 * This boolean indicates if the thread has to continue.<br>
	 * If false, the thread will stop.
	 */
	private boolean		alive;
	
	/**
	 * Constructs the Memory Checker for the main Frame
	 * 
	 * @param cadre
	 *            the main Frame
	 */
	public MemoryChecker(Application cadre)
	{
		this.cadre = cadre;
	}
	
	private static int maxSleepTime = 10000;
	private static int minSleepTime = 500;
	private static int sleepRange = maxSleepTime - minSleepTime;
	
	/**
	 * The Run Method for the Thread
	 */
	public void run()
	{		
		/*
		 * Marko : I reduced the amount of calculations done in every iteration.
		 * Before it included 1 subtraction and 2 divisions and a fetch from GlobalConfig,
		 * now only 1 subtraction is used to determine consumed > consumptionThreshold
		 * and another long comparison was added to regulate frequency of memory checks.
		 */
		long consumptionThreshold =  GlobalConfig.getMemoryThreshold();
		long listenThreshold = (long) (0.8 * consumptionThreshold);

		
		/*
		 * I increased default sleeping time to about 10 seconds, because a check every 1 second is too much overhead for doing nothing useful.
		 * However, as soon as the listenThreshold is reached, the sleepTime will start to decrease.
		 * Only once the listenThreshold is reached,
		 * the MemoryChecker becomes important to prevent an OutOfMemoryError and start suggesting the garbage collector to work.)
		 */
		int sleepTime = maxSleepTime;
		
		alive = true;
		
		while (alive)
		{
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException ignore)
			{}
			long free = Runtime.getRuntime().freeMemory();
			long total = Runtime.getRuntime().totalMemory();
			long consumed = (total - free);
			
			if (consumed >= listenThreshold)
			{
				sleepTime = (int) (maxSleepTime - consumed / total * sleepRange);
				
				/*
				 * Marko : 
				 * In XLogo, when the error dialog was displayed, it usually kept displaying several times, disrupting any further use of the interpreter.
				 * The only out was to restart the application. This indicates that, although heavy memory usage was detected by this thread,
				 * the garbage collector did not yet start to free memory from dead objects.
				 * (Discussions about GC in the Internet indicate that the Java GC starts working as soon as a heap generation gets full.
				 * It is possible that the young generation is not full enough when 0.9*MaxMemory is reached, thus the message keeps appearing all the time,
				 * because memory is not sufficiently cleaned up.)
				 * Therefore, at least suggest the GC that it would be a good moment for garbage collection, and help recover. That's the best we can do.
				 */
				System.gc();
			}
			else
				sleepTime = maxSleepTime; 
			
			if (consumed > consumptionThreshold)
			{
				cadre.error = true;
				alive = false;
				DialogMessenger.getInstance().dispatchError(Logo.messages.getString("erreur"),
						Logo.messages.getString("depassement_memoire"));
			}
		}
	}
	
	/**
	 * Causes the memory checker loop to break.
	 * @author Marko
	 */
	public void kill()
	{
		alive = false;
	}
	
}
