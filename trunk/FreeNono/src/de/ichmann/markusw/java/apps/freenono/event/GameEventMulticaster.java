package de.ichmann.markusw.java.apps.freenono.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

/**
 * EventMulticast to dispatch events to event listeners all over the program.
 * 
 * (copied from http://www.javaworld.com/javaworld/javatips/jw-javatip35.html)
 * 
 */
public class GameEventMulticaster 
		extends AWTEventMulticaster 
		implements GameListener {
	
	protected GameEventMulticaster(EventListener a, EventListener b) {
		super(a, b);
	}

	public static GameListener add(GameListener a, GameListener b) {
		return (GameListener) addInternal(a, b);
	}

	public static GameListener remove(GameListener l, GameListener oldl) {
		return (GameListener) removeInternal(l, oldl);
	}

	public void FieldOccupied(GameEvent e) {
		if (a != null)
			((GameListener) a).FieldOccupied(e);
		if (b != null)
			((GameListener) b).FieldOccupied(e);
	}

	public void FieldMarked(GameEvent e) {
		if (a != null)
			((GameListener) a).FieldMarked(e);
		if (b != null)
			((GameListener) b).FieldMarked(e);
	}

	public void ActiveFieldChanged(GameEvent e) {
		if (a != null)
			((GameListener) a).ActiveFieldChanged(e);
		if (b != null)
			((GameListener) b).ActiveFieldChanged(e);
	}

	public void StateChanged(GameEvent e) {
		if (a != null)
			((GameListener) a).StateChanged(e);
		if (b != null)
			((GameListener) b).StateChanged(e);
	}

	public void Timer(GameEvent e) {
		if (a != null)
			((GameListener) a).Timer(e);
		if (b != null)
			((GameListener) b).Timer(e);
	}

	protected static EventListener addInternal(EventListener a, EventListener b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		return new GameEventMulticaster(a, b);
	}

	protected EventListener remove(EventListener oldl) {
		if (oldl == a)
			return b;
		if (oldl == b)
			return a;
		EventListener a2 = removeInternal(a, oldl);
		EventListener b2 = removeInternal(b, oldl);
		if (a2 == a && b2 == b)
			return this;
		return addInternal(a2, b2);
	}
}
