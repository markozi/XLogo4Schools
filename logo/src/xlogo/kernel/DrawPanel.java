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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;

import javax.swing.JViewport;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;

import org.jogamp.vecmath.Color3f;

import java.awt.FontMetrics;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.Dimension;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.ReplicateScaleFilter;
import java.awt.image.FilteredImageSource;

import org.jogamp.java3d.utils.geometry.Text2D;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import org.jogamp.java3d.*;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Point3d;

import java.util.Stack;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.event.*;

import xlogo.Application;
import xlogo.storage.WSManager;
import xlogo.storage.user.DrawQuality;
import xlogo.storage.user.PenShape;
import xlogo.storage.user.UserConfig;
import xlogo.utils.Utils;
import xlogo.Logo;
import xlogo.kernel.grammar.LogoNumber;
import xlogo.kernel.grammar.LogoType;
import xlogo.kernel.grammar.LogoTypeNull;
import xlogo.kernel.gui.*;
import xlogo.kernel.perspective.*;
/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo
 * 						programming language
 * @author Loïc Le Coq
 */
 public class DrawPanel extends JPanel implements MouseMotionListener,MouseListener {
	public static final LogoTypeNull nullType=new LogoTypeNull();
	private static final long serialVersionUID = 1L;
	public Turtle tortue;
	public Turtle[] tortues;

	/**
	 * When a turtle is active on screen, its number is added to this stack
	 * @uml.property  name="tortues_visibles"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	public Stack<String> tortues_visibles;

	/**
	 * this int indicates the window mode, default 0
	 */
	protected static int WINDOW_MODE = 0;
	/**
	 *  WINDOW MODE: 0 <br>
	 *  Turtles can go out the drawing area
	 */
	protected static final int WINDOW_CLASSIC=0;
	/**
	 *  WRAP MODE: 1 <br>
	 *  Turtles can go out the drawing area and reappear on the other side
	 */
	protected static final int WINDOW_WRAP=1;
	/**
	 * CLOSE MODE: 2 <br>
	 * Turtles can't go out the drawing area
	 */
	protected static final int WINDOW_CLOSE=2;
	/**
	 * Perspective MODE <br>
	 * The screen is a projection of the 3D universe
	 */
	protected static final int WINDOW_3D=3;

	/** Boolean for animation mode */
	public static boolean classicMode=true; // true si classique false si animation
	/** Animation mode:  */
	public final static boolean MODE_ANIMATION=false;
	/** Classic mode */
	public final static boolean MODE_CLASSIC=true;

	/**
	 * 	default predefined colors
	 */
	public static final Color[] defaultColors={Color.BLACK,Color.RED,Color.GREEN,Color.YELLOW,Color.BLUE,
			Color.MAGENTA,Color.CYAN,Color.WHITE,Color.GRAY,Color.LIGHT_GRAY,new Color(128,0,0),new Color(0,128,0),
			new Color(0,0,128),new Color(255,128,0),Color.PINK,new Color(128,0,255),new Color(153,102,0)};


	/**
	 * The id for the drawing font (with primitive label)
	 * @uml.property  name="police_etiquette"
	 */
	protected int police_etiquette;
	/**
	 * The default drawing area color
	 * @uml.property  name="couleurfond"
	 */
	private Color couleurfond = Color.white;
	private Shape shape=null;
	private Line2D line;
	private Rectangle2D rec;
	private final Application cadre;
	/** This Image is used for Buffering the drawing*/
	public static BufferedImage dessin;
	/**
	 * Graphics of the BufferedImage dessin
	 * @uml.property  name="g"
	 */
	private Graphics2D g;
	/** The scale for the zoom*/
	public static double zoom=1;


	/**
	 * All Gui Objects on the drawing area are stored in the GuiMap gm
	 * @uml.property  name="gm"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private final GuiMap gm;
	/**
	 * The Stroke for the triangle turtle
	 * @uml.property  name="crayon_triangle"
	 */
	private final BasicStroke crayon_triangle = new BasicStroke(1);
	/**
	 * Tools for 3D Mode
	 * @uml.property  name="w3d"
	 * @uml.associationEnd
	 */
	private World3D w3d=null;
	/**
	 * Boolean that indicates if the interpreter is recording polygon in 3D Mode
	 */
	protected static int record3D=0;
	protected final static int record3D_NONE=0;
	protected final static int record3D_POLYGON=1;
	protected final static int record3D_LINE=2;
	protected final static int record3D_POINT=3;
	protected final static int record3D_TEXT=4;

	/**
	 * Boolean that indicates if the interpreter is recording polygon in 2D Mode
	 */
	private static int record2D=0;
	private final static int record2D_NONE=0;
	private  final static int record2D_POLYGON=1;
	private Vector<Point2D.Double> stackTriangle;


	protected static Element3D poly;

	private double[] coords;
	private double oldx;
	private double oldy;
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	// Were used for clipping
	//	private double nx,ny,vx,vy,factor,length;
	//	private GeneralPath gp;
	// private Arc2D clipArc;
		private Arc2D arc;
		/**
		 * Button number when user click on the drawing area
		 * @uml.property  name="bouton_souris"
		 */
	private int bouton_souris=0;  	// Numéro du bouton de souris appuyé sur la zone de dessin
	/**
	 * Last coords for last mouse event
	 * @uml.property  name="possouris"
	 */
	private String possouris="[ 0 0 ] ";	// Coordonnées du point du dernier événement souris

	/**
	 * Notify if a mouse event has occured
	 * @uml.property  name="lissouris"
	 */
	private boolean lissouris=false; //Indique si un événement souris est intervenu depuis le debut du programme
	/**
	 * The rectangular selection zone
	 * @uml.property  name="selection"
	 */
	private Rectangle selection;
	/**
	 * Color for the rectangular selection
	 * @uml.property  name="colorSelection"
	 */
	private Color colorSelection;
	/**
	 * The First clicked point when the rectangular selection is created
	 * @uml.property  name="origine"
	 */
	Point origine;
	public DrawPanel(final Application cadre){
		this.gm= cadre.getKernel().getWorkspace().getGuiMap();
		 setLayout(null);
		 this.setPreferredSize(new Dimension(
				 (int)(WSManager.getUserConfig().getImageWidth()*zoom),
				 (int)(WSManager.getUserConfig().getImageHeight()*zoom)));
		 this.cadre=cadre;
    	addMouseListener(this);
    	addMouseMotionListener(this);
		initGraphics();
	}
	/**
	 * This method is used to draw for primitive "forward"
	 * @param arg LogoType which represents the number of steps to move
	 */
	protected LogoType av(final LogoType number){
		if (number.isException()) return number;
		final LogoNumber ln=(LogoNumber)number;
		return av(ln.getValue());

	}
	/**
	 * This method is used to draw for primitive "backward"
	 * @param arg LogoType which represents the number of steps to move
	 */
	protected LogoType re(final LogoType number){
		if (number.isException()) return number;
		final LogoNumber ln=(LogoNumber)number;
		return av(-ln.getValue());

	}
	/**
	 * This method is used to draw for primitive "right"
	 * @param arg LogoType which represents the number of steps to rotate
	 */
	protected LogoType td(final LogoType number){
		if (number.isException()) return number;
		final LogoNumber ln=(LogoNumber)number;
		return td(ln.getValue());

	}	/**
	 * This method is used to draw for primitive "left"
	 * @param arg LogoType which represents the number of steps to rotate
	 */
	protected LogoType tg(final LogoType number){
		if (number.isException()) return number;
		final LogoNumber ln=(LogoNumber)number;
		return td(-ln.getValue());

	}

	/**
	 * This method is used to draw for primitive "forward" and "backward"
	 * @param arg Number of steps
	 */
	protected LogoType av(final double arg) {
	//	Graphics2D g=(Graphics2D)dessin.getGraphics();

		oldx = tortue.corX;
		oldy = tortue.corY;
		if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_CLASSIC) { //mode fenetre
			montrecacheTortue(false);

			tortue.corX = tortue.corX + arg
					* Math.cos(tortue.angle);
			tortue.corY = tortue.corY - arg
					* Math.sin(tortue.angle);
			if (tortue.isPenDown()) {
				g.setStroke(tortue.stroke); // TODO Marko : fix penerase problem. TODO also for other ops, circle etc
				if (tortue.isPenReverse()) {
					g.setColor(couleurfond);
					g.setXORMode(tortue.couleurcrayon);
				} else {
					g.setColor(tortue.couleurcrayon);
					g.setPaintMode();
				}
				if (null==line) line=new Line2D.Double();
			/*	if (null==gp) gp=new GeneralPath();
				else gp.reset();*/
				if (oldx < tortue.corX){
					x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
				}
				if (oldx>tortue.corX){
					x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
				}
				else if (oldx==tortue.corX){
					if (oldy<tortue.corY){
						x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
						}
					else{
						x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
					}
				}

				line.setLine(x1,y1,x2,y2);

				/*
				// perpendicular vector
				nx=y1-y2;
				ny=x2-x1;
				length=Math.sqrt(nx*nx+ny*ny);
				if (length!=0){
					factor=(1+tortue.getPenWidth())/length;
					vx=x2-x1;
					vy=y2-y1;
					gp.moveTo((float)(x1-vx*factor-nx*factor),
						(float)(y1-vy*factor-ny*factor));
					gp.lineTo((float)(x1-vx*factor+nx*factor),
						(float)(y1-vy*factor+ny*factor));
					gp.lineTo((float)(x2+vx*factor+nx*factor),
						(float)(y2+vy*factor+ny*factor));
					gp.lineTo((float)(x2+vx*factor-nx*factor),
						(float)(y2+vy*factor-ny*factor));
					gp.lineTo((float)(x1-vx*factor-nx*factor),
						(float)(y1-vy*factor-ny*factor));
				}
				else{
					float width=tortue.getPenWidth()+0.5f;
					gp.moveTo((float)(x1-width),
							(float)(y1-width));
					gp.lineTo((float)(x1+width),
							(float)(y1-width));
					gp.lineTo((float)(x1+width),
							(float)(y1+width));
					gp.lineTo((float)(x1-width),
							(float)(y1+width));
					gp.lineTo((float)(x1-width),
							(float)(y1-width));
				}
				shape=gp;*/
				tryRecord2DMode(tortue.corX,tortue.corY);
				//g.draw(line);
				//if (!tortue.isVisible())
				//	 clip();
				//g.dispose();

				for (int i = 0; i < 7; i++) {// TODO find other solution for this hack
					g.draw(line);
					clip();
				}
			}
			montrecacheTortue(true);
		} else if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_WRAP) { //mode enroule
			trace_enroule(arg, oldx, oldy);
		} else if (DrawPanel.WINDOW_MODE == DrawPanel.WINDOW_CLOSE) { //mode clos
			try {
				trace_ferme(oldx, oldy, arg);
			} catch (final LogoError e) {
			}
		}
		else if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_3D){
			montrecacheTortue(false);
    		tortue.X=tortue.X+arg*tortue.getRotationMatrix()[0][1];
    		tortue.Y=tortue.Y+arg*tortue.getRotationMatrix()[1][1];
    		tortue.Z=tortue.Z+arg*tortue.getRotationMatrix()[2][1];

    		double tmp[]=new double[3];
    		tmp[0]=tortue.X;
     		tmp[1]=tortue.Y;
     		tmp[2]=tortue.Z;

     		tmp=this.toScreenCoord(tmp,true);
    		tortue.corX = tmp[0];
			tortue.corY = tmp[1];


			if (tortue.isPenDown()) {
				if (tortue.isPenReverse()) {
					g.setColor(couleurfond);
					g.setXORMode(tortue.couleurcrayon);
				} else {
					g.setColor(tortue.couleurcrayon);
					g.setPaintMode();
				}
				if (null==line) line=new Line2D.Double();

				if (oldx < tortue.corX){
					x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
				}
				if (oldx>tortue.corX){
					x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
				}
				else if (oldx==tortue.corX){
					if (oldy<tortue.corY){
						x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
						}
					else{
						x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
					}
				}

				line.setLine(x1,y1,x2,y2);

				g.draw(line);
				g.draw(line);
				g.draw(line);
					 clip();
			}
			montrecacheTortue(true);
		}
		return DrawPanel.nullType;
	}

	/**
	 * This method is used for drawing with primitive "right" or "left"
	 * @param arg The angle to rotate
	 */
	protected LogoType td(final double arg) {
//		System.out.println(tortue.angle);
		if (tortue.isVisible())
			montrecacheTortue(false);
		if (!enabled3D()){
			tortue.heading = ((tortue.heading + arg) % 360 + 360) % 360;
			tortue.angle = Math.toRadians(90 - tortue.heading);
		}
		else{
			tortue.setRotationMatrix(w3d.multiply(tortue.getRotationMatrix(),w3d.rotationZ(-arg)));
			final double[] tmp=w3d.rotationToEuler(tortue.getRotationMatrix());
			tortue.heading=tmp[2];
			tortue.roll=tmp[1];
			tortue.pitch=tmp[0];
		}
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande = false;

		return DrawPanel.nullType;
	}
	/**
	 * This method is used for drawing with primitive "rightroll" or "leftroll"
	 * @param arg
	 */
	protected void rightroll(final double arg) {
//		System.out.println(tortue.angle);
		if (tortue.isVisible())
			montrecacheTortue(false);
		if (enabled3D()){
			tortue.setRotationMatrix(w3d.multiply(tortue.getRotationMatrix(),w3d.rotationY(-arg)));
			final double[] tmp=w3d.rotationToEuler(tortue.getRotationMatrix());
			tortue.heading=tmp[2];
			tortue.roll=tmp[1];
			tortue.pitch=tmp[0];
		}
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande = false;
	}
	/**
	 * This method is used for drawing with primitive "uppitch" or "downpitch"
	 * @param arg
	 */
	protected void uppitch(final double arg) {
//		System.out.println(tortue.angle);
		if (tortue.isVisible())
			montrecacheTortue(false);
		if (enabled3D()){
			tortue.setRotationMatrix(w3d.multiply(tortue.getRotationMatrix(),w3d.rotationX(arg)));
			final double[] tmp=w3d.rotationToEuler(tortue.getRotationMatrix());
			tortue.heading=tmp[2];
			tortue.roll=tmp[1];
			tortue.pitch=tmp[0];
		}
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande = false;
	}
	/**
	 * This method set the turtle's Roll
	 * @param arg The new roll
	 */
	protected void setRoll(final double arg){
		if (tortue.isVisible())
			montrecacheTortue(false);
		tortue.roll=arg;
		tortue.setRotationMatrix(w3d.EulerToRotation(-tortue.roll, tortue.pitch, -tortue.heading));
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande=false;
	}
	/**
	 * This method set the turtle's heading
	 * @param arg The new heading
	 */
	protected void setHeading(final double arg){
		if (tortue.isVisible())
			montrecacheTortue(false);
		tortue.heading=arg;
		tortue.setRotationMatrix(w3d.EulerToRotation(-tortue.roll, tortue.pitch, -tortue.heading));
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande=false;
	}
	/**
	 * This method set the turtle's pitch
	 * @param arg The new pitch
	 */
	protected void setPitch(final double arg){
		if (tortue.isVisible())
			montrecacheTortue(false);
		tortue.pitch=arg;
		tortue.setRotationMatrix(w3d.EulerToRotation(-tortue.roll, tortue.pitch, -tortue.heading));
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande=false;
	}
	/**
	 *
	 * This method set the turtle's orientation
	 * @param arg The new orientation
	 * @throws LogoError If the list doesn't contain three numbers
	 */
	protected void setOrientation(final String arg) throws LogoError{
		initCoords();
		if (tortue.isVisible())
			montrecacheTortue(false);
		extractCoords(arg,Utils.primitiveName("3d.setorientation"));
		tortue.roll = coords[0];
		tortue.pitch = coords[1];
		tortue.heading = coords[2];
		tortue.setRotationMatrix(w3d.EulerToRotation(-tortue.roll, tortue.pitch, -tortue.heading));
		if (tortue.isVisible())
			montrecacheTortue(true);
		Interprete.operande=false;
	}
	/**
	 * Primitive "origine"
	 */
		protected void origine(){ // primitive origine
			try {
				if (!enabled3D())
					fpos("0 0");
				else fpos("0 0 0");
			} catch (final LogoError e) {
			}
			if (tortue.isVisible())
				montrecacheTortue(false);
			tortue.heading = 0;
			tortue.angle = Math.PI / 2;
			tortue.roll=0;
			tortue.pitch=0;
			if (enabled3D())
				tortue.setRotationMatrix(w3d.EulerToRotation(-tortue.roll, tortue.pitch, -tortue.heading));
			if (tortue.isVisible())
				montrecacheTortue(true);
		}


		/**
		 * Primitive distance
		 * @param liste The coords
		 * @param nom
		 * @return The distance from the turtle position to this point
		 * @throws LogoError If bad format list
		 */
		protected double distance(final String liste) throws LogoError {

			initCoords();
			extractCoords(liste,Utils.primitiveName("distance"));
			double distance;
			if (!enabled3D()){
				coords=this.toScreenCoord(coords,false);
				distance = Math.sqrt(Math.pow(tortue.corX - coords[0], 2)
						+ Math.pow(tortue.corY - coords[1], 2));
			}
			else distance= Math.sqrt(Math.pow(tortue.X - coords[0], 2)
						+ Math.pow(tortue.Y - coords[1], 2)+Math.pow(tortue.Z - coords[2], 2));
			return distance;
		}
		protected double[] vers3D(final String liste) throws LogoError{
			final double[] tmp=new double [3];
			initCoords();
			extractCoords(liste,Utils.primitiveName("vers"));
			tmp[0]=coords[0]-tortue.X;
			tmp[1]=coords[1]-tortue.Y;
			tmp[2]=coords[2]-tortue.Z;
			final double length=Math.sqrt(Math.pow(tmp[0],2)+Math.pow(tmp[1],2)+Math.pow(tmp[2],2));
			if (length==0) return tmp;
			tmp[0]=tmp[0]/length;
			tmp[1]=tmp[1]/length;
			tmp[2]=tmp[2]/length;
			final double heading=Math.acos(tmp[1]);
			final double f=Math.sin(heading);
			final double tr_x=-tmp[0]/f;
			final double tr_y=-tmp[2]/f;
			final double roll=Math.atan2(tr_y, tr_x);
			tmp[0]=-Math.toDegrees(roll);
			tmp[1]=0;
			tmp[2]=-Math.toDegrees(heading);
			for (int i=0;i<3;i++){
				if (tmp[i]<0) tmp[i]+=360;
			}
			return tmp;
		}

		/**
		 * Primitive towards in 2D MODE
		 * @param liste the coordinate for the point
		 * @return the rotation angle
		 * @throws LogoError if Bad format List
		 */
		protected double vers2D(final String liste) throws LogoError{
			initCoords();
			extractCoords(liste,Utils.primitiveName("vers"));
			double angle;
			coords=this.toScreenCoord(coords, false);
			if (tortue.corY == coords[1]) {
				if (coords[0] > tortue.corX)
					angle = 90;
				else if (coords[0] == tortue.corX)
					angle = 0;
				else
					angle = 270;
			}
			else if (tortue.corX == coords[0]) {
				if (tortue.corY > coords[1])
					angle = 0;
				else
					angle = 180;
			}
			else {
				angle = Math.toDegrees(Math.atan(Math
						.abs(coords[0] - tortue.corX)
						/ Math.abs(tortue.corY - coords[1])));
		//		System.out.println(coords[0] - tortue.corX+" "+Math.abs(tortue.corY - coords[1])+" "+angle);
				if (coords[0] > tortue.corX && coords[1] > tortue.corY)
					angle = 180 - angle; // 2eme quadrant
				else if (coords[0] < tortue.corX && coords[1] > tortue.corY)
					angle = 180 + angle; // 3eme quadrant
				else if (coords[0] < tortue.corX && coords[1] < tortue.corY)
					angle = 360 - angle; // 4eme quadrant
			}
			return angle;
		}
		/**
		 * Draw with the primitive "setposition" in 2D mode or 3D
		 * @param liste The list with the coordinates to move
		 * @throws LogoError If the coordinates are invalid
		 */
			protected void fpos(final String liste) throws LogoError {
				initCoords();
				oldx = tortue.corX;
				oldy = tortue.corY;
				extractCoords(liste,Utils.primitiveName("drawing.fpos"));
				montrecacheTortue(false);
				if (enabled3D()) {
					tortue.X = coords[0];
					tortue.Y = coords[1];
					tortue.Z = coords[2];
				}
				coords=toScreenCoord(coords,true);

				tortue.corX=coords[0];
				tortue.corY=coords[1];
				if (tortue.isPenDown()) {
					if (tortue.isPenReverse()) {
						g.setColor(couleurfond);
						g.setXORMode(tortue.couleurcrayon);
					} else {
						g.setColor(tortue.couleurcrayon);
						g.setPaintMode();
					}
					if (null==line) line=new Line2D.Double();
					if (oldx < tortue.corX){
						x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
					}
					if (oldx>tortue.corX){
						x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
					}
					else if (oldx==tortue.corX){
						if (oldy<tortue.corY){
							x2=oldx;y2=oldy;x1=tortue.corX;y1=tortue.corY;
							}
						else{
							x1=oldx;y1=oldy;x2=tortue.corX;y2=tortue.corY;
						}
					}
					line.setLine(x1,y1,x2,y2);
					tryRecord2DMode(tortue.corX,tortue.corY);
					g.draw(line);
					clip();
				}
				montrecacheTortue(true);
			}
	public void drawEllipseArc(final double xAxis,final double yAxis, final double angleRotation,final double xCenter,final double yCenter, final double angleStart, final double angleExtent){
		montrecacheTortue(false);
		arc=new Arc2D.Double(-xAxis,-yAxis,2*xAxis,2*yAxis,angleStart,angleExtent,Arc2D.OPEN);
		if (tortue.isPenReverse()) {
			g.setColor(couleurfond);
			g.setXORMode(tortue.couleurcrayon);
		} else {
			g.setColor(tortue.couleurcrayon);
			g.setPaintMode();
		}
		final double tmpx=WSManager.getUserConfig().getImageWidth()/2+xCenter;
		final double tmpy=WSManager.getUserConfig().getImageHeight()/2-yCenter;
		g.translate(tmpx, tmpy);
		g.rotate(-angleRotation);
		g.draw(arc);
		g.rotate(angleRotation);
		g.translate(-tmpx, -tmpy);
	/*	if (null==clipArc) clipArc=new Arc2D.Double();
		clipArc.setArcByCenter(tortue.corX,tortue.corY,
				rayon+2+tortue.getPenWidth(),0,360, Arc2D.OPEN);*/
		clip();
		montrecacheTortue(true); // on efface la tortue si elle st visible
	}
	/**
	 * This method draw an arc on the drawing area
	 * @param rayon The radius
	 * @param pangle Starting angle
	 * @param fangle End angle
	 * @throws LogoError
	 */
	protected void arc(final double rayon, double pangle, double fangle) throws LogoError {
		// Put fangle and pangle between 0 and 360
		fangle = ((90 - fangle) % 360);
		pangle = ((90 - pangle) % 360);
		if (fangle<0) fangle+=360;
		if (pangle<0) pangle+=360;
		// Calculate angle extend
		double angle=pangle-fangle;
		if (angle<0) angle+=360;
		montrecacheTortue(false);
		if (null==arc) arc=new Arc2D.Double();
		if (!enabled3D()){
			if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP) centers=new Vector<Point2D.Double>();
			arc2D(tortue.corX,tortue.corY,rayon,fangle,angle);

	/*	if (null==gp) gp=new GeneralPath();
		else gp.reset();
		gp.moveTo((float)(tortue.corX-rayon-tortue.getPenWidth()),
				(float)(tortue.corY-rayon-tortue.getPenWidth());
		gp.lineTo((float)(tortue.corX-rayon-tortue.getPenWidth()),
				(float)(tortue.corY-rayon-tortue.getPenWidth()));
		gp.lineTo((float)(tortue.corX-rayon-tortue.getPenWidth()),
				(float)(tortue.corY-rayon-tortue.getPenWidth()));
		gp.lineTo((float)(tortue.corX-rayon-tortue.getPenWidth()),
				(float)(tortue.corY-rayon-tortue.getPenWidth()));
		gp.lineTo((float)(tortue.corX-rayon-tortue.getPenWidth()),
				(float)(tortue.corY-rayon-tortue.getPenWidth()));*/
/*		if (null==rec) rec=new Rectangle2D.Double();
		rec.setRect(tortue.corX-rayon-tortue.getPenWidth(),
			tortue.corY-rayon-tortue.getPenWidth(),
			2*(rayon+tortue.getPenWidth()),2*(rayon+tortue.getPenWidth()));*/
		clip();

		}
		else{
			arcCircle3D(rayon,fangle,angle);
		}
		montrecacheTortue(true);
	}
	private void arc2D(final double x, final double y, final double radius,final double fangle, final double angle){
		arc.setArcByCenter(x,y,radius,
				fangle,angle, Arc2D.OPEN);
		if (tortue.isPenReverse()) {
			g.setColor(couleurfond);
			g.setXORMode(tortue.couleurcrayon);
		} else {
			g.setColor(tortue.couleurcrayon);
			g.setPaintMode();
		}
		g.draw(arc);
		clip();

		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP){
			if (x+radius>w&& x<=w){
				pt=new Point2D.Double(-w+x,y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					arc2D(-w+x,y,radius,fangle,angle);
				}
			}
			if (x-radius<0&& x>=0){
				pt=new Point2D.Double(w+x,y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					arc2D(w+x,y,radius,fangle,angle);
				}
			}
			if (y-radius<0&& y>=0){
				pt=new Point2D.Double(x,h+y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					arc2D(x,h+y,radius,fangle,angle);
				}
			}
			if (y+radius>h&&y<=h){
				pt=new Point2D.Double(x,-h+y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					arc2D(x,-h+y,radius,fangle,angle);
				}
			}
		}
	}


	private void arcCircle3D(final double radius,final double angleStart,final double angleExtent) throws LogoError{
		if (null==arc) arc=new Arc2D.Double();
		arc.setArcByCenter(0,0,radius,
					angleStart,angleExtent, Arc2D.OPEN);
		final Shape s=transformShape(arc);
		if (tortue.isPenReverse()) {
			g.setColor(couleurfond);
			g.setXORMode(tortue.couleurcrayon);
		} else {
			g.setColor(tortue.couleurcrayon);
			g.setPaintMode();
		}
		g.draw(s);
		if (DrawPanel.record3D==DrawPanel.record3D_LINE||DrawPanel.record3D==DrawPanel.record3D_POLYGON){
			recordArcCircle3D(radius,angleStart,angleExtent);
		}
	}


	/**
	 *
	 * returns the color for the pixel "ob"
	 * @param liste: The list containing the coordinates of the pixel
	 * @return Color of this pixel
	 * @throws LogoError If the list doesn't contain coordinates
	 */
		protected Color guessColorPoint(final String liste) throws LogoError {
			final UserConfig uc = WSManager.getUserConfig();
			final int w = uc.getImageWidth();
			final int h = uc.getImageHeight();
			initCoords();
			extractCoords(liste,Utils.primitiveName("tc"));
			coords=toScreenCoord(coords,false);
			int couleur = -1;
			final int x=(int)coords[0];
			final int y=(int)coords[1];
			if (0 < x && x < w && 0 < y && y < h) {
				couleur = DrawPanel.dessin.getRGB(x, y);
			}
			return new Color(couleur);
		}
		/**
		 * This method draw a circle from the turtle position on the drawing area
		 * @param radius The radius of the circle
		 * @throws LogoError
		 */
	protected void circle(final double radius) throws LogoError {
		montrecacheTortue(false);
		if (null==arc) arc=new Arc2D.Double();
		if (!enabled3D()){
			if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP) centers=new Vector<Point2D.Double>();
			circle2D(tortue.corX,tortue.corY,radius);
	/*	if (null==clipArc) clipArc=new Arc2D.Double();
		clipArc.setArcByCenter(tortue.corX,tortue.corY,
				rayon+2+tortue.getPenWidth(),0,360, Arc2D.OPEN);*/
		}
		else{
			circle3D(radius);
		}
		montrecacheTortue(true); // on efface la tortue si elle st visible
	}
	/**
	 * This method draws a circle in 2D mode in WRAP mode, makes recursion to draw all circle part on the screen
	 * @param x    x circle center
	 * @param y   y circle center
	 * @param circle   radius
	 * @uml.property  name="pt"
	 */
	private Point2D.Double pt;
	/**
	 * @uml.property  name="centers"
	 */
	private  Vector <Point2D.Double> centers;
	private void circle2D(final double x,final double y, final double radius){
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		arc.setArcByCenter(x,y,radius,
				0,360, Arc2D.OPEN);

		if (tortue.isPenReverse()) {
				g.setColor(couleurfond);
				g.setXORMode(tortue.couleurcrayon);
			} else {
				g.setColor(tortue.couleurcrayon);
				g.setPaintMode();
			}
			g.draw(arc);
			clip();
			if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP){
				if (x+radius>w&& x<=w){
					pt=new Point2D.Double(-w+x,y);
					if (! centers.contains(pt))	{
						centers.add(pt);
						circle2D(-w+x,y,radius);
					}
				}
				if (x-radius<0&& x>=0){
					pt=new Point2D.Double(w+x,y);
					if (! centers.contains(pt))	{
						centers.add(pt);
						circle2D(w+x,y,radius);
					}
				}
				if (y-radius<0&& y>=0){
					pt=new Point2D.Double(x,h+y);
					if (! centers.contains(pt))	{
						centers.add(pt);
						circle2D(x,h+y,radius);
					}
				}
				if (y+radius>h&&y<=h){
					pt=new Point2D.Double(x,-h+y);
					if (! centers.contains(pt))	{
						centers.add(pt);
						circle2D(x,-h+y,radius);
					}
				}
			}
	}

	/**
	 * used for drawing with primitive "dot"
	 * @param liste The list with the dot coordinates
	 * @throws LogoError If the list is invalid coordinates
	 */
	protected void point(final String liste) throws LogoError {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();
		initCoords();
		extractCoords(liste,Utils.primitiveName("drawing.point"));
		coords=toScreenCoord(coords,true);
//		System.out.println(coords[0]+" "+coords[1]+" "+h+" "+w);
		if (coords[0]>0 && coords[1]>0 && coords[0]<w && coords[1] < h) {
			if (tortue.isPenReverse()) {
				g.setColor(couleurfond);
				g.setXORMode(tortue.couleurcrayon);

			} else {
				g.setColor(tortue.couleurcrayon);
				g.setPaintMode();
			}
			if (rec==null) rec=new Rectangle2D.Double();
			 // High quality
			if (uc.getQuality()==DrawQuality.HIGH){
				final double width=tortue.getPenWidth();
				rec.setRect(coords[0]-width+0.5,coords[1]-width+0.5,
						2*width,2*width);
			}
			// Normal or Low Quality
			else{
				// penWidth is 2k or 2k+1??
				final int intWidth=(int)(2*tortue.getPenWidth()+0.5);
				if (intWidth%2==1){
					final double width=tortue.getPenWidth()-0.5;
//					System.out.println(coords[0]+" "+coords[1]);
					rec.setRect(coords[0]-width,coords[1]-width,
							2*width+1,2*width+1);
				}
				else {
					final double width=tortue.getPenWidth();
					rec.setRect(coords[0]-width,coords[1]-width,
							2*width,2*width);
				}
			}
				if (uc.getPenShape()==PenShape.SQUARE){ // MAKE ENUM
					g.fill(rec);
				}
				else if (uc.getPenShape()==PenShape.OVAL){
					if (null==arc) arc=new Arc2D.Double();
					arc.setArcByCenter(coords[0],coords[1],0,0,360,Arc2D.OPEN);
					g.draw(arc);
				}
				clip();
			}
		}



	/**
	 * @throws LogoError
	 *
	 */
	private void circle3D(final double radius) throws LogoError{

		// In camera world,
		// the circle is the intersection of
		// - a plane with the following equation: ax+by+cz+d=0 <-> f(x,y,z)=0
		// - and a sphere with the following equation: (x-tx)^2+(y-ty)^2+(z-tz)^2=R^2 <-> g(x,y,z)=0
		// I found the cone equation resolving f(x/lambda,y/lambda,z/lambda)=0=g(x/lambda,y/lambda,z/lambda)

		final double[] v=new double[3];
		for(int i=0;i<3;i++){
			v[i]=tortue.getRotationMatrix()[i][2];
		}
		v[0]+=w3d.xCamera;
		v[1]+=w3d.yCamera;
		v[2]+=w3d.zCamera;
		w3d.toCameraWorld(v);
		// Now v contains coordinates of a normal vector to the plane in camera world coordinates
		final double a=v[0];
		final double b=v[1];
		final double c=v[2];

		// We convert the turtle coordinates
		v[0]=tortue.X;
		v[1]=tortue.Y;
		v[2]=tortue.Z;
		w3d.toCameraWorld(v);

		final double x=v[0];
		final double y=v[1];
		final double z=v[2];
		// We calculate the number d for the plane equation
		final double d=-a*x-b*y-c*z;

		// We have to work with Bigdecimal because of precision problems

		final BigDecimal[] big=new BigDecimal[6];
		final BigDecimal bx=new BigDecimal(x);
		final BigDecimal by=new BigDecimal(y);
		final BigDecimal bz=new BigDecimal(z);
		final BigDecimal ba=new BigDecimal(a);
		final BigDecimal bb=new BigDecimal(b);
		final BigDecimal bc=new BigDecimal(c);
		final BigDecimal bd=new BigDecimal(d);
		final BigDecimal deux=new BigDecimal("2");
		final BigDecimal screenDistance=new BigDecimal(w3d.screenDistance);
		final BigDecimal bradius=new BigDecimal(String.valueOf(radius));

		// Now we calculate the coefficient for the conic ax^2+bxy+cy^2+dx+ey+f=0
		// Saved in an array

		// lambda=(x*x+y*y+z*z-radius*radius);
		final BigDecimal lambda=bx.pow(2).add(by.pow(2)).add(bz.pow(2)).subtract(bradius.pow(2));

		// x^2 coeff
		//	d*d+2*d*x*a+a*a*lambda;
		big[0]=bd.pow(2).add(bd.multiply(bx).multiply(ba).multiply(deux)).add(ba.pow(2).multiply(lambda));
		// xy coeff
		// 2*d*x*b+2*d*y*a+2*a*b*lambda;
		big[1]=deux.multiply(bd).multiply(bx).multiply(bb).add(deux.multiply(bd).multiply(by).multiply(ba)).add(deux.multiply(ba).multiply(bb).multiply(lambda));
		// y^2 coeff
		// d*d+2*d*y*b+b*b*lambda;
		big[2]=bd.pow(2).add(bd.multiply(by).multiply(bb).multiply(deux)).add(bb.pow(2).multiply(lambda));
		// x coeff
		// 2*w3d.screenDistance*(d*x*c+d*z*a+lambda*a*c);
		big[3]=deux.multiply(screenDistance).multiply(bd.multiply(bx).multiply(bc).add(bd.multiply(bz).multiply(ba)).add(lambda.multiply(ba).multiply(bc)));
		// y coeff
		// 2*w3d.screenDistance*(d*y*c+d*z*b+lambda*b*c);
		big[4]=deux.multiply(screenDistance).multiply(bd.multiply(by).multiply(bc).add(bd.multiply(bz).multiply(bb)).add(lambda.multiply(bb).multiply(bc)));
		// Numbers
		// Math.pow(w3d.screenDistance,2)*(d*d+2*d*z*c+lambda*c*c);
		big[5]=screenDistance.pow(2).multiply(bd.pow(2).add(deux.multiply(bd).multiply(bz).multiply(bc)).add(lambda.multiply(bc.pow(2))));
		new Conic(this,big);
		if (DrawPanel.record3D==DrawPanel.record3D_LINE||DrawPanel.record3D==DrawPanel.record3D_POLYGON){
			recordArcCircle3D(radius,0,360);
		}
	}
	/**
	 * This method records this circle in the polygon's List
	 * @param radius The circle's radius
	 * @param angleStart The starting Angle
	 * @param angleExtent The angle for the sector
	 * @throws LogoError
	 */
	public void recordArcCircle3D(final double radius,final double angleStart,final double angleExtent) throws LogoError{
		final double[][] d=tortue.getRotationMatrix();
		final Matrix3d m=new Matrix3d(d[0][0],d[0][1],d[0][2],d[1][0],d[1][1],d[1][2],d[2][0],d[2][1],d[2][2]);
		// Vector X
		final Point3d v1=new Point3d(radius/1000,0,0);
		final Transform3D t=new Transform3D(m,new Vector3d(),1);
		t.transform(v1);
		// Vector Y
		final Point3d v2=new Point3d(0,radius/1000,0);
		t.transform(v2);

		// Turtle position
		final Point3d pos=new Point3d(tortue.X/1000,tortue.Y/1000,tortue.Z/1000);
		int indexMax=(int)angleExtent;
		if (indexMax!=angleExtent) indexMax+=2;
		else indexMax+=1;
		if (null!=DrawPanel.poly&&DrawPanel.poly.getVertexCount()>1)
			DrawPanel.poly.addToScene();
		if (DrawPanel.record3D==DrawPanel.record3D_POLYGON) {
			DrawPanel.poly=new ElementPolygon(cadre.getViewer3D());
			DrawPanel.poly.addVertex(pos, tortue.couleurcrayon);
		}
		else {
			DrawPanel.poly=new ElementLine(cadre.getViewer3D(), cadre.getKernel().getActiveTurtle().getPenWidth());
		}

		for(int i=0;i<indexMax-1;i++){
			final Point3d tmp1=new Point3d(v1);
			tmp1.scale(Math.cos(Math.toRadians(angleStart+i)));
			final Point3d  tmp2=new Point3d(v2);
			tmp2.scale(Math.sin(Math.toRadians(angleStart+i)));
			tmp1.add(tmp2);
			tmp1.add(pos);
			DrawPanel.poly.addVertex(tmp1, tortue.couleurcrayon);
		}
		final Point3d tmp1=new Point3d(v1);
		tmp1.scale(Math.cos(Math.toRadians(angleStart+angleExtent)));
		final Point3d  tmp2=new Point3d(v2);
		tmp2.scale(Math.sin(Math.toRadians(angleStart+angleExtent)));
		tmp1.add(tmp2);
		tmp1.add(pos);
		DrawPanel.poly.addVertex(tmp1, tortue.couleurcrayon);
	}

