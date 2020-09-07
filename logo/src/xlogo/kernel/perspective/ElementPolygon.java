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
package xlogo.kernel.perspective;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Point3d;
import javax.media.j3d.Shape3D;
import javax.vecmath.Vector3f;
import javax.vecmath.Color3f;

import xlogo.Logo;
import xlogo.kernel.LogoError;

/**
 * This class represent A polygon surface in 3D mode
 *
 * @author loic
 *
 * @author Marko Zivkovic - I decoupled this from Application
 */
public class ElementPolygon extends Element3D
{
	public ElementPolygon(final Viewer3D v3d)
	{
		super(v3d);
	}

	/**
	 * This method calculates all attributes for polygon and add it to the
	 * Polygon's list
	 */
	public void addToScene() throws LogoError
	{

		if (vertex.size() < 3)
			throw new LogoError(Logo.messages.getString("error.3d.3vertex"));

		// Create normal vector

		final Point3d origine = vertex.get(0);
		// System.out.println(" origine "+origine.x+" "+origine.y+" "+origine.z);

		Point3d point1;
		Vector3f vec1 = null;
		Vector3f vec2 = null;
		for (int i = 1; i < vertex.size(); i++)
		{
			point1 = vertex.get(i);
			if (!point1.equals(origine))
			{
				// System.out.println(" point1 "+point1.x+" "+point1.y+" "+point1.z);
				vec1 = new Vector3f((float) (point1.x - origine.x), (float) (point1.y - origine.y),
						(float) (point1.z - origine.z));
				break;
			}
		}
		if (null == vec1)
			throw new LogoError(Logo.messages.getString("error.3d.emptypolygon"));
		for (int i = 2; i < vertex.size(); i++)
		{
			point1 = vertex.get(i);
			// System.out.println(" point1 "+point1.x+" "+point1.y+" "+point1.z);
			vec2 = new Vector3f((float) (point1.x - origine.x), (float) (point1.y - origine.y),
					(float) (point1.z - origine.z));
			if (vec1.dot(vec2) == 0)
				vec2 = null;
			else
			{
				// System.out.println(" vec1 "+vec1.x+" "+vec1.y+" "+vec1.z);
				// System.out.println(" vec2 "+vec2.x+" "+vec2.y+" "+vec2.z);
				vec2.cross(vec1, vec2);
				vec2.normalize();
				vec1 = new Vector3f(vec2);
				vec1.negate();
				// System.out.println("Après"+" vec1 "+vec1.x+" "+vec1.y+" "+vec1.z);
				// System.out.println("Après"+" vec2 "+vec2.x+" "+vec2.y+" "+vec2.z);
				// if (vec1.x!=0&& vec1.y!=0&& vec1.z!=0)
				// System.out.println("coucou"+" vec1 "+vec1.x+" "+vec1.y+" "+vec1.z);
				break;
			}
			if (null == vec2)
				throw new LogoError(Logo.messages.getString("error.3d.emptypolygon"));
		}

		// Create Geometry

		final int[] tab = new int[1];
		tab[0] = vertex.size();
		final TriangleFanArray tfa = new TriangleFanArray(vertex.size(), GeometryArray.COORDINATES
				| GeometryArray.COLOR_3 | GeometryArray.NORMALS, tab);
		// TriangleFanArray tfa2=new
		// TriangleFanArray(vertex.size(),TriangleFanArray.COORDINATES|TriangleFanArray.COLOR_3|TriangleFanArray.NORMALS,tab);
		for (int i = 0; i < vertex.size(); i++)
		{

			tfa.setCoordinate(i, vertex.get(i));
			// tfa2.setCoordinate(i, vertex.get(vertex.size()-1-i));
			
			tfa.setColor(i, new Color3f(color.get(i)));
			// tfa2.setColor(i, new Color3f(color.get(color.size()-i-1)));

			tfa.setNormal(i, vec2);
			// tfa2.setNormal(i, vec1);

		}

		final Shape3D s = new Shape3D(tfa);
		final Appearance appear = new Appearance();
		final Material mat = new Material(new Color3f(1.0f, 1.0f, 1.0f), new Color3f(0.0f, 0f, 0f), new Color3f(1f, 1.0f,
				1.0f), new Color3f(1f, 1f, 1f), 64);
		appear.setMaterial(mat);
		final PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		appear.setPolygonAttributes(pa);

		s.setAppearance(appear);
		v3d.add3DObject(s);
		// DrawPanel.listPoly.add(s);
		// DrawPanel.listPoly.add(new Shape3D(tfa2));
		// System.out.println(DrawPanel.listPoly.size()+" "+vertex.get(i).x+" "+vertex.get(i).y+" "+vertex.get(i).z);
	}

	public boolean isPoint()
	{
		return false;
	}

	public boolean isPolygon()
	{
		return true;
	}

	public boolean isLine()
	{
		return false;
	}

	public boolean isText()
	{
		return false;
	}
}
