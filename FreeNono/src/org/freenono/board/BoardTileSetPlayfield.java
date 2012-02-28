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
package org.freenono.board;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.freenono.event.GameAdapter;
import org.freenono.event.GameEvent;
import org.freenono.event.GameEventHelper;
import org.freenono.event.GameEvent.ProgramControlType;
import org.freenono.model.Game;
import org.freenono.model.GameState;
import org.freenono.model.Token;


public class BoardTileSetPlayfield extends BoardTileSet {

	private static final long serialVersionUID = 723055953042228828L;

	private boolean hidePlayfield = false;
	private int tempCounter = 0;

	private static Logger logger = Logger
			.getLogger(BoardTileSetPlayfield.class);

	private GameAdapter gameAdapter = new GameAdapter() {

		public void StateChanged(GameEvent e) {

			switch (e.getNewState()) {
			case gameOver:
				break;

			case solved:
				break;

			case paused:
				// clear board during pause
				if (hidePlayfield) {
					clearBoard();
				}
				break;

			case running:
				if (e.getOldState() == GameState.paused) {
					// restore board after pause
					if (hidePlayfield) {
						restoreBoard();
					}
				}
				break;

			default:
				break;
			}

		}

	};

	public BoardTileSetPlayfield(Game game, boolean hidePlayfield,
			Dimension tileDimension) {
		super(game, tileDimension);

		this.hidePlayfield = hidePlayfield;
		tileSetWidth = game.width();
		tileSetHeight = game.height();

		initialize();

		addListeners();
		paintBorders();

	}

	public void setEventHelper(GameEventHelper eventHelper) {
		this.eventHelper = eventHelper;
		eventHelper.addGameListener(gameAdapter);
	}

