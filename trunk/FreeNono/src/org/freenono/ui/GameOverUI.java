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
package org.freenono.ui;

import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.event.KeyEvent;
import java.awt.Font;
import javax.swing.JButton;

import org.freenono.board.BoardPreview;
import org.freenono.model.Nonogram;

public class GameOverUI extends JDialog {

	private static final long serialVersionUID = 1L;

	private Nonogram pattern = null;
	private boolean isSolved = false;

	private JPanel jContentPane = null;
	private JLabel nonogramNameLabel = null;
	private JButton closeButton = null;
	private JLabel messageLabel = null;

	private BoardPreview boardPreview = null;

	/**
	 * This is the default constructor
	 */
	public GameOverUI() {
		super();
		initialize();
	}

	public GameOverUI(Nonogram pattern, BoardPreview boardPreview, boolean isSolved) {
		super();

		this.pattern = pattern;
		this.boardPreview = boardPreview;
		this.isSolved = isSolved;

		initialize();
		nonogramNameLabel.setText(pattern.getName());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
		if (isSolved)
			this.setSize(300, 300);
		else 
			this.setSize(300, 150);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setContentPane(getJContentPane());
		this.setTitle(Messages.getString("GameOverUI.Title"));
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setAlwaysOnTop(true);
		this.setVisible(false);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			messageLabel = new JLabel();
			if (isSolved) {
				messageLabel.setText("<html><p style=\"text-align:center;\">"
						+ Messages.getString("GameOverUI.WinningText")
						+ "</p></html>");
			} else {
				messageLabel.setText("<html><p style=\"text-align:center;\">"
						+ Messages.getString("GameOverUI.LosingText")
						+ "</p></html>");
			}
			messageLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			nonogramNameLabel = new JLabel();
			nonogramNameLabel.setText(pattern.getName());
			nonogramNameLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			nonogramNameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			nonogramNameLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
			nonogramNameLabel.setVerticalAlignment(SwingConstants.CENTER);
			nonogramNameLabel.setFont(new Font("Dialog", Font.BOLD, 18));
			nonogramNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jContentPane = new JPanel();
			FlowLayout layout = new FlowLayout();
			layout.setHgap(100);
			layout.setVgap(20);
			jContentPane.setLayout(layout);
			jContentPane.add(messageLabel);
			if (isSolved)
			{
				jContentPane.add(nonogramNameLabel);
				jContentPane.add(boardPreview);
			}
			jContentPane.add(getJButton());
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText(Messages.getString("GameOverUI.CloseButton"));
			closeButton.grabFocus();
			getRootPane().setDefaultButton(closeButton);
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		}
		return closeButton;
	}

}
