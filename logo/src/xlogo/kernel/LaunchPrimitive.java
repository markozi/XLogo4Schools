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
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq
 */
package xlogo.kernel;

import java.util.Stack;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.*;

import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.vecmath.Point3d;

import java.math.BigDecimal;

import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.user.DrawQuality;
import xlogo.storage.user.PenShape;
import xlogo.storage.user.UserConfig;
import xlogo.storage.workspace.Language;
import xlogo.utils.Utils;
import xlogo.gui.Lis;
import xlogo.gui.MyTextAreaDialog;
import xlogo.kernel.DrawPanel;
import xlogo.gui.HistoryPanel;
import xlogo.Application;
import xlogo.Logo;
import xlogo.kernel.network.*;
import xlogo.kernel.gui.*;
import xlogo.kernel.perspective.ElementPolygon;
import xlogo.kernel.perspective.ElementLine;
import xlogo.kernel.perspective.ElementPoint;
import xlogo.kernel.userspace.UserSpace;
import xlogo.kernel.userspace.files.LogoFile;
import xlogo.kernel.userspace.procedures.Procedure;
import xlogo.messages.async.history.HistoryMessenger;

/*******************************************************************************
 * When a primitive or a procedure has all arguments, LauchPrimitive executes
 * the appropriate code.
 ******************************************************************************/
public class LaunchPrimitive
{
	/**
	 * Default Application frame
	 */
	private Application			cadre;
	/**
	 * Default kernel
	 */
	private Kernel				kernel;
	/**
	 * Default workspace
	 */
	private UserSpace			wp;
	
	private Procedure			procedure;
	// private MathContext mc=MathContext.DECIMAL64;
	/**
	 * This is the start for the String returned by primitive or procedure.<br>
	 * It is "\"" for words and "" for numbers. <br>
	 * <br>
	 * Ceci est le début de la chaine générique renvoyé par les primitives<br>
	 * Elle vaut "\"" pour les mots et "" pour les nombres<br>
	 */
	private String				debut_chaine	= "";
	/**
	 * When we launch the primitive "listentcp", we have to save workspaces
	 */
	//private Stack<LogoContext>	savedWorkspace;
	
	/**
	 * @param cadre
	 *            Default frame Application
	 * @param wp
	 *            Default workspace
	 */
	public LaunchPrimitive(Application cadre, UserSpace wp)
	{
		this.wp = wp;
		this.cadre = cadre;
		this.kernel = cadre.getKernel();
	}
	