	/**
	 * Adding Listeners for key and mouse events on the nonogram board.
	 */
	private void addListeners() {
		// set this Component focusable to capture key events
		this.setFocusable(true);
		this.grabFocus();

		// add Listener for mouse and keyboard usage
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Since the user clicked on us, let us get focus!
				requestFocusInWindow();
			}

			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				switch (e.getButton()) {
				case MouseEvent.BUTTON1:
					handleClick(p);
					occupyActiveField();
					break;
				case MouseEvent.BUTTON3:
					handleClick(p);
					markActiveField();
					break;
				default:
					break;
				}
			}
		});
		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				handleMouseMovement(p);
			}
		});
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				int keyCode = evt.getKeyCode();
				if (keyCode == KeyEvent.VK_LEFT) {
					moveActiveLeft();
				} else if (keyCode == KeyEvent.VK_RIGHT) {
					moveActiveRight();
				} else if (keyCode == KeyEvent.VK_UP) {
					moveActiveUp();
				} else if (keyCode == KeyEvent.VK_DOWN) {
					moveActiveDown();
				} else if (keyCode == KeyEvent.VK_PERIOD) {
					markActiveField();
				} else if (keyCode == KeyEvent.VK_COMMA) {
					occupyActiveField();
				} else if (keyCode == KeyEvent.VK_F1) {
					eventHelper.fireProgramControlEvent(new GameEvent(this,
							ProgramControlType.START_GAME));
				} else if (keyCode == KeyEvent.VK_F2) {
					eventHelper.fireProgramControlEvent(new GameEvent(this,
							ProgramControlType.RESTART_GAME));
				} else if (keyCode == KeyEvent.VK_F3) {
					eventHelper.fireProgramControlEvent(new GameEvent(this,
							ProgramControlType.PAUSE_GAME));
				} else if (keyCode == KeyEvent.VK_F4) {
					eventHelper.fireProgramControlEvent(new GameEvent(this,
							ProgramControlType.RESUME_GAME));
				} else if (keyCode == KeyEvent.VK_F5) {
					eventHelper.fireProgramControlEvent(new GameEvent(this,
							ProgramControlType.STOP_GAME));
				}
			}
		});
	}

	protected void handleMouseMovement(Point p) {
		Component c = this.findComponentAt(p);
		if (c instanceof BoardTile) {
			// deactivate old tile...
			board[activeFieldRow][activeFieldColumn].setActive(false);
			// ...find coordinates for clicked tile by searching the board...
			for (int i = 0; i < tileSetHeight; i++) {
				for (int j = 0; j < tileSetWidth; j++) {
					if (((BoardTile) c).equals(board[i][j])) {
						activeFieldRow = i;
						activeFieldColumn = j;
					}
				}
			}
			// ...and set it as active tile.
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
		eventHelper.fireActiveFieldChangedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	protected void handleClick(Point p) {
		Component c = this.findComponentAt(p);
		if (c instanceof BoardTile) {
			// find coordinates for clicked tile...
			for (int i = 0; i < tileSetHeight; i++) {
				for (int j = 0; j < tileSetWidth; j++) {
					if (((BoardTile) c).equals(board[i][j])) {
						activeFieldRow = i;
						activeFieldColumn = j;
					}
				}
			}
		}

	}

	private void paintBorders() {
		for (int i = 0; i < tileSetHeight; i++) {
			for (int j = 0; j < tileSetWidth; j++) {
				board[i][j].setDrawBorderWest(true);
				if ((j + 1) % 5 == 0 || (j + 1) == tileSetWidth) {
					board[i][j].setDrawBorderEast(true);
				}
				board[i][j].setDrawBorderNorth(true);
				if ((i + 1) % 5 == 0 || (i + 1) == tileSetHeight) {
					board[i][j].setDrawBorderSouth(true);
				}
			}
		}
	}

	public void occupyActiveField() {

		if (!game.canOccupy(activeFieldColumn, activeFieldRow)) {
			// unable to occupy field, maybe it is already occupied
			logger.debug("can not occupy field (" + activeFieldColumn + ", "
					+ activeFieldRow + ")");
			// TODO add user message
			return;
		}
		if (!game.occupy(activeFieldColumn, activeFieldRow)) {
			// failed to occupy field, there maybe some changes
			board[activeFieldRow][activeFieldColumn].setCrossed(true);
			logger.debug("failed move on field (" + activeFieldColumn + ", "
					+ activeFieldRow + ")");
			// TODO add user message
			return;
		} else {
			logger.debug("field (" + activeFieldColumn + ", " + activeFieldRow
					+ ") occupied");
		}

		board[activeFieldRow][activeFieldColumn].setMarked(true);
		eventHelper.fireFieldOccupiedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	public void markActiveField() {

		if (!game.canMark(activeFieldColumn, activeFieldRow)) {
			// unable to mark field, maybe it is already occupied
			logger.debug("can not mark field (" + activeFieldColumn + ", "
					+ activeFieldRow + ")");
			// TODO add user message
			return;
		}
		if (!game.mark(activeFieldColumn, activeFieldRow)) {
			// failed to mark field
			logger.debug("failed to mark field (" + activeFieldColumn + ", "
					+ activeFieldRow + ")");
			// TODO add user message
			return; // return, because there has been no change

		} else {
			if (game.getFieldValue(activeFieldColumn, activeFieldRow) == Token.MARKED) {
				board[activeFieldRow][activeFieldColumn].setCrossed(true);
				eventHelper.fireFieldMarkedEvent(new GameEvent(this,
						activeFieldColumn, activeFieldRow));
			} else {
				board[activeFieldRow][activeFieldColumn].setCrossed(false);
			}
			logger.debug("field (" + activeFieldColumn + ", " + activeFieldRow
					+ ") marked");
		}
	}

	public void setActive(int column, int row) {
		if (column >= 0 && column < tileSetWidth && row >= 0
				&& row < tileSetHeight) {
			board[activeFieldRow][activeFieldColumn].setActive(false);
			activeFieldColumn = column;
			activeFieldRow = row;
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
	}

	public void moveActiveLeft() {
		if (activeFieldColumn > 0) {
			board[activeFieldRow][activeFieldColumn].setActive(false);
			activeFieldColumn -= 1;
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
		eventHelper.fireActiveFieldChangedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	public void moveActiveRight() {
		if (activeFieldColumn < tileSetWidth - 1) {
			board[activeFieldRow][activeFieldColumn].setActive(false);
			activeFieldColumn += 1;
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
		eventHelper.fireActiveFieldChangedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	public void moveActiveUp() {
		if (activeFieldRow > 0) {
			board[activeFieldRow][activeFieldColumn].setActive(false);
			activeFieldRow -= 1;
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
		eventHelper.fireActiveFieldChangedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	public void moveActiveDown() {
		if (activeFieldRow < tileSetHeight - 1) {
			board[activeFieldRow][activeFieldColumn].setActive(false);
			activeFieldRow += 1;
			board[activeFieldRow][activeFieldColumn].setActive(true);
		}
		eventHelper.fireActiveFieldChangedEvent(new GameEvent(this,
				activeFieldColumn, activeFieldRow));
	}

	public void clearBoard() {
		for (int i = 0; i < tileSetHeight; i++) {
			for (int j = 0; j < tileSetWidth; j++) {
				board[i][j].setMarked(false);
				board[i][j].setCrossed(false);
			}
		}
	}

	public void restoreBoard() {
		for (int i = 0; i < tileSetHeight; i++) {
			for (int j = 0; j < tileSetWidth; j++) {
				if (game.getFieldValue(j, i) == Token.MARKED) {
					board[i][j].setCrossed(true);
				} else if (game.getFieldValue(j, i) == Token.OCCUPIED) {
					board[i][j].setMarked(true);
				} else {
					//
				}
			}
		}
	}

	public void solveBoard() {
		for (int i = 0; i < tileSetHeight; i++) {
			for (int j = 0; j < tileSetWidth; j++) {
				board[i][j].setCrossed(false);
				if (game.getPattern().getFieldValue(j, i)) {
					board[i][j].setMarked(true);
				} else {
					board[i][j].setMarked(false);
				}
			}
		}
	}

}
