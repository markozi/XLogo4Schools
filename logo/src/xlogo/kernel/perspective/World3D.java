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

import xlogo.storage.WSManager;
import xlogo.storage.user.UserConfig;

public class World3D
{
	
	double				theta			= Math.toRadians(45);
	
	double				phi				= Math.toRadians(30);
	
	public double		r				= 1500;
	
	public double		xCamera			= r * Math.cos(theta) * Math.cos(phi);
	
	public double		yCamera			= r * Math.sin(theta) * Math.cos(phi);
	
	public double		zCamera			= r * Math.sin(phi);					;
	
	public double		screenDistance	= 1000;
	
	private double[][]	array3D;
	
	public World3D()
	{
		initArray3D();
		
	}
	
	/**
	 * This method converts the coordinates in coord to the screen coord
	 */
	public void toScreenCoord(double[] coord)
	{
		// Coord in the camera world
		toCameraWorld(coord);
		/*
		 * double x=array3D[0][0]*coord[0]+array3D[1][0]*coord[1];
		 * double
		 * y=array3D[0][1]*coord[0]+array3D[1][1]*coord[1]+array3D[2][1]*coord
		 * [2];
		 * double
		 * z=array3D[0][2]*coord[0]+array3D[1][2]*coord[1]+array3D[2][2]*coord
		 * [2]+r;
		 */
		cameraToScreen(coord);
	}
	
	/**
	 * This method converts coordinates conatined in coord from camera world to
	 * screen
	 */
	public void cameraToScreen(double[] coord)
	{
		UserConfig uc = WSManager.getUserConfig();
		
		double x = coord[0];
		double y = coord[1];
		double z = coord[2];
		coord[0] = screenDistance * x / z + uc.getImageWidth() / 2;
		coord[1] = uc.getImageHeight() / 2 - screenDistance * y / z;
	}
	
	/**
	 * This method initializes the 3D array
	 */
	private void initArray3D()
	{
		double cost = Math.cos(theta);
		double sint = Math.sin(theta);
		double cosp = Math.cos(phi);
		double sinp = Math.sin(phi);
		array3D = new double[4][4];
		array3D[0][0] = -sint;
		array3D[0][1] = -cost * sinp;
		array3D[0][2] = -cost * cosp;
		array3D[0][3] = 0;
		array3D[1][0] = cost;
		array3D[1][1] = -sint * sinp;
		array3D[1][2] = -sint * cosp;
		array3D[1][3] = 0;
		array3D[2][0] = 0;
		array3D[2][1] = cosp;
		array3D[2][2] = -sinp;
		array3D[2][3] = 0;
		array3D[3][0] = 0;
		array3D[3][1] = 0;
		array3D[3][2] = r;
		array3D[3][3] = 1;
	}
	
	/**
	 * This method converts coordinates from Real world to coodinates in camera
	 * world
	 * 
	 * @param coord
	 *            The real World coordinates
	 * @return Array that contains coordinates in camera world
	 */
	
	public void toCameraWorld(double[] coord)
	{
		double x = array3D[0][0] * coord[0] + array3D[1][0] * coord[1];
		double y = array3D[0][1] * coord[0] + array3D[1][1] * coord[1] + array3D[2][1] * coord[2];
		double z = array3D[0][2] * coord[0] + array3D[1][2] * coord[1] + array3D[2][2] * coord[2] + r;
		coord[0] = x;
		coord[1] = y;
		coord[2] = z;
	}
	
