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
 * Contents of this file were entirely written Marko Zivkovic 
 */

package xlogo.kernel;

/**
 * In XLogo, myException was used. My exception was something rather funny.
 * Actually it was really bad. myException was an exception that handled itself, by showing an error dialog and by aborting execution of the running Logo program.
 * Thus throwing a myException was more like a function call with the side effect, that execution jumped to some other place, wherever it was caught.
 * Note that to handle itself, myException had to have a reference to {@link Application}. Thus every part of the interpreter that wanted to throw Logo errors
 * had to have such a reference, even though Application was not used otherwise.<br>
 * By moving the exception handling at the root, where interpretation is started, in {@link Affichage}, I managed to decouple several classes from Application.
 * (Still, many of them are unnecessarily dependent on Application, but I cannot refactor everything in the given time)
 * 
 * Also note how ugly myException was used before. LaunchPrimitive is the best example. The general pattern was this:<br>
 * 
 * <pre>
 * {@code
 * case i: // i : the id of some Logo primitive
 *  try
 *  {
 *   ...
 *   if (the next token is not as expected)
 *    throw new myException(application, errorMessage);
 *   ...
 *  }
 *  catch (myException)
 *  {}
 *  break; // => end of execute()
 *  // Because myException "handled itself", Affichage and Interprete will not continue execution
 * }
 * </pre>
 * 
 * Note that almost all of the more than 300 Logo primitives contained such a statement, sometimes even more.
 * That is more than 5x300 = 1500 unnecessary lines of code (after my style guide) that make the reading very hard,
 * and introducing multiple levels of blocks { } to the structure.
 * 
 * I took myself the time to remove all these unnecessary try-catches, place only one in Affichage and thus make the core of the application much more readable.
 * <p><p>
 * Note that I cannot test all the 300 Logo procedures for every case, but since throwing a myException caused the Interpreter to stop anyway,
 * Redirecting the exception up to the root in Affichage will not change the flow of correct Logo Execution. Only executions which caused an exception can maybe behave slightly different now.
 * I will for sure test all the procedures that we use in our school projects and a few more.
 * 
 * <p><p>
 * Another interesting thing, or again, really bad.
 * The {@code static boolean lance} in myException was initialized with false. There are several assignments that set, again, false to it, but there is no single set to true.
 * In most cases where lance is tested in a if-statement, it is tested in conjunction with Application.error (which can be true or false)
 * 
 * <p> And then, there was also the nested class Affiche, which was never used.
 * 
 *@author Marko Zivkovic
 */
public class LogoError extends Exception
{
	private static final long	serialVersionUID	= 9184760816698357437L;
	
	public LogoError()
	{
	}
	
	public LogoError(String st)
	{
		super(st);
	}
}
