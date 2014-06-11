/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loïc Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Zürich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loïc Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The base class for anything that must be stored persistently.
 * @author Marko Zivkovic
 */
public abstract class StorableObject extends Storable implements Serializable {

	private static final long serialVersionUID = -1738873382662156052L;
	
	/*
	 * PATH BUILDERS
	 */

	/**
	 * @param c
	 * @return X4S_ClassName.ser
	 */
	@SuppressWarnings("rawtypes")
	public static String getX4SObjectFileName(Class c)
	{
		return "X4S_" + c.getSimpleName();
	}
	
	/**
	 * This naming scheme shall be used to store Objects in XLogo4Schools
	 * @param dir - where the instance of c should be stored.
	 * @param c - Class of Objects to be stored persistently
	 * @return pathname for dir/X4S_ClassName.ser
	 */
	@SuppressWarnings("rawtypes")
	public static String getFilePath(File dir, Class c)
	{
		return dir.toString() + File.separator + getX4SObjectFileName(c);
	}
	
	/**
	 * @param dir
	 * @param c
	 * @return file for pathname as defined by {@link #getFilePath(File, Class)}
	 */
	@SuppressWarnings("rawtypes")
	public static File getFile(File dir, Class c)
	{
		String path = getFilePath(dir, c) + ".ser";
		return new File(path);
	}
	
	@Override
	public String getFileNameExtension()
	{
		return ".ser";
	}
	
	/**
	 * Constructor. The FileName will be equal to 
	 */
	public StorableObject()
	{
		setFileName(getX4SObjectFileName(getClass()));
	}
	
	/*
	 * Store & Load
	 */
	
	public void store() throws IOException
	{
		if(isDirty() && !isVirtual())
			storeCopyToFile(getFilePath());
	}
	
	public void storeCopyToFile(File file) throws IOException, IllegalArgumentException
	{
		if(file == null)
			throw new IllegalArgumentException("file must not be null.");
		
		if (!isVirtual())
		{
			FileOutputStream fileOut = new FileOutputStream(file);
	        ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(this);
	        out.close();
	        fileOut.close();
		}
		makeClean();
	}
	
	/**
	 * Load a Storable object from the specified file
	 * @param file
	 * @return the loaded Storable
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 */
	public static StorableObject loadObject(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fileIn = new FileInputStream(file);
	    ObjectInputStream in = new ObjectInputStream(fileIn);
	    Object object = in.readObject();
    	in.close();
    	fileIn.close();
	    
	    if (!(object instanceof StorableObject))
	    	throw new ClassCastException("The specified file (" + file.toString() + ") does not contain an instance of Storable: " + object.getClass().toString());

	    StorableObject storable = (StorableObject) object;
	    storable.makeClean();
	    return storable;
	}
		
}
