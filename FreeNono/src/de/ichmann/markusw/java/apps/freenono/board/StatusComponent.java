package de.ichmann.markusw.java.apps.freenono.board;

import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import de.ichmann.christianw.java.components.dotmatrix.DotMatrix;
import de.ichmann.christianw.java.components.dotmatrix.Emblem;
import de.ichmann.markusw.java.apps.freenono.event.GameAdapter;
import de.ichmann.markusw.java.apps.freenono.event.GameEvent;
import de.ichmann.markusw.java.apps.freenono.event.GameEventHelper;
import de.ichmann.markusw.java.apps.freenono.model.Game;

public class StatusComponent extends JPanel {

	private static final long serialVersionUID = 1283871798919081849L;

	private GameEventHelper eventHelper;
	
	private DotMatrix displayTime;
	private Emblem remainingTime;
	private JLabel jlabel;
	private Game game;

	private final SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss");
	private String timeLeft = "00:00";
	private int failCountLeft;
	
	private GameAdapter gameAdapter = new GameAdapter() {
		
		@Override
		public void Timer(GameEvent e) {
			refreshTime();
		}
		
		public void WrongFieldOccupied(GameEvent e) {
			refreshFailCount();
		}
		
	};

	public StatusComponent(Game game) {
		this.game = game;

		// set layout
		FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 40, 15);
		this.setLayout(layout);

		// set border
		Border border = new EtchedBorder(EtchedBorder.RAISED);
		this.setBorder(border);

		// format and display time
		if (game.usesMaxTime()) {
			timeLeft = timeFormatter.format(game.getTimeLeft());
		} else {
			timeLeft = timeFormatter.format(game.getElapsedTime());
		}
		remainingTime = new Emblem(timeLeft, 1, 0);
		displayTime = new DotMatrix(36, 8);
		displayTime.addEmblem(remainingTime);
		this.add(displayTime);

		// set fail count label
		jlabel = new JLabel();
		jlabel.setFont(new Font("FreeSans", Font.PLAIN, 18));
		failCountLeft = game.getFailCountLeft();
		if (failCountLeft != 0) {
			jlabel.setText(Integer.toString(failCountLeft) + " errors left");
		}
		this.add(jlabel);

	}

	public void setEventHelper(GameEventHelper eventHelper) {
		this.eventHelper = eventHelper;
		eventHelper.addGameListener(gameAdapter);
	}

	private void refreshTime() {
		if (game.usesMaxTime()) {
			timeLeft = timeFormatter.format(game.getTimeLeft());
		} else {
			timeLeft = timeFormatter.format(game.getElapsedTime());
		}
		remainingTime.setText(timeLeft);
		displayTime.refresh();
	}

	private void refreshFailCount() {
		
		failCountLeft = game.getFailCountLeft();
		
		if (failCountLeft != 0) {
			jlabel.setText(Integer.toString(failCountLeft) + " errors left");
		}
		
	}
}
