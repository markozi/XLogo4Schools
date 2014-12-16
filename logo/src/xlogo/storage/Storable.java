/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the
 * GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public abstract class Storable implements Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3506253939129765438L;
	
	/**
	 * The file's name with extension
	 */
	private String				fileName;
	
	/**
	 * The Directory where this is stored
	 */
	private File				location;
	
	/**
	 * Dirty : an object is dirty if it was changed since it was loaded or stored the last time.
	 */
	private transient boolean	dirty				= true;
	
	/**
	 * Will not be stored if virtual.
	 */
	private transient boolean	isVirtual			= false;
	
	/*
	 * PATH BUILDERS
	 */
	
	public static File getFile(File dir, String fileName) {
		return new File(dir.toString() + File.separator + fileName);
	}
	
	public static File getDirectory(File prefix, String dirName) {
		return new File(prefix.toString() + File.separator + dirName);
	}
	
	/*
	 * Abstract
	 */
	
	/**
	 * Store this object to the file specified by {@link #getFilePath()} if it is dirty
	 * @throws IOException
	 */
	public abstract void store() throws IOException;
	
	public abstract void storeCopyToFile(File file) throws IOException, IllegalArgumentException;
	
	/**
	 * Store this object to the specified file, regardless of whether this is virtual or not.
	 * @param file
	 * @throws IOException
	 * @throws IllegalArgumentException - null is not accepted
	 */
	
	/*
	 * file name & location
	 */
	
	public abstract String getFileNameExtension();
	
	public String getFileName() {
		return getPlainName() + getFileNameExtension();
	}
	
	/**
	 * @return FileName without file extension
	 */
	public String getPlainName() {
		return fileName;
	}
	
	/**
	 * If this exists on the file system, that file will be renamed. <p>
	 * If newFileName already existed, it is deleted first.
	 * @param newFileName
	 * @throws IllegalArgumentException - If the provided name is not legal.
	 */
	public void setFileName(String newFileName) throws IllegalArgumentException //TODO make sure callers conform with contract
	{
		if (newFileName == null || newFileName.length() == 0)
			throw new IllegalArgumentException("File name must not be null or empty.");
		
		if (!checkLegalName(newFileName))
			throw new IllegalArgumentException("The chose file name contains illegal characters.");
		
		String ext = getFileNameExtension();
		String oldName = getPlainName();
		String newName = newFileName.endsWith(ext) && newFileName.length() > ext.length() ? newFileName.substring(0,
				newFileName.length() - ext.length()) : newFileName;
		
		if (newName.equals(oldName) && oldName != null)
			return;
		
		if (isVirtual || oldName == null) {
			this.fileName = newFileName;
			return;
		}
		
		File oldPath = getFilePath();
		this.fileName = newName;
		
		if (!oldPath.exists())
			return;
		
		File newPath = getFilePath();
		if (newPath.exists())
			newPath.delete();
		
		oldPath.renameTo(newPath);
	}
	
	/**
	 * @return the directory where this should be stored to.
	 */
	public File getLocation() {
		return location;
	}
	
	/**
	 * To set null or a file that is not a directory or a directory with no write permissions is an error, as long as this is not virtual.<br>
	 * Setting location has no effect if this is virtual.<br>
	 * @param location - the directory where this should be stored to.
	 * @throws IOException 
	 * @throws IOException If the specified location is not a directory or no write permissions exist, or the chosen name is not legal.
	 */
	public void setLocation(File location) throws IllegalArgumentException {
		if (isVirtual) { return; }
		
		if (location == null) { throw new IllegalArgumentException("Location must not be null."); }
		
		this.location = location;
		makeDirty();
	}
	
	/**
	 * If the specified location does not exist yet, it is created using mkdirs.
	 */
	public void mkDirs() {
		mkDirs(location);
	}
	
	public void mkDirs(File location) {
		if (!location.isDirectory()) {
			location.mkdirs();
		}
		if (!location.isDirectory() || !location.canWrite()) { throw new IllegalArgumentException(
				"Cannot store this to specified location : " + location.toString()); }
	}
	
	public void mkParentDirs(File file) {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}

		if (!parent.isDirectory() || !parent.canWrite()) { throw new IllegalArgumentException(
				"Cannot store this to specified location : " + location.toString()); }
	}
	
	/**
	 * @return the file where this should be stored to. Returns null if {@link getLocation()} returns null.
	 */
	public File getFilePath() {
		if (getLocation() == null)
			return null;
		return getFile(getLocation(), getFileName());
	}
	
	/**
	 * @return whether the file specified by {@link #getFilePath()} exists.
	 */
	public boolean existsPhysically() {
		if (getFilePath() == null)
			return false;
		
		return getFilePath().exists();
	}
	
	/*
	 * isDirty
	 */
	
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * Should be called from every setter that sets a property that should be stored later
	 * @see StorableObject#makeClean()
	 */
	protected void makeDirty() {
		dirty = true;
	}
	
	/**
	 * Should be called whenever this was synchronized with its version on the file system (load or store)
	 * @see StorableObject#makeDirty()
	 */
	protected void makeClean() {
		dirty = false;
	}
	
	/*
	 * isVirtual
	 */
	
	/**
	 * @see #isVirtual()
	 */
	protected void makeVirtual() {
		isVirtual = true;
	}
	
	/**
	 * A virtual object will not be stored on the file system, even though {@link store()} was called.
	 * This allows to use the application without having an actual user account and without automatic saving.
	 * @return
	 */
	public boolean isVirtual() {
		return isVirtual;
	}
	
	// The best I found : http://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid
	// some windows specific chars are not contained...
	public static final String	ILLEGAL_NAME_CHARACTERS	= "/\n\r\t\0\f`?*\\<>|\":";
	
	public static boolean checkLegalName(String name) {
		if (name == null || name.length() == 0)
			return false;
		
		//StringTokenizer check = new StringTokenizer(name, ILLEGAL_NAME_CHARACTERS, true);
		//return (check.countTokens() == 1);
		
		for (char c : name.toCharArray()) {
			if (ILLEGAL_NAME_CHARACTERS.indexOf(c) > -1)
				return false;
		}
		
		return true;
	}
	
}