/**
 * Load an image and draw it on the drawing area
 * @param image The image to draw
 */
	protected void chargeimage(final BufferedImage image) {
		if (tortue.isVisible())
			montrecacheTortue(false);
		g.setPaintMode();
		g.translate(tortue.corX, tortue.corY);
		g.rotate(-tortue.angle);
		g.drawImage(image, null, 0,0);
		g.rotate(tortue.angle);
		g.translate(-tortue.corX, -tortue.corY);

		clip();
//		repaint();
/*		if (null==rec) rec=new Rectangle2D.Double();
		rec.setRect(tortue.corX,tortue.corY,
				image.getWidth(),image.getHeight());*/
		if (tortue.isVisible())
			montrecacheTortue(true);
	}
	/**
	 * To guess the length before going out the drawing area in WRAP mode
	 * @param mini The minimum distance before leaving
	 * @param maxi The maximum distance before leaving
	 * @param oldx The X turtle location
	 * @param oldy The Y turtle location
	 * @return the number of steps (Recursive dichotomy)
	 */
	private double trouve_longueur(final double mini, final double maxi, final double oldx, final double oldy) {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();
		// renvoie la longueur dont on peut encore avancer
		if (Math.abs(maxi - mini) < 0.5){
			return (mini);}
		else {
			final double milieu = (mini + maxi) / 2;
			final double nx = oldx + milieu * Math.cos(tortue.angle);
			final double ny = oldy - milieu * Math.sin(tortue.angle);
			if (nx < 0 || nx > w|| ny < 0 || ny > h)
				return trouve_longueur(mini, milieu, oldx, oldy);
			else
				return trouve_longueur(milieu, maxi, oldx, oldy);
		}
	}
