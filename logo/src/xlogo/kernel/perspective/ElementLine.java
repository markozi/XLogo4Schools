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

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineStripArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.TransformGroup;

import org.jogamp.java3d.utils.geometry.Sphere;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.AxisAngle4d;

import org.jogamp.java3d.utils.geometry.Cylinder;

import org.jogamp.vecmath.Vector3d;

import xlogo.kernel.LogoError;

/**
 *
 * @author Marko Zivkovic - I decoupled this from Application
 *
 */
public class ElementLine extends Element3D
{
	/**
	 * This float stores the line Width for each 3D line
	 *
	 * @uml.property name="lineWidth"
	 */
	private final float	lineWidth;

	public ElementLine(final Viewer3D v3d, final float lineWidth)
	{
		super(v3d);
		this.lineWidth = lineWidth; //app.getKernel().getActiveTurtle().getPenWidth();
	}

	public void addToScene() throws LogoError
	{
		final int size = vertex.size();
		if (size > 1)
		{
			if (lineWidth == 0.5)
				createSimpleLine(size);
			else
				createComplexLine(size);
		}
	}

	/**
	 * This method draws a line with width 1
	 *
	 * @param size
	 */
	private void createSimpleLine(final int size)
	{
		final int[] tab = { size };
		final LineStripArray line = new LineStripArray(size, GeometryArray.COORDINATES | GeometryArray.COLOR_3, tab);
		for (int i = 0; i < size; i++)
		{
			line.setCoordinate(i, vertex.get(i));
			// System.out.println("sommet "+(2*i-1)+" "+vertex.get(i).x+" "+vertex.get(i).y+" "+vertex.get(i).z+" ");
			line.setColor(i, new Color3f(color.get(i).getRGBColorComponents(null)));
		}
		final Shape3D s = new Shape3D(line);
		final Appearance appear = new Appearance();
		final Material mat = new Material(new Color3f(1.0f, 1.0f, 1.0f), new Color3f(0.0f, 0f, 0f), new Color3f(1f, 1.0f,
				1.0f), new Color3f(1f, 1f, 1f), 64);
		appear.setMaterial(mat);
		appear.setLineAttributes(new LineAttributes(2 * lineWidth, LineAttributes.PATTERN_SOLID, false));
		s.setAppearance(appear);
		// DrawPanel.listPoly.add(s);
		v3d.add3DObject(s);
		/*
		 * for (int i=0;i<line.getVertexCount();i++){
		 * double[] d=new double[3];
		 * line.getCoordinate(i, d);
		 * for(int j=0;j<d.length;j++){
		 * System.out.println(i+" "+d[j]);
		 * }
		 * }
		 */
	}

	/**
	 * This method draws a sequence of lines with a width different from 1
	 * Draws a cylinder around the line
	 *
	 * @param size
	 */

	private void createComplexLine(final int size)
	{
		for (int i = 0; i < size - 1; i++)
		{
			createLine(vertex.get(i), vertex.get(i + 1), new Color3f(color.get(i + 1).getRGBColorComponents(null)));
		}

	}

	/**
	 * This method creates an elementary line with a width different from 1
	 * It draws a cylinder around the line and two spheres on each extremities
	 *
	 * @param p1
	 *            the first point
	 * @param p2
	 *            the second point
	 * @param color
	 *            the sphere color
	 */
	private void createLine(final Point3d p1, final Point3d p2, final Color3f color)
	{
		// create a new CylinderTransformer between the two atoms
		final CylinderTransformer cT = new CylinderTransformer(p1, p2);

		// get the length
		final double length = cT.getLength();
		final float radius = lineWidth / 1000;
		// holds the translation
		final Transform3D transform1 = new Transform3D();
		// ...move the coordinates there
		transform1.setTranslation(cT.getTranslation());

		// get the axis and angle for rotation
		final AxisAngle4d rotation = cT.getAxisAngle();
		final Transform3D transform2 = new Transform3D();
		transform2.setRotation(rotation);

		// combine the translation and rotation into transform1
		transform1.mul(transform2);

		// create a new Cylinder
		final Appearance appear = new Appearance();
		final Material mat = new Material(new Color3f(1.0f, 1.0f, 1.0f), color, new Color3f(1f, 1.0f, 1.0f), new Color3f(1f,
				1f, 1f), 64);
		appear.setMaterial(mat);
		final PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		appear.setPolygonAttributes(pa);
		final Cylinder cyl = new Cylinder(radius, (float) length, appear);

		// and add it to the TransformGroup
		final TransformGroup tg = new TransformGroup();
		tg.setTransform(transform1);
		tg.addChild(cyl);
		v3d.add2DText(tg);

		// Add Sphere to the line extremities.
		final Sphere sphere1 = new Sphere(radius, appear);
		final TransformGroup tg2 = new TransformGroup();
		final Transform3D transform3 = new Transform3D();
		transform3.setTranslation(new Vector3d(p1));
		tg2.setTransform(transform3);
		tg2.addChild(sphere1);
		v3d.add2DText(tg2);

		final Sphere sphere2 = new Sphere(radius, appear);
		final TransformGroup tg3 = new TransformGroup();
		final Transform3D transform4 = new Transform3D();
		transform4.setTranslation(new Vector3d(p2));
		tg3.setTransform(transform4);
		tg3.addChild(sphere2);
		v3d.add2DText(tg3);

	}

