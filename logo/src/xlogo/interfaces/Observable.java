package xlogo.interfaces;

/** Implementation Template <br><br>
 <code>
private transient HashMap<String, List<PropertyChangeListener>> listeners = new HashMap<>(); <br><br>

public void addPropertyChangeListener(String property, PropertyChangeListener listener){ <br>
	List<PropertyChangeListener> list = null; <br>
	if (listeners.containsKey(property)){ <br>
		list = listeners.get(property); <br>
	} else { <br>
		list = new ArrayList<AbstractObservable.PropertyChangeListener>(); <br>
		listeners.put(property, list); <br>
	} <br>
	list.add(listener); <br>
} <br>
</code>
 */
public interface Observable<E extends Enum<E>> {
	
	/**
	 * @param property - if null, trigger for all properties
	 * @param listener
	 */
	public void addPropertyChangeListener(E property, PropertyChangeListener listener);
	public void removePropertyChangeListener(E property, PropertyChangeListener listener);
	
	public interface PropertyChangeListener {
		public void propertyChanged();
	}
}
