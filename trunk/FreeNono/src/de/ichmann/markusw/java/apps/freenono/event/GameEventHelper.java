/*****************************************************************************
 * FreeNono - A free implementation of the nonogram game
 * Copyright (c) 2010 Markus Wichmann
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *****************************************************************************/
package de.ichmann.markusw.java.apps.freenono.event;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

public class GameEventHelper {

	private static Logger logger = Logger.getLogger(GameEventHelper.class);

	private EventListenerList listeners = new EventListenerList();
	//private GameListener gameListener = null;

	public GameEventHelper() {
	}

	/**
	 * Adds an {@code GameListener} to the helper class.
	 * 
	 * @param l
	 *            the {@code GameListener} to be added
	 */
	public synchronized void addGameListener(GameListener l) {
		listeners.add(GameListener.class, l);
	}

	/**
	 * Removes an {@code GameListener} from the helper class.
	 * 
	 * @param l
	 *            the listener to be removed
	 */
	public synchronized void removeGameListener(GameListener l) {
		listeners.remove(GameListener.class, l);
	}

	// public synchronized void addGameListener(GameListener l) {
	// gameListener = GameEventMulticaster.add(gameListener, l);
	// }
	//
	// public synchronized void removeGameListener(GameListener l) {
	// gameListener = GameEventMulticaster.remove(gameListener, l);
	// }
	//
	// public void fireFieldOccupiedEvent(GameEvent e) {
	// if (gameListener != null) {
	// gameListener.FieldOccupied(new GameEvent());
	// }
	// }

	public synchronized void fireFieldOccupiedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.FieldOccupied(e);
	}

	public synchronized void fireFieldMarkedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.FieldMarked(e);
	}

	public synchronized void fireActiveFieldChangedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.ActiveFieldChanged(e);
	}

	public synchronized void fireStateChangedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.StateChanged(e);
		logger.debug("Game state changed from " + e.getOldState() + " to "
				+ e.getNewState());
	}

	public synchronized void fireTimerEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.Timer(e);
	}
	
	public synchronized void fireOptionsChangedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.OptionsChanged(e);
	}
	
	public synchronized void fireWrongFieldOccupiedEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.WrongFieldOccupied(e);
	}
	
	public synchronized void fireProgramControlEvent(GameEvent e) {
		for (GameListener l : listeners.getListeners(GameListener.class))
			l.ProgramControl(e);
	}
	
}
