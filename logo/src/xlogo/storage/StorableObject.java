/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
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
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.interfaces.Observable;
import xlogo.interfaces.Observable.PropertyChangeListener;
import xlogo.storage.workspace.Serializer;
import xlogo.utils.Utils;

/**
 * The container class for anything that must be stored persistently.
 * @author Marko Zivkovic
 */
public class StorableObject<T extends Observable<E>, E extends Enum<E>> extends Storable {

	private static final long	serialVersionUID	= -3394846264634066188L;
	private static Logger		logger				= LogManager.getLogger(StorableObject.class.getSimpleName());

	public static final String DEFAULT_PLAIN_NAME_PREFIX = "X4S_";
	public static final File DEFAULT_LOCATION = new File(System.getProperty("user.home"));
	
	private Class<T>			targetClass			= null;
	private T					object				= null;
	private Serializer<T>		serializer			= null;
	private Initializer<T>		creationInitilizer = null;
	private Initializer<T>		loadInitilizer 	= null;
	
	private transient final PropertyChangeListener objectDirtyListener = new PropertyChangeListener(){
		@Override
		public void propertyChanged() {
			makeDirty();
		}
	};
	
	/*
	 * PATH BUILDERS
	 */
	
	/**
	 * @param c
	 * @return X4S_ClassName.ser
	 */
	public static String getDefaulPlainName(Class<?> c) {
		return DEFAULT_PLAIN_NAME_PREFIX + c.getSimpleName();
	}
	
	public static String getFileName(String plainName){
		return "." + plainName;
	}
	
	public static String getFileName(Class<?> c){
		return getFileName(getDefaulPlainName(c));
	}
	
	public static File getFilePath(File dir, Class<?> c) {
		return new File(dir.toString() + File.separator + getFileName(c));
	}
	
	/*
	 * Constructors
	 */
	
	/**
	 * @param c - The class of the object to be stored / loaded
	 */
	public StorableObject(Class<T> c) {
		this(c, null, getDefaulPlainName(c), false);
	}
	
	/**
	 * @param c - The class of the object to be stored / loaded
	 * @param dir - The location where the object is stored
	 */
	public StorableObject(Class<T> c, File dir) {
		this(c, dir, getDefaulPlainName(c), false);
	}
	
	/**
	 * @param c - The class of the object to be stored / loaded
	 * @param dir - The location where the object is stored
	 * @param isDeferred - A deferred persistent object is not immediately persisted after creation, but after the next call to {@link #store()}
	 */
	public StorableObject(Class<T> c, File dir, boolean isDeferred) {
		this(c, dir, getDefaulPlainName(c), isDeferred);
	}
	
	/**
	 * @param c - The class of the object to be stored / loaded
	 * @param dir - The location where the object is stored
	 * @param plainName - The object's file name without extensions or dots
	 */
	public StorableObject(Class<T> c, File dir, String plainName) {
		this(c, dir, plainName, false);
	}
	
	/**
	 * @param c - The class of the object to be stored / loaded
	 * @param dir - The location where the object is stored
	 * @param plainName - The object's file name without extensions or dots
	 * @param isDeferred - A deferred persistent object is not immediately persisted after creation, but after the next call to {@link #store()}
	 */
	public StorableObject(Class<T> c, File dir, String plainName, boolean isDeferred) {
		this.targetClass = c;
		setLocation(dir);
		setPlainName(plainName);
		setStoringDeferred(isDeferred);
	}
	
	/*
	 * Create, Store & Load
	 */

	public StorableObject<T, E> createOrLoad() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		File file = getFilePath();
		
		if (file.exists()) {
			try {
				load();
			}
			catch (Exception e) {
				logger.warn("Could not load object from file " + e.toString());
				file.delete();
			}
		}
		
