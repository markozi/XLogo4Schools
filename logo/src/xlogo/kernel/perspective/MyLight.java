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

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.SpotLight;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.java3d.BranchGroup;

public class MyLight extends BranchGroup
{
	protected final static int		LIGHT_OFF			= 0;
	protected final static int		LIGHT_AMBIENT		= 1;
	protected final static int		LIGHT_DIRECTIONAL	= 2;
	protected final static int		LIGHT_POINT			= 3;
	protected final static int		LIGHT_SPOT			= 4;
	protected final static float	DEFAULT_ANGLE		= 15;
	private int						type				= LIGHT_OFF;
	private Color3f					color;
	private Point3f					position;
	private Vector3f				direction;
	private float					angle				= DEFAULT_ANGLE;
	private Light					light;
	
	MyLight(int type)
	{
		super();
		this.type = type;
	}
	
	MyLight(int type, Color3f color, Point3f position)
	{
		super();
		this.type = type;
		this.color = color;
		this.position = position;
	}
	
	/**
	 * This method creates a light according to each parameter:<br>
	 * type, color, position, direction and angle
	 */
	void createLight()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		switch (type)
		{
			case LIGHT_OFF:
				light = null;
				break;
			case LIGHT_AMBIENT:
				light = new AmbientLight(color);
				light.setInfluencingBounds(new BoundingSphere(new Point3d(position), Double.MAX_VALUE));
				break;
			case LIGHT_DIRECTIONAL:
				light = new DirectionalLight(color, direction);
				light.setInfluencingBounds(new BoundingSphere(new Point3d(position), Double.MAX_VALUE));
				break;
			case LIGHT_POINT:
				light = new PointLight(color, position, new Point3f(1, 0, 0));
				light.setInfluencingBounds(new BoundingSphere(new Point3d(position), Double.MAX_VALUE));
				break;
			case LIGHT_SPOT:
				light = new SpotLight(color, position, new Point3f(1, 0, 0), direction, (float) Math.toRadians(angle),
						64);
				light.setInfluencingBounds(new BoundingSphere(new Point3d(position), Double.MAX_VALUE));
				break;
		}
		if (null != light)
			addChild(light);
	}
	
	/**
	 * This method returns the light type
	 * 
	 * @return an integer which represents the light type
	 */
	int getType()
	{
		return type;
	}
	
	/**
	 * @param t
	 */
	void setType(int t)
	{
		type = t;
	}
	
	/**
	 * @return
	 */
	Color3f getColor()
	{
		return color;
	}
	
	/**
	 * @param c
	 */
	void setColor(Color3f c)
	{
		color = c;
	}
	
	/**
	 * @return
	 */
	Point3f getPosition()
	{
		return position;
	}
	
	/**
	 * @param p
	 */
	void setPosition(Point3f p)
	{
		
		position = p;
	}
	
	/**
	 * @return
	 */
	Vector3f getDirection()
	{
		return direction;
	}
	
	/**
	 * @param v
	 */
	void setDirection(Vector3f v)
	{
		direction = v;
	}
	
	/**
	 * @return
	 */
	float getAngle()
	{
		return angle;
	}
	
	/**
	 * @param f
	 */
	void setAngle(float f)
	{
		angle = f;
	}
	
}