	/**
	 * This method multiply two matrices
	 * 
	 * @param a
	 *            The first matrix
	 * @param b
	 *            The second matrix
	 * @return The matrix product
	 */
	public double[][] multiply(double[][] a, double[][] b)
	{
		int n = a.length;
		int p = b[0].length;
		int s = a[0].length;
		double[][] m = new double[n][p];
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < p; j++)
			{
				m[i][j] = 0;
				for (int k = 0; k < s; k++)
				{
					m[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return m;
	}
	
	/**
	 * This method returns a matrix for a Z Axis rotation
	 * 
	 * @param angle
	 *            The rotation angle
	 * @return The rotation matrix
	 */
	public double[][] rotationZ(double angle)
	{
		double[][] m = new double[3][3];
		double cos = Math.cos(Math.toRadians(angle));
		double sin = Math.sin(Math.toRadians(angle));
		m[0][0] = cos;
		m[1][0] = sin;
		m[0][1] = -sin;
		m[1][1] = cos;
		m[2][2] = 1;
		m[2][1] = m[2][0] = m[0][2] = m[1][2] = 0;
		return m;
	}
	
	/**
	 * This method returns a matrix for a Y Axis rotation
	 * 
	 * @param angle
	 *            The rotation angle
	 * @return The rotation matrix
	 */
	public double[][] rotationY(double angle)
	{
		double[][] m = new double[3][3];
		double cos = Math.cos(Math.toRadians(angle));
		double sin = Math.sin(Math.toRadians(angle));
		m[0][0] = cos;
		m[2][0] = sin;
		m[0][2] = -sin;
		m[2][2] = cos;
		m[1][1] = 1;
		m[0][1] = m[1][0] = m[1][2] = m[2][1] = 0;
		return m;
	}
	
	/**
	 * This method returns a matrix for a X Axis rotation
	 * 
	 * @param angle
	 *            The rotation angle
	 * @return The rotation matrix
	 */
	public double[][] rotationX(double angle)
	{
		double[][] m = new double[3][3];
		double cos = Math.cos(Math.toRadians(angle));
		double sin = Math.sin(Math.toRadians(angle));
		m[0][0] = 1;
		m[1][1] = cos;
		m[2][1] = sin;
		m[1][2] = -sin;
		m[2][2] = cos;
		m[1][0] = m[2][0] = m[0][1] = m[0][2] = 0;
		return m;
	}
	
	/**
	 * This method with the 3 Euler's angle builds a rotation matrix
	 * 
	 * @param heading
	 *            The turtle heading
	 * @param roll
	 *            The turtle roll
	 * @param pitch
	 *            The turtle pitch
	 * @return The rotation Matrix
	 */
	
	public double[][] EulerToRotation(double roll, double pitch, double heading)
	{
		double[][] m = new double[3][3];
		double rpitch = Math.toRadians(pitch);
		double rheading = Math.toRadians(heading);
		double rroll = Math.toRadians(roll);
		double a = Math.cos(rpitch);
		double b = Math.sin(rpitch);
		double c = Math.cos(rroll);
		double d = Math.sin(rroll);
		double e = Math.cos(rheading);
		double f = Math.sin(rheading);
		double bd = b * d;
		double bc = b * c;
		m[0][0] = c * e - bd * f;
		m[0][1] = -c * f - bd * e;
		m[0][2] = -a * d;
		m[1][0] = a * f;
		m[1][1] = a * e;
		m[1][2] = -b;
		m[2][0] = d * e + bc * f;
		m[2][1] = -d * f + bc * e;
		m[2][2] = a * c;
		return m;
	}
	
	/**
	  * 
	  */
	public double[] rotationToEuler(double[][] m)
	{
		double[] v = new double[3];
		
		double angle_x, angle_y, angle_z;
		double a, tr_x, tr_y;
		// Angle x
		angle_x = -Math.asin(m[1][2]);
		
		a = Math.cos(angle_x);
		// Gimbal Lock?
		if (Math.abs(a) > 0.005)
		{
			// No gimbal Lock
			// Angle z
			tr_x = m[1][1] / a;
			tr_y = m[1][0] / a;
			angle_z = Math.atan2(tr_y, tr_x);
			// Angle y
			tr_x = m[2][2] / a;
			tr_y = -m[0][2] / a;
			angle_y = Math.atan2(tr_y, tr_x);
			v[0] = Math.toDegrees(angle_x);
			v[1] = -Math.toDegrees(angle_y);
			v[2] = -Math.toDegrees(angle_z);
		}
		else
		{ // Gimbal Lock
			angle_y = 0;
			// Angle z
			tr_x = m[0][0];
			tr_y = m[2][0];
			angle_z = Math.atan2(tr_y, tr_x);
			v[0] = 270;
			v[1] = 0;
			v[2] = Math.toDegrees(angle_z);
		}
		
		for (int i = 0; i < v.length; i++)
		{
			if (v[i] < 0)
				v[i] += 360;
		}
		return v;
	}
}