/**
 * This method is used for drawing with primitive forward, backward in WRAP MODE
 * @param arg the length to forward
 * @param oldx X position
 * @param oldy Y position
 */
	private void trace_enroule(double arg, final double oldx, final double oldy) {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		boolean re = false;
		if (arg < 0) {
			re = true;
		}
		final double diagonale=Math.sqrt(Math.pow(w,2)+Math.pow(h,2))+1;
		double longueur;
		if (re)
			longueur = trouve_longueur(0, -diagonale, oldx, oldy);
		else
			longueur = trouve_longueur(0, diagonale, oldx, oldy);
//		System.out.println(diagonale+" "+oldx+" "+oldy);
		while (Math.abs(longueur) < Math.abs(arg)) {
		//	System.out.println(Math.abs(longueur)+" "+Math.abs(arg));
			arg -= longueur;
			DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLASSIC;
			av(longueur);
			//System.out.println(Math.abs(longueur)+" "+Math.abs(arg));
			if (cadre.error)
				break; //permet d'interrompre avec le bouton stop
			DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_WRAP;
			if (uc.getTurtleSpeed() != 0) {
				try {
					Thread.sleep(uc.getTurtleSpeed() * 5);
				} catch (final InterruptedException e) {
				}
			}
			if (tortue.isVisible())
				this.montrecacheTortue(false);
				if (re) tortue.heading=(tortue.heading+180)%360;
			if (tortue.corX > w-1
					&& (tortue.heading < 180 && tortue.heading != 0)) {
				tortue.corX = 0;
				if (tortue.corY > h-1
						&& (tortue.heading > 90 && tortue.heading < 270))
					tortue.corY = 0;
				else if (tortue.corY < 1
						&& (tortue.heading < 90 || tortue.heading > 270))
					tortue.corY = h;
			} else if (tortue.corX < 1 && tortue.heading > 180) {
				tortue.corX = w;
				if (tortue.corY > h-1
						&& (tortue.heading > 90 && tortue.heading < 270))
					tortue.corY = 0;
				else if (tortue.corY < 1
						&& (tortue.heading < 90 || tortue.heading > 270))
					tortue.corY = h;
			} else if (tortue.corY > h-1)
				tortue.corY = 0;
			else if (tortue.corY < 1)
				tortue.corY = h;
			if (re) tortue.heading=(tortue.heading+180)%360;
			if (tortue.isVisible())
				this.montrecacheTortue(true);
			if (re)
				longueur = trouve_longueur(0, -diagonale, tortue.corX,
						tortue.corY);
			else
				longueur = trouve_longueur(0, diagonale, tortue.corX,
						tortue.corY);
		}
		DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLASSIC;
		if (!cadre.error)
			av(arg);
		DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_WRAP;
	}