	public boolean isLine()
	{
		return true;
	}

	public boolean isPoint()
	{
		return false;
	}

	public boolean isPolygon()
	{
		return false;
	}

	public boolean isText()
	{
		return false;
	}

	/**
	 * A class to calculate the length and transformations necessary to produce
	 * a cylinder to connect two points. Useful for Java3D and VRML where a
	 * cylinder object is created aligned along the y-axis.
	 *
	 * @author Alastair Hill
	 */
	class CylinderTransformer
	{

		/** point A */
		private final Point3d	pointA;
		/** point B */
		private final Point3d	pointB;
		/**
		 * the angle through which to rotate the cylinder
		 */
		private double			angle;
		/**
		 * the axis around which to rotate the cylinder
		 *
		 */
		private Vector3d		axis;
		/**
		 * The translation required to translate the cylinder to the midpoint of
		 * the two points
		 */
		private Vector3d		translation;
		/**
		 * The length of the cylinder required to join the two points
		 *
		 */
		private double			length;

		/**
		 * Creates a new instance of CylinderTransformer
		 *
		 * @param a
		 *            point a
		 * @param b
		 *            point b
		 */
		CylinderTransformer(final Point3d a, final Point3d b)
		{
			pointA = a;
			pointB = b;

			// carry out the calculations
			doCalculations();
		}

		/**
		 * Carries out the necessary calculations so that values may be returned
		 */
		private void doCalculations()
		{
			length = pointA.distance(pointB);

			final double[] arrayA = new double[3];
			pointA.get(arrayA);
			final double[] arrayB = new double[3];
			pointB.get(arrayB);
			final double[] arrayMid = new double[3];

			for (int i = 0; i < arrayA.length; i++)
			{
				arrayMid[i] = (arrayA[i] + arrayB[i]) / 2f;
			}

			// the translation needed is
			translation = new Vector3d(arrayMid);

			// the initial orientation of the bond is in the y axis
			final Vector3d init = new Vector3d(0.0f, 1.0f, 0.0f);

			// the vector needed is the same as that from a to b
			final Vector3d needed = new Vector3d(pointB.x - pointA.x, pointB.y - pointA.y, pointB.z - pointA.z);

			// so the angle to rotate the bond by is:
			angle = needed.angle(init);

			// and the axis to rotate by is the cross product of the initial and
			// needed vectors - ie the vector orthogonal to them both
			axis = new Vector3d();
			axis.cross(init, needed);
		}

		/**
		 * Returns the angle (in radians) through which to rotate the cylinder
		 *
		 * @return the angle.
		 */
		double getAngle()
		{
			return angle;
		}

		/**
		 * The axis around which the cylinder must be rotated
		 *
		 * @return the axis
		 */
		Vector3d getAxis()
		{
			return axis;
		}

		/**
		 * The length required for the cylinder to join the two points
		 *
		 * @return the length
		 */
		double getLength()
		{
			return length;
		}

		/**
		 * The translation required to move the cylinder to the midpoint of the
		 * two points
		 *
		 * @return the translation
		 */
		Vector3d getTranslation()
		{
			return translation;
		}

		/**
		 * Generates a (pretty) string representation of the the
		 * CylinderTransformer
		 *
		 * @return the string representation
		 */
		public String toString()
		{
			return "tr: " + translation + ", ax: " + axis + ", an: " + angle + ", le: " + length;
		}

		/**
		 * Generates the required axis and angle combined into an AxisAngle4f
		 * object
		 *
		 * @return the axis and angle
		 */
		public AxisAngle4d getAxisAngle()
		{
			return new AxisAngle4d(axis.x, axis.y, axis.z, angle);
		}
	}

}
