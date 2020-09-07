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

package xlogo.kernel.perspective;

import java.awt.Color;
import java.util.Vector;

import org.jogamp.vecmath.Point3d;

import xlogo.kernel.LogoError;

/**
 * @author Marko - I decoupled this and its subclasses from Application
 *
 */
abstract public class Element3D
{
	protected Viewer3D v3d;
	
	/**
	 * Color for each vertex
	 * 
	 * @uml.property name="color"
	 * @uml.associationEnd multiplicity="(0 -1)" elementType="java.awt.Color"
	 */
	protected Vector<Color>		color;
	
	/**
	 * Vertex coordinates in virtual world
	 * 
	 * @uml.property name="vertex"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 *                     elementType="org.jogamp.vecmath.Point3d"
	 */
	protected Vector<Point3d>	vertex;
	
	public Element3D(Viewer3D v3d)
	{
		this.v3d = v3d;
		vertex = new Vector<Point3d>();
		color = new Vector<Color>();
		
	}
	
	public void addVertex(Point3d p, Color c)
	{
		vertex.add(p);
		color.add(c);
	}
	
	/**
	 * This method calculates all attributes for polygon and add it in the 3D
	 * Viewer
	 */
	abstract public void addToScene() throws LogoError;
	
	/**
	 * This method indicates if the Element3D is a Point.
	 * 
	 * @return true if this Element3D is a Point, false otherwise
	 */
	abstract public boolean isPoint();
	
	/**
	 * This method indicates if the Element3D is a Polygon.
	 * 
	 * @return true if this Element3D is a Polygon, false otherwise
	 */
	abstract public boolean isPolygon();
	
	/**
	 * This method indicates if the Element3D is a line.
	 * 
	 * @return true if this Element3D is a line, false otherwise
	 */
	abstract public boolean isLine();
	
	/**
	 * This method indicates if the Element3D is a Text2D.
	 * 
	 * @return true if this Element3D is a Text2D, false otherwise
	 */
	abstract public boolean isText();
	
	public int getVertexCount()
	{
		return vertex.size();
	}
}
