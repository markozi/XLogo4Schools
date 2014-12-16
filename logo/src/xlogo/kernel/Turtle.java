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

import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.geom.GeneralPath;
import java.awt.Color;
import java.awt.Image;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.StringTokenizer;

import xlogo.Logo;
import xlogo.storage.WSManager;
import xlogo.storage.user.PenShape;
import xlogo.storage.user.UserConfig;
import xlogo.utils.Utils;
import xlogo.Application;

public class Turtle
{
	
	private Application			app;
	
	public Color				couleurcrayon						= Color.black;
	public Stroke				stroke								= new BasicStroke(1);
	
	Color						couleurmodedessin					= Color.black;
	
	public int					id									= -1;
	
	BasicStroke					crayon								= null;
	
	int							police								= 12;
	
	private int					labelHorizontalAlignment			= 0;
	protected static final int	LABEL_HORIZONTAL_ALIGNMENT_LEFT		= 0;
	protected static final int	LABEL_HORIZONTAL_ALIGNMENT_CENTER	= 1;
	protected static final int	LABEL_HORIZONTAL_ALIGNMENT_RIGHT	= 2;
	
	private int					labelVerticalAlignment				= 0;
	protected static final int	LABEL_VERTICAL_ALIGNMENT_BOTTOM		= 0;
	protected static final int	LABEL_VERTICAL_ALIGNMENT_CENTER		= 1;
	protected static final int	LABEL_VERTICAL_ALIGNMENT_TOP		= 2;
	
	// Image for the turtle
	// If null then draw the triangle
	
	Image						tort								= null;
	
	GeneralPath					triangle;
	/**
	 * The turtle heading (degree)
	 */
	public double				heading;
	/**
	 * The turtle roll (degree)
	 */
	public double				roll;
	/**
	 * The turtle pitch (degree)
	 */
	public double				pitch;
	
	/**
	 * The X coordinates on the screen
	 */
	public double				corX;
	/**
	 * The Y coordinates on the screen
	 */
	public double				corY;
	
	public double				angle;
	/**
	 * The X coordinates in real World (3D or 2D)
	 */
	public double				X									= 0;
	/**
	 * The Y coordinates in real World (3D or 2D)
	 */
	public double				Y									= 0;
	/**
	 * The Z coordinates in real World (3D or 2D)
	 */
	public double				Z									= 0;
	/**
	 * Identity Matrix
	 */
	private final double[][]	identity							= new double[3][3];
	{
		identity[0][0] = identity[1][1] = identity[2][2] = 1;
		identity[0][1] = identity[0][2] = identity[1][2] = 0;
		identity[1][0] = identity[2][1] = identity[2][0] = 0;
	}
	/**
	 * This is the rotation Matrix (3x3) in 3D world
	 */
	private double[][]			rotationMatrix						= identity;
	
	int							largeur								= 0;
	
	int							hauteur								= 0;
	
	int							gabarit								= 0;
	
	private boolean				pendown								= true;
	
	private boolean				penReverse							= false;
	
	private boolean				visible								= true;
	
	private int					shape								= WSManager.getUserConfig().getActiveTurtle();
	
	private float				penWidth							= 0;											// half
																													// of
																													// the
																													// pen
																													// width
																													
