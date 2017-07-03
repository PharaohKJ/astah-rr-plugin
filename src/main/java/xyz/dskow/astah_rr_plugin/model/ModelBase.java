package xyz.dskow.astah_rr_plugin.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static lombok.AccessLevel.PROTECTED;
import lombok.Getter;

public abstract class ModelBase {

	@Getter(PROTECTED)
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	protected static boolean isSameObject(final Object oldValue, final Object newValue) {
		if (oldValue == null && newValue == null) {
			return true;
		} else if (oldValue == null && newValue != null) {
			return false;
		} else if (oldValue != null && newValue == null) {
			return false;
		} else if (oldValue != null && newValue != null) {
			return oldValue.equals(newValue);
		}
		return false;
	}

	protected static boolean isSameString(final String oldValue, final String newValue) {
		return isSameObject(oldValue, newValue);
	}
}