/**
 * This method is used for drawing with primitive forward, backward in CLOSE MODE
 * @param oldx X position
 * @param oldy Y position
 * @param arg The length to forward
 * @throws LogoError
 */
	private void trace_ferme(final double oldx, final double oldy, final double arg) throws LogoError {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		boolean re = false;
		double longueur;
		final double diagonale=Math.sqrt(Math.pow(w,2)+Math.pow(h,2))+1;
		if (arg < 0)
			re = true;
		if (re)
			longueur = trouve_longueur(0, -diagonale, oldx, oldy);
		else
			longueur = trouve_longueur(0, diagonale, oldx, oldy);
		if (Math.abs(longueur) < Math.abs(arg))
			throw new LogoError(Logo.messages
					.getString("erreur_sortie1")
					+ "\n"
					+ Logo.messages.getString("erreur_sortie2")
					+ Math.abs((int) (longueur)));
		else {
			DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLASSIC;
			av(arg);
			DrawPanel.WINDOW_MODE = DrawPanel.WINDOW_CLOSE;
		}
	}
	/**
	 * This method extract coords from a list <br>
	 * X is stored in coords(0], Y stored in coords[1], Z Stored in coords[2]
	 * @param liste The list
	 * @param prim The calling primitive
	 * @throws LogoError If List isn't a list coordinate
	 */

	private void extractCoords(final String liste,final String prim)throws LogoError{
		final StringTokenizer st = new StringTokenizer(liste);
		try {
			for(int i=0;i<coords.length;i++){
			coords[i]=1;
			if (!st.hasMoreTokens())
				throw new LogoError(prim
						+ " " + Logo.messages.getString("n_aime_pas") + liste
						+ Logo.messages.getString("comme_parametre"));
			String element = st.nextToken();
			if (element.equals("-")) {
				if (st.hasMoreTokens())
					element = st.nextToken();
				coords[i] = -1;
			}
			coords[i] = coords[i] * Double.parseDouble(element);
			}

			} catch (final NumberFormatException e) {
			throw new LogoError(prim
					+ " " + Logo.messages.getString("n_aime_pas") + liste
					+ Logo.messages.getString("comme_parametre"));
		}
		if (st.hasMoreTokens())
			throw new LogoError(prim
					+ " " + Logo.messages.getString("n_aime_pas") + liste
					+ Logo.messages.getString("comme_parametre"));
	}
	/**
	 * This method sets the drawing area to perspective mode
	 */

	protected void perspective(){
		final UserConfig uc = WSManager.getUserConfig();
    	if (!enabled3D()) {
    		uc.setDrawXAxis(false);
    		uc.setDrawYAxis(false);
    		uc.setDrawGrid(false);
    		change_image_tortue(cadre,"tortue0.png");
        	montrecacheTortue(false);
    		DrawPanel.WINDOW_MODE=DrawPanel.WINDOW_3D;
        	w3d=new World3D();
        	montrecacheTortue(true);
    	}
	}
	/**
	 * This method sets the drawing area to Wrap, Close or Window mode
	 * @param id The window Mode
	 */
	protected void setWindowMode(final int id){
		if (DrawPanel.WINDOW_MODE!=id) {
    		montrecacheTortue(false);
    		DrawPanel.WINDOW_MODE=id;
        	w3d=null;
    		montrecacheTortue(true);
    	}
	}


	/**
	 * This method converts the coordinates contained in "coords" towards the coords on the drawing area
	 */
	double[] toScreenCoord(final double[] coord,final boolean drawPoly){
		// If Mode perspective is active
		if (enabled3D()){
	//		w3d.toScreenCoord(coord);
			// camera world
			// If we have to record the polygon coordinates
    		if (DrawPanel.record3D!=DrawPanel.record3D_NONE&&DrawPanel.record3D!=DrawPanel.record3D_TEXT&&drawPoly){

    			DrawPanel.poly.addVertex(new Point3d(coord[0]/1000,coord[1]/1000,coord[2]/1000),tortue.couleurcrayon);
			}

			w3d.toCameraWorld(coord);

    		// Convert to screen Coordinates
    		w3d.cameraToScreen(coord);
		}
		// Mode2D
		else {
			final UserConfig uc = WSManager.getUserConfig();
			final int w = uc.getImageWidth();
			final int h = uc.getImageHeight();
			coord[0]=w/2+coord[0];
			coord[1]=h/2-coord[1];
		}
		return coord;
	}



	/**
	 * This method creates an instance of coord with the valid size:<br>
	 * size 2 for 2D coordinates<br>
	 * size 3 for 3D coordinates
	 */

	private void initCoords(){

		if (null==coords) coords=new double[2];
		if (enabled3D()){
			if (coords.length!=3) coords=new double[3];
			}
		else  {
			if (coords.length!=2) coords=new double[2];
		}
	}
	public boolean  enabled3D(){
		return (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_3D);
	}

	/**
	 * For hideturtle and showturtle
	 */
	protected void ct_mt() {
		if (null == tortue.tort) {
			g.setXORMode(couleurfond);
			g.setColor(tortue.couleurcrayon);
			tortue.drawTriangle();
			final BasicStroke crayon_actuel = (BasicStroke) g.getStroke();
			if (crayon_actuel.getLineWidth() == 1)
				g.draw(tortue.triangle);
			else {
				g.setStroke(crayon_triangle);
				g.draw(tortue.triangle);
				g.setStroke(crayon_actuel);
			}
		} else {
			g.setXORMode(couleurfond);
			final double angle = Math.PI / 2 - tortue.angle;
			final float x = (float) (tortue.corX * Math.cos(angle) + tortue.corY
					* Math.sin(angle));
			final float y = (float) (-tortue.corX * Math.sin(angle) + tortue.corY
					* Math.cos(angle));
			g.rotate(angle);
			g.drawImage(tortue.tort, (int) x - tortue.largeur / 2,
					(int) y - tortue.hauteur / 2, this);
			g.rotate(-angle);
		}
/*		if (null==rec) rec=new Rectangle2D.Double();
		rec.setRect(tortue.corX - tortue.gabarit,
				tortue.corY - tortue.gabarit,
				tortue.gabarit * 2,
				tortue.gabarit * 2);
	*/
		clip();

/*		clip((int) (tortue.corX - tortue.gabarit),
				(int) (tortue.corY - tortue.gabarit),
				tortue.gabarit * 2, tortue.gabarit * 2);*/
	}
	/**
	 * When the turtle has to be redrawn, this method erase the turtle on the drawing screen
	 *
	 */
	protected void montrecacheTortue(final boolean b) {
			g.setColor(couleurfond);
			for (int i = 0; i < tortues_visibles.size(); i++) {
				final int id = Integer.parseInt(tortues_visibles.get(i));
				// Turtle triangle
				if (null == tortues[id].tort) {
					g.setXORMode(couleurfond);
					g.setColor(tortues[id].couleurmodedessin);
					tortues[id].drawTriangle();
					final BasicStroke crayon_actuel = (BasicStroke) g.getStroke();
					if (crayon_actuel.getLineWidth() == 1)
						g.draw(tortues[id].triangle);
					else {
						g.setStroke(crayon_triangle);
						g.draw(tortues[id].triangle);
						g.setStroke(crayon_actuel);
					}
				} else {
					// Image turtle
					g.setXORMode(couleurfond);
					final double angle = Math.PI / 2 - tortues[id].angle;
					final float x = (float) (tortues[id].corX * Math.cos(angle) + tortues[id].corY
							* Math.sin(angle));
					final float y = (float) (-tortues[id].corX * Math.sin(angle) + tortues[id].corY
							* Math.cos(angle));
					g.rotate(angle);
					g.drawImage(tortues[id].tort, (int) x
							- tortues[id].largeur / 2, (int) y
							- tortues[id].hauteur / 2, this);
					g.rotate(-angle);
				}
				/*if (null==rec) rec=new Rectangle2D.Double();
				rec.setRect(tortues[id].corX - tortues[id].gabarit,
						tortues[id].corY - tortues[id].gabarit,
						tortues[id].gabarit * 2,
						tortues[id].gabarit * 2);
				shape=rec;*/
				if (b) clip();
			}
		}



