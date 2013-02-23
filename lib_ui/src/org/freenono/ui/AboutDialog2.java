/*****************************************************************************
 * FreeNono - A free implementation of the nonogram game
 * Copyright (c) 2012 Christian Wichmann
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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

public class AboutDialog2 extends JDialog {

	private static final long serialVersionUID = -3174417107818960578L;

	private static Logger logger = Logger.getLogger(AboutDialog.class);

	private FSScrollPane scroll;
	private XHTMLPanel panel;
	private GridBagConstraints gc;
	private GridBagLayout layout;

	private Font programNameFont;
	private Font programVersionFont;
	private Color backgroundColor;
	private String programName;
	private String programVersion;
	private String programDescription;
	private String programIcon;

		
	public AboutDialog2(String programName, String programVersion,
			String programDescriptionFile, String programIconFile, Color backgroundColor) {

		super();

		this.programName = programName;
		this.programVersion = programVersion;
		this.programDescription = programDescriptionFile;
		this.programIcon = programIconFile;
		this.backgroundColor = backgroundColor;

		setFonts();
		
		initialize();

		addListener();
		
		setScrollThread();
	}
	
	public AboutDialog2(String programName, String programDescriptionFile, 
			String programIconFile, Color backgroundColor) {
		
		this(programName, "", programDescriptionFile, programIconFile,backgroundColor);
	}


	
	private void setFonts() {
		
		programNameFont = new Font("FreeSans", Font.BOLD, 24);
		programVersionFont = new Font("FreeSerif", Font.ITALIC, 16);
	}

	
	private void setScrollThread() {
		
		class ScrollThread extends Thread {
			public void run() {
				while (true) {

					try {
						Thread.sleep(1000);

					} catch (InterruptedException e) {

						logger.debug("Thread interrupted!");
					}

					JScrollBar sb = scroll.getVerticalScrollBar();
					sb.setValue(sb.getValue() + 10);
				}
			}
		};
		new ScrollThread().start();
	}

	private void initialize() {

		setTitle(programName);
		setUndecorated(true);
		setLocationRelativeTo(null);
		getContentPane().setBackground(backgroundColor);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(500, 500);

		// use GridBagLayout as layout manager
		layout = new GridBagLayout();
		gc = new GridBagConstraints();
		getContentPane().setLayout(layout);


		// add icon
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.gridheight = 5;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		getContentPane().add(getProgramIcon(), gc);

		// add program name and version
		gc.gridx = 1;
		gc.gridy = 2;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(getProgramNameLabel(), gc);
		gc.gridy = 3;
		gc.anchor = GridBagConstraints.NORTHWEST;
		getContentPane().add(getProgramVersionLabel(), gc);

		// set up XHTML panel and add scroll pane
		gc.gridx = 0;
		gc.gridy = 5;
		gc.gridwidth = 2;
		gc.gridheight = 1;
		gc.weightx = 1;
		gc.weighty = 8;
		gc.insets = new Insets(10, 20, 10, 20);
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		getContentPane().add(getScrollPane(), gc);

		// add close button
		gc.gridx = 1;
		gc.gridy = 6;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.weightx = 1;
		gc.weighty = 1;
		gc.anchor = GridBagConstraints.EAST;
		gc.fill = GridBagConstraints.NONE;
		getContentPane().add(getCloseButton(), gc);

		// pack();

		setVisible(true);
	}

	private JButton getCloseButton() {
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performExit();
			}
		});
		return closeButton;
	}

	private JLabel getProgramNameLabel() {

		JLabel programNameLabel = new JLabel();
		programNameLabel.setFont(programNameFont);
		programNameLabel.setText(programName);
		return programNameLabel;
	}

	private JLabel getProgramVersionLabel() {

		JLabel programVersionLabel = new JLabel();
		programVersionLabel.setFont(programVersionFont);
		programVersionLabel.setText(programVersion);
		return programVersionLabel;
	}

	private FSScrollPane getScrollPane() {

		panel = new XHTMLPanel();
	    panel.setOpaque(false);	
	    
		scroll = new FSScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setOpaque(false);

		try {

			panel.setDocument(programDescription);

		} catch (Exception e) {

			logger.debug("Could not insert file content into HTML pane.");
		}

		return scroll;
	}

	private JLabel getProgramIcon() {

		JLabel icon = new JLabel("", new ImageIcon(programIcon), JLabel.CENTER);
		icon.setToolTipText(programName);
		return icon;
	}

	private void addListener() {

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("ESCAPE"), "Close");
		getRootPane().getActionMap().put("Close", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				performExit();
			}
		});		
	}

	private void performExit() {

		setVisible(false);
		dispose();
	}

}