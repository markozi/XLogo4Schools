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
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

/**
 * Title : XLogo Description : XLogo is an interpreter for the Logo programming
 * language
 * 
 * @author Loïc Le Coq
 */
package xlogo.kernel;

import java.util.Stack;
import java.util.HashMap;
import java.awt.event.*;

import javax.swing.SwingUtilities;

import xlogo.Application;
import xlogo.messages.async.history.HistoryMessenger;
import xlogo.utils.Utils;
import xlogo.MemoryChecker;
import xlogo.Logo;

// Ce thread gère l'animation de la tortue pendant l'exécution
/**
 * This Thread is responsible of the turtle animation.
 * This animation has to be executed in a separated thread, else it will block
 * the event dispatcher thread
 */
public class Affichage extends Thread
{
	public static boolean	execution_lancee	= false;
	private boolean			pause				= false;
	private Application		cadre;
	private StringBuffer	instruction;
	private Souris			souris				= new Souris();
	private MemoryChecker	memoryChecker					= null;
	
	public Affichage()
	{
	}
	
	public Affichage(Application cadre, StringBuffer instruction)
	{
		this.cadre = cadre;
		this.instruction = instruction;
	}
	
	class Souris extends MouseAdapter
	{ // Si on déplace les Scrollbar pendant
		// le dessin
		public Souris()
		{
		} // Ceci permet d'interrompre momentanément l'exécution
		
		public void mousePressed(MouseEvent e)
		{
			pause = true;
		}
		
		public void mouseReleased(MouseEvent e)
		{
			pause = false;
		}
	}
	
	public void run()
	{
		// currentThread().setPriority(Thread.MIN_PRIORITY);
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				cadre.setCommandLine(false);// la ligne de commandes
											// n'est plus active
			}
		});
		execution_lancee = true;
		cadre.getDrawPanel().active_souris(); // On active les événements souris
											// sur
		// la zone de dessin
		cadre.scrollArea.getVerticalScrollBar().addMouseListener(souris);
		cadre.scrollArea.getHorizontalScrollBar().addMouseListener(souris);
		try
		{
			cadre.setCar(-1);
			cadre.error = false;
			Interprete.operande = Interprete.operateur = Interprete.drapeau_ouvrante = false;
			cadre.getKernel().getInstructionBuffer().clear();
			Interprete.calcul = new Stack<String>();
			Interprete.nom = new Stack<String>();
			Interprete.locale = new HashMap<String, String>();
			Interprete.en_cours = new Stack<String>();
			memoryChecker = new MemoryChecker(cadre);
			memoryChecker.start();
			boolean b = true;
			while (b)
			{
				String st = cadre.getKernel().execute(instruction);
				if (!st.equals(""))
					throw new LogoError(Logo.messages.getString("error.whattodo") + " " + st + " ?");
				if (Interprete.actionInstruction.length() == 0)
					b = false;
				else
				{
					instruction = Interprete.actionInstruction;
					Interprete.actionInstruction = new StringBuffer();
				}
			}
		}
		catch (LogoError e)
		{
			// if (st.equals("siwhile")) st=Logo.messages.getString("tantque");
			while (!Interprete.en_cours.isEmpty() && Interprete.en_cours.peek().equals("("))
				Interprete.en_cours.pop();
			if (!cadre.error & !Interprete.en_cours.isEmpty())
			{
				HistoryMessenger.getInstance().dispatchError(
						Logo.messages.getString("dans") + " " + Interprete.en_cours.pop() + ", "
								+ Logo.messages.getString("line") + " " + getLineNumber() + ":\n");
			}
			if (!cadre.error)
				HistoryMessenger.getInstance().dispatchError(Utils.SortieTexte(e.getMessage()) + "\n");
			abortExecution();
		}
		cadre.setCommandLine(true);
		if (!cadre.viewer3DVisible())
			cadre.focusCommandLine();
		execution_lancee = false;
		memoryChecker.kill();
		cadre.error = false;
		cadre.scrollArea.getVerticalScrollBar().removeMouseListener(souris);
		cadre.scrollArea.getHorizontalScrollBar().removeMouseListener(souris);
	}
	
	private void abortExecution()
	{
		cadre.focusCommandLine();
		cadre.error = true;
		Interprete.calcul = new Stack<String>();
		cadre.getKernel().getInstructionBuffer().clear();
		Primitive.stackLoop = new Stack<LoopProperties>();
	}
	
	private int getLineNumber()
	{
		String string = Interprete.lineNumber;
		// System.out.println("bb"+string+"bb");
		if (string.equals(""))
			string = cadre.getKernel().getInstructionBuffer().toString();
		// System.out.println("cc"+string+"cc");
		int id = string.indexOf("\\l");
		if (id != -1)
		{
			String lineNumber = "";
			int i = id + 2;
			char c = string.charAt(i);
			while (c != ' ')
			{
				lineNumber += c;
				i++;
				c = string.charAt(i);
			}
			// System.out.println(lineNumber);
			return Integer.parseInt(lineNumber);
		}
		return 1;
	}
	
	protected boolean isOnPause()
	{
		return pause;
	}
	
	/**
	 * @param b
	 * @uml.property name="pause"
	 */
	public void setPause(boolean b)
	{
		pause = b;
	}
}