/*	private void montrecacheTortue() {
	//	Graphics2D g=(Graphics2D)dessin.getGraphics();
		g.setColor(couleurfond);
		for (int i = 0; i < tortues_visibles.size(); i++) {
			int id = Integer.parseInt(String.valueOf(tortues_visibles
					.get(i)));

			if (null == tortues[id].tort) {
				g.setXORMode(couleurfond);
				g.setColor(tortues[id].couleurmodedessin);
				tortues[id].coord();
				BasicStroke crayon_actuel = (BasicStroke) g.getStroke();
				if (crayon_actuel.getLineWidth() == 1)
					g.draw(tortues[id].triangle);
				else {
					g.setStroke(crayon_triangle);
					g.draw(tortues[id].triangle);
					g.setStroke(crayon_actuel);
				}
			} else {
				g.setXORMode(couleurfond);
				double angle = Math.PI / 2 - tortues[id].angle;
				float x = (float) (tortues[id].corX * Math.cos(angle) + tortues[id].corY
						* Math.sin(angle));
				float y = (float) (-tortues[id].corX * Math.sin(angle) + tortues[id].corY
						* Math.cos(angle));
				g.rotate(angle);
				g.drawImage(tortues[id].tort, (int) x
						- tortues[id].largeur / 2, (int) y
						- tortues[id].hauteur / 2, cadre.getArdoise());
				g.rotate(-angle);
			}
	/*		if (null==rec) rec=new Rectangle2D.Double();
			rec.setRect(tortues[id].corX - tortues[id].gabarit,
					tortues[id].corY - tortues[id].gabarit,
					tortues[id].gabarit * 2,
					tortues[id].gabarit * 2);

			clip();
		//	g.dispose();
		}
	}
*/
	/**
	 * Primitive clearscreen
	 */
	protected void videecran() {
		// Delete all Gui Component
		final Set<String> set=gm.keySet();
		final Iterator<String> it=set.iterator();
		while(it.hasNext()){
			final String element=it.next();
			gui=gm.get(element).getGuiObject();
			it.remove();
			if (SwingUtilities.isEventDispatchThread()){
				remove(gui);
				validate();
			}
			else {
				try{
					SwingUtilities.invokeAndWait(new Runnable(){
						public void run(){
							remove(gui);
							validate();
						}
					});
				}
				catch(final Exception e){}
			}
		}


		// Delete List Polygon in 3D mode
//		DrawPanel.listPoly=new Vector<Shape3D>();
//		DrawPanel.listText=new Vector<TransformGroup>();
		// Erase the 3d viewer if visible
		if (null!=cadre.getViewer3D())	{
			cadre.getViewer3D().clearScreen();
			System.gc();
		}

		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		g.setPaintMode();
		couleurfond=uc.getScreencolor();
		g.setColor(uc.getScreencolor());
		g.fillRect(0, 0, w,h);
		stopRecord2DPolygon();

		// Draw Grid
		g.setStroke(new BasicStroke(1));
		drawGrid();
		drawXAxis();
		drawYAxis();
		// Init Turtles
		if (null == tortues[0])
			tortues[0] = new Turtle(cadre);
		// The active turtle will be the turtle 0
		tortue = tortues[0];
		tortue.id = 0;
		// We delete all other turtles
		for (int i = 1; i < tortues.length; i++) {
			tortues[i] = null;
		}
		tortues_visibles.removeAllElements();
		tortues_visibles.push("0");
		g.setColor(tortue.couleurcrayon);
		clip();
		tortue.init();
		tortue.setVisible(true);
		g.setStroke(new BasicStroke(1));
		montrecacheTortue(true);
		// Update the selection frame
		updateColorSelection();

	}
	/**
	 * Primitive wash
	 */
	protected void nettoie() {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		stopRecord2DPolygon();
		g.setPaintMode();
		g.setColor(couleurfond);
		g.fillRect(0, 0, w,h);

		drawGrid();
		/* Réinitialiser les tortues
		if (null == tortues[0])
			tortues[0] = new Tortue(cadre);
		tortue = tortues[0]; //la tortue active sera à présent la
		// numéro 0
		tortue.id = 0;
		for (int i = 1; i < tortues.length; i++) { //On élimine les
			// autres tortues
			tortues[i] = null;
		}
		tortues_visibles.removeAllElements();
		tortues_visibles.push("0");*/
		g.setColor(tortue.couleurcrayon);
		clip();

		if (tortue.isVisible())
			montrecacheTortue(true);
		else
			tortues_visibles=new Stack<String>();
	}
	/**
	 * Used for primitive fillzone
	 * @param x
	 * @param y
	 * @param increment
	 * @param couleur_frontiere
	 * @return
	 */

	private int bornes_remplis_zone(int x, final int y, final int increment, final int couleur_frontiere) {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
//		System.out.println(x+" "+y);
		while (!meme_couleur(DrawPanel.dessin.getRGB(x, y) ,couleur_frontiere)) {
			DrawPanel.dessin.setRGB(x, y, couleur_frontiere);
			x = x + increment;
			if (!(x > 0 && x < w-1))
				break;
		}
		return x - increment;
	}
	/**
	 * Are the two color equals?
	 * @param col1 The first color
	 * @param col2 The second color
	 * @return true or false
	 */
	private boolean meme_couleur(final int col1,final int col2){
/*		if (Config.quality==Logo.QUALITY_HIGH){
			int rouge1 = (col1 >> 16) & 0xFF;
			int vert1 = (col1 >> 8) & 0xFF;
			int bleu1 = col1 & 0xFF;
			int rouge2 = (col2 >> 16) & 0xFF;
			int vert2 = (col2 >> 8) & 0xFF;
			int bleu2 = col2 & 0xFF;
			int tolerance=120;
			int diff_rouge=rouge1-rouge2;
			int diff_bleu=bleu1-bleu2;
			int diff_vert=vert1-vert2;
			boolean rouge;boolean vert; boolean bleu;
			if (rouge1>rouge2){
				if (rouge1-rouge2< 128 -rouge2/2) rouge=true;
				else rouge=false;
			}
			else{
				if (rouge2-rouge1<rouge2/2) rouge=true;
				else rouge=false;
			}
			if (vert1>vert2){
				if (vert1-vert2< 128 -vert2/2) vert=true;
				else vert=false;
			}
			else{
				if (vert2-vert1<vert2/2) vert=true;
				else vert=false;
			}
			if (bleu1>bleu2){
				if (bleu1-bleu2< 128 -bleu2/2) bleu=true;
				else bleu=false;
			}
			else{
				if (bleu2-bleu1<bleu2/2) bleu=true;
				else bleu=false;
			}

		return rouge&&bleu&&vert;
//			if (Math.abs(rouge1-rouge2)<tolerance&&Math.abs(vert1-vert2)<tolerance&&Math.abs(bleu1-bleu2)<tolerance&&Math.abs(rouge1+bleu1+vert1-rouge2-bleu2-vert2)<450)
//			return true;
	//	else return false;
		}
		else{*/
			return (col1==col2);
		//}
	}
	/**
	 * Primitive fillzone
	 */
	protected void rempliszone() {
		montrecacheTortue(false);
		final int x = (int) (tortue.corX + 0.5);
		final int y = (int) (tortue.corY + 0.5);
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();
		if (x > 0 & x < w & y > 0 & y < h) {
			final int couleur_origine = DrawPanel.dessin.getRGB(x, y);
			final int couleur_frontiere = tortue.couleurcrayon.getRGB();
		//	System.out.println(couleur_origine+" " +couleur_frontiere);
			final Stack<Point> pile_germes = new Stack<Point>();
			final boolean couleurs_differentes = !meme_couleur(couleur_origine,couleur_frontiere);
			if (couleurs_differentes)
				pile_germes.push(new Point(x, y));
			while (!pile_germes.isEmpty()) {

				final Point p = pile_germes.pop();
				final int xgerme = p.x;
				final int ygerme = p.y;
				final int xmax = bornes_remplis_zone(xgerme, ygerme, 1,
						couleur_frontiere);
				int xmin=0;
				if (xgerme>0) xmin = bornes_remplis_zone(xgerme - 1, ygerme, -1,
						couleur_frontiere);
				boolean ligne_dessus = false;
				boolean ligne_dessous = false;
				for (int i = xmin; i < xmax + 1; i++) {
					//on recherche les germes au dessus et au dessous
					if (ygerme > 0
							&& meme_couleur(DrawPanel.dessin.getRGB(i, ygerme - 1) ,couleur_frontiere)) {
						if (ligne_dessus)
							pile_germes.push(new Point(i - 1, ygerme - 1));
						ligne_dessus = false;
					} else {
						ligne_dessus = true;
						if (i == xmax && ygerme > 0)
							pile_germes.push(new Point(xmax, ygerme - 1));
					}
					if (ygerme < h-1
							&& meme_couleur(DrawPanel.dessin.getRGB(i, ygerme + 1),couleur_frontiere)) {
						if (ligne_dessous)
							pile_germes.push(new Point(i - 1, ygerme + 1));
						ligne_dessous = false;
					} else {
						ligne_dessous = true;
						if (i == xmax && ygerme < h-1)
							pile_germes.push(new Point(xmax, ygerme + 1));
					}
				}
			}
			clip();
			montrecacheTortue(true);
		}
	}
	/**
	 * Used for primitive "fill"
	 * @param x
	 * @param y
	 * @param increment
	 * @param couleur_crayon
	 * @param couleur_origine
	 * @return
	 */
	private int bornes_remplis(int x, final int y, final int increment, final int couleur_crayon,
			final int couleur_origine) {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		while (DrawPanel.dessin.getRGB(x, y) == couleur_origine) {
			DrawPanel.dessin.setRGB(x, y, couleur_crayon);
			x = x + increment;
			if (!(x > 0 && x < w-1))
				break;
		}
		return x - increment;
	}
	/**
	 * Primitive "fill"
	 */
	protected void remplis() {
		montrecacheTortue(false);
		final int x = (int) (tortue.corX + 0.5);
		final int y = (int) (tortue.corY + 0.5);
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		if (x > 0 & x < w & y > 0 & y < h) {
		final int couleur_origine = DrawPanel.dessin.getRGB(x, y);
		final int couleur_crayon = tortue.couleurcrayon.getRGB();
		if (x > 0 & x < w & y > 0 & y < h) {
			final Stack<Point> pile_germes = new Stack<Point>();
			final boolean couleurs_differentes = !(couleur_origine == couleur_crayon);
			if (couleurs_differentes)
				pile_germes.push(new Point(x, y));
			while (!pile_germes.isEmpty()) {
				final Point p =  pile_germes.pop();
				final int xgerme = p.x;
				final int ygerme = p.y;
				//			System.out.println(xgerme+" "+ygerme);
				final int xmax = bornes_remplis(xgerme, ygerme, 1, couleur_crayon,
						couleur_origine);
				int xmin=0;
				if (xgerme>0) xmin = bornes_remplis(xgerme - 1, ygerme, -1,
						couleur_crayon, couleur_origine);
				//				System.out.println("xmax "+xmax+"xmin "+xmin);
				boolean ligne_dessus = false;
				boolean ligne_dessous = false;
				for (int i = xmin; i < xmax + 1; i++) {
					//on recherche les germes au dessus et au dessous
					if (ygerme > 0
							&& DrawPanel.dessin.getRGB(i, ygerme - 1) != couleur_origine) {
						if (ligne_dessus)
							pile_germes.push(new Point(i - 1, ygerme - 1));
						ligne_dessus = false;
					} else {
						ligne_dessus = true;
						if (i == xmax && ygerme > 0)
							pile_germes.push(new Point(xmax, ygerme - 1));
					}
					if (ygerme < h-1
							&& DrawPanel.dessin.getRGB(i, ygerme + 1) != couleur_origine) {
						if (ligne_dessous)
							pile_germes.push(new Point(i - 1, ygerme + 1));
						ligne_dessous = false;
					} else {
						ligne_dessous = true;
						if (i == xmax && ygerme < h-1)
							pile_germes.push(new Point(xmax, ygerme + 1));
					}
				}
			}
			clip();
			montrecacheTortue(true);
		}
		}
	}
	/**
	 * Primitive "label"
	 * @param mot The word to write on the drawing area
	 */
	protected void etiquette(final String mot) {
		//	Graphics2D g = (Graphics2D) Ardoise.dessin.getGraphics();
		montrecacheTortue(false);
		if (!enabled3D()){
			final double angle = Math.PI / 2 - tortue.angle;
			if(DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP) centers=new Vector<Point2D.Double>();
			etiquette2D(tortue.corX,tortue.corY,angle,mot);
/*			g.rotate(angle);
			g.setPaintMode();
			g.setColor(tortue.couleurcrayon);
			float x = (float) (tortue.corX * Math.cos(angle) + tortue.corY
				* Math.sin(angle));
			float y = (float) (-tortue.corX * Math.sin(angle) + tortue.corY
				* Math.cos(angle));
			g.setFont(Panel_Font.fontes[police_etiquette]
				.deriveFont((float) tortue.police));
			g.drawString(mot, x, y);
			g.rotate(-angle);*/
		}
		else{
			final FontRenderContext frc=g.getFontRenderContext();
			final GlyphVector gv=g.getFont().createGlyphVector(frc, mot);
			final Shape outline=gv.getOutline(0, 0);
			final Shape s=transformShape(outline);
			g.setPaintMode();
			g.setColor(tortue.couleurcrayon);
			g.fill(s);
			if (record3D==DrawPanel.record3D_TEXT){
				final Text2D text=new Text2D(
						mot,new Color3f(tortue.couleurcrayon.getRGBColorComponents(null)), UserConfig.fontes[police_etiquette].getName(),
						tortue.police,Font.PLAIN);

				text.setRectangleScaleFactor(0.001f);
				final Appearance appear=text.getAppearance();
				final PolygonAttributes pa=new PolygonAttributes();
				pa.setCullFace(PolygonAttributes.CULL_NONE);
				pa.setBackFaceNormalFlip(true);
				appear.setPolygonAttributes(pa);
				text.setAppearance(appear);
//				if (null==DrawPanel.listText) DrawPanel.listText=new Vector<TransformGroup>();
				final TransformGroup tg=new TransformGroup();
				final double[][] d=tortue.getRotationMatrix();
				final Matrix3d m=new Matrix3d(d[0][0],d[0][1],d[0][2],d[1][0],d[1][1],d[1][2],d[2][0],d[2][1],d[2][2]);
				final Transform3D t=new Transform3D(m,new Vector3d(tortue.X/1000,tortue.Y/1000,tortue.Z/1000),1);
				tg.setTransform(t);
				tg.addChild(text);
				cadre.getViewer3D().add2DText(tg);
//				DrawPanel.listText.add(tg);
			}


		}
		montrecacheTortue(true);
		if (classicMode) repaint();
	}
	private void etiquette2D(final double x,final double y, final double angle, final String word){
		if (word.length()==0) return;

		g.setPaintMode();
		g.setColor(tortue.couleurcrayon);
		final Font f= UserConfig.fontes[police_etiquette]
		         				.deriveFont((float) tortue.police);
		g.setFont(f);
		g.translate(x, y);
		g.rotate(angle);
		final FontRenderContext frc = g.getFontRenderContext();
		final TextLayout layout = new TextLayout(word, f, frc);
		final Rectangle2D bounds = layout.getBounds();
	    final float height=(float)bounds.getHeight();
	    final float width=(float)bounds.getWidth();
	    float x1=0,y1=0;
	    switch(tortue.getLabelHorizontalAlignment()){
	    	case Turtle.LABEL_HORIZONTAL_ALIGNMENT_LEFT:
	    		x1=0;
	    	break;
	    	case Turtle.LABEL_HORIZONTAL_ALIGNMENT_CENTER:
	    		x1=-width/2;
	    	break;
	    	case Turtle.LABEL_HORIZONTAL_ALIGNMENT_RIGHT:
	    		x1=-width;
	    	break;
	    }
	    switch(tortue.getLabelVerticalAlignment()){
    	case Turtle.LABEL_VERTICAL_ALIGNMENT_BOTTOM:
    		y1=0;
    	break;
    		case Turtle.LABEL_VERTICAL_ALIGNMENT_CENTER:
    		y1=height/2;
    		break;
    		case Turtle.LABEL_VERTICAL_ALIGNMENT_TOP:
    			y1=height;
    		break;
	    }
	    layout.draw(g, x1, y1);
	    g.drawString(word, x1, y1);
		g.rotate(-angle);
		g.translate(-x, -y);
		if (DrawPanel.WINDOW_MODE==DrawPanel.WINDOW_WRAP){
		    final Rectangle2D.Double rec=new Rectangle2D.Double(0,0,width,height);
		    final AffineTransform at=new AffineTransform();
		    at.translate(x, y);
		    at.rotate(angle);
		    final Rectangle2D bound =at.createTransformedShape(rec).getBounds2D();
		    final double right= bound.getX()+bound.getWidth()-x;
		    final double left= x-bound.getX();
		    final double up=y-bound.getY();
		    final double down=bound.getY()+bound.getHeight()-y;

		    final UserConfig uc = WSManager.getUserConfig();
			final int w = uc.getImageWidth();
			final int h = uc.getImageHeight();

			if (x+right>w&& x<=w){
				pt=new Point2D.Double(-w+x,y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					etiquette2D(-w+x,y,angle,word);
				}
			}
			if (x-left<0&& x>=0){
				pt=new Point2D.Double(w+x,y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					etiquette2D(w+x,y,angle,word);
				}
			}
			if (y-up<0&& y>=0){
				pt=new Point2D.Double(x,h+y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					etiquette2D(x,h+y,angle,word);
				}
			}
			if (y+down>h&&y<=h){
				pt=new Point2D.Double(x,-h+y);
				if (! centers.contains(pt))	{
					centers.add(pt);
					etiquette2D(x,-h+y,angle,word);
				}
			}
		}
	}

	/**
	 * This method transform a plane 2D shape in the shape corresponding to the turtle plane
	 * @param s the first shape
	 * @return the new shape after transformation
	 */
	private Shape transformShape(final Shape s){
		final PathIterator it=s.getPathIterator(null);
		final double[] d=new double[6];
		double[][] coor=new double[3][1];
		final GeneralPath gp=new GeneralPath();
		final double[] end=new double[3];
		final double[] ctl1=new double[3];
		final double[] ctl2=new double[3];
		boolean b=false;
		while(!it.isDone()){
			it.next();
			int id=0;
			if (!it.isDone()) id=it.currentSegment(d);
			else break;
			coor[0][0]=d[0];
			coor[1][0]=-d[1];
			coor[2][0]=0;
			coor=w3d.multiply(tortue.getRotationMatrix(), coor);

			end[0]=coor[0][0]+tortue.X;
			end[1]=coor[1][0]+tortue.Y;
			end[2]=coor[2][0]+tortue.Z;
			w3d.toScreenCoord(end);

			if (id==PathIterator.SEG_MOVETO)
				gp.moveTo((float)end[0], (float)end[1]);
			else if (id==PathIterator.SEG_LINETO)
				{
				if (!b) {
					b=true;
					gp.moveTo((float)end[0], (float)end[1]);
				}
				else gp.lineTo((float)end[0], (float)end[1]);
				}
			else if (id==PathIterator.SEG_CLOSE){
				gp.closePath();
			}
			else {
				if (!b) {
					b=true;
					Point2D p=null;
					if (s instanceof Arc2D.Double)
					 p=((Arc2D.Double)s).getStartPoint();
					else if (s instanceof GeneralPath)
						 p=((GeneralPath)s).getCurrentPoint();
					coor[0][0]=p.getX();
					coor[1][0]=-p.getY();
					coor[2][0]=0;
					coor=w3d.multiply(tortue.getRotationMatrix(), coor);
					ctl1[0]=coor[0][0]+tortue.X;
					ctl1[1]=coor[1][0]+tortue.Y;
					ctl1[2]=coor[2][0]+tortue.Z;
					w3d.toScreenCoord(ctl1);
					gp.moveTo((float)ctl1[0], (float)ctl1[1]);
				}
				coor[0][0]=d[2];
				coor[1][0]=-d[3];
				coor[2][0]=0;
				coor=w3d.multiply(tortue.getRotationMatrix(), coor);
				ctl1[0]=coor[0][0]+tortue.X;
				ctl1[1]=coor[1][0]+tortue.Y;
				ctl1[2]=coor[2][0]+tortue.Z;
				w3d.toScreenCoord(ctl1);
				if(id==PathIterator.SEG_QUADTO){
					final QuadCurve2D qc=new QuadCurve2D.Double(gp.getCurrentPoint().getX(),gp.getCurrentPoint().getY()
							,end[0], end[1],ctl1[0], ctl1[1]);
					gp.append(qc, true);}
				else if (id==PathIterator.SEG_CUBICTO){
					coor[0][0]=d[4];
					coor[1][0]=-d[5];
					coor[2][0]=0;
					coor=w3d.multiply(tortue.getRotationMatrix(), coor);

					ctl2[0]=coor[0][0]+tortue.X;
					ctl2[1]=coor[1][0]+tortue.Y;
					ctl2[2]=coor[2][0]+tortue.Z;

					w3d.toScreenCoord(ctl2);
					final CubicCurve2D qc=new CubicCurve2D.Double(gp.getCurrentPoint().getX(),gp.getCurrentPoint().getY()
							,end[0], end[1],ctl1[0], ctl1[1],ctl2[0], ctl2[1]);
					gp.append(qc, true);
				}
				}
			}
			return gp;
		}
	public World3D getWorld3D(){
		return w3d;
	}
	/**
	 * primitive setscreencolor
	 * @param color The Color of the nackground screen
	 */
	protected void fcfg(final Color color) {
		couleurfond=color;
		updateColorSelection();
		if (enabled3D()){
			if (cadre.getViewer3D()!=null){
				cadre.getViewer3D().updateBackGround(couleurfond);
			}
		}
		nettoie();
	}
	/**
	 * Primitive setpencolor
	 * @param color The pen Color
	 */
	protected void fcc(final Color color) {
		if (tortue.isVisible()&&null==tortue.tort) montrecacheTortue(false);
		tortue.couleurcrayon = color;
		tortue.couleurmodedessin = color;
		if (tortue.isVisible()&&null==tortue.tort) montrecacheTortue(true);
	}

		/**
		 * Primitive "guiposition"
		 * @param id The id for the gui Object
		 * @param liste The Coordinates list
		 * @param name The translated name for the primitive "guiposition"
		 * @throws LogoError If coordinates list is invalid
		 */
	protected void guiposition(final String id, final String liste,final String name) throws LogoError{
		if (guiExist(id)){
			initCoords();
			extractCoords(liste,name);
			coords=toScreenCoord(coords,false);
			gm.get(id).setLocation((int)coords[0],(int)coords[1]);
		}
	}
	/**
	 * Draw the Gui object refered with "id"
	 * @param id The Gui Object Id
	 * @throws LogoError If this object doesn't exist
	 */
	protected void guiDraw(final String id) throws LogoError{
		if (guiExist(id)){
			final GuiComponent gc=gm.get(id);
			add(gc.getGuiObject());
			validate();
			repaint();
		//	updateGuiComponents();
		}
	}
	/**
	 * @uml.property  name="gui"
	 * @uml.associationEnd
	 */
	private javax.swing.JComponent gui;
	/**
	 * This method erases a Gui on the drawing area
	 * @param id The Gui Object id
	 * @throws LogoError
	 */

	protected void guiRemove(final String id) throws LogoError{
		if (guiExist(id)){
			gui=gm.get(id).getGuiObject();
			gm.remove(id);
			if (SwingUtilities.isEventDispatchThread()){
				remove(gui);
				validate();
			}
			else {
				try{
					SwingUtilities.invokeAndWait(new Runnable(){
						public void run(){
							remove(gui);
							validate();
						}
					});
				}
				catch(final Exception e){}
			}
			repaint();
		}
	}
	private StringBuffer extractList(final String list) throws LogoError{
		final StringBuffer sb=new StringBuffer();
		int compteur=0;
		int i=0;
		while(list.length()!=0){
			final char c=list.charAt(i);
			if (c=='[') compteur++;
			else if (c==']') {
				if (compteur==0) return sb;
				else compteur--;
			}
			sb.append(c);
			i++;
		}
		throw new LogoError("[ "+list+" "+Logo.messages.getString("pas_liste"));
	}

	protected void guiAction(final String id, String liste) throws LogoError{
		if (guiExist(id)){
			final GuiComponent gc=gm.get(id);
			// If gc is a JButton
			if (gc.isButton()){
				((GuiButton)gc).setAction(Utils.decoupe(liste));
				if (!gc.hasAction()){
					((javax.swing.JButton)gc.getGuiObject()).addActionListener(gc);
					gc.hasAction=true;
				}
			}
			// gc is a JcomboBox
			else if (gc.isMenu()){
				liste=liste.trim();
				int i=0;
				while(liste.length()!=0){
					if (liste.charAt(0)=='['){
						liste=liste.substring(1).trim();
						final StringBuffer sb=extractList(liste);
						liste=liste.substring(sb.length()+1).trim();
						((GuiMenu)gc).setAction(sb, i);
						i++;
					}
					else  throw new LogoError(liste.charAt(0)+" "+Logo.messages.getString("pas_liste"));
				}
				final GuiMenu gm=(GuiMenu)gc;
				if (!gm.hasAction){
					gm.hasAction=true;
					((javax.swing.JComboBox)gc.getGuiObject()).addActionListener(gm);
				}
			}
		}
	}
	private boolean guiExist(final String id) throws LogoError{
		if (gm.containsKey(id.toLowerCase())) return true;
		else throw new LogoError(Logo.messages.getString("no_gui")+" "+id);
	}