	/**
	 * Execute the primitive number "id" with the arguments contained in "param"<br>
	 * <ul>
	 * <li>if id<0: it is a procedure. <br>
	 * For example, if id=-3, it is procedure number -i-2=-(-3)-2=1</li>
	 * <li>if d>=0: it is primitive number "id"</li>
	 * </ul>
	 * 
	 * @param id
	 *            The number representing the procedure or the primitive
	 * @param param
	 *            The Stack that contains all arguments
	 */
	protected void execute(int id, String element_minuscule, Stack<String> param) throws LogoError
	{
		UserConfig uc = WSManager.getUserConfig();
		int w = uc.getImageWidth();
		int h = uc.getImageHeight();
		Font font = WSManager.getWorkspaceConfig().getFont();
		String defaultFolder;
		
		// identifiant procédure ou primitive, valeur des paramètres
		if (id < 0)
		{
			procedure = wp.getExecutable(element_minuscule);
			Interprete.stockvariable.push(Interprete.locale);
			Interprete.locale = new HashMap<String, String>();
			// Read local Variable
			int optSize = procedure.optVariables.size();
			int normSize = procedure.variable.size();
			for (int j = 0; j < optSize + normSize; j++)
			{
				// Add local Variable
				if (j < normSize)
				{
					Interprete.locale.put(procedure.variable.get(j), param.get(j));
				}	// add optional variables
				else
				{
					String value = "";
					if (j < param.size())
						value = param.get(j);
					else
						value = procedure.optVariablesExp.get(j - param.size()).toString();
					Interprete.locale.put(procedure.optVariables.get(j - normSize), value);
					
				}
			}
			// Add Optional variable
			if (Kernel.mode_trace)
			{
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < Interprete.en_cours.size(); i++)
					buffer.append("  ");
				buffer.append(procedure.name);
				for (int i = 0; i < param.size(); i++)
					buffer.append(" " + Utils.SortieTexte(param.get(i)));
				String msg = buffer + "\n";
				HistoryMessenger.getInstance().dispatchMessage(msg);
			}
			Interprete.en_cours.push(procedure.name);
			procedure.decoupe();
			// Add Procedure code in Interprete.instruction
			kernel.getInstructionBuffer().insert("\n ");
			kernel.getInstructionBuffer().insertCode(procedure.instr);
			// System.out.println("instr " +Interprete.instruction);
			// System.out.println("stock "+Interprete.stockInstruction);
			// System.out.println("a"+Interprete.instruction+"a");
			Interprete.nom.push("\n");
		}
		else
		{
			Language lang = WSManager.getInstance().getWorkspaceConfigInstance().getLanguage();
			
			switch (id)
			{
				case 0: // av
					delay();
					cadre.getDrawPanel().av(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 1: // re
					delay();
					cadre.getDrawPanel().av(-kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 2: // td
					delay();
					cadre.getDrawPanel().td(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 3: // tg
					delay();
					cadre.getDrawPanel().td(-kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 4: // arithmetic.power puissance
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().power(param.get(0), param.get(1)));
					break;
				case 5: // repete controls.repeat
					String liste = getList(param.get(1));
					kernel.primitive.repete(kernel.getCalculator().getInteger(param.get(0)), liste);
					break;
				case 6: // ve
					cadre.getDrawPanel().videecran();
					break;
				case 7: // ct
					if (kernel.getActiveTurtle().isVisible())
					{
						cadre.getDrawPanel().ct_mt();
						cadre.getDrawPanel().tortues_visibles.remove(String.valueOf(kernel.getActiveTurtle().id));
					}
					kernel.getActiveTurtle().setVisible(false);
					break;
				case 8: // mt
					if (!kernel.getActiveTurtle().isVisible())
					{
						cadre.getDrawPanel().ct_mt();
						cadre.getDrawPanel().tortues_visibles.push(String.valueOf(kernel.getActiveTurtle().id));
					}
					kernel.getActiveTurtle().setVisible(true);
					break;
				case 9: // ecris, ec
					int size = param.size();
					String result = "";
					String mot;
					for (int i = 0; i < size; i++)
					{
						String par = param.get(i).trim();
						if (isList(par))
							par = formatList(par.substring(1, par.length() - 1));
						mot = getWord(param.get(i));
						if (null == mot)
							result += Utils.SortieTexte(par) + " ";
						else
							result += Utils.SortieTexte(mot) + " ";
					}
					HistoryMessenger.getInstance().dispatchLogoOutput(result + "\n");
					break;
				case 10: // si // if
					
					liste = getList(param.get(1));
					liste = new String(Utils.decoupe(liste));
					String liste2 = null;
					boolean predicat = predicat(param.get(0));
					InstructionBuffer instruction = cadre.getKernel().getInstructionBuffer();
					if (instruction.getLength() != 0)
					{
						try
						{
							String element = instruction.getNextWord();
							// System.out.println("a"+element+"a");
							if (element.startsWith("\\l"))
							{
								instruction.deleteFirstWord(element);
								Interprete.lineNumber = element + " ";
							}
							if (instruction.charAt(0) == '[')
							{
								instruction.deleteFirstWord("[");
								liste2 = getFinalList(kernel.listSearch());
								liste2 = new String(Utils.decoupe(liste2));
							}
						}
						catch (Exception e)
						{}
					}
					kernel.primitive.si(predicat, liste, liste2);
					Interprete.renvoi_instruction = true;
					break;
				case 11: // STOP
					kernel.primitive.stop();
					break;
				case 12: // origine
					delay();
					cadre.getDrawPanel().origine();
					break;
				case 13: // fpos
					delay();
					String list = getFinalList(param.get(0));
					cadre.getDrawPanel().fpos(list);
					break;
				case 14: // fixex
					delay();
					if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
					{
						double x = kernel.getCalculator().numberDouble(param.get(0));
						double y = h / 2 - kernel.getActiveTurtle().corY;
						cadre.getDrawPanel().fpos(x + " " + y);
					}
					else
						cadre.getDrawPanel().fpos(
								kernel.getCalculator().numberDouble(param.get(0)) + " " + kernel.getActiveTurtle().Y
										+ " " + kernel.getActiveTurtle().Z);
					break;
				case 15: // fixey
					delay();
					if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
					{
						double y = kernel.getCalculator().numberDouble(param.get(0));
						double x = kernel.getActiveTurtle().corX - w / 2;
						cadre.getDrawPanel().fpos(x + " " + y);
					}
					else
						cadre.getDrawPanel().fpos(
								kernel.getActiveTurtle().X + " " + kernel.getCalculator().numberDouble(param.get(0))
										+ " " + kernel.getActiveTurtle().Z);
					break;
				case 16: // fixexy
					delay();
					primitive2D("drawing.fixexy");
					cadre.getDrawPanel().fpos(
							kernel.getCalculator().numberDouble(param.get(0)) + " "
									+ kernel.getCalculator().numberDouble(param.get(1)));
					break;
				case 17: // fixecap
					delay();
					if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
						cadre.getDrawPanel().td(
								360 - kernel.getActiveTurtle().heading
										+ kernel.getCalculator().numberDouble(param.pop()));
					else
					{
						cadre.getDrawPanel().setHeading(kernel.getCalculator().numberDouble(param.pop()));
					}
					break;
				case 18: // lc
					kernel.getActiveTurtle().setPenDown(false);
					break;
				case 19: // bc
					kernel.getActiveTurtle().setPenDown(true);
					break;
				case 20: // gomme
					kernel.getActiveTurtle().setPenDown(true);
					// if mode penerase isn't active yet
					if (kernel.getActiveTurtle().couleurmodedessin.equals(kernel.getActiveTurtle().couleurcrayon))
					{
						kernel.getActiveTurtle().couleurmodedessin = kernel.getActiveTurtle().couleurcrayon;
						kernel.getActiveTurtle().couleurcrayon = cadre.getDrawPanel().getBackgroundColor();
						kernel.getActiveTurtle().stroke = new BasicStroke(3); // TODO
																				// make
																				// member
					}
					break;
				case 21: // inversecrayon
					kernel.getActiveTurtle().setPenDown(true);
					kernel.getActiveTurtle().setPenReverse(true);
					break;
				case 22: // dessine
					kernel.getActiveTurtle().setPenReverse(false);
					kernel.getActiveTurtle().setPenDown(true);
					kernel.getActiveTurtle().couleurcrayon = kernel.getActiveTurtle().couleurmodedessin;
					kernel.getActiveTurtle().stroke = new BasicStroke(1); // TODO
																			// make
																			// member
					break;
				case 23: // somme
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().add(param));
					break;
				
				case 24: // difference
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().substract(param));
					break;
				case 25: // arithmetic.minus moins (opposé)
					Interprete.calcul.push(kernel.getCalculator().minus(param.get(0)));
					Interprete.operande = true;
					break;
				case 26: // produit
					Interprete.calcul.push(kernel.getCalculator().multiply(param));
					Interprete.operande = true;
					break;
				case 27: // div
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().divide(param));
					break;
				case 28: // reste
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().remainder(param.get(0), param.get(1)));
					break;
				case 29: // retourne
					kernel.primitive.retourne(param.get(0));
					break;
				case 30: // *
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().multiply(param));
					break;
				case 31: // diviser /
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().divide(param));
					break;
				case 32: // +
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().add(param));
					break;
				case 33: // -
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().substract(param));
					break;
				case 34: // =
					equal(param);
					break;
				case 35: // <
					inf(param);
					break;
				case 36: // >
					sup(param);
					break;
				case 37: // |
					boolean b1 = predicat(param.get(0));
					boolean b2 = predicat(param.get(1));
					b1 = b1 | b2;
					if (b1)
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					Interprete.operande = true;
					break;
				case 38: // &
					b1 = predicat(param.get(0));
					b2 = predicat(param.get(1));
					b1 = b1 & b2;
					if (b1)
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					Interprete.operande = true;
					break;
				case 39: // opérateur interne \n signalant une fin de
							// procédure
					Interprete.locale = Interprete.stockvariable.pop();
					if (Interprete.nom.peek().equals("\n"))
					{
						Interprete.nom.pop();
						Interprete.lineNumber = "";
					}
					else
					{
						/*
						 * Example
						 * to bug
						 * av
						 * end
						 */
						throw new LogoError(Logo.messages.getString("pas_assez_de") + " " + Interprete.nom.peek());
					}
					/*
					 * to bug [:a] | (bug 10)
					 * av :a |
					 * end |
					 */
					if (!Interprete.nom.isEmpty() && !Interprete.nom.peek().equals("\n")
							&& !Interprete.nom.peek().equals("("))
					{
						if (!cadre.error)
							throw new LogoError(Interprete.en_cours.peek() + " "
									+ Logo.messages.getString("ne_renvoie_pas") + " "
									+ Interprete.nom.peek().toString());
					}
					if (!Interprete.en_cours.isEmpty())
						Interprete.en_cours.pop();
					break;
				case 40: // opérateur interne \ signalant une fin de boucle
					
					LoopProperties loop = Primitive.stackLoop.peek();
					// LOOP REPEAT
					if (loop.isRepeat())
					{
						BigDecimal compteur = loop.getCounter();
						BigDecimal fin = loop.getEnd();
						if (compteur.compareTo(fin) < 0)
						{
							loop.incremente();
							Primitive.stackLoop.pop();
							Primitive.stackLoop.push(loop);
							cadre.getKernel().getInstructionBuffer().insert(loop.getInstr() + Primitive.END_LOOP + " ");
						}
						else if (compteur.compareTo(fin) == 0)
						{
							Primitive.stackLoop.pop();
						}
					}
					// LOOP FOR or LOOP FOREACH
					else if (loop.isFor() || loop.isForEach())
					{
						BigDecimal inc = loop.getIncrement();
						BigDecimal compteur = loop.getCounter();
						BigDecimal fin = loop.getEnd();
						if ((inc.compareTo(BigDecimal.ZERO) == 1 && (compteur.add(inc).compareTo(fin) <= 0))
								|| (inc.compareTo(BigDecimal.ZERO) == -1 && (compteur.add(inc).compareTo(fin) >= 0)))
						{
							loop.incremente();
							((LoopFor) loop).AffecteVar(false);
							Primitive.stackLoop.pop();
							Primitive.stackLoop.push(loop);
							cadre.getKernel().getInstructionBuffer().insert(loop.getInstr() + Primitive.END_LOOP + " ");
						}
						else
						{
							((LoopFor) loop).DeleteVar();
							Primitive.stackLoop.pop();
						}
					}
					// LOOP FOREVER
					else if (loop.isForEver())
					{
						cadre.getKernel().getInstructionBuffer().insert(loop.getInstr() + Primitive.END_LOOP + " ");
					}
					// LOOP FILL POLYGON
					else if (loop.isFillPolygon())
					{
						cadre.getDrawPanel().stopRecord2DPolygon();
						Primitive.stackLoop.pop();
					}
					break;
				case 41: // pos
					Interprete.operande = true;
					if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
					{
						double a = kernel.getActiveTurtle().corX - w / 2;
						double b = h / 2 - kernel.getActiveTurtle().corY;
						Interprete.calcul.push("[ " + MyCalculator.teste_fin_double(a) + " "
								+ MyCalculator.teste_fin_double(b) + " ] ");
					}
					else
					{
						Interprete.calcul.push("[ " + kernel.getActiveTurtle().X + " " + kernel.getActiveTurtle().Y
								+ " " + kernel.getActiveTurtle().Z + " ] ");
						
					}
					break;
				case 42: // cap
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().heading));
					break;
				case 43: // arrondi
					Interprete.operande = true;
					Interprete.calcul
							.push(String.valueOf(Math.round(kernel.getCalculator().numberDouble(param.get(0)))));
					break;
				case 44: // log10
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().log10(param.get(0)));
					break;
				case 45: // arithmetic.sin
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().sin(param.get(0)));
					break;
				case 46: // arithmetic.cos
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().cos(param.get(0)));
					break;
				case 47: // ou
					ou(param);
					break;
				case 48: // et
					et(param);
					break;
				case 49: // non
					Interprete.operande = true;
					b1 = predicat(param.get(0));
					if (b1)
						Interprete.calcul.push(Logo.messages.getString("faux"));
					else
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					break;
				case 50: // liste
					liste = "[ ";
					Interprete.operande = true;
					String mot2;
					for (int i = 0; i < param.size(); i++)
					{
						mot2 = param.get(i);
						mot = getWord(param.get(i));
						if (null == mot)
						{
							liste += mot2;
							// System.out.println("a"+mot2+"a");
						}
						else
						{
							if (mot.equals(""))
								mot = "\\v";
							liste += mot + " ";
						}
					}
					Interprete.calcul.push(liste + "] ");
					break;
				case 51: // phrase
					liste = "[ ";
					Interprete.operande = true;
					for (int i = 0; i < param.size(); i++)
					{
						mot = getWord(param.get(i));
						mot2 = param.get(i).trim();
						if (null == mot)
						{
							if (isList(mot2))
								liste += mot2.substring(1, mot2.length() - 1).trim() + " ";
							else
								liste += mot2 + " ";
						}
						else
						{
							if (mot.equals(""))
								mot = "\\v";
							liste += mot + " ";
						}
					}
					Interprete.calcul.push(liste + "] ");
					break;
				case 52: // metspremier
					liste = getFinalList(param.get(1));
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null != mot && mot.equals(""))
						mot = "\\v";
					if (null == mot)
					{
						if (!liste.equals(""))
							Interprete.calcul.push("[ " + param.get(0).trim() + " " + liste.trim() + " ] ");
						else
							Interprete.calcul.push("[ " + param.get(0).trim() + " ] ");
					}
					else
					{
						if (!liste.equals(""))
							Interprete.calcul.push("[ " + mot + " " + liste.trim() + " ] ");
						else
							Interprete.calcul.push("[ " + mot + " ] ");
					}
					break;
				case 53: // metsdernier
					liste = getFinalList(param.get(1)).trim();
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null != mot && mot.equals(""))
						mot = "\\v";
					if (null == mot)
					{ // Si c'est une liste
						Interprete.calcul.push(("[ " + liste).trim() + " " + param.get(0).trim() + " ] ");
						
					}
					else
						Interprete.calcul.push(("[ " + liste).trim() + " " + mot + " ] ");
					
					break;
				case 54: // inverse liste
					liste = getFinalList(param.get(0)).trim();
					Interprete.operande = true;
					StringTokenizer st = new StringTokenizer(liste);
					liste = " ] ";
					String element = "";
					while (st.hasMoreTokens())
					{
						element = st.nextToken();
						if (element.equals("["))
							element = extractList(st);
						liste = " " + element + liste;
					}
					Interprete.calcul.push("[" + liste);
					break;
				case 55: // choix
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{
						liste = getFinalList(param.get(0));
						int nombre = (int) Math.floor(numberOfElements(liste) * Math.random()) + 1;
						String tmp = item(liste, nombre);
						if (tmp.equals("\"\\v"))
							tmp = "\"";
						Interprete.calcul.push(tmp);
					}
					else
					{
						int nombre = (int) Math.floor(Math.random() * getWordLength(mot)) + 1;
						String str = "";
						try
						{
							str = itemWord(nombre, mot);
							Double.parseDouble(str);
							Interprete.calcul.push(str);
						}
						catch (NumberFormatException e1)
						{
							Interprete.calcul.push("\"" + str);
						}
					}
					break;
				case 56: // enleve
					Interprete.operande = true;
					liste = getFinalList(param.get(1));
					st = new StringTokenizer(liste);
					liste = "[ ";
					mot = getWord(param.get(0));
					String str;
					if (null != mot && mot.equals(""))
						mot = "\\v";
					if (null == mot)
						mot = param.get(0).trim();
					
					while (st.hasMoreTokens())
					{
						str = st.nextToken();
						if (str.equals("["))
							str = extractList(st);
						if (!str.equals(mot))
							liste += str + " ";
					}
					Interprete.calcul.push(liste.trim() + " ] ");
					break;
				case 57: // item
					Interprete.operande = true;
					mot = getWord(param.get(1));
					if (null == mot)
						Interprete.calcul.push(item(getFinalList(param.get(1)),
								kernel.getCalculator().getInteger(param.get(0))));
					else
					{
						int i = kernel.getCalculator().getInteger(param.get(0));
						if (i < 1 || i > getWordLength(mot))
							throw new LogoError(Utils.primitiveName("item") + " "
									+ Logo.messages.getString("n_aime_pas") + i + " "
									+ Logo.messages.getString("comme_parametre") + ".");
						else
						{
							str = itemWord(i, mot);
							try
							{
								Double.parseDouble(str);
								Interprete.calcul.push(str);
							}
							catch (NumberFormatException e1)
							{
								Interprete.calcul.push("\"" + str);
							}
						}
					}
					break;
				case 58: // saufdernier
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{
						liste = getFinalList(param.get(0)).trim();
						element = item(liste, numberOfElements(liste));
						int longueur = element.length();
						
						if (element.startsWith("\"") || element.startsWith("["))
							longueur--;
						Interprete.calcul.push("[ " + liste.substring(0, liste.length() - longueur) + "] ");
					}
					else if (mot.equals(""))
					{
						throw new LogoError(Logo.messages.getString("mot_vide"));
					}
					else if (getWordLength(mot) == 1)
						Interprete.calcul.push("\"");
					else
					{
						String tmp = mot.substring(0, mot.length() - 1);
						if (tmp.endsWith("\\"))
							tmp = tmp.substring(0, tmp.length() - 1);
						try
						{
							Double.parseDouble(tmp);
							Interprete.calcul.push(tmp);
						}
						catch (NumberFormatException e)
						{
							Interprete.calcul.push(debut_chaine + tmp);
						}
					}
					break;
				case 59: // saufpremier
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{
						liste = getFinalList(param.get(0)).trim();
						element = item(liste, 1);
						int longueur = element.length();
						if (element.startsWith("\"") || element.startsWith("["))
							longueur--;
						Interprete.calcul.push("[" + liste.substring(longueur, liste.length()) + " ] ");
					}
					else if (mot.equals(""))
					{
						throw new LogoError(Logo.messages.getString("mot_vide"));
					}
					else if (getWordLength(mot) == 1)
						Interprete.calcul.push("\"");
					else
					{
						if (!mot.startsWith("\\"))
							mot = mot.substring(1);
						else
							mot = mot.substring(2);
						try
						{
							Double.parseDouble(mot);
							Interprete.calcul.push(mot);
						}
						catch (NumberFormatException e)
						{
							Interprete.calcul.push(debut_chaine + mot);
						}
					}
					
					break;
				case 60: // dernier
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{ // Si c'est une liste
						liste = getFinalList(param.get(0));
						Interprete.calcul.push(item(liste, numberOfElements(liste)));
					}
					else if (getWordLength(mot) == 1)
						Interprete.calcul.push(debut_chaine + mot);
					else
					{
						str = "";
						try
						{
							str = itemWord(getWordLength(mot), mot);
							Double.parseDouble(str);
							Interprete.calcul.push(str);
						}
						catch (NumberFormatException e1)
						{
							Interprete.calcul.push("\"" + str);
						}
					}
					break;
				case 61: // premier first
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{ // SI c'est une liste
						liste = getFinalList(param.get(0));
						// System.out.println("b"+item(liste, 1)+"b");
						Interprete.calcul.push(item(liste, 1));
					}
					else if (getWordLength(mot) == 1)
						Interprete.calcul.push(debut_chaine + mot);
					else
					{
						str = "";
						try
						{
							str = itemWord(1, mot);
							Double.parseDouble(str);
							Interprete.calcul.push(str);
						}
						catch (NumberFormatException e2)
						{
							Interprete.calcul.push("\"" + str);
						}
					}
					break;
				case 62: // compte
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
					{
						liste = getFinalList(param.get(0));
						Interprete.calcul.push(String.valueOf(numberOfElements(liste)));
					}
					else
						Interprete.calcul.push(String.valueOf(getWordLength(mot)));
					break;
				case 63: // mot?
					mot = getWord(param.get(0));
					if (null == mot)
						Interprete.calcul.push(Logo.messages.getString("faux"));
					else
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					Interprete.operande = true;
					break;
				case 64: // nombre?
					try
					{
						Double.parseDouble(param.get(0));
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					}
					catch (NumberFormatException e)
					{
						Interprete.calcul.push(Logo.messages.getString("faux"));
					}
					Interprete.operande = true;
					break;
				case 65: // liste?
					liste = param.get(0).trim();
					if (isList(liste))
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					Interprete.operande = true;
					break;
				case 66: // vide?
					liste = param.get(0).trim();
					mot = getWord(param.get(0));
					if (null == mot)
					{ // si c'est une liste ou un nombre
						liste = getFinalList(liste).trim();
						if (liste.equals(""))
							Interprete.calcul.push(Logo.messages.getString("vrai"));
						else
							Interprete.calcul.push(Logo.messages.getString("faux"));
					}
					else
					{ // Si c'est un mot
						if (mot.equals(""))
							Interprete.calcul.push(Logo.messages.getString("vrai"));
						else
							Interprete.calcul.push(Logo.messages.getString("faux"));
					}
					Interprete.operande = true;
					break;
				case 67: // egal?
					equal(param);
					break;
				case 68: // precede?
					precede(param);
					break;
				case 69: // membre ?
					membre(param, id);
					break;
				case 70: // racine arithmetic.sqrt
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().sqrt(param.get(0)));
					break;
				case 71: // membre
					membre(param, id);
					break;
				case 72: // donne
					donne(param);
					Interprete.operande = false;
					
					break;
				case 73: // locale
					locale(param);
					Interprete.operande = false;
					break;
				case 74: // donnelocale
					locale(param);
					donne(param);
					Interprete.operande = false;
					break;
				case 75: // fcc
					Color color = null;
					if (isList(param.get(0)))
					{
						color = rgb(param.get(0), Utils.primitiveName("fcc"));
					}
					else
					{
						int coul = kernel.getCalculator().getInteger(param.get(0)) % DrawPanel.defaultColors.length;
						if (coul < 0)
							coul += DrawPanel.defaultColors.length;
						color = DrawPanel.defaultColors[coul];
					}
					cadre.getDrawPanel().fcc(color);
					break;
				case 76: // fcfg setscreencolor
					color = null;
					if (isList(param.get(0)))
					{
						color = rgb(param.get(0), Utils.primitiveName("fcfg"));
					}
					else
					{
						int coul = kernel.getCalculator().getInteger(param.get(0)) % DrawPanel.defaultColors.length;
						if (coul < 0)
							coul += DrawPanel.defaultColors.length;
						color = DrawPanel.defaultColors[coul];
					}
					cadre.getDrawPanel().fcfg(color);
					break;
				case 77: // hasard
					Interprete.operande = true;
					int i = kernel.getCalculator().getInteger(param.get(0));
					i = (int) Math.floor(Math.random() * i);
					Interprete.calcul.push(String.valueOf(i));
					break;
				case 78: // attends
					try
					{
						int temps = kernel.getCalculator().getInteger(param.get(0));
						if (temps < 0)
						{
							String attends = Utils.primitiveName("attends");
							throw new LogoError(attends + " " + Logo.messages.getString("attend_positif"));
						}
						else
						{
							int nbsecondes = temps / 60;
							int reste = temps % 60;
							for (i = 0; i < nbsecondes; i++)
							{
								Thread.sleep(1000);
								if (cadre.error)
									break;
							}
							if (!cadre.error)
								Thread.sleep(reste * 50 / 3);
						}
						
					}
					catch (InterruptedException e2)
					{}
					break;
				case 79: // procedures
					Interprete.operande = true;
					Interprete.calcul.push(new String(getAllProcedures()));
					break;
				case 80: // effaceprocedure efp
					erase(param.get(0), "procedure");
					break;
				
				case 81: // effacevariable
					erase(param.get(0), "variable");
					break;
				case 82: // effacetout erall
					/*
					 * Marko Zivkovic:
					 * In XLogo4Schools, we delete all files (together with the
					 * procedures) and clear all variables and property lists.
					 */
					wp.eraseAll();
					break;
				case 83: // mot
					Interprete.operande = true;
					result = "";
					for (i = 0; i < param.size(); i++)
					{
						mot = getWord(param.get(i));
						if (null == mot)
							throw new LogoError(param.get(i) + " " + Logo.messages.getString("error.word"));
						result += mot;
					}
					try
					{
						Double.parseDouble(result);
					}
					catch (NumberFormatException e)
					{
						result = "\"" + result;
					}
					Interprete.calcul.push(result);
					break;
				case 84: // etiquette
					String par = param.get(0).trim();
					if (isList(par))
						par = formatList(par.substring(1, par.length() - 1));
					mot = getWord(param.get(0));
					if (null == mot)
						cadre.getDrawPanel().etiquette(Utils.SortieTexte(par));
					else
						cadre.getDrawPanel().etiquette(Utils.SortieTexte(mot));
					break;
				case 85: // /trouvecouleur
					if (kernel.getActiveTurtle().isVisible())
						cadre.getDrawPanel().montrecacheTortue(false);
					
					liste = getFinalList(param.get(0));
					Color r = cadre.getDrawPanel().guessColorPoint(liste);
					Interprete.operande = true;
					Interprete.calcul.push("[ " + r.getRed() + " " + r.getGreen() + " " + r.getBlue() + " ] ");
					if (kernel.getActiveTurtle().isVisible())
						cadre.getDrawPanel().montrecacheTortue(true);
					break;
				case 86: // fenetre
					cadre.getDrawPanel().setWindowMode(DrawPanel.WINDOW_CLASSIC);
					break;
				case 87: // enroule
					cadre.getDrawPanel().setWindowMode(DrawPanel.WINDOW_WRAP);
					break;
				case 88: // clos
					cadre.getDrawPanel().setWindowMode(DrawPanel.WINDOW_CLOSE);
					break;
				case 89: // videtexte
					cadre.getHistoryPanel().vide_texte();
					break;
				case 90: // chargeimage
					BufferedImage image = null;
					
					primitive2D("ci");
					image = getImage(param.get(0));
					if (null != image)
						cadre.getDrawPanel().chargeimage(image);
					break;
				case 91: // ftc, fixetaillecrayon
					double nombre = kernel.getCalculator().numberDouble(param.get(0));
					if (nombre < 0)
						nombre = Math.abs(nombre);
					if (DrawPanel.record3D == DrawPanel.record3D_LINE || DrawPanel.record3D == DrawPanel.record3D_POINT)
					{
						if (kernel.getActiveTurtle().getPenWidth() != (float) nombre)
							DrawPanel.poly.addToScene();
					}
					kernel.getActiveTurtle().fixe_taille_crayon((float) nombre);
					cadre.getDrawPanel().setStroke(kernel.getActiveTurtle().crayon);
					if (DrawPanel.record3D == DrawPanel.record3D_LINE)
					{
						DrawPanel.poly = new ElementLine(cadre.getViewer3D(), cadre.getKernel().getActiveTurtle().getPenWidth());
						DrawPanel.poly.addVertex(
								new Point3d(kernel.getActiveTurtle().X / 1000, kernel.getActiveTurtle().Y / 1000,
										kernel.getActiveTurtle().Z / 1000), kernel.getActiveTurtle().couleurcrayon);
					}
					else if (DrawPanel.record3D == DrawPanel.record3D_POINT)
					{
						DrawPanel.poly = new ElementPoint(cadre.getViewer3D(), cadre.getKernel().getActiveTurtle().getPenWidth());
					}
					break;
				case 92: // tantque
					String li1 = getList(param.get(0));
					li1 = new String(Utils.decoupe(li1));
					String li2 = getList(param.get(1));
					li2 = new String(Utils.decoupe(li2));
					String instr = "\\siwhile " + li1 + "[ " + li2 + "] ";
					LoopWhile bp = new LoopWhile(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, instr);
					Primitive.stackLoop.push(bp);
					cadre.getKernel().getInstructionBuffer().insert(instr + Primitive.END_LOOP + " ");
					
					break;
				case 93: // lis
					liste = getFinalList(param.get(0));
					mot = getWord(param.get(1));
					if (null == mot)
						throw new LogoError(Logo.messages.getString("error.word"));
					java.awt.FontMetrics fm = cadre.getFrame().getGraphics().getFontMetrics(font);
					int longueur = fm.stringWidth(liste) + 100;
					Lis lis = new Lis(liste, longueur);
					while (lis.isVisible())
					{
						try
						{
							Thread.sleep(50);
						}
						catch (InterruptedException e)
						{}
					}
					param = new Stack<String>();
					param.push("\"" + mot);
					String phrase = lis.getText();
					// phrase="[ "+Logo.rajoute_backslash(phrase)+" ] ";
					StringBuffer tampon = new StringBuffer();
					for (int j = 0; j < phrase.length(); j++)
					{
						char c = phrase.charAt(j);
						if (c == '\\')
							tampon.append("\\\\");
						else
							tampon.append(c);
					}
					int offset = tampon.indexOf(" ");
					if (offset != -1)
					{
						tampon.insert(0, "[ ");
						tampon.append(" ] ");
					}
					else
					{
						try
						{
							Double.parseDouble(phrase);
						}
						catch (NumberFormatException e)
						{
							tampon.insert(0, "\"");
						}
					}
					phrase = new String(tampon);
					param.push(phrase);
					donne(param);
					String texte = liste + "\n" + phrase;
					HistoryMessenger.getInstance().dispatchComment(Utils.SortieTexte(texte) + "\n");
					cadre.focus_Commande();
					lis.dispose();
					cadre.focus_Commande();
					break;
				case 94: // touche?
					Interprete.operande = true;
					if (cadre.getCar() != -1)
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 95: // siwhile --> Evalue l'expression test du while
					liste = getFinalList(param.get(1));
					boolean pred = predicat(param.get(0));
					kernel.primitive.whilesi(pred, liste);
					break;
				case 96: // liscar
					while (cadre.getCar() == -1)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{}
						//if (LogoError.lance) //TODO this was always false
						//	break;
					}
					Interprete.calcul.push(String.valueOf(cadre.getCar()));
					Interprete.operande = true;
					cadre.setCar(-1);
					break;
				case 97: // remplis
					cadre.getDrawPanel().remplis();
					break;
				case 98: // point
					if (kernel.getActiveTurtle().isVisible())
						cadre.getDrawPanel().montrecacheTortue(false);
					
					cadre.getDrawPanel().point(getFinalList(param.get(0)));
					if (kernel.getActiveTurtle().isVisible())
						cadre.getDrawPanel().montrecacheTortue(true);
					break;
				case 99: // vers=towards vers
					
					Interprete.operande = true;
					if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
					{
						double angle = cadre.getDrawPanel().vers2D(getFinalList(param.get(0)));
						Interprete.calcul.push(MyCalculator.teste_fin_double(angle));
					}
					else
					{
						double[] orientation = cadre.getDrawPanel().vers3D(getFinalList(param.get(0)));
						Interprete.calcul.push("[ " + orientation[0] + " " + orientation[1] + " " + orientation[2]
								+ " ] ");
					}
					break;
				case 100: // distance
					Interprete.operande = true;
					double distance = cadre.getDrawPanel().distance(getFinalList(param.get(0)));
					Interprete.calcul.push(MyCalculator.teste_fin_double(distance));
					break;
				case 101: // couleurcrayon
					Interprete.operande = true;
					Interprete.calcul.push("[ " + kernel.getActiveTurtle().couleurcrayon.getRed() + " "
							+ kernel.getActiveTurtle().couleurcrayon.getGreen() + " "
							+ kernel.getActiveTurtle().couleurcrayon.getBlue() + " ] ");
					break;
				case 102: // couleurfond
					Interprete.operande = true;
					color = cadre.getDrawPanel().getBackgroundColor();
					Interprete.calcul.push("[ " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()
							+ " ] ");
					break;
				case 103: // bc?
					Interprete.operande = true;
					if (kernel.getActiveTurtle().isPenDown())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 104: // visible?
					Interprete.operande = true;
					if (kernel.getActiveTurtle().isVisible())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 105: // prim?
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					if (Primitive.primitives.containsKey(mot))
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 106: // proc?
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (wp.isExecutable(mot))
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 107: // exec
					mot = getWord(param.get(0));
					if (null == mot)
					{
						mot = getList(param.get(0).trim());
						mot = new String(Utils.decoupe(mot));
					}
					else
						mot = mot + " ";
					cadre.getKernel().getInstructionBuffer().insert(mot);
					Interprete.renvoi_instruction = true;
					break;
				case 108: // catalogue
					defaultFolder = uc.getDefaultFolder();
					str = Utils.SortieTexte(defaultFolder);
					File f = new File(str);
					String fichier = "";
					String dossier = "";
					int nbdossier = 0;
					int nbfichier = 0;
					String[] l = f.list();
					for (i = 0; i < l.length; i++)
					{
						if ((new File(str + File.separator + l[i])).isDirectory())
						{
							nbdossier++;
							if (nbdossier % 5 == 0)
								dossier += l[i] + "\n";
							else
								dossier += l[i] + " ";
						}
						else
						{
							nbfichier++;
							if (nbfichier % 5 == 0)
								fichier += l[i] + "\n";
							else
								fichier += l[i] + " ";
						}
					}
					texte = "";
					if (!dossier.equals(""))
						texte += Logo.messages.getString("repertoires") + ":\n" + dossier + "\n";
					if (!fichier.equals(""))
						texte += Logo.messages.getString("fichiers") + ":\n" + fichier + "\n";
					HistoryMessenger.getInstance().dispatchComment(texte);
					break;
				case 109: // frepertoire
					liste = getWord(param.get(0));
					if (null == liste)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					String chemin = Utils.SortieTexte(liste);
					if ((new File(chemin)).isDirectory() && !chemin.startsWith(".."))
					{
						defaultFolder = Utils.rajoute_backslash(chemin);
						uc.setDefaultFolder(defaultFolder);
					}
					else
						throw new LogoError(liste + " " + Logo.messages.getString("erreur_pas_repertoire"));
					break;
				case 110: // repertoire
					Interprete.operande = true;
					defaultFolder = uc.getDefaultFolder();
					Interprete.calcul.push("\"" + defaultFolder);
					break;
				case 111: // sauve
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(Logo.messages.getString("error.word"));
					liste = getFinalList(param.get(1));
					st = new StringTokenizer(liste);
					Stack<String> pile = new Stack<String>();
					while (st.hasMoreTokens())
						pile.push(st.nextToken());
					saveProcedures(mot, pile);
					break;
				case 112: // sauved
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					saveProcedures(mot, null);
					break;
				case 113: // ramene load
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					defaultFolder = uc.getDefaultFolder();
					String path = Utils.SortieTexte(defaultFolder) + File.separator + mot;
					String fileName = wp.makeUniqueFileName(mot);
					try
					{
						String txt = Utils.readLogoFile(path); // TODO this
																// does not
																// work...
																// should
																// load from
																// path....
																// later:
																// looks ok,
																// not?
						wp.createFile(fileName);
						wp.writeFileText(fileName, txt);
					}
					catch (IOException e1)
					{
						throw new LogoError(Logo.messages.getString("error.iolecture"));
					}
					
					break;
				case 114: // pi
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().pi());
					break;
				case 115: // tangente arithmetic.tan
					Interprete.operande = true;
					
					Interprete.calcul.push(kernel.getCalculator().tan(param.get(0)));
					
					break;
				case 116: // acos
					
					Interprete.calcul.push(kernel.getCalculator().acos(param.get(0)));
					Interprete.operande = true;
					break;
				case 117: // asin
					
					Interprete.calcul.push(kernel.getCalculator().asin(param.get(0)));
					Interprete.operande = true;
					break;
				case 118: // atan
					Interprete.calcul.push(kernel.getCalculator().atan(param.get(0)));
					Interprete.operande = true;
					break;
				case 119: // vrai
					Interprete.operande = true;
					Interprete.calcul.push(Logo.messages.getString("vrai"));
					break;
				case 120: // faux
					Interprete.operande = true;
					Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 121: // forme
					primitive2D("turtle.forme");
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(kernel.getActiveTurtle().getShape()));
					break;
				case 122: // fixeforme setshape
					primitive2D("turtle.fforme");
					i = kernel.getCalculator().getInteger(param.get(0)) % 7;
					if (kernel.getActiveTurtle().id == 0)
					{
						uc.setActiveTurtle(i);
					}
					chemin = "tortue" + i + ".png";
					kernel.change_image_tortue(chemin);
					break;
				case 123: // definis workspace.define
					define(param); // Method extracted by Marko Zivkovic
									// 21.6.2013
					break;
				
				case 124: // tortue
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(kernel.getActiveTurtle().id));
					break;
				case 125: // tortues
					Interprete.operande = true;
					String li = "[ ";
					for (i = 0; i < cadre.getDrawPanel().tortues.length; i++)
					{
						if (null != cadre.getDrawPanel().tortues[i])
							li += String.valueOf(i) + " ";
					}
					li += "]";
					Interprete.calcul.push(li);
					break;
				case 126: // fixetortue
					try
					{
						i = Integer.parseInt(param.get(0));
						if (i > -1 && i < uc.getMaxTurtles())
						{
							if (null == cadre.getDrawPanel().tortues[i])
							{
								cadre.getDrawPanel().tortues[i] = new Turtle(cadre);
								cadre.getDrawPanel().tortues[i].id = i;
								cadre.getDrawPanel().tortues[i].setVisible(false);
							}
							cadre.getDrawPanel().tortue = cadre.getDrawPanel().tortues[i];
							cadre.getDrawPanel().setStroke(kernel.getActiveTurtle().crayon);
							String police = cadre.getDrawPanel().getGraphicsFont().getName();
							cadre.getDrawPanel().setGraphicsFont(
									new java.awt.Font(police, java.awt.Font.PLAIN, kernel.getActiveTurtle().police));
							
						}
						else
						{
							throw new LogoError(Logo.messages.getString("tortue_inaccessible"));
						}
					}
					catch (NumberFormatException e)
					{
						kernel.getCalculator().getInteger(param.get(0));
					}
					break;
				case 127: // police
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(kernel.getActiveTurtle().police));
					break;
				case 128: // fixetaillepolice
					int taille = kernel.getCalculator().getInteger(param.get(0));
					kernel.getActiveTurtle().police = taille;
					Font police = font;
					cadre.getDrawPanel().setGraphicsFont(police.deriveFont((float) kernel.getActiveTurtle().police));
					
					break;
				case 129: // tuetortue
					try
					{
						id = Integer.parseInt(param.get(0));
						if (id > -1 && id < uc.getMaxTurtles())
						{
							// On compte le nombre de tortues à l'écran
							int compteur = 0;
							int premier_dispo = -1;
							for (i = 0; i < uc.getMaxTurtles(); i++)
							{
								if (null != cadre.getDrawPanel().tortues[i])
								{
									if (i != id && premier_dispo == -1)
										premier_dispo = i;
									compteur++;
								}
							}
							// On vérifie que ce n'est pas la seule tortue
							// dispopnible:
							if (null != cadre.getDrawPanel().tortues[id])
							{
								if (compteur > 1)
								{
									int tortue_utilisee = kernel.getActiveTurtle().id;
									cadre.getDrawPanel().tortue = cadre.getDrawPanel().tortues[id];
									cadre.getDrawPanel().ct_mt();
									cadre.getDrawPanel().tortue = cadre.getDrawPanel().tortues[tortue_utilisee];
									cadre.getDrawPanel().tortues[id] = null;
									if (cadre.getDrawPanel().tortues_visibles.search(String.valueOf(id)) > 0)
										cadre.getDrawPanel().tortues_visibles.remove(String.valueOf(id));
									if (kernel.getActiveTurtle().id == id)
									{
										cadre.getDrawPanel().tortue = cadre.getDrawPanel().tortues[premier_dispo];
										cadre.getDrawPanel().setStroke(kernel.getActiveTurtle().crayon); // on
										// adapte
										// le
										// nouveau
										// crayon
										str = cadre.getDrawPanel().getGraphicsFont().getName();
										cadre.getDrawPanel().setFont(
												new java.awt.Font(str, java.awt.Font.PLAIN,
														kernel.getActiveTurtle().police));
										
									}
								}
								else
								{
									throw new LogoError(Logo.messages.getString("seule_tortue_dispo"));
								}
							}
						}
					}
					catch (NumberFormatException e)
					{
						kernel.getCalculator().getInteger(param.get(0));
					}
					break;
				case 130: // sequence
					liste = getFinalList(param.get(0));
					cadre.getSon().cree_sequence(Utils.decoupe(liste).toString());
					break;
				case 131: // instrument
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(cadre.getSon().getInstrument()));
					break;
				case 132: // fixeinstrument
					i = kernel.getCalculator().getInteger(param.get(0));
					cadre.getSon().setInstrument(i);
					
					break;
				case 133: // joue
					cadre.getSon().joue();
					break;
				case 134: // effacesequence
					cadre.getSon().efface_sequence();
					break;
				case 135: // indexsequence
					Interprete.operande = true;
					double d = (double) cadre.getSon().getTicks() / 64;
					Interprete.calcul.push(MyCalculator.teste_fin_double(d));
					
					break;
				case 136: // fixeindexsequence
					i = kernel.getCalculator().getInteger(param.get(0));
					cadre.getSon().setTicks(i * 64);
					break;
				case 137:// fpt
					i = kernel.getCalculator().getInteger(param.get(0));
					cadre.getHistoryPanel().getDsd().fixepolice(i);
					break;
				case 138: // ptexte
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(cadre.getHistoryPanel().police()));
					break;
				case 139: // fct,fixecouleurtexte
					if (isList(param.get(0)))
					{
						cadre.getHistoryPanel().getDsd().fixecouleur(rgb(param.get(0), Utils.primitiveName("fct")));
					}
					else
					{
						int coul = kernel.getCalculator().getInteger(param.get(0)) % DrawPanel.defaultColors.length;
						if (coul < 0)
							coul += DrawPanel.defaultColors.length;
						cadre.getHistoryPanel().getDsd().fixecouleur(DrawPanel.defaultColors[coul]);
					}
					break;
				case 140: // couleurtexte
					Interprete.operande = true;
					Color c = cadre.getHistoryPanel().getCouleurtexte();
					Interprete.calcul.push("[ " + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + " ] ");
					break;
				case 141: // lissouris readmouse
					while (!cadre.getDrawPanel().get_lissouris())
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{}
						//if (LogoError.lance) //TODO Marko: this was always false
						//	break;
					}
					Interprete.calcul.push(String.valueOf(cadre.getDrawPanel().get_bouton_souris()));
					Interprete.operande = true;
					break;
				case 142: // possouris
					Interprete.calcul.push(cadre.getDrawPanel().get_possouris());
					Interprete.operande = true;
					break;
				case 143: // msg message
					liste = getFinalList(param.get(0));
					st = new StringTokenizer(liste); // On
														// découpe
														// le
														// message
														// en
														// tranche
														// de
														// longueurs
														// acceptables
					fm = cadre.getFrame().getGraphics().getFontMetrics(font);
					liste = "";
					String buf = "";
					while (st.hasMoreTokens())
					{
						buf += st.nextToken() + " ";
						if (fm.stringWidth(buf) > 200)
						{
							liste += buf + "\n";
							buf = "";
						}
					}
					liste += buf;
					liste = Utils.SortieTexte(liste);
					
					MyTextAreaDialog jt = new MyTextAreaDialog(liste, cadre.getHistoryPanel().getDsd());
					ImageIcon icone = new ImageIcon(Utils.class.getResource("icone.png"));
					JOptionPane.showMessageDialog(cadre.getFrame(), jt, "", JOptionPane.INFORMATION_MESSAGE,
							(Icon) icone);
					break;
				case 144: // date
					Interprete.operande = true;
					Calendar cal = Calendar.getInstance(lang.getLocale());
					int jour = cal.get(Calendar.DAY_OF_MONTH);
					int mois = cal.get(Calendar.MONTH) + 1;
					int annee = cal.get(Calendar.YEAR);
					Interprete.calcul.push("[ " + jour + " " + mois + " " + annee + " ] ");
					break;
				case 145: // heure
					Interprete.operande = true;
					cal = Calendar.getInstance(lang.getLocale());
					int heure = cal.get(Calendar.HOUR_OF_DAY);
					int minute = cal.get(Calendar.MINUTE);
					int seconde = cal.get(Calendar.SECOND);
					Interprete.calcul.push("[ " + heure + " " + minute + " " + seconde + " ] ");
					break;
				case 146: // temps
					Interprete.operande = true;
					long heure_actuelle = Calendar.getInstance().getTimeInMillis();
					Interprete.calcul.push(String.valueOf((heure_actuelle - uc.getHeure_demarrage()) / 1000));
					break;
				case 147: // debuttemps
					int temps = kernel.getCalculator().getInteger(param.get(0));
					Kernel.chrono = Calendar.getInstance().getTimeInMillis() + 1000 * temps;
					break;
				case 148: // fintemps?
					Interprete.operande = true;
					if (Calendar.getInstance().getTimeInMillis() > Kernel.chrono)
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 149: // fnp fixenompolice
					int int_police = kernel.getCalculator().getInteger(param.get(0));
					cadre.getDrawPanel().police_etiquette = int_police % UserConfig.fontes.length;
					break;
				case 150: // np nompolice
					Interprete.operande = true;
					Interprete.calcul.push("[ " + cadre.getDrawPanel().police_etiquette + " [ "
							+ UserConfig.fontes[cadre.getDrawPanel().police_etiquette].getFontName() + " ] ] ");
					break;
				case 151: // fnpt fixenompolicetexte
					int_police = kernel.getCalculator().getInteger(param.get(0));
					HistoryPanel.fontPrint = int_police % UserConfig.fontes.length;
					cadre.getHistoryPanel().getDsd().fixenompolice(int_police);
					
					break;
				case 152: // npt nompolicetexte
					Interprete.operande = true;
					Interprete.calcul.push("[ " + HistoryPanel.fontPrint + " [ "
							+ UserConfig.fontes[HistoryPanel.fontPrint].getFontName() + " ] ] ");
					break;
				case 153: // listeflux
					liste = "[ ";
					for (MyFlow flow : kernel.flows)
					{
						liste += "[ " + flow.getId() + " " + flow.getPath() + " ] ";
					}
					liste += "] ";
					Interprete.operande = true;
					Interprete.calcul.push(liste);
					break;
				case 154: // lisligneflux
					try
					{
						int ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						MyFlow flow = kernel.flows.get(index);
						MyFlowReader flowReader;
						// If the flow is a writable flow, throw error
						if (flow.isWriter())
							throw new LogoError(Logo.messages.getString("flux_lecture"));
						// else if the flow is a readable flow, convert to
						// MyFlowReader
						else if (flow.isReader())
						{
							flowReader = ((MyFlowReader) flow);
						}
						// else the flow isn't yet defined, initialize
						else
							flowReader = new MyFlowReader(flow);
						
						if (flowReader.isFinished())
							throw new LogoError(Logo.messages.getString("fin_flux") + " " + ident);
						// Reading line
						String line = flowReader.readLine();
						if (null == line)
						{
							flow.setFinished(true);
							throw new LogoError(Logo.messages.getString("fin_flux") + " " + ident);
						}
						Interprete.operande = true;
						Interprete.calcul.push("[ " + Utils.decoupe(line.trim()) + " ] ");
						kernel.flows.set(index, flowReader);
					}
					catch (FileNotFoundException e1)
					{
						throw new LogoError(Logo.messages.getString("error.iolecture"));
					}
					catch (IOException e2)
					{}
					break;
				case 155: // liscaractereflux
					try
					{
						int ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						MyFlow flow = kernel.flows.get(index);
						MyFlowReader flowReader;
						// If the flow is a writable flow, throw error
						if (flow.isWriter())
							throw new LogoError(Logo.messages.getString("flux_lecture"));
						// else if the flow is reader, convert to MyFlowReader
						else if (flow.isReader())
						{
							flowReader = ((MyFlowReader) flow);
						}
						// else the flow isn't yet defined, initialize
						else
							flowReader = new MyFlowReader(flow);
						
						if (flowReader.isFinished())
							throw new LogoError(Logo.messages.getString("fin_flux") + " " + ident);
						
						int character = ((MyFlowReader) flow).readChar();
						if (character == -1)
						{
							flow.setFinished(true);
							throw new LogoError(Logo.messages.getString("fin_flux") + " " + ident);
						}
						Interprete.operande = true;
						String car = String.valueOf(character);
						if (car.equals("\\"))
							car = "\\\\";
						Interprete.calcul.push(car);
						kernel.flows.set(index, flowReader);
					}
					catch (FileNotFoundException e1)
					{
						throw new LogoError(Logo.messages.getString("error.iolecture"));
					}
					catch (IOException e2)
					{}
					break;
				case 156: // ecrisligneflux
					try
					{
						int ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						liste = getFinalList(param.get(1));
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						MyFlow flow = kernel.flows.get(index);
						MyFlowWriter flowWriter;
						// If the flow is a readable flow, throw an error
						if (flow.isReader())
							throw new LogoError(Logo.messages.getString("flux_ecriture"));
						// Else if the flow is a writable flow , convert to
						// MrFlowWriter
						else if (flow.isWriter())
							flowWriter = (MyFlowWriter) flow;
						// Else the flow isn't defined yet, initialize
						else
							flowWriter = new MyFlowWriter(flow);
						
						// System.out.println(flow.isReader()+" "+flow.isWriter());
						// Write the line
						flowWriter.write(Utils.SortieTexte(liste));
						kernel.flows.set(index, flowWriter);
					}
					catch (FileNotFoundException e1)
					{}
					catch (IOException e2)
					{}
					break;
				case 157: // finficher?
					try
					{
						int ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						else
						{
							MyFlow flow = kernel.flows.get(index);
							MyFlowReader flowReader = null;
							// If the flow isn't defined yet, initialize
							if (!flow.isWriter() && !flow.isReader())
							{
								flowReader = new MyFlowReader(flow);
							}
							else if (flow.isReader())
								flowReader = (MyFlowReader) flow;
							if (null != flowReader)
							{
								if (flow.isFinished())
								{
									Interprete.operande = true;
									Interprete.calcul.push(Logo.messages.getString("vrai"));
								}
								else
								{
									int read = flowReader.isReadable();
									if (read == -1)
									{
										Interprete.operande = true;
										Interprete.calcul.push(Logo.messages.getString("vrai"));
										flow.setFinished(true);
									}
									else
									{
										Interprete.operande = true;
										Interprete.calcul.push(Logo.messages.getString("faux"));
									}
								}
							}
							else
								throw new LogoError(Logo.messages.getString("flux_lecture"));
						}
					}
					catch (FileNotFoundException e1)
					{}
					catch (IOException e2)
					{}
					break;
				case 158: // ouvreflux
					mot = getWord(param.get(1));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					defaultFolder = uc.getDefaultFolder();
					liste = Utils.SortieTexte(defaultFolder) + File.separator + Utils.SortieTexte(mot);
					int ident = kernel.getCalculator().getInteger(param.get(0));
					if (kernel.flows.search(ident) == -1)
						kernel.flows.add(new MyFlow(ident, liste, false));
					else
						throw new LogoError(ident + " " + Logo.messages.getString("flux_existant"));
					break;
				case 159: // fermeflux
					try
					{
						ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						MyFlow flow = kernel.flows.get(index);
						// If the flow is a readable flow
						if (flow.isReader())
							((MyFlowReader) flow).close();
						// Else if it's a writable flow
						else if (flow.isWriter())
							((MyFlowWriter) flow).close();
						kernel.flows.remove(index);
					}
					catch (IOException e2)
					{}
					break;
				case 160: // ajouteligneflux
					try
					{
						ident = kernel.getCalculator().getInteger(param.get(0));
						int index = kernel.flows.search(ident);
						liste = getFinalList(param.get(1));
						if (index == -1)
							throw new LogoError(Logo.messages.getString("flux_non_disponible") + " " + ident);
						MyFlow flow = kernel.flows.get(index);
						MyFlowWriter flowWriter;
						// If the flow is a readable flow, throw an error
						if (flow.isReader())
							throw new LogoError(Logo.messages.getString("flux_ecriture"));
						// Else if the flow is a writable flow , convert to
						// MrFlowWriter
						else if (flow.isWriter())
							flowWriter = (MyFlowWriter) flow;
						// Else the flow isn't defined yet, initialize
						else
							flowWriter = new MyFlowWriter(flow);
						
						// Write the line
						flowWriter.append(Utils.SortieTexte(liste));
						kernel.flows.set(index, flowWriter);
					}
					catch (FileNotFoundException e1)
					{}
					catch (IOException e2)
					{}
					break;
				case 161: // souris?
					Interprete.operande = true;
					if (cadre.getDrawPanel().get_lissouris())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 162: // variables
					Interprete.operande = true;
					Interprete.calcul.push(new String(getAllVariables()));
					break;
				case 163: // chose thing
					mot = getWord(param.get(0));
					if (null == mot)
					{
						throw new LogoError(Logo.messages.getString("error.word"));
					} // si c'est une liste
					else if (debut_chaine.equals("")) { throw new LogoError(
							Logo.messages.getString("erreur_variable")); } // si
																			// c'est
																			// un
																			// nombre
					Interprete.operande = true;
					String value;
					mot = mot.toLowerCase();
					if (!Interprete.locale.containsKey(mot))
					{
						if (!wp.getGlobals().getVariables().contains(mot))
							throw new LogoError(mot + " " + Logo.messages.getString("erreur_variable"));
						else
							value = wp.getGlobals().getValue(mot);
					}
					else
					{
						value = Interprete.locale.get(mot);
					}
					if (null == value)
						throw new LogoError(mot + "  " + Logo.messages.getString("erreur_variable"));
					Interprete.calcul.push(value);
					break;
				case 164: // nettoie
					cadre.getDrawPanel().nettoie();
					break;
				case 165: // tape
					par = param.get(0).trim();
					if (isList(par))
						par = formatList(par.substring(1, par.length() - 1));
					mot = getWord(param.get(0));
					if (null == mot)
						HistoryMessenger.getInstance().dispatchLogoOutput(Utils.SortieTexte(par));
					else
						HistoryMessenger.getInstance().dispatchLogoOutput(Utils.SortieTexte(mot));
					break;
				case 166: // cercle
					cadre.getDrawPanel().circle((kernel.getCalculator().numberDouble(param.pop())));
					break;
				case 167: // arc
					cadre.getDrawPanel().arc(kernel.getCalculator().numberDouble(param.get(0)),
							kernel.getCalculator().numberDouble(param.get(1)),
							kernel.getCalculator().numberDouble(param.get(2)));
					break;
				case 168: // rempliszone
					cadre.getDrawPanel().rempliszone();
					break;
				case 169: // animation
					cadre.getDrawPanel().setAnimation(true);
					Interprete.operande = false;
					break;
				case 170: // rafraichis
					if (DrawPanel.classicMode == DrawPanel.MODE_ANIMATION)
					{
						cadre.getDrawPanel().refresh();
					}
					break;
				
				case 171: // tailledessin
					Interprete.operande = true;
					StringBuffer sb = new StringBuffer();
					sb.append("[ ");
					sb.append(w);
					sb.append(" ");
					sb.append(h);
					sb.append(" ] ");
					Interprete.calcul.push(new String(sb));
					break;
				case 172: // quotient
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().quotient(param.get(0), param.get(1)));
					break;
				case 173: // entier?
					Interprete.operande = true;
					double ent = kernel.getCalculator().numberDouble(param.get(0));
					if ((int) ent == ent)
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 174: // fixeseparation
					nombre = kernel.getCalculator().numberDouble(param.get(0));
					if (nombre < 0 || nombre > 1)
						throw new LogoError(nombre + " " + Logo.messages.getString("entre_zero_un"));
					cadre.drawingAndHistory.setResizeWeight(nombre);
					cadre.drawingAndHistory.setDividerLocation(nombre);
					break;
				case 175: // separation
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(cadre.drawingAndHistory.getResizeWeight()));
					break;
				case 176: // tronque
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().truncate(param.get(0)));
					break;
				case 177: // trace
					Kernel.mode_trace = true;
					Interprete.operande = false;
					break;
				case 178:// changedossier
					Interprete.operande = false;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					chemin = "";
					defaultFolder = uc.getDefaultFolder();
					if (defaultFolder.endsWith(File.separator))
						chemin = Utils.SortieTexte(defaultFolder + mot);
					else
						chemin = Utils.SortieTexte(defaultFolder + Utils.rajoute_backslash(File.separator) + mot);
					if ((new File(chemin)).isDirectory())
					{
						try
						{
							defaultFolder = Utils.rajoute_backslash((new File(chemin)).getCanonicalPath());
							uc.setDefaultFolder(defaultFolder);
						}
						catch (NullPointerException e1)
						{}
						catch (IOException e2)
						{}
					}
					else
						throw new LogoError(Utils.rajoute_backslash(chemin) + " "
								+ Logo.messages.getString("erreur_pas_repertoire"));
					
					break;
				case 179:// unicode
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					else if (getWordLength(mot) != 1)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("un_caractere"));
					else
					{
						Interprete.operande = true;
						str = String.valueOf((int) Utils.SortieTexte(itemWord(1, mot)).charAt(0));
						Interprete.calcul.push(str);
					}
					break;
				case 180:// caractere
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i < 0 || i > 65535)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("nombre_unicode"));
					else
					{
						str = "";
						Interprete.operande = true;
						if (i == 92)
							str = "\"\\\\";
						else if (i == 10)
							str = "\"\\n";
						else if (i == 32)
							str = "\"\\e";
						else
						{
							str = String.valueOf((char) i);
							try
							{
								Double.parseDouble(str);
							}
							catch (NumberFormatException e)
							{
								str = "\"" + str;
							}
						}
						Interprete.calcul.push(str);
					}
					break;
				case 181: // stoptout
					cadre.error = true;
					break;
				case 182: // compteur
					boolean erreur = false;
					if (!Primitive.stackLoop.isEmpty())
					{
						LoopProperties lp = Primitive.stackLoop.peek();
						if (lp.isRepeat())
						{
							Interprete.operande = true;
							Interprete.calcul.push(lp.getCounter().toString());
						}
						else
							erreur = true;
					}
					else
						erreur = true;
					if (erreur) { throw new LogoError(Logo.messages.getString("erreur_compteur")); }
					break;
				case 183: // controls.for repetepour
					li2 = getList(param.get(1));
					li2 = new String(Utils.decoupe(li2));
					li1 = getFinalList(param.get(0));
					int nb = numberOfElements(li1);
					if (nb < 3 || nb > 4)
						throw new LogoError(Logo.messages.getString("erreur_repetepour"));
					st = new StringTokenizer(li1);
					String var = st.nextToken().toLowerCase();
					BigDecimal deb = kernel.getCalculator().numberDecimal(st.nextToken());
					BigDecimal fin = kernel.getCalculator().numberDecimal(st.nextToken());
					BigDecimal increment = BigDecimal.ONE;
					if (nb == 4)
						increment = kernel.getCalculator().numberDecimal(st.nextToken());
					if (var.equals(""))
						throw new LogoError(Logo.messages.getString("variable_vide"));
					try
					{
						Double.parseDouble(var);
						throw new LogoError(Logo.messages.getString("erreur_nom_nombre_variable"));
					}
					catch (NumberFormatException e)
					{
						LoopFor lf = new LoopFor(deb, fin, increment, li2, var);
						lf.AffecteVar(true);
						
						if ((increment.compareTo(BigDecimal.ZERO) == 1 && fin.compareTo(deb) >= 0)
								|| (increment.compareTo(BigDecimal.ZERO) == -1 && fin.compareTo(deb) <= 0))
						{
							cadre.getKernel().getInstructionBuffer().insert(li2 + Primitive.END_LOOP + " ");
							Primitive.stackLoop.push(lf);
						}
					}
					break;
				case 184: // absolue
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().abs(param.get(0)));
					break;
				case 185: // remplace
					String reponse = "";
					liste = getFinalList(param.get(0));
					int entier = kernel.getCalculator().getInteger(param.get(1));
					mot = getWord(param.get(2));
					if (null != mot && mot.equals(""))
						mot = "\\v";
					if (null == mot)
						mot = "[ " + getFinalList(param.get(2)) + "]";
					char ch;
					int compteur = 1;
					boolean espace = true;
					boolean crochet = false;
					boolean error = true;
					for (int j = 0; j < liste.length(); j++)
					{
						if (compteur == entier)
						{
							error = false;
							compteur = j;
							break;
						}
						ch = liste.charAt(j);
						if (ch == '[')
						{
							if (espace)
								crochet = true;
							espace = false;
						}
						if (ch == ' ')
						{
							espace = true;
							if (crochet)
							{
								crochet = false;
								j = extractList(liste, j);
							}
							compteur++;
						}
					}
					if (error)
						throw new LogoError(Logo.messages.getString("y_a_pas") + " " + entier + " "
								+ Logo.messages.getString("element_dans_liste") + liste + "]");
					reponse = "[ " + liste.substring(0, compteur) + mot;
					// On extrait le mot suivant
					if (compteur + 1 < liste.length() && liste.charAt(compteur) == '['
							&& liste.charAt(compteur + 1) == ' ')
					{
						compteur = extractList(liste, compteur + 2);
						reponse += liste.substring(compteur) + "] ";
						
					}
					else
					{
						for (i = compteur + 1; i < liste.length(); i++)
						{
							if (liste.charAt(i) == ' ')
							{
								compteur = i;
								break;
							}
						}
						reponse += liste.substring(compteur) + "] ";
					}
					Interprete.operande = true;
					Interprete.calcul.push(reponse);
					break;
				case 186: // ajoute
					reponse = "";
					liste = getFinalList(param.get(0));
					entier = kernel.getCalculator().getInteger(param.get(1));
					mot = getWord(param.get(2));
					if (null != mot && mot.equals(""))
						mot = "\\v";
					if (null == mot)
						mot = "[ " + getFinalList(param.get(2)) + "]";
					compteur = 1;
					espace = true;
					crochet = false;
					error = true;
					for (int j = 0; j < liste.length(); j++)
					{
						if (compteur == entier)
						{
							error = false;
							compteur = j;
							break;
						}
						ch = liste.charAt(j);
						if (ch == '[')
						{
							if (espace)
								crochet = true;
							espace = false;
						}
						if (ch == ' ')
						{
							espace = true;
							if (crochet)
							{
								crochet = false;
								j = extractList(liste, j);
							}
							compteur++;
							if (j == liste.length() - 1 && compteur == entier)
							{
								error = false;
								compteur = liste.length();
							}
						}
					}
					if (error && entier != compteur)
						throw new LogoError(Logo.messages.getString("y_a_pas") + " " + entier + " "
								+ Logo.messages.getString("element_dans_liste") + liste + "]");
					if (!liste.trim().equals(""))
						reponse = "[ " + liste.substring(0, compteur) + mot + " " + liste.substring(compteur) + "] ";
					else
						reponse = "[ " + mot + " ] ";
					Interprete.operande = true;
					Interprete.calcul.push(reponse);
					break;
				case 187: // gris
					colorCode(8);
					break;
				case 188: // grisclair
					colorCode(9);
					break;
				case 189: // rougefonce
					colorCode(10);
					break;
				case 190: // vertfonce
					colorCode(11);
					break;
				case 191: // bleufonce
					colorCode(12);
					break;
				case 192: // orange
					colorCode(13);
					break;
				case 193: // rose
					colorCode(14);
					break;
				case 194: // violet
					colorCode(15);
					break;
				case 195: // marron
					colorCode(16);
					break;
				case 196: // noir
					colorCode(0);
					break;
				case 197: // rouge
					colorCode(1);
					break;
				case 198: // vert
					colorCode(2);
					break;
				case 199: // jaune
					colorCode(3);
					break;
				case 200: // bleu
					colorCode(4);
					break;
				case 201: // magenta
					colorCode(5);
					break;
				case 202: // cyan
					colorCode(6);
					break;
				case 203: // blanc
					colorCode(7);
					break;
				case 204: // Parenthese fermante
							// Distinguons les deux cas : (3)*2 et (4+3)*2
							// Le 3 est ici a retourner au +
					boolean a_retourner = true;
					// On enleve le "(" correspondant a la parenthese ouvrante
					// de la
					// pile nom
					// a condition que l'element attendant de la pile nom ne
					// soit
					// pas une procedure
					boolean est_procedure = false;
					int pos = Interprete.nom.lastIndexOf("(");
					if (pos == -1)
					{
						// Parenthese fermante sans parenthese ouvrante au
						// prealable
						throw new LogoError(Logo.messages.getString("parenthese_ouvrante"));
					}
					else
					{ // Evitons l'erreur en cas de par exemple: "ec )"
						// (parenthese fermante sans ouvrante)--> else a
						// executer qu'en cas de non erreur
						if (Interprete.drapeau_ouvrante)
						{
							// parenthese vide
							throw new LogoError(Logo.messages.getString("parenthese_vide"));
							
						}
						for (int j = pos; j < Interprete.nom.size(); j++)
						{
							String proc = Interprete.nom.get(j).toLowerCase();
							if (Primitive.primitives.containsKey(proc))
								est_procedure = true;
							else
							{
								if (wp.isExecutable(proc))	// TODO changed
								{
									est_procedure = true;
									break;
								}
							}
						}
					}
					// Si une procedure est presente dans la pile nom, on garde
					// les parenteses
					// System.out.println(Primitive.primitives.containsKey("puissance")+"
					// "+est_procedure);
					if (est_procedure)
					{
						cadre.getKernel().getInstructionBuffer().insert(") ");
					}
					// Sinon on les enleve avec leurs imbrications eventuelles
					else
					{
						if (Interprete.en_cours.isEmpty() || !Interprete.en_cours.peek().equals("("))
						{
							throw new LogoError(Logo.messages.getString("parenthese_ouvrante"));
						}
						else
							Interprete.en_cours.pop();
						if (!Interprete.nom.isEmpty())
						{
							if (Interprete.nom.peek().equals("("))
								a_retourner = false;
							pos = Interprete.nom.lastIndexOf("(");
							if (pos == -1)
							{
								// Parenthese fermante sans parenthese ouvrante
								// au prelable
								throw new LogoError(Logo.messages.getString("parenthese_ouvrante"));
							}
							else
							{
								Interprete.nom.removeElementAt(pos);
								// S'il y a imbrication de parentheses (((20)))
								pos--;
								instruction = cadre.getKernel().getInstructionBuffer();
								while (instruction.getNextWord().equals(")") && (pos > -1))
								{
									if (!Interprete.nom.isEmpty() && Interprete.nom.get(pos).equals("("))
									{
										instruction.deleteFirstWord(")");
										Interprete.nom.removeElementAt(pos);
										pos--;
									}
									else
										break;
								}
							}
						}
					}
					if (Interprete.calcul.isEmpty())
					{
						Interprete.operande = false;
					}
					else
					{
						Interprete.operande = true;
						Interprete.drapeau_fermante = a_retourner;
					}
					break;
				case 205: // fixestyle
					boolean gras = false;
					boolean italique = false;
					boolean souligne = false;
					boolean exposant = false;
					boolean indice = false;
					boolean barre = false;
					mot = getWord(param.get(0));
					if (null == mot)
						liste = getFinalList(param.get(0));
					else
						liste = mot;
					if (liste.trim().equals(""))
						liste = Logo.messages.getString("style.none");
					st = new StringTokenizer(liste);
					while (st.hasMoreTokens())
					{
						element = st.nextToken().toLowerCase();
						if (element.equals(Logo.messages.getString("style.underline").toLowerCase()))
						{
							souligne = true;
						}
						else if (element.equals(Logo.messages.getString("style.bold").toLowerCase()))
						{
							gras = true;
						}
						else if (element.equals(Logo.messages.getString("style.italic").toLowerCase()))
						{
							italique = true;
						}
						else if (element.equals(Logo.messages.getString("style.exposant").toLowerCase()))
						{
							exposant = true;
						}
						else if (element.equals(Logo.messages.getString("style.subscript").toLowerCase()))
						{
							indice = true;
						}
						else if (element.equals(Logo.messages.getString("style.strike").toLowerCase()))
						{
							barre = true;
						}
						else if (element.equals(Logo.messages.getString("style.none").toLowerCase()))
						{}
						else
							throw new LogoError(Logo.messages.getString("erreur_fixestyle"));
					}
					cadre.getHistoryPanel().getDsd().fixegras(gras);
					cadre.getHistoryPanel().getDsd().fixeitalique(italique);
					cadre.getHistoryPanel().getDsd().fixesouligne(souligne);
					cadre.getHistoryPanel().getDsd().fixeexposant(exposant);
					cadre.getHistoryPanel().getDsd().fixeindice(indice);
					cadre.getHistoryPanel().getDsd().fixebarre(barre);
					break;
				case 206: // style
					StringBuffer buffer = new StringBuffer();
					compteur = 0;
					if (cadre.getHistoryPanel().getDsd().estgras())
					{
						buffer.append(Logo.messages.getString("style.bold").toLowerCase() + " ");
						compteur++;
					}
					if (cadre.getHistoryPanel().getDsd().estitalique())
					{
						buffer.append(Logo.messages.getString("style.italic").toLowerCase() + " ");
						compteur++;
					}
					if (cadre.getHistoryPanel().getDsd().estsouligne())
					{
						buffer.append(Logo.messages.getString("style.underline").toLowerCase() + " ");
						compteur++;
					}
					if (cadre.getHistoryPanel().getDsd().estexposant())
					{
						buffer.append(Logo.messages.getString("style.exposant").toLowerCase() + " ");
						compteur++;
					}
					if (cadre.getHistoryPanel().getDsd().estindice())
					{
						buffer.append(Logo.messages.getString("style.subscript").toLowerCase() + " ");
						compteur++;
					}
					if (cadre.getHistoryPanel().getDsd().estbarre())
					{
						buffer.append(Logo.messages.getString("style.strike").toLowerCase() + " ");
						compteur++;
					}
					Interprete.operande = true;
					if (compteur == 0)
						Interprete.calcul.push("\"" + Logo.messages.getString("style.none").toLowerCase());
					else if (compteur == 1)
						Interprete.calcul.push("\"" + new String(buffer).trim());
					else if (compteur > 1)
						Interprete.calcul.push("[ " + new String(buffer) + "]");
					break;
				case 207: // listaillefenetre
					Interprete.operande = true;
					java.awt.Point p = cadre.scrollArea.getViewport().getViewPosition();
					Rectangle rec = cadre.scrollArea.getVisibleRect();
					sb = new StringBuffer();
					int x1 = p.x - w / 2;
					int y1 = h / 2 - p.y;
					int x2 = x1 + rec.width - cadre.scrollArea.getVerticalScrollBar().getWidth();
					int y2 = y1 - rec.height + cadre.scrollArea.getHorizontalScrollBar().getHeight();
					sb.append("[ ");
					sb.append(x1);
					sb.append(" ");
					sb.append(y1);
					sb.append(" ");
					sb.append(x2);
					sb.append(" ");
					sb.append(y2);
					sb.append(" ] ");
					Interprete.calcul.push(new String(sb));
					break;
				case 208: // LongueurEtiquette
					mot = getWord(param.get(0));
					if (null != mot)
						mot = Utils.SortieTexte(mot);
					else
						mot = getFinalList(param.get(0)).trim();
					Interprete.operande = true;
					fm = cadre.getDrawPanel().getGraphics().getFontMetrics(cadre.getDrawPanel().getGraphicsFont());
					longueur = fm.stringWidth(mot);
					Interprete.calcul.push(String.valueOf(longueur));
					break;
				case 209: // envoietcp // sendtcp // enviatcp etcp
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot) { throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word")); }
					mot = mot.toLowerCase();
					liste = "";
					liste = getFinalList(param.get(1));
					NetworkClientSend ncs = new NetworkClientSend(mot, liste);
					Interprete.calcul.push("[ " + ncs.getAnswer() + " ] ");
					/*
					 * {
					 * liste = "[ "; mot2 =
					 * getFinalList(param.get(0).toString()); liste += mot2 + "
					 * ]"; String rip = liste.substring(2,17); // cadre.ecris("perso
					 * ", rip + "\n");
					 * //para debug String rdat = "_" + liste.substring(18,23) +
					 * "*\n\r"; //
					 * cadre.ecris("perso", rdat + "\n"); //para debug Socket
					 * echoSocket = null;
					 * DataOutputStream tcpout = null; BufferedReader tcpin =
					 * null; String resp =
					 * null; { echoSocket = new Socket(rip, 1948); tcpout =
					 * new
					 * DataOutputStream(echoSocket.getOutputStream()); tcpin=
					 * new BufferedReader(new
					 * InputStreamReader(echoSocket.getInputStream()));
					 * tcpout.writeBytes(rdat);
					 * resp = tcpin.readLine(); // readLine detiene el programa
					 * hasta que recibe una
					 * respuesta del robot. Que hacer si no recibe nada?
					 * tcpout.close();
					 * tcpin.close(); echoSocket.close(); } catch
					 * (UnknownHostException e) { throw
					 * new myException( Logo.messages.getString("erreur_tcp"));
					 * } catch
					 * (IOException e) { throw new myException(
					 * Logo.messages.getString("erreur_tcp")); }
					 * Interprete.calcul.push("[ " + resp + "
					 * ]"); } catch(myException e){}
					 */
					break;
				case 210: // ecoutetcp
					/**
					 * Marko Zivkovic : new implementation of context switch.
					 * saving of workspace (now context) is done in {@link ContextManager}
					 */
					Interprete.operande = false;
					new NetworkServer(cadre);
					
					break;
				case 211: // executetcp
					mot = getWord(param.get(0));
					if (null == mot) { throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word")); }
					mot = mot.toLowerCase();
					liste = "";
					liste = getFinalList(param.get(1));
					new NetworkClientExecute(cadre.getKernel(), mot, liste);
					break;
				case 212: // \x internal operator to specify
							// the end of network instructions with
							// "executetcp"
							// have to replace workspace
					Interprete.operande = false;
										
					/*
					 * Marko Zivkovic : new implementation of network context
					 */
					wp.popNetworkMode();
					
					break;
				case 213: // chattcp
					Interprete.operande = false;
					mot = getWord(param.get(0));
					if (null == mot) { throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word")); }
					mot = mot.toLowerCase();
					liste = "";
					liste = getFinalList(param.get(1));
					new NetworkClientChat(cadre, mot, liste);
					break;
				case 214: // init resetall
					Interprete.operande = false;
					// resize drawing zone if necessary
					if (h != 1000 || w != 1000)
					{
						h = 1000;
						w = 1000;
						cadre.resizeDrawingZone();
					}
					uc.setDrawGrid(false);
					uc.setDrawXAxis(false);
					uc.setDrawYAxis(false);
					cadre.getDrawPanel().origine();
					kernel.getActiveTurtle().stroke = new BasicStroke(1);
					cadre.getDrawPanel().setBackgroundColor(Color.WHITE);
					if (kernel.getActiveTurtle().id == 0)
					{
						uc.setActiveTurtle(0);
					}
					DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLASSIC;
					chemin = "tortue0.png";
					kernel.change_image_tortue(chemin);
					cadre.getDrawPanel().fcfg(Color.WHITE);
					cadre.getDrawPanel().fcc(Color.BLACK);
					cadre.getDrawPanel().setAnimation(false);
					font = new Font("dialog", Font.PLAIN, 12);
					WSManager.getWorkspaceConfig().setFont(font);
					kernel.getActiveTurtle().police = 12;
					cadre.getDrawPanel().setGraphicsFont(font);
					HistoryPanel.fontPrint = UserConfig.police_id(font);
					cadre.getHistoryPanel().getDsd().fixepolice(12);
					cadre.getHistoryPanel().getDsd().fixenompolice(HistoryPanel.fontPrint);
					cadre.getHistoryPanel().getDsd().fixecouleur(Color.black);
					uc.setPenShape(PenShape.SQUARE);
					uc.setQuality(DrawQuality.NORMAL);
					kernel.setDrawingQuality(uc.getQuality());
					kernel.setNumberOfTurtles(16);
					uc.setTurtleSpeed(0);
					Kernel.mode_trace = false;
					DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLASSIC;
					cadre.getDrawPanel().zoom(1, false);
					break;
				case 215: // tc taillecrayon
					Interprete.operande = true;
					double penwidth = 2 * kernel.getActiveTurtle().getPenWidth();
					Interprete.calcul.push(String.valueOf(MyCalculator.teste_fin_double(penwidth)));
					break;
				case 216: // setpenshape=ffc fixeformecrayon
					Interprete.operande = false;
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i != PenShape.OVAL.getValue() && i != PenShape.SQUARE.getValue())
					{
						str = Utils.primitiveName("setpenshape") + " " + Logo.messages.getString("error_bad_values");
						str += " " + PenShape.SQUARE.getValue() + " " + PenShape.OVAL.getValue();
						throw new LogoError(str);
					}
					uc.setPenShape(PenShape.getPenShape(i));
					cadre.getDrawPanel().updateAllTurtleShape();
					cadre.getDrawPanel().setStroke(kernel.getActiveTurtle().crayon);
					break;
				case 217: // penshape=fc formecrayon
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(uc.getPenShape().getValue()));
					break;
				case 218: // setdrawingquality=fqd fixequalitedessin
					Interprete.operande = false;
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i != DrawQuality.NORMAL.getValue() && i != DrawQuality.HIGH.getValue()
							&& i != DrawQuality.LOW.getValue())
					{
						str = Utils.primitiveName("setdrawingquality") + " "
								+ Logo.messages.getString("error_bad_values") + " 0 1 2";
						throw new LogoError(str);
					}
					uc.setQuality(DrawQuality.getDrawQuality(i));
					kernel.setDrawingQuality(uc.getQuality());
					break;
				case 219: // drawingquality=qd qualitedessin
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(uc.getQuality().getValue()));
					break;
				case 220: // setturtlesnumber=fmt fixemaxtortues
					Interprete.operande = false;
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i < 0)
					{
						String fmt = Utils.primitiveName("setturtlesnumber");
						throw new LogoError(fmt + " " + Logo.messages.getString("attend_positif"));
					}
					else if (i == 0)
						i = 1;
					kernel.setNumberOfTurtles(i);
					break;
				case 221: // turtlesnumber=maxtortues
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(uc.getMaxTurtles()));
					
					break;
				case 222: // setscreensize=ftd fixetailledessin
					Interprete.operande = false;
					
					String prim = Utils.primitiveName("setscreensize");
					liste = getFinalList(param.get(0));
					int width,
					height;
					st = new StringTokenizer(liste);
					try
					{
						if (!st.hasMoreTokens())
							throw new LogoError(prim + " " + Logo.messages.getString("n_aime_pas") + liste
									+ Logo.messages.getString("comme_parametre"));
						width = Integer.parseInt(st.nextToken().toString());
						if (!st.hasMoreTokens())
							throw new LogoError(prim + " " + Logo.messages.getString("n_aime_pas") + liste
									+ Logo.messages.getString("comme_parametre"));
						height = Integer.parseInt(st.nextToken().toString());
					}
					catch (NumberFormatException e)
					{
						throw new LogoError(prim + " " + Logo.messages.getString("n_aime_pas") + liste
								+ Logo.messages.getString("comme_parametre"));
					}
					if (st.hasMoreTokens())
						throw new LogoError(prim + " " + Logo.messages.getString("n_aime_pas") + liste
								+ Logo.messages.getString("comme_parametre"));
					boolean changement = false;
					if (height != h)
						changement = true;
					int tmp_hauteur = h;
					h = height;
					if (width != w)
						changement = true;
					int tmp_largeur = w;
					w = width;
					if (w < 100 || h < 100)
					{
						w = 1000;
						h = 1000;
					}
					if (changement)
					{
						int memoire_necessaire = w * h * 4 / 1024 / 1024;
						int memoire_image = tmp_hauteur * tmp_largeur * 4 / 1024 / 1024;
						long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
						long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
						/*
						 * System.out.println("memoire nécessaire
						 * "+memoire_necessaire); System.out.println("memoire
						 * image
						 * "+memoire_image); System.out.println("memoire
						 * libre "+free); System.out.println("memoire totale
						 * "+total); System.out.println("memoire envisagee
						 * "+(total-free+memoire_necessaire-memoire_image));
						 * System.out.println();
						 */
						if (total - free + memoire_necessaire - memoire_image < GlobalConfig.getMaximumMemory() * 0.8)
						{
							cadre.resizeDrawingZone();
						}
						else
						{
							w = tmp_largeur;
							h = tmp_hauteur;
							long conseil = 64 * ((total - free + memoire_necessaire - memoire_image) / 64) + 64;
							if (total - free + memoire_necessaire - memoire_image > 0.8 * conseil)
								conseil += 64;
							if (conseil == GlobalConfig.getMaximumMemory())
								conseil += 64;
							String message = Logo.messages.getString("erreur_memoire") + " " + conseil + "\n"
									+ Logo.messages.getString("relancer");
							jt = new MyTextAreaDialog(message);
							JOptionPane.showMessageDialog(cadre.getFrame(), jt, Logo.messages.getString("erreur"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
					break;
				case 223: // guibutton guibouton
					String identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					mot = getWord(param.get(1));
					if (null == mot)
						throw new LogoError(param.get(1) + " " + Logo.messages.getString("error.word"));
					GuiButton gb = new GuiButton(identifier.toLowerCase(), mot, cadre);
					cadre.getDrawPanel().addToGuiMap(gb);
					break;
				case 224: // guiaction
					identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					liste = getFinalList(param.get(1));
					cadre.getDrawPanel().guiAction(identifier, liste);
					break;
				case 225: // guiremove
					identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					cadre.getDrawPanel().guiRemove(identifier);
					break;
				case 226: // guiposition
					identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					liste = getFinalList(param.get(1));
					cadre.getDrawPanel().guiposition(identifier, liste, Utils.primitiveName("guiposition"));
					break;
				case 227: // guidraw
					identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					cadre.getDrawPanel().guiDraw(identifier);
					break;
				case 228: // zoom
					Interprete.operande = false;
					d = kernel.getCalculator().numberDouble(param.get(0));
					if (d <= 0)
					{
						String name = Utils.primitiveName("zoom");
						throw new LogoError(name + " " + Logo.messages.getString("attend_positif"));
					}
					cadre.getDrawPanel().zoom(d, false);
					break;
				case 229: // grille
					Interprete.operande = false;
					primitive2D("grille");
					int[] args = new int[2];
					for (i = 0; i < 2; i++)
					{
						args[i] = kernel.getCalculator().getInteger(param.get(i));
						if (args[i] < 0)
						{
							String grille = Utils.primitiveName("grille");
							throw new LogoError(grille + " " + Logo.messages.getString("attend_positif"));
						}
						else if (args[i] == 0)
						{
							args[i] = 1;
						}
					}
					uc.setDrawGrid(true);
					uc.setXGrid(args[0]);
					uc.setYGrid(args[1]);
					cadre.getDrawPanel().videecran();
					break;
				case 230: // stopgrille
					Interprete.operande = false;
					uc.setDrawGrid(false);
					cadre.getDrawPanel().videecran();
					break;
				case 231: // stopanimation
					cadre.getDrawPanel().setAnimation(false);
					Interprete.operande = false;
					break;
				case 232: // stoptrace
					Kernel.mode_trace = false;
					Interprete.operande = false;
					break;
				case 233: // guimenu
					identifier = getWord(param.get(0));
					if (null == identifier)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					liste = getFinalList(param.get(1));
					GuiMenu gm = new GuiMenu(identifier.toLowerCase(), liste, cadre);
					cadre.getDrawPanel().addToGuiMap(gm);
					break;
				case 234: // axis
					
					Interprete.operande = false;
					
					primitive2D("axis");
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i < 0)
					{
						String name = Utils.primitiveName("axis");
						throw new LogoError(name + " " + Logo.messages.getString("attend_positif"));
					}
					else if (i < 25)
						i = 25;
					uc.setDrawXAxis(true);
					uc.setXAxis(i);
					uc.setDrawYAxis(true);
					uc.setYAxis(i);
					cadre.getDrawPanel().videecran();
					break;
				case 235: // xaxis
					Interprete.operande = false;
					primitive2D("xaxis");
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i < 0)
					{
						String name = Utils.primitiveName("xaxis");
						throw new LogoError(name + " " + Logo.messages.getString("attend_positif"));
					}
					else if (i < 25)
						i = 25;
					uc.setDrawXAxis(true);
					uc.setXAxis(i);
					cadre.getDrawPanel().videecran();
					break;
				case 236: // yaxis
					Interprete.operande = false;
					primitive2D("yaxis");
					i = kernel.getCalculator().getInteger(param.get(0));
					if (i < 0)
					{
						String name = Utils.primitiveName("yaxis");
						throw new LogoError(name + " " + Logo.messages.getString("attend_positif"));
					}
					else if (i < 25)
						i = 25;
					uc.setDrawYAxis(true);
					uc.setYAxis(i);
					cadre.getDrawPanel().videecran();
					break;
				case 237: // stopaxis
					uc.setDrawXAxis(false);
					uc.setDrawYAxis(false);
					Interprete.operande = false;
					cadre.getDrawPanel().videecran();
					break;
				case 238: // bye
					cadre.closeWindow();
					break;
				case 239: // var? variable?
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					mot = mot.toLowerCase();
					if (wp.getGlobals().getVariables().contains(mot) || Interprete.locale.containsKey(mot)) // TODO
																											// changed
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 240: // axiscolor= couleuraxes
					Interprete.operande = true;
					c = new Color(uc.getAxisColor());
					Interprete.calcul.push("[ " + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + " ] ");
					
					break;
				case 241: // gridcolor=couleurgrille
					Interprete.operande = true;
					c = new Color(uc.getGridColor());
					Interprete.calcul.push("[ " + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + " ] ");
					break;
				case 242: // grid?=grille?
					Interprete.operande = true;
					if (uc.isDrawGrid())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 243: // xaxis?=axex?
					Interprete.operande = true;
					if (uc.isDrawXAxis())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 244: // yaxis?=axey?
					Interprete.operande = true;
					if (uc.isDrawYAxis())
						Interprete.calcul.push(Logo.messages.getString("vrai"));
					else
						Interprete.calcul.push(Logo.messages.getString("faux"));
					break;
				case 245: // setgridcolor=fcg fixecouleurgrille
					Interprete.operande = false;
					if (isList(param.get(0)))
					{
						uc.setGridColor(rgb(param.get(0), Utils.primitiveName("setgridcolor")).getRGB());
					}
					else
					{
						int coul = kernel.getCalculator().getInteger(param.get(0)) % DrawPanel.defaultColors.length;
						if (coul < 0)
							coul += DrawPanel.defaultColors.length;
						uc.setGridColor(DrawPanel.defaultColors[coul].getRGB());
					}
					break;
				case 246: // setaxiscolor=fca fixecouleuraxes
					Interprete.operande = false;
					if (isList(param.get(0)))
					{
						uc.setAxisColor(rgb(param.get(0), Utils.primitiveName("setaxiscolor")).getRGB());
					}
					else
					{
						int coul = kernel.getCalculator().getInteger(param.get(0)) % DrawPanel.defaultColors.length;
						if (coul < 0)
							coul += DrawPanel.defaultColors.length;
						uc.setAxisColor(DrawPanel.defaultColors[coul].getRGB());
					}
					break;
				case 247: // perspective
					
					cadre.getDrawPanel().perspective();
					
					break;
				case 248:// rightroll=rd roulisdroite
					delay();
					primitive3D("3d.rightroll");
					cadre.getDrawPanel().rightroll(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 249:// uppitch=cabre
					delay();
					primitive3D("3d.uppitch");
					cadre.getDrawPanel().uppitch(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 250:// leftroll=rg roulisgauche
					delay();
					primitive3D("3d.leftroll");
					cadre.getDrawPanel().rightroll(-kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 251:// downpitch=pique
					delay();
					primitive3D("3d.downpitch");
					cadre.getDrawPanel().uppitch(-kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 252:// roll=roulis
					primitive3D("3d.roll");
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().roll));
					break;
				case 253:// pitch=cabrement tangage
					primitive3D("3d.pitch");
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().pitch));
					break;
				case 254:// setroll=fixeroulis
					primitive3D("3d.setroll");
					delay();
					cadre.getDrawPanel().setRoll(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 255:// setpitch=fixetangage
					primitive3D("3d.setpitch");
					delay();
					cadre.getDrawPanel().setPitch(kernel.getCalculator().numberDouble(param.pop()));
					break;
				case 256:// setorientation=fixeorientation
					primitive3D("3d.setorientation");
					delay();
					cadre.getDrawPanel().setOrientation(getFinalList(param.pop()));
					break;
				case 257: // orientation=orientation
					primitive3D("3d.orientation");
					Interprete.operande = true;
					String pitch = MyCalculator.teste_fin_double(kernel.getActiveTurtle().pitch);
					String roll = MyCalculator.teste_fin_double(kernel.getActiveTurtle().roll);
					String heading = MyCalculator.teste_fin_double(kernel.getActiveTurtle().heading);
					Interprete.calcul.push("[ " + roll + " " + pitch + " " + heading + " ] ");
					break;
				case 258: // setxyz=fposxyz
					primitive3D("3d.setxyz");
					cadre.getDrawPanel().fpos(
							kernel.getCalculator().numberDouble(param.get(0)) + " "
									+ kernel.getCalculator().numberDouble(param.get(1)) + " "
									+ kernel.getCalculator().numberDouble(param.get(2)));
					break;
				case 259: // setz=fixez
					delay();
					primitive3D("3d.setz");
					cadre.getDrawPanel().fpos(
							kernel.getActiveTurtle().X + " " + kernel.getActiveTurtle().Y + " "
									+ kernel.getCalculator().numberDouble(param.get(0)));
					break;
				case 260: // pprop=dprop
					Interprete.operande = false;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					mot2 = getWord(param.get(1));
					if (null == mot2)
						throw new LogoError(param.get(1) + " " + Logo.messages.getString("error.word"));
					wp.getPropertyLists().addPropList(mot, mot2, param.get(2)); // TODO
																				// changed
					break;
				case 261: // gprop=rprop
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					mot2 = getWord(param.get(1));
					if (null == mot2)
						throw new LogoError(param.get(1) + " " + Logo.messages.getString("error.word"));
					value = wp.getPropertyLists().getPropList(mot, mot2);  // TODO
																			// CHANGED
					if (value.startsWith("["))
						value += " ";
					Interprete.calcul.push(value);
					break;
				case 262: // remprop=efprop
					Interprete.operande = false;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					mot2 = getWord(param.get(1));
					if (null == mot2)
						throw new LogoError(param.get(1) + " " + Logo.messages.getString("error.word"));
					wp.getPropertyLists().removePropList(mot, mot2);  // TODO
																		// CHANGED
					break;
				case 263: // plist=lprop
					Interprete.operande = true;
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					Interprete.calcul.push(wp.getPropertyLists().displayPropList(mot)); // TODO
																						// CHANGED
					
					break;
				case 264: // polystart=polydef
					DrawPanel.record3D = DrawPanel.record3D_POLYGON;
					cadre.initViewer3D();
					// if (null==DrawPanel.listPoly) DrawPanel.listPoly=new
					// java.util.Vector<Shape3D>();
					DrawPanel.poly = new ElementPolygon(cadre.getViewer3D());
					break;
				case 265: // polyend=polyfin
					DrawPanel.record3D = DrawPanel.record3D_NONE;
					DrawPanel.poly.addToScene();
					break;
				case 266: // polyview=polyaf vue3d
					primitive3D("3d.polyview");
					cadre.viewerOpen();
					break;
				case 267: // linestart=lignedef
					DrawPanel.record3D = DrawPanel.record3D_LINE;
					cadre.initViewer3D();
					// if (null==DrawPanel.listPoly) DrawPanel.listPoly=new
					// java.util.Vector<Shape3D>();
					DrawPanel.poly = new ElementLine(cadre.getViewer3D(), cadre.getKernel().getActiveTurtle().getPenWidth());
					DrawPanel.poly.addVertex(new Point3d(kernel.getActiveTurtle().X / 1000,
							kernel.getActiveTurtle().Y / 1000, kernel.getActiveTurtle().Z / 1000), kernel
							.getActiveTurtle().couleurcrayon);
					break;
				case 268: // lineend=lignefin
					DrawPanel.record3D = DrawPanel.record3D_NONE;
					DrawPanel.poly.addToScene();
					break;
				case 269: // pointstart=pointdef
					DrawPanel.record3D = DrawPanel.record3D_POINT;
					cadre.initViewer3D();
					// if (null==DrawPanel.listPoly) DrawPanel.listPoly=new
					// java.util.Vector<Shape3D>();
					DrawPanel.poly = new ElementPoint(cadre.getViewer3D(), cadre.getKernel().getActiveTurtle().getPenWidth());
					break;
				case 270: // pointend=pointfin
					DrawPanel.record3D = DrawPanel.record3D_NONE;
					DrawPanel.poly.addToScene();
					break;
				case 271: // textstart=textedef
					DrawPanel.record3D = DrawPanel.record3D_TEXT;
					cadre.initViewer3D();
					// if (null==DrawPanel.listText) DrawPanel.listText=new
					// java.util.Vector<TransformGroup>();
					DrawPanel.poly = null;
					break;
				case 272: // textend=textefin
					DrawPanel.record3D = DrawPanel.record3D_NONE;
					break;
				case 273: // operator <=
					infequal(param);
					break;
				case 274: // operator >=
					supequal(param);
					break;
				case 275: // primitives
					Interprete.operande = true;
					Interprete.calcul.push(kernel.primitive.getAllPrimitives());
					break;
				case 276: // listesproprietes propertylists
					Interprete.operande = true;
					Interprete.calcul.push(new String(getAllpropertyLists()));
					break;
				case 277: // contenu
					Interprete.operande = true;
					sb = new StringBuffer("[ ");
					sb.append(this.getAllProcedures());
					sb.append(this.getAllVariables());
					sb.append(this.getAllpropertyLists());
					sb.append("] ");
					Interprete.calcul.push(new String(sb));
					break;
				case 278: // erpl=eflp effacelistepropriete
					Interprete.operande = false;
					this.erase(param.get(0), "propertylist");
					break;
				case 279: // arithmetic.exp
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().exp(param.get(0)));
					break;
				case 280: // arithmetic.log
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().log(param.get(0)));
					break;
				case 281: // controls.ifelse
					liste = getList(param.get(1));
					liste = new String(Utils.decoupe(liste));
					pred = predicat(param.get(0));
					liste2 = getList(param.get(2));
					liste = new String(Utils.decoupe(liste));
					kernel.primitive.si(pred, liste, liste2);
					Interprete.renvoi_instruction = true;
					break;
				case 282: // workspace.ed
					
					/*
					 * Marko Zivkovic:
					 * With the new Workspace (UserSpace + LogoFile) ed makes
					 * not so much sense, because we work with
					 * multiple files at the same time.
					 * I therefore restrict this command to allow only a single
					 * word as parameter, to open the file,
					 * where the specified procedure is defined, and scroll to
					 * the procedure's line.
					 */
					
					mot = this.getWord(param.get(0));
					if (mot == null)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					if (!wp.isExecutable(mot))
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.proc.does.not.exist"));
					
					cadre.displayProcedure(mot);
					break;
				case 283: // workspace.edall
					/*
					 * Marko Zivkovic:
					 * Again, it makes no sense to open all files at a time.
					 * Instead, I just open the last edited file.
					 */
					wp.editAll();
					break;
				case 284: // controls.foreach pourchaque
							// Variable name
					var = getWord(param.get(0));
					// If it isn't a word
					if (null == var)
						throw new LogoError(param.get(0).toString() + " " + Logo.messages.getString("error.word"));
					// If it's a number
					else
					{
						try
						{
							Double.parseDouble(var);
							throw new LogoError(Logo.messages.getString("erreur_nom_nombre_variable"));
						}
						catch (NumberFormatException e1)
						{}
					}
					li2 = getList(param.get(2));
					li2 = new String(Utils.decoupe(li2));
					li1 = getWord(param.get(1));
					boolean isList = false;
					if (null == li1)
					{
						isList = true;
						li1 = getFinalList(param.get(1));
					}
					Vector<String> elements = new Vector<String>();
					while (!li1.equals(""))
					{
						String character = "";
						// If it's a list
						if (isList)
						{
							character = this.item(li1, 1);
							// If it's a number
							try
							{
								// Fix Bug: foreach "i [1 2 3][pr :i]
								// character=1 , 2 , 3 (without quote)
								Double.parseDouble(character);
								li1 = li1.substring(character.length() + 1);
							}
							catch (NumberFormatException e)
							{
								// Fix Bug: foreach "i [r s t][pr :i]
								// character="r ,  "s or "t
								li1 = li1.substring(character.length());
							}
						}
						// If it's a word
						else
						{
							character = this.itemWord(1, li1);
							li1 = li1.substring(character.length());
							// If it isn't a number, adding a quote
							try
							{
								Double.parseDouble(character);
							}
							catch (NumberFormatException e)
							{
								character = "\"" + character;
							}
						}
						
						elements.add(character);
					}
					if (elements.size() > 0)
					{
						LoopForEach lfe = new LoopForEach(BigDecimal.ZERO, new BigDecimal(elements.size() - 1),
								BigDecimal.ONE, li2, var.toLowerCase(), elements);
						lfe.AffecteVar(true);
						cadre.getKernel().getInstructionBuffer().insert(li2 + Primitive.END_LOOP + " ");
						Primitive.stackLoop.push(lfe);
					}
					break;
				case 285: // controls.forever repetetoujours
					li2 = getList(param.get(0));
					li2 = new String(Utils.decoupe(li2));
					LoopProperties lp = new LoopProperties(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, li2);
					cadre.getKernel().getInstructionBuffer().insert(li2 + Primitive.END_LOOP + " ");
					Primitive.stackLoop.push(lp);
					break;
				case 286: // arithmetic.setdigits
					Interprete.operande = false;
					kernel.initCalculator(kernel.getCalculator().getInteger(param.get(0)));
					break;
				case 287: // arithmetic.digits
					Interprete.operande = true;
					Interprete.calcul.push(String.valueOf(kernel.getCalculator().getDigits()));
					break;
				case 288: // workspace.text
					var = getWord(param.get(0));
					if (null == var)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					
					Procedure proc = wp.getExecutable(var);
					if (proc != null)
					{
						sb = new StringBuffer();
						sb.append("[ [ ");
						// Append variable names
						for (int j = 0; j < proc.nbparametre; j++)
						{
							sb.append(proc.variable.get(j));
							sb.append(" ");
						}
						for (int j = 0; j < proc.optVariables.size(); j++)
						{
							sb.append("[ ");
							sb.append(proc.optVariables.get(j));
							sb.append(" ");
							sb.append(proc.optVariablesExp.get(j).toString());
							sb.append(" ] ");
						}
						sb.append("] ");
						// Append body procedure
						sb.append(proc.cutInList());
						sb.append("] ");
						Interprete.operande = true;
						Interprete.calcul.push(sb.toString());
					}
					else
						throw new LogoError(var + " " + Logo.messages.getString("error.procedure.must.be"));
					break;
				case 289: // workspace.externalcommand
					Interprete.operande = false;
					
					{
						list = getFinalList(param.get(0));
						int index = numberOfElements(list);
						String[] cmd = new String[index];
						for (i = 0; i < index; i++)
						{
							String liste1 = item(list, i + 1);
							cmd[i] = Utils.SortieTexte(getFinalList(liste1).trim());
						}
						try
						{
							/*
							 * String com="";
							 * for(int i=0;i<cmd.length;i++){
							 * com+=cmd[i]+" ";
							 * }
							 * System.out.println(com);
							 */
							Runtime.getRuntime().exec(cmd);
						}
						catch (IOException e2)
						{
							// System.out.println("a");
						}
						
					}
					
					{
						// System.out.println("coucou");
					}
					break;
				case 290: // drawing.saveimage
					String word = getWord(param.get(0));
					if (null == word)
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
					if (word.equals(""))
						throw new LogoError(param.get(0) + " " + Logo.messages.getString("mot_vide"));
					// xmin, ymin, width, height
					int[] coord = new int[4];
					list = getFinalList(param.get(1));
					st = new StringTokenizer(list);
					if (st.countTokens() == 4)
					{
						try
						{
							int j = 0;
							while (st.hasMoreTokens())
							{
								coord[j] = Integer.parseInt(st.nextToken());
								j++;
							}
							coord[0] += w / 2;
							coord[2] += w / 2;
							coord[1] = h / 2 - coord[1];
							coord[3] = h / 2 - coord[3];
							if (coord[2] < coord[0])
							{
								int tmp = coord[0];
								coord[0] = coord[2];
								coord[2] = tmp;
							}
							if (coord[3] < coord[1])
							{
								int tmp = coord[1];
								coord[1] = coord[3];
								coord[3] = tmp;
							}
							coord[2] = coord[2] - coord[0];
							coord[3] = coord[3] - coord[1];
						}
						catch (NumberFormatException e)
						{
							coord[0] = 0;
							coord[2] = w;
							coord[1] = 0;
							coord[3] = h;
						}
					}
					else
					{
						coord[0] = 0;
						coord[2] = w;
						coord[1] = 0;
						coord[3] = h;
					}
					if (coord[2] == 0 || coord[3] == 0)
					{
						coord[0] = 0;
						coord[2] = w;
						coord[1] = 0;
						coord[3] = h;
					}
					cadre.getDrawPanel().saveImage(word, coord);
					Interprete.operande = false;
					break;
				case 291: // sound.mp3play
					Interprete.operande = false;
					if (kernel.getMp3Player() != null)
						kernel.getMp3Player().getPlayer().close();
					mot = getWord(param.get(0));
					if (null == mot)
						throw new LogoError(mot + " " + Logo.messages.getString("error.word"));
					MP3Player player = new MP3Player(cadre, mot);
					kernel.setMp3Player(player);
					kernel.getMp3Player().start();
					break;
				case 292: // sound.mp3stop
					Interprete.operande = false;
					if (null != kernel.getMp3Player())
						kernel.getMp3Player().getPlayer().close();
					break;
				case 293: // zoom
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(DrawPanel.zoom));
					break;
				case 294: // drawing.x
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().getX()));
					break;
				case 295:// drawing.y
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().getY()));
					break;
				case 296: // drawing.z
					Interprete.operande = true;
					primitive3D("drawing.z");
					Interprete.calcul.push(MyCalculator.teste_fin_double(kernel.getActiveTurtle().Z));
					break;
				case 297: // drawing.fillpolygon
					Interprete.operande = false;
					list = getFinalList(param.get(0));
					LoopFillPolygon lfp = new LoopFillPolygon();
					Primitive.stackLoop.push(lfp);
					cadre.getKernel().getInstructionBuffer().insert(Utils.decoupe(list) + Primitive.END_LOOP + " ");
					cadre.getDrawPanel().startRecord2DPolygon();
					break;
				case 298: // arithmetic.alea
					Interprete.operande = true;
					Interprete.calcul.push(MyCalculator.teste_fin_double(Math.random()));
					break;
				case 299: // loop.dountil
					li1 = getList(param.get(0));
					li1 = new String(Utils.decoupe(li1));
					li2 = getList(param.get(1));
					li2 = new String(Utils.decoupe(li2));
					instr = "\\siwhile " + Utils.primitiveName("non") + " " + li2 + "[ " + li1 + "] ";
					LoopWhile lw = new LoopWhile(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, instr);
					Primitive.stackLoop.push(lw);
					cadre.getKernel().getInstructionBuffer().insert(instr + Primitive.END_LOOP + " ");
					break;
				case 300: // loop.dowhile
					li1 = getList(param.get(0));
					li1 = new String(Utils.decoupe(li1));
					li2 = getList(param.get(1));
					li2 = new String(Utils.decoupe(li2));
					instr = "\\siwhile " + li2 + "[ " + li1 + "] ";
					lw = new LoopWhile(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, instr);
					Primitive.stackLoop.push(lw);
					cadre.getKernel().getInstructionBuffer().insert(li1 + instr + Primitive.END_LOOP + " ");
					break;
				case 301: // arithmetic.modulo
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getCalculator().modulo(param.get(0), param.get(1)));
					break;
				case 302: // drawing.setfontjustify
					Interprete.operande = false;
					li1 = getFinalList(param.get(0));
					kernel.getActiveTurtle().setFontJustify(li1);
					break;
				case 303: // drawing.fontjustify
					Interprete.operande = true;
					Interprete.calcul.push(kernel.getActiveTurtle().getFontJustify());
					break;
				case 304: // arithmetic.inf
					inf(param);
					break;
				case 305: // arithmetic.sup
					sup(param);
					break;
				case 306: // arithmetic.infequal
					infequal(param);
					break;
				case 307: // arithmetic.supequal
					supequal(param);
					break;
			
			}
		}
	}
	
	/**
	 * This implementation of the Logo command "define" is slightly changed towards the original XLogo
	 * implementation.
	 * In {@link xlogo.gui.Editor#analyseprocedure()} (which is now {@link LogoFile#setText(String)}),
	 * I added the line statement {@code wp.deleteAllProcedures()}
	 * because we don't want the procedures to
	 * be ordered by definition time in the editor.
	 * We want the programmer to have the procedures in the editor exactly in
	 * the same order as the programmer writes them.
	 * 
	 * @param params
	 * @author Marko Zivkovic
	 * @throws LogoError
	 */
	private void define(Stack<String> params) throws LogoError
	{
		String procedureName;
		String procedureText = null;
		procedureName = getWord(params.get(0));
		if (null == procedureName)
			throw new LogoError(params.get(0) + " " + Logo.messages.getString("error.word"));
		if (procedureName.equals(""))
			throw new LogoError(Logo.messages.getString("procedure_vide"));
		String list = getFinalList(params.get(1));
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i <= numberOfElements(list); i++)
		{
			String liste1 = item(list, i);
			liste1 = getFinalList(liste1);
			
			// First line
			if (i == 1)
			{
				StringTokenizer st = new StringTokenizer(liste1);
				sb.append(Logo.messages.getString("pour"));
				sb.append(" ");
				sb.append(procedureName);
				sb.append(" ");
				
				while (st.hasMoreTokens())
				{
					// Optional variables
					String token = st.nextToken();
					if (token.equals("["))
					{
						sb.append("[ :");
						while (st.hasMoreTokens())
						{
							token = st.nextToken();
							if (token.equals("]"))
							{
								sb.append("] ");
								break;
							}
							else
							{
								sb.append(token);
								sb.append(" ");
							}
						}
					}
					else
					{
						sb.append(":");
						sb.append(token);
						sb.append(" ");
					}
				}
			}
			// Body of the procedure
			else if (i > 1)
			{
				sb.append("\n");
				sb.append(liste1);
			}
		}
		sb.append("\n");
		sb.append(Logo.messages.getString("fin"));
		
		/*
		 * Marko Zivkovic:
		 * In XLogo4Schools, procedures are not created by setting the text
		 * of the editor,
		 * we directly define them in LogoFiles.
		 * Because we have multiple files, the defined procedure will be put
		 * in a special file for programatically defined procedures.
		 * If the procedure name was already defined, then it will be
		 * redefined, as before in XLogo.
		 * If there is an ambiguity with the procedure name, an error will
		 * be displayed "I don't know which one to redefine..."
		 */
		
		if (wp.isProcedureAmbiguous(procedureName))
			throw new LogoError(procedureName + " " + Logo.messages.getString("ws.redefine.ambiguous.procedure"));
		// procedureText is generated above => it is executable
		procedureText = "\n" + new String(sb); 
		Procedure procedure = new Procedure(procedureText);
		
		Procedure other = wp.getExecutable(procedureName);
		String fileName = other != null ? other.getOwnerName() : Logo.messages.getString("ws.generated.procedure");
		procedure.setOwnerName(fileName);
		
		try
		{
			wp.defineProcedure(procedure);
		}
		catch (IOException e)
		{
			throw new LogoError(Logo.messages.getString("ws.could.not.create.file") + " " + fileName);
		}
	}
	
	/**
	 * This method tests if the primitive name exist in 2D mode
	 * 
	 * @param name
	 *            The primitive name
	 * @throws LogoError
	 */
	private void primitive2D(String name) throws LogoError
	{
		if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_3D)
			throw new LogoError(Utils.primitiveName(name) + " " + Logo.messages.getString("error.primitive2D"));
	}
	
	/**
	 * This method tests if the primitive name exist in 2D mode
	 * 
	 * @param name
	 *            The primitive name
	 * @throws LogoError
	 */
	private void primitive3D(String name) throws LogoError
	{
		if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
			throw new LogoError(Utils.primitiveName(name) + " " + Logo.messages.getString("error.primitive3D"));
	}
	
	/**
	 * Returns the code [r g b] for the color i
	 * 
	 * @param i
	 *            Integer representing the Color
	 */
	private void colorCode(int i)
	{
		Interprete.operande = true;
		Color co = DrawPanel.defaultColors[i];
		Interprete.calcul.push("[ " + co.getRed() + " " + co.getGreen() + " " + co.getBlue() + " ] ");
	}
	
	/**
	 * Save all procedures whose name are contained in the Stack pile
	 * 
	 * @param fichier
	 *            The patch to the saved file
	 * @param pile
	 *            Stack Stack containing all procedure names
	 */
	private void saveProcedures(String fichier, Stack<String> pile)
	{
		try
		{
			String aecrire = "";
			boolean bool = true;
			if (!fichier.endsWith(".lgo"))
				fichier += ".lgo";
			String path = Utils.SortieTexte(WSManager.getUserConfig().getDefaultFolder()) + File.separator + fichier;
			try
			{
				// TODO CHANGED
				for (Procedure procedure : wp.getExecutables())
				{
					if (null == pile)
						bool = true;
					else
						bool = (pile.search(procedure.name) != -1);
					if (bool)
					{
						aecrire += Logo.messages.getString("pour") + " " + procedure.name;
						for (int j = 0; j < procedure.nbparametre; j++)
						{
							aecrire += " :" + procedure.variable.get(j);
						}
						aecrire += "\n" + procedure.instruction + Logo.messages.getString("fin") + "\n\n";
					}
				}
			}
			catch (NullPointerException ex)
			{} // Si aucune procédure n'a été définie.
			Utils.writeLogoFile(path, aecrire);
		}
		catch (IOException e2)
		{
			HistoryMessenger.getInstance().dispatchError(Logo.messages.getString("error.ioecriture"));
		}
	}
	
	/**
	 * Returns the Image defined by the path "chemin"
	 * 
	 * @param chemin
	 *            The absolute path for the image
	 * @return BufferedImage defined by the path "chemin"
	 * @throws LogoError
	 *             If Image format isn't valid(jpg or png)
	 */
	private BufferedImage getImage(String path) throws LogoError
	{
		BufferedImage image = null;
		String pathWord = getWord(path);
		if (null == pathWord)
			throw new LogoError(path + " " + Logo.messages.getString("error.word"));
		if (!(pathWord.endsWith(".png") || pathWord.endsWith(".jpg")))
			throw new LogoError(Logo.messages.getString("erreur_format_image"));
		else
		{
			try
			{
				pathWord = Utils.SortieTexte(pathWord);
				File f = new File(Utils.SortieTexte(WSManager.getUserConfig().getDefaultFolder()) + File.separator
						+ pathWord);
				image = ImageIO.read(f);
			}
			catch (Exception e1)
			{
				throw new LogoError(Logo.messages.getString("error.iolecture"));
			}
		}
		return image;
	}
	
	/**
	 * Create a local variable called "mot" with no value.
	 * 
	 * @param mot
	 *            Variable name
	 */
	private void createLocaleName(String mot)
	{
		mot = mot.toLowerCase();
		if (!Interprete.locale.containsKey(mot))
		{
			Interprete.locale.put(mot, null);
		}
	}
	
	/**
	 * Create a new local variable
	 * 
	 * @param param
	 *            The variable name or a list of variable names
	 * @throws LogoError
	 *             If "param" isn't a list containing all variable names, or a
	 *             word
	 */
	
	private void locale(Stack<String> param) throws LogoError
	{
		String li = param.get(0);
		if (LaunchPrimitive.isList(li))
		{
			li = getFinalList(li);
			StringTokenizer st = new StringTokenizer(li);
			while (st.hasMoreTokens())
			{
				String item = st.nextToken();
				isVariableName(item);
				createLocaleName(item);
			}
		}
		else
		{
			String mot = getWord(param.get(0));
			if (null != mot)
			{
				createLocaleName(mot);
			}
			else
				throw new LogoError(param.get(0) + Logo.messages.getString("error.word"));
		}
	}
	
	/**
	 * returns the color defined by [r g b] contained in "ob"
	 * 
	 * @param obj
	 *            the list [r g b]
	 * @param name
	 *            The name of the calling primitive
	 * @return The Object Color
	 * @throws LogoError
	 *             If the list doesn't contain 3 numbers
	 */
	
	private Color rgb(String obj, String name) throws LogoError
	{
		String liste = getFinalList(obj);
		StringTokenizer st = new StringTokenizer(liste);
		if (st.countTokens() != 3)
			throw new LogoError(name + " " + Logo.messages.getString("color_3_arguments"));
		int[] entier = new int[3];
		for (int i = 0; i < 3; i++)
		{
			String element = st.nextToken();
			try
			{
				entier[i] = (int) (Double.parseDouble(element) + 0.5);
			}
			catch (NumberFormatException e)
			{
				throw new LogoError(element + " " + Logo.messages.getString("pas_nombre"));
			}
			if (entier[i] < 0)
				entier[i] = 0;
			if (entier[i] > 255)
				entier[i] = 255;
			
		}
		return (new Color(entier[0], entier[1], entier[2]));
	}
	
	/**
	 * Primitive member or member?
	 * 
	 * @param param
	 *            Stack that contains arguments for the primitive member
	 * @param id
	 *            69 --> member? or 70--> member
	 * @throws LogoError
	 *             Incorrect arguments
	 */
	private void membre(Stack<String> param, int id) throws LogoError
	{
		Interprete.operande = true;
		String mot_retourne = null;
		boolean b = false;
		String mot = getWord(param.get(1));
		String liste = "[ ";
		if (null == mot)
		{ // on travaille sur une liste
		
			liste = getFinalList(param.get(1));
			StringTokenizer st = new StringTokenizer(liste);
			liste = "[ ";
			mot = getWord(param.get(0));
			String str;
			if (null != mot && mot.equals(""))
				mot = "\\v";
			if (null == mot)
				mot = param.get(0).trim();
			while (st.hasMoreTokens())
			{
				str = st.nextToken();
				if (str.equals("["))
					str = extractList(st);
				if (!liste.equals("[ "))
					liste += str + " ";
				if (str.equals(mot) && liste.equals("[ "))
				{
					if (id == 69)
					{
						b = true;
						break;
					}
					else
						liste += str + " ";
				}
			}
		}
		else
		{ // on travaille sur un mot
			String mot2 = getWord(param.get(0));
			if (null != mot2)
			{
				boolean backslash = false;
				for (int i = 0; i < mot.length(); i++)
				{
					char c = mot.charAt(i);
					if (!backslash && c == '\\')
						backslash = true;
					else
					{
						String tmp = Character.toString(c);
						if (backslash)
							tmp = "\\" + tmp;
						if (tmp.equals(mot2))
						{
							if (id == 69)
							{
								b = true;
								break;
							}
							else
							{
								if (!backslash)
									mot_retourne = mot.substring(i, mot.length());
								else
									mot_retourne = mot.substring(i - 1, mot.length());
								break;
							}
						}
						backslash = false;
					}
				}
			}
		}
		if (!liste.equals("[ "))
			Interprete.calcul.push(liste + "] ");
		else if (null != mot_retourne)
		{
			try
			{
				Double.parseDouble(mot_retourne);
				Interprete.calcul.push(mot_retourne);
			}
			catch (NumberFormatException e)
			{
				Interprete.calcul.push(debut_chaine + mot_retourne);
			}
		}
		else if (b)
			Interprete.calcul.push(Logo.messages.getString("vrai"));
		else
			Interprete.calcul.push(Logo.messages.getString("faux"));
	}
	
	/**
	 * Primitive before?
	 * 
	 * @param param
	 *            Stack that contains all arguments
	 * @throws LogoError
	 *             Bad argument type
	 */
	
	private void precede(Stack<String> param) throws LogoError
	{
		Interprete.operande = true;
		boolean b = false;
		String ope[] = { "", "" };
		String mot = "";
		for (int i = 0; i < 2; i++)
		{
			mot = getWord(param.get(i));
			if (null == mot)
				throw new LogoError(param.get(i) + " " + Logo.messages.getString("pas_mot"));
			else
				ope[i] = mot;
		}
		if (ope[1].compareTo(ope[0]) > 0)
			b = true;
		if (b)
			mot = Logo.messages.getString("vrai");
		else
			mot = Logo.messages.getString("faux");
		Interprete.calcul.push(mot);
	}
	
	private void infequal(Stack<String> param) throws LogoError
	{
		Interprete.operande = true;
		Interprete.calcul.push(kernel.getCalculator().infequal(param));
	}
	
	private void supequal(Stack<String> param) throws LogoError
	{
		Interprete.operande = true;
		Interprete.calcul.push(kernel.getCalculator().supequal(param));
	}
	
	private void inf(Stack<String> param) throws LogoError
	{
		Interprete.operande = true;
		Interprete.calcul.push(kernel.getCalculator().inf(param));
	}
	
	private void sup(Stack<String> param) throws LogoError
	{
		Interprete.operande = true;
		Interprete.calcul.push(kernel.getCalculator().sup(param));
	}
	
	/**
	 * / Primitive equal?
	 * 
	 * @param param
	 *            Stack that contains all arguments
	 * @throws LogoError
	 */
	private void equal(Stack<String> param) throws LogoError
	{
		try
		{
			Double.parseDouble(param.get(0));
			Double.parseDouble(param.get(1));
			Interprete.calcul.push(kernel.getCalculator().equal(param));
		}
		catch (NumberFormatException e)
		{
			if (param.get(0).toString().equals(param.get(1).toString()))
				Interprete.calcul.push(Logo.messages.getString("vrai"));
			else
				Interprete.calcul.push(Logo.messages.getString("faux"));
		}
		Interprete.operande = true;
	}
	
	/**
	 * this method returns the boolean corresponding to the string st
	 * 
	 * @param st
	 *            true or false
	 * @return The boolean corresponding to the string st
	 * @throws LogoError
	 *             If st isn't equal to true or false
	 */
	
	private boolean predicat(String st) throws LogoError
	{
		if (st.toLowerCase().equals(Logo.messages.getString("vrai")))
			return true;
		else if (st.toLowerCase().equals(Logo.messages.getString("faux")))
			return false;
		else
			throw new LogoError(st + " " + Logo.messages.getString("pas_predicat"));
		
	}
	
	/**
	 * Returns the word contained in st. If it isn't a word, returns null
	 * 
	 * @param st
	 *            The Object to convert
	 * @return The word corresponding to st
	 */
	private String getWord(Object st)
	{ // Si c'est un mot
		String liste = st.toString();
		if (liste.equals("\""))
		{
			debut_chaine = "";
			return "";
		}
		if (liste.length() > 0 && liste.substring(0, 1).equals("\""))
		{
			debut_chaine = "\"";
			return (liste.substring(1, liste.length()));
		}
		else
			try
			{
				if (liste == String.valueOf(Double.parseDouble(liste)))
					debut_chaine = "";
				else
					debut_chaine = "\"";
				return Utils.SortieTexte(liste);
			}
			catch (NumberFormatException e)
			{}
		return (null);
	}
	
	/**
	 * Returns the list contained in the string li without any lineNumber
	 * 
	 * @param li
	 *            The String corresponding to the list
	 * @return A list without any line Number tag (\0, \1, \2 ...)
	 * @throws LogoError
	 *             List bad format
	 */
	
	private String getFinalList(String li) throws LogoError
	{
		// remove line number
		li = li.replaceAll("\\\\l([0-9])+ ", "");
		// return list
		return getList(li);
	}
	
	/**
	 * Returns the list contained in the string li
	 * 
	 * @param li
	 *            The String corresponding to the list
	 * @return A list with line Number tag (\0, \1, \2 ...)
	 * @throws LogoError
	 *             List bad format
	 */
	private String getList(String li) throws LogoError
	{
		li = li.trim();
		// Retourne la liste sans crochets;
		if (li.substring(0, 1).equals("[") && li.substring(li.length() - 1, li.length()).equals("]"))
		{
			li = li.substring(1, li.length() - 1).trim() + " ";
			if (!li.equals(" "))
				return li;
			else
				return ("");
		}
		else
			throw new LogoError(li + " " + Logo.messages.getString("pas_liste"));
	}
	
	/**
	 * Tests if "li" is a list
	 * 
	 * @param li
	 *            The String to test
	 * @return true if it is a list, else false
	 */
	//
	protected static boolean isList(String li)
	{
		li = li.trim();
		if (li.length() > 0 && li.substring(0, 1).equals("[") && li.substring(li.length() - 1, li.length()).equals("]"))
			return (true);
		return false;
	}
	
	// Format the List (only one space between two elements)
	private String formatList(String li)
	{
		String tampon = "";
		String precedent = "";
		StringTokenizer st = new StringTokenizer(li, " []", true);
		String element = "";
		while (st.hasMoreTokens())
		{
			element = st.nextToken();
			while (st.hasMoreTokens() && element.equals(" "))
			{
				element = st.nextToken();
			}
			if (element.equals("]"))
				tampon = tampon.trim() + "] ";
			else if (element.equals("["))
			{
				if (precedent.equals("["))
					tampon += "[";
				else
					tampon = tampon.trim() + " [";
			}
			else
				tampon += element + " ";
			precedent = element;
		}
		return (tampon.trim());
	}
	
	private String extractList(StringTokenizer st)
	{
		int compteur = 1;
		String crochet = "[ ";
		String element = "";
		while (st.hasMoreTokens())
		{
			element = st.nextToken();
			if (element.equals("["))
			{
				compteur++;
				crochet += "[ ";
			}
			else if (!element.equals("]"))
				crochet += element + " ";
			else if (compteur != 1)
			{
				compteur--;
				crochet += "] ";
			}
			else
			{
				crochet += element + " ";
				break;
			}
		}
		element = crochet;
		compteur = 0;
		return element.trim();
	}
	
	private int extractList(String st, int deb)
	{
		int compteur = 1;
		char element;
		boolean espace = true;
		boolean crochet_ouvert = false;
		boolean crochet_ferme = false;
		for (int i = deb; i < st.length(); i++)
		{
			element = st.charAt(i);
			if (element == '[')
			{
				if (espace)
					crochet_ouvert = true;
				espace = false;
				crochet_ferme = false;
			}
			else if (element == ']')
			{
				if (espace)
					crochet_ferme = true;
				espace = false;
				crochet_ouvert = false;
			}
			else if (element == ' ')
			{
				espace = true;
				if (crochet_ouvert)
				{
					compteur++;
					crochet_ouvert = false;
				}
				else if (crochet_ferme)
				{
					crochet_ferme = false;
					if (compteur != 1)
						compteur--;
					else
					{
						compteur = i;
						break;
					}
				}
			}
		}
		return compteur;
	}
	
	// returns how many elements contains the list "liste"
	private int numberOfElements(String liste)
	{ // calcule le nombre
		// d'éléments dans une
		// liste
		StringTokenizer st = new StringTokenizer(liste);
		int i = 0;
		String element = "";
		while (st.hasMoreTokens())
		{
			element = st.nextToken();
			if (element.equals("["))
				element = extractList(st);
			i++;
		}
		return i;
	}
	
	// returns the item "i" from the list "liste"
	private String item(String liste, int i) throws LogoError
	{ // retourne
		// l'élément i d'une
		// liste
		StringTokenizer st = new StringTokenizer(liste);
		String element = "";
		int j = 0;
		while (st.hasMoreTokens())
		{
			j++;
			element = st.nextToken();
			if (element.equals("["))
				element = extractList(st);
			if (j == i)
				break;
		}
		if (j != i)
			throw new LogoError(Logo.messages.getString("y_a_pas") + " " + i + " "
					+ Logo.messages.getString("element_dans_liste") + liste + "]");
		else if (i == 0 && j == 0)
			throw new LogoError(Logo.messages.getString("liste_vide"));
		try
		{
			Double.parseDouble(element);
			return element;
		} // Si c'est un nombre, on le renvoie.
		catch (Exception e)
		{}
		if (element.startsWith("["))
			return element + " "; // C'est une liste, on la renvoie telle
									// quelle.
		if (element.equals("\\v"))
			element = "";
		return "\"" + element; // C'est forcément un mot, on le renvoie.
	}
	
	// Test if the name of the variable is valid
	private void isVariableName(String st) throws LogoError
	{
		if (st.equals(""))
			throw new LogoError(Logo.messages.getString("variable_vide"));
		if (":+-*/() []=<>&|".indexOf(st) > -1)
			throw new LogoError(st + " " + Logo.messages.getString("erreur_variable"));
		
		try
		{
			Double.parseDouble(st);
			throw new LogoError(Logo.messages.getString("erreur_nom_nombre_variable"));
		}
		catch (NumberFormatException e)
		{	
			
		}
		
	}
	
	// primitve make
	private void donne(Stack<String> param) throws LogoError
	{
		String mot = getWord(param.get(0));
		if (null == mot)
			throw new LogoError(param.get(0) + " " + Logo.messages.getString("error.word"));
		mot = mot.toLowerCase();
		isVariableName(mot);
		if (Interprete.locale.containsKey(mot))
		{
			Interprete.locale.put(mot, param.get(1));
		}
		else
		{
			wp.getGlobals().define(mot, param.get(1)); // TODO changed
		}
	}
	
	private void delay()
	{
		if (WSManager.getUserConfig().getTurtleSpeed() != 0)
		{
			try
			{
				Thread.sleep((long) (WSManager.getUserConfig().getTurtleSpeed()*3));
			}
			catch (InterruptedException e)
			{}
		}
	}
	
	// How many characters in the word "mot"
	private int getWordLength(String mot)
	{// retourne le nombre de caractères
		// d'un mot
		int compteur = 0;
		boolean backslash = false;
		for (int i = 0; i < mot.length(); i++)
		{
			if (!backslash && mot.charAt(i) == '\\')
				backslash = true;
			else
			{
				backslash = false;
				compteur++;
			}
		}
		return compteur;
	}
	
	// the character number "i" in the word "mot"
	private String itemWord(int entier, String mot) throws LogoError
	{
		String reponse = "";
		int compteur = 1;
		boolean backslash = false;
		if (mot.equals(""))
			throw new LogoError(Logo.messages.getString("mot_vide"));
		for (int i = 0; i < mot.length(); i++)
		{
			char c = mot.charAt(i);
			if (!backslash && c == '\\')
				backslash = true;
			else
			{
				if (compteur == entier)
				{
					if (backslash)
						reponse = "\\" + Character.toString(c);
					else
						reponse = Character.toString(c);
					break;
				}
				else
				{
					compteur++;
					backslash = false;
				}
			}
		}
		return reponse;
	}
	
	protected void setWorkspace(UserSpace workspace)
	{
		wp = workspace;
	}
	
	private void ou(Stack<String> param) throws LogoError
	{
		int size = param.size();
		boolean result = false;
		boolean b;
		for (int i = 0; i < size; i++)
		{
			b = predicat(param.get(i).toString());
			result = result | b;
		}
		if (result)
			Interprete.calcul.push(Logo.messages.getString("vrai"));
		else
			Interprete.calcul.push(Logo.messages.getString("faux"));
		Interprete.operande = true;
	}
	
	private void et(Stack<String> param) throws LogoError
	{
		int size = param.size();
		boolean result = true;
		boolean b;
		for (int i = 0; i < size; i++)
		{
			b = predicat(param.get(i).toString());
			result = result & b;
		}
		Interprete.operande = true;
		if (result)
			Interprete.calcul.push(Logo.messages.getString("vrai"));
		else
			Interprete.calcul.push(Logo.messages.getString("faux"));
		
	}
	
	/**
	 * This methods returns a list that contains all procedures name
	 * 
	 * @return A list with all procedure names
	 */
	private StringBuffer getAllProcedures()
	{
		StringBuffer sb = new StringBuffer("[ ");
		// TODO CHANGED
		for (Procedure proc : wp.getExecutables())
		{
			sb.append(proc.getName());
			sb.append(" ");
		}
		sb.append("] ");
		return sb;
	}
	
	/**
	 * This methods returns a list that contains all variables name
	 * 
	 * @return A list with all variables names
	 */
	
	private StringBuffer getAllVariables()
	{
		StringBuffer sb = new StringBuffer("[ ");
		Iterator<String> it = Interprete.locale.keySet().iterator();
		while (it.hasNext())
		{
			String name = it.next();
			sb.append(name);
			sb.append(" ");
		}
		// TODO changed
		for (String key : wp.getGlobals().getVariables())
		{
			if (!Interprete.locale.containsKey(key))
			{
				sb.append(key.toString());
				sb.append(" ");
			}
		}
		sb.append("] ");
		return sb;
	}
	
	/**
	 * This methods returns a list that contains all Property Lists name
	 * 
	 * @return A list with all Property Lists names
	 */
	private StringBuffer getAllpropertyLists()
	{
		StringBuffer sb = new StringBuffer("[ ");
		// TODO changed
		for (String propList : wp.getPropertyLists().getPropListKeys())
		{
			sb.append(propList);
			sb.append(" ");
		}
		sb.append("] ");
		return sb;
	}
	
	/**
	 * Delete The variable called "name" from the workspace if it exists
	 * 
	 * @param name
	 *            The variable name
	 */
	private void deleteVariable(String name)
	{
		if (!Interprete.locale.isEmpty())
		{
			if (Interprete.locale.containsKey(name))
			{
				Interprete.locale.remove(name);
			}
		}
		else
		{
			wp.getGlobals().deleteVariable(name.toLowerCase());
		}
	}
	
	/**
	 * Delete the procedure called "name" from the workspace
	 * 
	 * @param name
	 *            The procedure name
	 */
	private void deleteProcedure(String name)
	{
		wp.eraseProcedure(name.toLowerCase());
	}
	
	/**
	 * According to the type of the data, erase from workspace the resource
	 * called "name"
	 * 
	 * @param name
	 *            The name of the deleted resource, it couls be a list with all
	 *            resource names
	 * @param type
	 *            The type for the data, it could be "variable", "procedure" or
	 *            "propertylist"
	 * @throws LogoError
	 */
	
	private void erase(String name, String type) throws LogoError
	{
		Interprete.operande = false;
		if (LaunchPrimitive.isList(name))
		{
			name = getFinalList(name);
			StringTokenizer st = new StringTokenizer(name);
			while (st.hasMoreTokens())
			{
				String item = st.nextToken();
				this.eraseItem(item, type);
			}
		}
		else
		{
			name = getWord(name);
			if (null != name)
			{
				this.eraseItem(name, type);
			}
			else
				throw new LogoError(name + Logo.messages.getString("error.word"));
			
		}
	}
	
	/**
	 * According to the type of the data, erase from workspace the resource
	 * called "name"
	 * 
	 * @param name
	 *            The name of the deleted resource
	 * @param type
	 *            The type for the data, it could be "variable", "procedure" or
	 *            "propertylist"
	 */
	private void eraseItem(String name, String type)
	{
		if (type.equals("procedure"))
		{
			this.deleteProcedure(name.toLowerCase());
		}
		else if (type.equals("variable"))
		{
			this.deleteVariable(name.toLowerCase());
		}
		else if (type.equals("propertylist"))
		{
			wp.getPropertyLists().removePropList(name.toLowerCase());
		}
		
	}
}
