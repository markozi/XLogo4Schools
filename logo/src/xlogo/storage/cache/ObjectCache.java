package xlogo.storage.cache;

import java.io.File;
import java.util.HashMap;

import xlogo.interfaces.Observable;
import xlogo.storage.StorableObject;

/**
 * Cache {@link StorableObject} based on {@link StorableObject#getFilePath()}
 * @author Marko
 *
 */
public class ObjectCache {
	
	/*
	 * Singleton
	 */
	
	private static ObjectCache instance;
	
	public static final ObjectCache getInstance(){
		if(instance == null){
			instance = new ObjectCache();
		}
		return instance;
	}
	
	/*
	 * Cache fields
	 */
	
	/**
	 * Class of T -> Map({@link StorableObject#getFilePath()} -> {@link StorableObject} of T)
	 */
	private HashMap<Class<?>,HashMap<File,StorableObject<?,?>>> cache;
	
	private ObjectCache(){
		cache = new HashMap<Class<?>, HashMap<File,StorableObject<?,?>>>();
	}

	/** 
	 * Cache {@link StorableObject} based on {@link StorableObject#getFilePath()}
	 * @param storable
	 */
	public <T extends Observable<?>> void cache(StorableObject<T,?> storable){
		if (storable == null || storable.get() == null){
			// TODO log warning
			return;
		}
		Class<?> c = storable.get().getClass();
		HashMap<File, StorableObject<?,?>> classCache = cache.get(c);
		if (classCache == null){
			classCache = new HashMap<File, StorableObject<?, ?>>();
			cache.put(c, classCache);
		}
		classCache.put(storable.getFilePath(), storable);
	}
	
	/**
	 * 
	 * @param file
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked") // implementation of cache guarantees that the cast succeeds
	public <T extends Observable<E>,E extends Enum<E>> StorableObject<T, E> get(File file, Class<T> c){
		HashMap<File, StorableObject<?, ?>> classCache = cache.get(c);
		if (classCache == null){
			return null;
		}
		return (StorableObject<T, E>) classCache.get(file);
	}
	
	public <T extends Observable<?>> void remove(StorableObject<T,?> storable){
		if (storable == null || storable.get() == null){
			return;
		}
		Class<?> c = storable.get().getClass();
		remove(storable.getFilePath(), c);
	}
	
	public void remove(File file, Class<?> c){
		HashMap<File, StorableObject<?,?>> classCache = cache.get(c);
		if (classCache == null){
			return;
		}
		classCache.remove(file);
	}
	
}