//	boolean access=false;
	private void clip(){
		if (classicMode){

			//access=true;
//			refresh();

			repaint();
	/*		if (SwingUtilities.isEventDispatchThread()){
				repaint();
			}
			else {
				try {

				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						repaint();
					}
				});
			}
			catch(Exception e2){}
			}*/
		}
		/*Rectangle rec1=cadre.jScrollPane1.getViewport().getViewRect();
		boolean inter=sh.intersects(rec1);
		if (inter){
			if (classicMode){
				repaint();
			}
		}*/
	}
	public void setQuality(final DrawQuality q){
		/*
		 * Marko Zivkovic
		 * I improved all the qualities for one level. I introduced a super high quality that is assigned to "high"
		 * and dropped the low quality, which is now the old normal/default quality
		 */

		if (q==DrawQuality.HIGH){
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
		}
		else if(q==DrawQuality.LOW){
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_DEFAULT);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_DEFAULT);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		}
		else { //normal
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
	}
	public  void change_image_tortue(final Application cadre, String chemin) {
		if (tortue.isVisible())
			montrecacheTortue(false);
		if (chemin.equals("tortue0.png")) {
			tortue.tort = null;
			tortue.largeur = 26;
			tortue.hauteur = 26;
		} else {
			//ON teste tout d'abord si le chemin est valide
			if (null == Utils.class.getResource(chemin))
				chemin = "tortue1.png";
			tortue.tort = Toolkit.getDefaultToolkit().getImage(
					Utils.class.getResource(chemin));
			MediaTracker tracker = new MediaTracker(cadre.getFrame());
			tracker.addImage(tortue.tort, 0);
			try {
				tracker.waitForID(0);
			} catch (final InterruptedException e1) {
			}
			final double largeur_ecran = Toolkit.getDefaultToolkit().getScreenSize()
					.getWidth();
			// On fait attention à la résolution de l'utilisateur
			final double facteur = largeur_ecran / 1024.0;

			if ((int) (facteur + 0.001) != 1) {
				tortue.largeur = tortue.tort.getWidth(cadre.getFrame());
				tortue.hauteur = tortue.tort.getHeight(cadre.getFrame());
				tortue.tort = tortue.tort.getScaledInstance(
						(int) (facteur * tortue.largeur),
						(int) (facteur * tortue.hauteur),
						Image.SCALE_SMOOTH);
				tracker = new MediaTracker(cadre.getFrame());
				tracker.addImage(tortue.tort, 0);
				try {
					tracker.waitForID(0);
				} catch (final InterruptedException e1) {
				}
			}
			tortue.largeur = tortue.tort.getWidth(cadre.getFrame());
			tortue.hauteur = tortue.tort.getHeight(cadre.getFrame());
		}
		tortue.gabarit = Math.max(tortue.hauteur,
				tortue.largeur);
		if (tortue.isVisible())
			montrecacheTortue(true);

	}
	// animation
	protected void setAnimation(final boolean predic){
		if (predic==classicMode){
			if (predic) {
				cadre.getHistoryPanel().active_animation();
			}
			else {
				cadre.getHistoryPanel().stop_animation();
				repaint();
			}
		}
	}

	protected void setGraphicsFont(final Font f){
		g.setFont(f);
	}
	protected Font getGraphicsFont(){
		return g.getFont();
	}
	protected void setStroke(final Stroke st){
		g.setStroke(st);
	}
	public Color getBackgroundColor(){
		return couleurfond;
	}
	protected void setBackgroundColor(final Color c){
		couleurfond=c;
	}
	protected void updateColorSelection(){
    	final float r=(255-couleurfond.getRed())/255;
    	final float v=(255-couleurfond.getGreen())/255;
		final float b=(255-couleurfond.getBlue())/255;
		colorSelection=new Color(r,v,b,0.2f);
	}
	public void setNumberOfTurtles(final int max){
		WSManager.getUserConfig().setMaxTurtles(max);
		final Turtle[] tampon = tortues.clone();
		tortues = new Turtle[max];
		final int borne_sup=Math.min(tampon.length,tortues.length);
		for(int i=0;i<borne_sup;i++){
			tortues[i]=tampon[i];
		}
		for(int i=tortues_visibles.size()-1;i>-1;i--){
			final int integer=Integer.parseInt(tortues_visibles.get(i));
			if (integer>=max){
				tortues_visibles.remove(i);
			}
		}
	}
	protected void initGraphics(){
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		police_etiquette=UserConfig.police_id(WSManager.getWorkspaceConfig().getFont());
		//		 init all turtles
		tortues = new Turtle[uc.getMaxTurtles()];
		tortues_visibles=new Stack<String>();
		tortue=new Turtle(cadre);
		tortues[0] = tortue;
		tortue.id = 0;
		tortues_visibles.push("0");
		for (int i = 1; i < tortues.length; i++) {
			// All other turtles are null
				tortues[i] = null;
		}
		g=(Graphics2D)dessin.getGraphics();
		couleurfond=uc.getScreencolor();
		setQuality(uc.getQuality());
	    g.setColor(uc.getScreencolor());
	    g.fillRect(0,0,w,h);
	    g.setColor(uc.getScreencolor());
	    if (!enabled3D()){
	    	drawGrid();
	    	drawXAxis();
	    	drawYAxis();
	    }
	    	MediaTracker tracker;
	    	if (0==uc.getActiveTurtle()) {
	    		g.setXORMode(couleurfond);
	    		tortue.drawTriangle();
	    		g.setColor(tortue.couleurcrayon);
	    		g.draw(tortue.triangle);
	    	}
	    	else {
	    		g.setXORMode(couleurfond);
	    		tracker=new MediaTracker(cadre.getFrame());
	    		tracker.addImage(tortue.tort,0);
	    		try{tracker.waitForID(0);}
	    		catch(final InterruptedException e){}
	    		if (tracker.checkID(0))  g.drawImage(tortue.tort, w/2 - tortue.largeur / 2,
	                    h/2 - tortue.hauteur/2, this);
	    		}
	    	updateColorSelection();
	}

	private void resizeAllGuiComponents(final double d){
		// Resize all GuiComponent
		final Set<String> set=gm.keySet();
		final Iterator<String> it=set.iterator();
		while (it.hasNext()){
			final String element=it.next();
			final GuiComponent gui=gm.get(element);
			gui.getGuiObject().setSize((int)(gui.getOriginalWidth()*d),
					(int)(gui.getOriginalHeight()*d) );
			final Font f=gui.getGuiObject().getFont();
			gui.getGuiObject().setFont(f.deriveFont((float)(WSManager.getWorkspaceConfig().getFont().getSize()*d)));
			final double x=gui.getLocation().x/zoom;
			final double y=gui.getLocation().y/zoom;
			gui.setLocation((int)(x*d),(int)(y*d));

		}

	}


	/**
	 * Make a zoom on the drawing area
	 * @param d The absolute factor
	 */
	public void zoom(double d, final boolean zoomIn){
		// Disable zoom buttons
		//cadre.setZoomEnabled(false); // TODO REMOVE ZOOM COMPLETELY?

		final javax.swing.JViewport jv=cadre.scrollArea.getViewport();
		Point p=jv.getViewPosition();
		final Rectangle r=jv.getVisibleRect();


	// If a selection rectangle is displaying on the drawing area
	// And If zoomout has been pressed
	// Zooming on the rectangular selection
		if (null!=selection&&cadre.commande_isEditable()&&zoomIn){
			final int originalWidth=jv.getWidth();
			final double width=selection.getWidth();
			d=zoom*originalWidth/width;
			p=selection.getLocation();
			r.width=selection.width;
			// adjust height in the same ratio as width
			r.height=r.height*(int)width/originalWidth;
			// erase selection
			selection=null;
		}
		// Resize all Gui Components on the drawing area
		resizeAllGuiComponents(d);

		final double oldZoom=zoom;
		zoom=d;

		/*
		 * 		-------------------------------------
		 * 		|									|
		 *      |  	-------------------------		|
		 * 		|	|						|		|
		 * 		|	|						|		|
		 * 		|	|			x--	dx-----	|  --> CenterView Point of the rectangle
		 * 		|	|			|			|		|
		 * 		|	|			dy			|		|
		 * 		|	-------------------------		|
		 * 		-------------------------------------
		 * */

		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		final double dx=Math.min(r.width,w*oldZoom)/2;
		final double dy=Math.min(r.height,h*oldZoom)/2;
		final Point centerView=new Point((int)(p.x+dx),(int)(p.y+dy));

		// Dynamically modify the drawing Area size
		setPreferredSize(new java.awt.Dimension(
				(int)(w*zoom)
				,(int)(h*zoom)));

		SwingUtilities.invokeLater(new PositionJViewport(jv,
				new Point((int)(centerView.x/oldZoom*zoom-dx),
						(int)(centerView.y/oldZoom*zoom-dy))));

	}
	private Color getTransparencyColor(final int color,final int trans){
		final Color c=new Color(color);
		return new Color(c.getRed(),c.getGreen(),c.getBlue(),trans);
	}
	/**
	 * Draw the horizontal axis
	 */
	private void drawXAxis(){
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		if (uc.isDrawXAxis()){
			g.setColor(getTransparencyColor(uc.getAxisColor(),128));
			g.drawLine(0,h/2,w,h/2);
			for (int i=w/2%uc.getXAxis();i<w;i=i+uc.getXAxis()){
				g.drawLine(i, h/2-2, i, h/2+2);
				g.setFont(new Font("Dialog",Font.PLAIN,10));
				final String tick=String.valueOf(i-w/2);
				final FontMetrics fm=g.getFontMetrics();
				final int back=fm.stringWidth(String.valueOf(tick))/2;
				// if the both axes are drawn, the zero has to translated
				// So we don't draw the zero
				if (i!=w/2||!uc.isDrawYAxis())   g.drawString(tick, i-back, h/2+20);
			}
		}
	}
	/**
	 * Draw the vertical axis
	 */
	private void drawYAxis(){
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		if (uc.isDrawYAxis()){
			g.setColor(getTransparencyColor(uc.getAxisColor(),128));
			g.drawLine(w/2,0,w/2,h);
			for (int i=h/2%uc.getYAxis();i<h;i=i+uc.getYAxis()){
				g.drawLine( w/2-2, i, w/2+2,i);
				g.setFont(new Font("Dialog",Font.PLAIN,10));
				final String tick=String.valueOf(h/2-i);
				// If both axes are drawn, zero is translated
				if (i==h/2&&uc.isDrawXAxis()) g.drawString("0", w/2+10, i-5);
				else  g.drawString(tick, w/2+10, i+5);
			}
		}
	}
	private void drawGrid(){
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		if (uc.isDrawGrid()){
			g.setStroke(new BasicStroke(1));
			g.setColor(getTransparencyColor(uc.getGridColor(),100));
						for (int i=w/2%uc.getXGrid();i<w;i=i+uc.getXGrid())
				g.drawLine(i, 0, i, h);

			for (int i=h/2%uc.getYGrid();i<h;i=i+uc.getYGrid())
				g.drawLine(0,i, w, i);
		}
	}
	// In animation mode, we have to wait for the drawing to be finished before modifying graphics.
	// Thread must be synchronized.
	protected synchronized void refresh(){
		repaint();
		try{
			wait();
		}
		catch(final InterruptedException e){}

	}

  protected synchronized void paintComponent(final Graphics graph){
	  super.paintComponent(graph);
	  final Graphics2D g2d=(Graphics2D)graph;
	  if (null==shape){
		  g2d.setClip(cadre.scrollArea.getViewport().getViewRect());
	  }
	  else {
		  g2d.setClip(shape);
		  shape=null;
	  }
	  g2d.scale(DrawPanel.zoom,DrawPanel.zoom);
	  g2d.drawImage(dessin,0,0,this);
	  g2d.scale(1/DrawPanel.zoom,1/DrawPanel.zoom);
	  if (!Affichage.execution_lancee&&null!=selection&&cadre.commande_isEditable()){
		  g2d.setColor(colorSelection);
		  g2d.fillRect(selection.x, selection.y, selection.width, selection.height);
	  }
	  notify();
  }
	 public void active_souris(){
		 lissouris=false;
	 }
	 public boolean get_lissouris(){
		 return lissouris;
	 }
	 public int get_bouton_souris(){
		 lissouris=false;
		 return bouton_souris;
	 }
	 public String get_possouris(){
		 lissouris=false;
		 return possouris;
	 }
	  public void mousePressed(final MouseEvent e){
		 if (!Affichage.execution_lancee) {
			 selection=new Rectangle();
			 origine=new Point(e.getPoint());
			 selection.setSize(0, 0);
		 }
	 }
	 public void mouseReleased(final MouseEvent e){}
	 public void mouseClicked(final MouseEvent ev){
		 final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();
		 if (!Affichage.execution_lancee){
			 selection=null;
			 origine=null;
			 repaint();
		 }
		 else{
			 lissouris=true;
			 bouton_souris=ev.getButton();
			 final Point point=ev.getPoint();
			 possouris="[ "+(point.x-w/2)+" "+(h/2-point.y)+" ] ";
		 }
	 }

	 public void mouseExited(final MouseEvent e){
	 }
	 public void mouseEntered(final MouseEvent e){
	 }
	 // Select an export area
	 public void mouseDragged(final MouseEvent e){
		 if (!Affichage.execution_lancee&&null!=selection){
			 // First, we test if we need to move the scrollbars
			 	final Point pos=e.getPoint();
				final javax.swing.JViewport jv=cadre.scrollArea.getViewport();
				final Point viewPosition=jv.getViewPosition();
				final Rectangle r=jv.getVisibleRect();
				r.setLocation(viewPosition);
				// Is the point visible on screen?
				final boolean b=r.contains(pos);

				final UserConfig uc = WSManager.getUserConfig();
				final int w = uc.getImageWidth();
				final int h = uc.getImageHeight();

				// Move the scroolPane if necessary
				if (!b){
					int x,y;
					if (pos.x<viewPosition.x) x=Math.max(0,pos.x);
					else if (pos.x>viewPosition.x+r.width) x=Math.min(pos.x-r.width,(int)(w*zoom-r.width));
					else x=viewPosition.x;
					if (pos.y<viewPosition.y) y=Math.max(0,pos.y);
					else if (pos.y>viewPosition.y+r.height) y=Math.min(pos.y-r.height,(int)(h*zoom-r.height));
					else y=viewPosition.y;
					jv.setViewPosition(new Point(x,y));
				}

			 // Then , drawing the selection area

			 selection.setFrameFromDiagonal(origine, e.getPoint());
			 repaint();
		 }
	 }

	public void mouseMoved(final MouseEvent ev) {
		final UserConfig uc = WSManager.getUserConfig();
		final int w = uc.getImageWidth();
		final int h = uc.getImageHeight();

		lissouris = true;
		bouton_souris = 0;
		final Point point = ev.getPoint();
		possouris = "[ " + (point.x - w / 2) + " " + (h / 2 - point.y) + " ] ";
	}

	protected void addToGuiMap(final GuiComponent gc) throws xlogo.kernel.LogoError {
		gm.put(gc);
	}

	// This method modifies all Shape for any turtle on screen
	protected void updateAllTurtleShape() {
		for (int i = 0; i < tortues.length; i++) {
			if (null != tortues[i]) {
				tortues[i].fixe_taille_crayon(2 * tortues[i].getPenWidth());
			}
		}
	}
   /**
    * Saves the a part of the drawing area as an image
    * @param name The image name
    * @param coords The upper left corner and the right bottom corner
    */
   protected void saveImage(String name, final int[] coords){
	   final BufferedImage buffer=getImagePart(coords);
	   final String lowerName=name.toLowerCase();
	   String format="png";
	   if (lowerName.endsWith(".jpg")||lowerName.endsWith(".jpeg")) {
		   format="jpg";
	   }
	   else if (!lowerName.endsWith(".png")) {
		   name=name+".png";
	   }
	   name=WSManager.getUserConfig().getDefaultFolder()+File.separator+name;
	   try{
		   final File f=new File(name);
		   ImageIO.write(buffer, format, f);
	   }
	   catch(final IOException e){}

   }
   /**
    * Return a part of the drawing area as an image
    * @return
    */
   private BufferedImage getImagePart(final int[] coords){
	   Image pic=DrawPanel.dessin;
	   if (zoom!=1){
		  pic=createImage(new FilteredImageSource(pic.getSource(),
				 new ReplicateScaleFilter((int)(dessin.getWidth()*zoom),(int)(dessin.getHeight()*zoom))));
	   }
		 pic=createImage(new FilteredImageSource(pic.getSource(),
				 new CropImageFilter(coords[0],coords[1],coords[2],coords[3])));
		 return toBufferedImage(pic);
   }


   public BufferedImage getSelectionImage(){
	   Image pic=DrawPanel.dessin;
	   if (zoom!=1){
		  pic=createImage(new FilteredImageSource(pic.getSource(),
				 new ReplicateScaleFilter((int)(dessin.getWidth()*zoom),(int)(dessin.getHeight()*zoom))));
	   }
	   if (null!=selection){
		 final int x=(int)(selection.getBounds().getX());
		 final int y=(int)(selection.getBounds().getY());
		 final int width=(int)(selection.getBounds().getWidth());
		 final int height=(int)(selection.getBounds().getHeight());
		 pic=createImage(new FilteredImageSource(pic.getSource(),
				 new CropImageFilter(x,y,width,height)));
	}
		 return toBufferedImage(pic);
   }