	public Turtle(Application app)
	{
		UserConfig uc = WSManager.getUserConfig();
		
		this.app = app;
		fixe_taille_crayon(1);
		String chemin = "tortue" + uc.getActiveTurtle() + ".png";
		couleurcrayon = uc.getPencolor();
		couleurmodedessin = uc.getPencolor();
		if (uc.getActiveTurtle() == 0)
		{
			tort = null;
			largeur = 26;
			hauteur = 26;
		}
		else
		{
			// ON teste tout d'abord si le chemin est valide
			if (null == Utils.class.getResource(chemin))
				chemin = "tortue1.png";
			tort = Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(chemin));
			MediaTracker tracker = new MediaTracker(app.getFrame());
			tracker.addImage(tort, 0);
			try
			{
				tracker.waitForID(0);
			}
			catch (InterruptedException e1)
			{}
			largeur = tort.getWidth(app.getFrame());
			hauteur = tort.getHeight(app.getFrame());
			double largeur_ecran = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
			// On fait attention à la résolution de l'utilisateur
			double facteur = largeur_ecran / 1024.0;
			if ((int) (facteur + 0.001) != 1)
			{
				tort = tort.getScaledInstance((int) (facteur * largeur), (int) (facteur * hauteur), Image.SCALE_SMOOTH);
				tracker = new MediaTracker(app.getFrame());
				tracker.addImage(tort, 0);
				try
				{
					tracker.waitForID(0);
				}
				catch (InterruptedException e1)
				{}
			}
			largeur = tort.getWidth(app.getFrame());
			hauteur = tort.getHeight(app.getFrame());
		}
		gabarit = Math.max(hauteur, largeur);
		corX = uc.getImageWidth() / 2;
		corY = uc.getImageHeight() / 2;
		angle = Math.PI / 2;
		heading = 0.0;
		pitch = 0;
		roll = 0;
		X = 0;
		Y = 0;
		Z = 0;
	}
	
	protected void init()
	{
		UserConfig uc = WSManager.getUserConfig();
		
		corX = uc.getImageWidth() / 2;
		corY = uc.getImageHeight() / 2;
		X = 0;
		Y = 0;
		Z = 0;
		heading = 0;
		pitch = 0;
		roll = 0;
		rotationMatrix = identity;
		angle = Math.PI / 2;
		pendown = true;
		stroke = new BasicStroke(1);
		fixe_taille_crayon(1);
		couleurcrayon = uc.getPencolor();
		couleurmodedessin = uc.getPencolor();
		penReverse = false;
	}
	
	void drawTriangle()
	{
		if (null == tort)
		{
			if (null == triangle)
			{
				triangle = new GeneralPath();
			}
			else
				triangle.reset();
			if (DrawPanel.WINDOW_MODE != DrawPanel.WINDOW_3D)
			{
				triangle.moveTo((float) (corX - 10.0 * Math.sin(angle)), (float) (corY - 10.0 * Math.cos(angle)));
				triangle.lineTo((float) (corX + 24.0 * Math.cos(angle)), (float) (corY - 24.0 * Math.sin(angle)));
				triangle.lineTo((float) (corX + 10.0 * Math.sin(angle)), (float) (corY + 10.0 * Math.cos(angle)));
				triangle.lineTo((float) (corX - 10.0 * Math.sin(angle)), (float) (corY - 10.0 * Math.cos(angle)));
			}
			else
			{
				double[] screenCoord = new double[2];
				// The triangle has coordinates: (-10,0,0);(0,24,0);(10,0,0)
				double[] x1 = new double[3];
				x1[0] = X - 20 * rotationMatrix[0][0];
				x1[1] = Y - 20 * rotationMatrix[1][0];
				x1[2] = Z - 20 * rotationMatrix[2][0];
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.moveTo((float) screenCoord[0], (float) screenCoord[1]);
				x1[0] = X + 48 * rotationMatrix[0][1];
				x1[1] = Y + 48 * rotationMatrix[1][1];
				x1[2] = Z + 48 * rotationMatrix[2][1];
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.lineTo((float) screenCoord[0], (float) screenCoord[1]);
				x1[0] = X + 20 * rotationMatrix[0][0];
				x1[1] = Y + 20 * rotationMatrix[1][0];
				x1[2] = Z + 20 * rotationMatrix[2][0];
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.lineTo((float) screenCoord[0], (float) screenCoord[1]);
				triangle.closePath();
				
				// the "aileron" has coordinates: (0,10,0);(0,0,10);(0,0,0)
				x1[0] = X + 15 * rotationMatrix[0][1];
				x1[1] = Y + 15 * rotationMatrix[1][1];
				x1[2] = Z + 15 * rotationMatrix[2][1];
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.moveTo((float) screenCoord[0], (float) screenCoord[1]);
				x1[0] = X + 15 * rotationMatrix[0][2];
				x1[1] = Y + 15 * rotationMatrix[1][2];
				x1[2] = Z + 15 * rotationMatrix[2][2];
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.lineTo((float) screenCoord[0], (float) screenCoord[1]);
				x1[0] = X;
				x1[1] = Y;
				x1[2] = Z;
				screenCoord = app.getDrawPanel().toScreenCoord(x1, false);
				triangle.lineTo((float) screenCoord[0], (float) screenCoord[1]);
			}
		}
	}
	
	public void fixe_taille_crayon(float nb)
	{
		UserConfig uc = WSManager.getUserConfig();
		
		if (nb < 0)
			nb = 1;
		else if (uc.getMaxPenWidth() != -1 && nb > uc.getMaxPenWidth())
			nb = 1;
		if (uc.getPenShape() == PenShape.SQUARE)
			crayon = new BasicStroke(nb, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		else
			crayon = new BasicStroke(nb, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		penWidth = nb / 2;
	}
	
	public float getPenWidth()
	{
		return penWidth;
	}
	
	protected int getShape()
	{
		return shape;
	}
	
	public void setShape(int id)
	{
		shape = id;
	}
	
	protected boolean isVisible()
	{
		return visible;
	}
	
	protected void setVisible(boolean b)
	{
		visible = b;
	}
	
	protected boolean isPenDown()
	{
		return pendown;
	}
	
	protected void setPenDown(boolean b)
	{
		pendown = b;
	}
	
	protected boolean isPenReverse()
	{
		return penReverse;
	}
	
	protected void setPenReverse(boolean b)
	{
		penReverse = b;
	}
	
	protected void setRotationMatrix(double[][] m)
	{
		rotationMatrix = m;
	}
	
	protected double[][] getRotationMatrix()
	{
		return rotationMatrix;
	}
	
	protected double getX()
	{
		if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_3D)
			return X;
		return corX - WSManager.getUserConfig().getImageWidth() / 2;
		
	}
	
	protected double getY()
	{
		if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_3D)
			return Y;
		return WSManager.getUserConfig().getImageHeight() / 2 - corY;
		
	}
	
	protected int getLabelHorizontalAlignment()
	{
		return labelHorizontalAlignment;
	}
	
	protected int getLabelVerticalAlignment()
	{
		return labelVerticalAlignment;
	}
	
	protected void setFontJustify(String list) throws LogoError
	{
		StringTokenizer st = new StringTokenizer(list);
		int i = 0;
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			try
			{
				int j = Integer.parseInt(s);
				if (j < 0 || j > 2)
					throw new LogoError(list + " " + Logo.messages.getString("pas_argument"));
				else
				{
					if (i == 0)
						labelHorizontalAlignment = j;
					else if (i == 1)
						labelVerticalAlignment = j;
				}
			}
			catch (NumberFormatException e)
			{
				throw new LogoError(list + " " + Logo.messages.getString("pas_argument"));
			}
			
			i++;
		}
		if (i != 2)
			throw new LogoError(list + " " + Logo.messages.getString("pas_argument"));
	}
	
	public String getFontJustify()
	{
		StringBuffer sb = new StringBuffer("[ ");
		sb.append(labelHorizontalAlignment);
		sb.append(" ");
		sb.append(labelVerticalAlignment);
		sb.append(" ] ");
		return new String(sb);
	}
}