		if(!file.exists() || object == null){
			create();
			store();
		}
		
		
		return this;
	}
	
	public StorableObject<T, E> create() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		set(getTargetClass().getConstructor().newInstance());
		if (getCreationInitilizer() != null) {
			getCreationInitilizer().init(get());
		}
		store();
		return this;
	}
	
	public StorableObject<T, E> load() throws IOException, ClassNotFoundException, ClassCastException{
		setPersisted(false);
		if (getSerializer() != null){
			set(loadObject(getFilePath(), getSerializer()));
		} else {
			set(loadObject(getFilePath(), getTargetClass()));
		}
		if (getLoadInitilizer() != null) {
			getLoadInitilizer().init(get());
		}
		makeClean();
		setPersisted(true);
		return this;
	}
	
	@Override
	public void storeCopyToFile(File file) throws IOException, IllegalArgumentException {
		if (file == null)
			throw new IllegalArgumentException("file must not be null.");
		
		logger.trace("Storing Object to " + file.getAbsolutePath());
		
		file.getParentFile().mkdirs();
		
		if (!isVirtual()) {
			if (getSerializer() != null) {
				Utils.store(file, getSerializer().serialize2String(get()));
			}
			else {
				Utils.store(file, get());
			}
		}
		makeClean();
	}
	
	/**
	 * Load a Storable object from the specified file
	 * @param file
	 * @param c
	 * @return Load an object of the given class from a file in Java's own byte representation
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 */
	public static <T> T loadObject(File file, Class<T> c) throws IOException, ClassNotFoundException, ClassCastException {
		logger.trace("Loading Object from " + file.getAbsolutePath());
		return Utils.readObject(file, c);
	}
	
	/**
	 * @param file
	 * @param serializer
	 * @return The object persistent in the file as interpreted by the serializer
	 * @throws IOException
	 */
	public static <T> T loadObject(File file, Serializer<T> serializer) throws IOException {
		logger.trace("Loading Object from " + file.getAbsolutePath() + " with " + serializer.toString());
		String serialized = Utils.readFile(file);
		T object = serializer.deserialize(serialized);
		return object;
	}

	
	/*
	 * Getters & Setters
	 */

	/**
	 * If this exists on the file system, that file will be renamed. <p>
	 * If plainName already existed, it is deleted first.
	 * @param newFileName - if null, {@link #getDefaulPlainName(Class)} is taken
	 * @throws IllegalArgumentException - If the provided name is not legal.
	 */
	@Override
	public void setPlainName(String plainName){
		if (plainName == null) {
			plainName = getDefaulPlainName(targetClass);
		}
		super.setPlainName(plainName);
	}
	
	@Override
	public String getFileNamePrefix(){
		return ".";
	}
	
	@Override
	public String getFileNameExtension() {
		return "";
	}
	
	public Class<T> getTargetClass(){
		return targetClass;
	}
	
	public Serializer<T> getSerializer() {
		return this.serializer;
	}
	
	public void setSerializer(Serializer<T> serializer){
		this.serializer = serializer;
	}
	
	/**
	 * Get the persistent object
	 * @return
	 */
	public T get() {
		return this.object;
	}
	
	/**
	 * Set the persistent object 
	 * @param object
	 */
	public void set(T object) {
		if (this.object == object){
			return;
		}
		
		if (this.object != null){
			this.object.removePropertyChangeListener(null, objectDirtyListener);
		} else {
			makeDirty();
		}
		
		this.object = object;
		
		if (this.object != null){
			object.addPropertyChangeListener(null, objectDirtyListener);
		}
	}
		
	public Initializer<T> getCreationInitilizer() {
		return creationInitilizer;
	}

	public void setCreationInitilizer(Initializer<T> firstTimeInitilizer) {
		this.creationInitilizer = firstTimeInitilizer;
	}

	public Initializer<T> getLoadInitilizer() {
		return loadInitilizer;
	}

	public void setLoadInitilizer(Initializer<T> loadTimeInitilizer) {
		this.loadInitilizer = loadTimeInitilizer;
	}
	
	/*
	 * Chainable setters
	 */
	
	/**
	 * Convenience method for {@link #setSerializer(Initializer)}
	 * that allows chaining with other methods such as {@link #createOrLoad()}
	 * @param serializer
	 * @return
	 */
	public StorableObject<T, E> withSerializer(Serializer <T> serializer){
		setSerializer(serializer);
		return this;
	}
	
	/**
	 * Convenience method for {@link #setCreationInitilizer(Initializer)}
	 * that allows chaining with other methods such as {@link #createOrLoad()}
	 * @param firstTimeInitializer
	 * @return
	 */
	public StorableObject<T, E> withCreationInitializer(Initializer<T> firstTimeInitializer){
		setCreationInitilizer(firstTimeInitializer);
		return this;
	}
	
	/**
	 * Convenience method for {@link #setLoadInitilizer(Initializer)}
	 * that allows chaining with other methods such as {@link #createOrLoad()}
	 * @param loadTimeInitializer
	 * @return
	 */
	public StorableObject<T, E> withLoadInitializer(Initializer<T> loadTimeInitializer){
		setLoadInitilizer(loadTimeInitializer);
		return this;
	}
	
	
	/*
	 * Interfaces
	 */
	
	public interface Initializer<T> {
		public void init(T target);
	}
}