//	 This method returns a buffered image with the contents of an image
   private BufferedImage toBufferedImage(Image image) {
       if (image instanceof BufferedImage)
			return (BufferedImage)image;

       // This code ensures that all the pixels in the image are loaded
       image = new ImageIcon(image).getImage();


       // Create a buffered image with a format that's compatible with the screen
       BufferedImage bimage = null;
       final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
       try {
           // Determine the type of transparency of the new buffered image
           final int transparency = Transparency.OPAQUE;

           // Create the buffered image
           final GraphicsDevice gs = ge.getDefaultScreenDevice();
           final GraphicsConfiguration gc = gs.getDefaultConfiguration();
           bimage = gc.createCompatibleImage(
               image.getWidth(null), image.getHeight(null), transparency);
       } catch (final HeadlessException e) {
           // The system does not have a screen
       }

       if (bimage == null) {
           // Create a buffered image using the default color model
           final int type = BufferedImage.TYPE_INT_RGB;
           bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
       }

       // Copy image to buffered image
       final Graphics g = bimage.createGraphics();

       // Paint the image onto the buffered image
       g.drawImage(image, 0, 0, null);
       g.dispose();

       return bimage;
   }
   class PositionJViewport implements Runnable{
	   JViewport jv;
	   Point p;
	   PositionJViewport(final JViewport jv, final Point p){
		   this.jv=jv;
		   this.p=p;
	   }
	   public void run(){
			revalidate();
			//cadre.calculateMargin(); // TODO here is a zoom bug TODO maybe return this
		   //  I have to add those two lines because of a bug I don't understand
		   	// zoom 8 zoom 1 zoom 8
		   // Sometimes after the method revalidate(), the left upper corner position
		   // wasn't correct
			cadre.scrollArea.invalidate();
			cadre.scrollArea.validate();
		// End Bug

			jv.setViewPosition(p);
			repaint();

			//cadre.setZoomEnabled(true);
	   }
   }
   private void tryRecord2DMode(final double a, final double b){
		if (DrawPanel.record2D==DrawPanel.record2D_POLYGON){
			// FillPolygon mode
			if (stackTriangle.size()==3){
				stackTriangle.remove(0);
				stackTriangle.add(new Point2D.Double(a,b));
			}
			else{
				stackTriangle.add(new Point2D.Double(a,b));
			}
			if (stackTriangle.size()==3){
				final GeneralPath gp=new GeneralPath();
				Line2D.Double ld=new Line2D.Double(stackTriangle.get(0),stackTriangle.get(1));
				gp.append(ld,false);
				ld=new Line2D.Double(stackTriangle.get(1),stackTriangle.get(2));
				gp.append(ld,true);
				ld=new Line2D.Double(stackTriangle.get(2),stackTriangle.get(0));
				gp.append(ld,true);
				g.fill(gp);
			}
		}

   }
   protected void startRecord2DPolygon(){
	   DrawPanel.record2D=DrawPanel.record2D_POLYGON;
	   	stackTriangle=new Vector<Point2D.Double>();
	    stackTriangle.add(new Point2D.Double(tortue.corX,tortue.corY));
   }
   protected void stopRecord2DPolygon(){
		DrawPanel.record2D=DrawPanel.record2D_NONE;
   }
}