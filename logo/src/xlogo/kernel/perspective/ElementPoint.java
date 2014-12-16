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

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import xlogo.kernel.LogoError;

import javax.media.j3d.Shape3D;

import com.sun.j3d.utils.geometry.Sphere;

/**
 * @author Marko - I decoupled this from Application
 */
public class ElementPoint extends Element3D
{
	float	pointWidth;
	
	public ElementPoint(Viewer3D v3d, float pointWidth)
	{
		super(v3d);
		this.pointWidth = pointWidth; //app.getKernel().getActiveTurtle().getPenWidth();
	}
	
	public void addToScene() throws LogoError
	{
		if (vertex.size() == 0)
			return;
		if (pointWidth == 0.5)
		{
			int[] tab = new int[1];
			tab[0] = vertex.size();
			PointArray point = new PointArray(vertex.size(), PointArray.COORDINATES | PointArray.COLOR_3);
			for (int i = 0; i < vertex.size(); i++)
			{
				point.setCoordinate(i, vertex.get(i));
				point.setColor(i, new Color3f(color.get(i)));
			}
			v3d.add3DObject(new Shape3D(point));
		}
		else
		{
			for (int i = 0; i < vertex.size(); i++)
			{
				createBigPoint(vertex.get(i), new Color3f(color.get(i)));
			}
			
		}
	}
	
	private void createBigPoint(Point3d p, Color3f color)
	{
		// Add a Sphere to main 3D scene.
		Appearance appear = new Appearance();
		Material mat = new Material(new Color3f(1.0f, 1.0f, 1.0f), color,// new
																			// Color3f(0.0f,0f,0f),
				new Color3f(1f, 1.0f, 1.0f), new Color3f(1f, 1f, 1f), 64);
		appear.setMaterial(mat);
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		appear.setPolygonAttributes(pa);
		
		Sphere sphere = new Sphere(pointWidth / 1000, appear);
		
		TransformGroup tg = new TransformGroup();
		Transform3D transform = new Transform3D();
		transform.setTranslation(new Vector3d(p));
		tg.setTransform(transform);
		tg.addChild(sphere);
		v3d.add2DText(tg);
	}
	
	public boolean isLine()
	{
		return false;
	}
	
	public boolean isPoint()
	{
		return true;
	}
	
	public boolean isPolygon()
	{
		return false;
	}
	
	public boolean isText()
	{
		return false;
	}
	
}
