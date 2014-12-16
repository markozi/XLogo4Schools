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

import javax.media.j3d.BranchGroup;
import javax.media.j3d.LinearFog;
import javax.media.j3d.ExponentialFog;
import javax.media.j3d.Fog;
import javax.media.j3d.BoundingSphere;

import javax.vecmath.Color3f;

public class MyFog extends BranchGroup
{
	protected final static int	FOG_OFF			= 0;
	protected final static int	FOG_LINEAR		= 1;
	protected final static int	FOG_EXPONENTIAL	= 2;
	private int					type			= FOG_OFF;
	private float				density			= 1;
	private float				backDistance	= 3.5f;
	private float				frontDistance	= 0.5f;
	private Fog					fog;
	Color3f						color;
	
	MyFog(int type, Color3f color)
	{
		super();
		setCapability(BranchGroup.ALLOW_DETACH);
		setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		this.type = type;
		this.color = color;
	}
	
	/**
	 * This method creates a light according to each parameter:<br>
	 * type, color, position, direction and angle
	 */
	void createFog()
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);
		this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		switch (type)
		{
			case FOG_OFF:
				fog = null;
				break;
			case FOG_LINEAR:
				fog = new LinearFog(color);
				((LinearFog) fog).setBackDistance(backDistance);
				((LinearFog) fog).setFrontDistance(frontDistance);
				fog.setInfluencingBounds(new BoundingSphere());
				break;
			case FOG_EXPONENTIAL:
				fog = new ExponentialFog(color, density);
				fog.setInfluencingBounds(new BoundingSphere());
				break;
		}
		if (null != fog)
			addChild(fog);
	}
	
	/**
	 * This method returns the light type
	 * 
	 * @return an integer which represents the light type
	 * @uml.property name="type"
	 */
	int getType()
	{
		return type;
	}
	
	void setType(int t)
	{
		type = t;
	}
	
	float getDensity()
	{
		return density;
	}
	
	void setDensity(float f)
	{
		density = f;
	}
	
	float getBack()
	{
		return backDistance;
	}
	
	void setBack(float f)
	{
		backDistance = f;
	}
	
	float getFront()
	{
		return frontDistance;
	}
	
	void setFront(float f)
	{
		frontDistance = f;
	}
	
}
