package xlogo.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PropertyChangePublisher<E extends Enum<E>> implements Observable<E>, Serializable {
	
	private static final long	serialVersionUID	= -1042512850782413412L;
	
	private boolean enableEvents = true;
	
	private final HashMap<E, List<PropertyChangeListener>>	listeners	= new HashMap<E, List<PropertyChangeListener>>();
	private final List<PropertyChangeListener> allPropertiesListeners = new ArrayList<PropertyChangeListener>();
	
	@Override
	public void addPropertyChangeListener(E property, PropertyChangeListener listener) {
		if (listener == null){
			throw new IllegalArgumentException("Cannot add an event listeners that is null.");
		}
		
		if (property == null){
			allPropertiesListeners.add(listener);
			return;
		}
		List<PropertyChangeListener> list = listeners.get(property);
		if (list == null) {
			list = new ArrayList<PropertyChangeListener>();
			listeners.put(property, list);
		}
		list.add(listener);
	}
	
	@Override
	public void removePropertyChangeListener(E property, PropertyChangeListener listener) {
		if (listener == null){
			throw new IllegalArgumentException("Cannot remove an event listeners that is null.");
		}
		
		if (property == null){
			allPropertiesListeners.remove(listener);
			return;
		}
		
		List<PropertyChangeListener> list = listeners.get(property);
		if(list != null){
			list.remove(listener);
		}
	}
	
	public void publishEvent(E property){
		if (!enableEvents){
			return;
		}
		setEnableEvents(false);
		if (listeners.containsKey(property)){
			for(PropertyChangeListener listener : listeners.get(property)){
				listener.propertyChanged();
			}
		}
		for(PropertyChangeListener listener : allPropertiesListeners){
			listener.propertyChanged();
		}
		
		setEnableEvents(true);
	}

	public void setEnableEvents(boolean enableEvents) {
		this.enableEvents = enableEvents;
	}
	
}
