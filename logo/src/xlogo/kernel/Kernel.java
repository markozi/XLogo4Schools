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

package xlogo.kernel;

import java.util.ArrayList;
import java.awt.Color;

import xlogo.kernel.userspace.UserSpace;
import xlogo.storage.user.DrawQuality;
import xlogo.storage.workspace.LogoLanguage;
/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq
 */
import xlogo.Application;

public class Kernel
{
	protected static long		chrono		= 0;
	
	protected ArrayFlow			flows		= new ArrayFlow();	// Contient les
																// flux de
																// lecture ou
																// d'écriture
																
	protected static boolean	mode_trace	= false;			// true si le
																// mode trace
																// est
																// enclenchée
																// (permet de
																// suivre les
																// procédures)
																
	// interprete the user command and launch primitive and procedure
	private Interprete			interprete;
	// For all drawing operation
	// protected DrawPanel dg;
	// For primitive
	protected Primitive			primitive	= null;
	private UserSpace			userSpace;
	private Application			app;
	private MP3Player			mp3Player;
	private MyCalculator		myCalculator;
	
	public Kernel(Application app, UserSpace userSpace)
	{
		this.app = app;
		this.userSpace = userSpace;
		initCalculator(-1);
	}
	
	public UserSpace getWorkspace()
	{
		return userSpace;
	}
	
	public void setWorkspace(UserSpace workspace)
	{
		userSpace = workspace;
		interprete.setWorkspace(userSpace);
	}
	
	protected String listSearch() throws xlogo.kernel.LogoError
	{
		return interprete.chercheListe();
	}
	
	public void fcfg(Color color)
	{
		app.getDrawPanel().fcfg(color);
	}
	
	public Turtle getActiveTurtle()
	{
		return app.getDrawPanel().tortue;
	}
	
	public MyCalculator getCalculator()
	{
		return myCalculator;
	}
	
	public void fcc(Color color)
	{
		app.getDrawPanel().fcc(color);
	}
	
	public void vide_ecran()
	{
		app.getDrawPanel().videecran();
	}
	
	public void setNumberOfTurtles(int i)
	{
		app.getDrawPanel().setNumberOfTurtles(i);
	}
	
	public void setDrawingQuality(DrawQuality q)
	{
		app.getDrawPanel().setQuality(q);
	}
	
	public Color getScreenBackground()
	{
		return app.getDrawPanel().getBackgroundColor();
	}
	
	public void change_image_tortue(String chemin)
	{
		app.getDrawPanel().change_image_tortue(app, chemin);
	}
	
	public void initGraphics()
	{
		app.getDrawPanel().initGraphics();
	}
	
	public void buildPrimitiveTreemap(LogoLanguage lang)
	{
		Primitive.buildPrimitiveTreemap(lang);
	}
	
	public String execute(StringBuffer st) throws LogoError
	{
		return interprete.execute(st);
	}
	
	protected void initCalculator(int s)
	{
		myCalculator = new MyCalculator(s);
		
	}
	
	public void initPrimitive()
	{
		primitive = new Primitive(app);
	}
	
	public void initInterprete()
	{
		interprete = new Interprete(app);
	}
	
	/**
	 * Returns the InstructionBuffer containing all commands to execute
	 */
	public InstructionBuffer getInstructionBuffer()
	{
		return interprete.getInstructionBuffer();
	}
	
	public void setMp3Player(MP3Player mp3Player)
	{
		this.mp3Player = mp3Player;
	}
	
	public MP3Player getMp3Player()
	{
		return mp3Player;
	}
	
	class ArrayFlow extends ArrayList<MyFlow>
	{
		ArrayFlow()
		{
			super();
		}
		
		private static final long	serialVersionUID	= 1L;
		
		protected int search(int id)
		{
			for (int i = 0; i < size(); i++)
			{
				if (get(i).getId() == id)
					return i;
			}
			return -1;
		}
		
	}
}
