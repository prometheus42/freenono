/*****************************************************************************
 * FreeNonoEditor - A editor for nonogram riddles
 * Copyright (c) 2012 Christian Wichmann
 * 
 * File name: $HeadURL$
 * Revision: $Revision$
 * Last modified: $Date$
 * Last modified by: $Author$
 * $Id$
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
package org.freenono.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.freenono.model.Course;
import org.freenono.model.Nonogram;
import org.freenono.serializer.CourseFormatException;
import org.freenono.serializer.NonogramFormatException;
import org.freenono.serializer.SimpleNonogramSerializer;
import org.freenono.serializer.XMLNonogramSerializer;
import org.freenono.serializer.ZipCourseSerializer;
import org.freenono.ui.SplashScreen;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class EditorFrame extends JFrame {

	private static Logger logger = Logger.getLogger(EditorFrame.class);

	private static final long serialVersionUID = 5991986713803903723L;
	
	private static final String nonoServer = "http://127.0.0.1:6666"; //$NON-NLS-1$

	private JPanel contentPane = null;
	private JMenuBar menuBar = null;
	private JMenuItem saveItem = null;
	private JMenuItem saveAsItem = null;
	private JMenuItem propertiesItem = null;
	private JMenuItem publishItem = null;
	private JPanel boardPanel = null;
	private PropertyDialog propertyDialog = null;
	private CourseViewDialog courseViewDialog = null;

	private Nonogram currentNonogram = null;
	private File currentOpenFile = null;
	private EditorTileSet boardComponent = null;

	private XMLNonogramSerializer xmlNonogramSerializer = new XMLNonogramSerializer();
	private ZipCourseSerializer zipCourseSerializer = new ZipCourseSerializer();

	public EditorFrame() {

		super();

		showSplashscreen();

		initialize();

		// add component Listener for handling the resize operation
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Component c = (Component) e.getSource();
				handleResize(c.getSize());
			}
		});

	}

	public EditorFrame(File file) {
		
		this();

		loadNonogram(file);
	}

	/**
	 * This method initializes EditorFrame
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setSize(1000, 850);
		this.setLocationRelativeTo(null);
		this.setName(Messages.getString("EditorFrame.Title")); //$NON-NLS-1$
		this.setTitle(Messages.getString("EditorFrame.FNE")); //$NON-NLS-1$

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				performExit();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});

		this.setJMenuBar(getMenu());
		this.setContentPane(getEditorPane());

		this.propertyDialog = new PropertyDialog(this);

	}

	private void handleResize(Dimension newSize) {
		if (boardComponent != null) {

			int tileHeight = (int) ((newSize.getHeight() - menuBar.getHeight()) / currentNonogram
					.height());
			int tiledWidth = (int) (newSize.getWidth() / currentNonogram
					.width());
			int tileSize = Math.min(tileHeight, tiledWidth) - 5;

			boardComponent.handleResize(new Dimension(tileSize, tileSize));

		}
	}

	/**
	 * This method initializes the menuBar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getMenu() {

		if (menuBar == null) {

			JMenu menu;
			JMenuItem menuItem;

			// create the menu bar.
			menuBar = new JMenuBar();

			// create file menu
			menu = new JMenu(Messages.getString("EditorFrame.FileMenu")); //$NON-NLS-1$
			menu.setMnemonic(KeyEvent.VK_F);
			menu.getAccessibleContext().setAccessibleDescription(Messages.getString("EditorFrame.FileMenuTooltip")); //$NON-NLS-1$
			menuBar.add(menu);

			// create menu items for file menu
			menuItem = new JMenuItem(Messages.getString("EditorFrame.NewNonogram"), KeyEvent.VK_N); //$NON-NLS-1$
			menuItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.NewNonogramTooltip")); //$NON-NLS-1$
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// create new nonogram
					createNewNonogram();
				}
			});

			menuItem = new JMenuItem(Messages.getString("EditorFrame.LoadNonogram"), KeyEvent.VK_L); //$NON-NLS-1$
			menuItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.LoadNonogramTolltip")); //$NON-NLS-1$
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openNonogram();
				}
			});

			saveItem = new JMenuItem(Messages.getString("EditorFrame.SaveNonogram"), KeyEvent.VK_S); //$NON-NLS-1$
			saveItem.setEnabled(false);
			saveItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.SaveNonogramTooltip")); //$NON-NLS-1$
			menu.add(saveItem);
			saveItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveNonogram();
				}
			});

			saveAsItem = new JMenuItem(Messages.getString("EditorFrame.SaveNonogramAs"), KeyEvent.VK_A); //$NON-NLS-1$
			saveAsItem.setEnabled(false);
			saveAsItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.SaveNonogramAsTooltip")); //$NON-NLS-1$
			menu.add(saveAsItem);
			saveAsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveNonogramAs();
				}
			});

			menu.addSeparator();

			publishItem = new JMenuItem(Messages.getString("EditorFrame.PublishNonogram"), KeyEvent.VK_P); //$NON-NLS-1$
			publishItem.setEnabled(false);
			publishItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.PublishNonogramTooltip")); //$NON-NLS-1$
			menu.add(publishItem);
			publishItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					publishNonogram();
				}
			});

			menu.addSeparator();

			propertiesItem = new JMenuItem(Messages.getString("EditorFrame.Properties"), KeyEvent.VK_R); //$NON-NLS-1$
			propertiesItem.setEnabled(false);
			propertiesItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.PropertiesTooltip")); //$NON-NLS-1$
			menu.add(propertiesItem);
			propertiesItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showPropertiesDialog();
				}
			});
			
			menu.addSeparator();

			menuItem = new JMenuItem(Messages.getString("EditorFrame.Exit"), KeyEvent.VK_X); //$NON-NLS-1$
			menuItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.ExitTooltip")); //$NON-NLS-1$
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					performExit();
				}
			});

			// create help menu
			menu = new JMenu(Messages.getString("EditorFrame.HelpMenu")); //$NON-NLS-1$
			menu.setMnemonic(KeyEvent.VK_H);
			menu.getAccessibleContext().setAccessibleDescription(Messages.getString("EditorFrame.HelpMenuTooltip")); //$NON-NLS-1$
			menuBar.add(menu);

			menuItem = new JMenuItem(Messages.getString("EditorFrame.Help"), KeyEvent.VK_H); //$NON-NLS-1$
			menuItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.HelpTooltip")); //$NON-NLS-1$
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showHelpDialog();
				}
			});

			menuItem = new JMenuItem(Messages.getString("EditorFrame.About"), KeyEvent.VK_A); //$NON-NLS-1$
			menuItem.getAccessibleContext().setAccessibleDescription(
					Messages.getString("EditorFrame.AboutTooltip")); //$NON-NLS-1$
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showAboutDialog();
				}
			});
		}

		return menuBar;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getEditorPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			// contentPane.add(new JButton(), BorderLayout.NORTH);
		}
		return contentPane;
	}

	private void buildBoard() {

		if (boardPanel == null) {
			boardPanel = new JPanel() {
				private static final long serialVersionUID = -5144877072997396393L;

				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g;
					BufferedImage cache = null;
					if (cache == null || cache.getHeight() != getHeight()) {
						cache = new BufferedImage(2, getHeight(),
								BufferedImage.TYPE_INT_RGB);
						Graphics2D g2d = cache.createGraphics();

						GradientPaint paint = new GradientPaint(0, 0,
								new Color(143, 231, 200), 0, getHeight(),
								Color.WHITE);
						g2d.setPaint(paint);
						g2d.fillRect(0, 0, 2, getHeight());
						g2d.dispose();
					}
					g2.drawImage(cache, 0, 0, getWidth(), getHeight(), null);
				}
			};
		} else {
			boardPanel.remove(boardComponent);
		}

		// clear all remnants of the old board
		this.repaint();

		// calculating maximum size for boardComponent
		int tileHeight = (this.getHeight() - menuBar.getHeight())
				/ currentNonogram.height();
		int tiledWidth = this.getWidth() / currentNonogram.width();
		int tileSize = Math.min(tileHeight, tiledWidth) - 5;

		boardComponent = new EditorTileSet(currentNonogram, new Dimension(
				tileSize, tileSize));
		boardPanel.add(boardComponent);
		contentPane.add(boardPanel, BorderLayout.CENTER);

		this.validate();

	}

	private void performExit() {

		// int answer = JOptionPane.showConfirmDialog(this,
		// "Do you really want to exit FNE?", "Exit FNE",
		// JOptionPane.YES_NO_OPTION);
		//
		// if (answer == JOptionPane.OK_OPTION) {

		this.setVisible(false);
		this.dispose();
		System.exit(1);

		// }
	}

	protected void createNewNonogram() {

		// set file pointer to null
		currentOpenFile = null;
		saveItem.setEnabled(false);
		saveAsItem.setEnabled(true);
		propertiesItem.setEnabled(true);
		publishItem.setEnabled(true);

		// get user input for width and height of nonogram
		propertyDialog.setVisible(true);
		currentNonogram = propertyDialog.getNonogram();

		if (currentNonogram != null) {

			buildBoard();
		}

	}

	protected void saveNonogramAs() {

		final JFileChooser fc = new JFileChooser();
		File file;

		// set filter for file chooser
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".nonogram"); //$NON-NLS-1$
			}

			@Override
			public String getDescription() {
				return Messages.getString("EditorFrame.NonogramFiles"); //$NON-NLS-1$
			}
		});

		fc.setSelectedFile(new File(currentNonogram.getName() + ".nonogram")); //$NON-NLS-1$

		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

			file = fc.getSelectedFile();

			// TODO: add string concatenation of standard file extension!
			// if (file.getName().toLowerCase().endsWith(".nonogram"))
			// file.

			try {
				xmlNonogramSerializer.save(file, currentNonogram);
			} catch (NullPointerException e) {
				logger.error("Null pointer encountered during nonogram serializing."); //$NON-NLS-1$
			} catch (IOException e) {
				logger.error("Could not write serialized nonogram to output stream."); //$NON-NLS-1$
			}

			currentOpenFile = file;
			saveItem.setEnabled(true);
			saveAsItem.setEnabled(true);
			propertiesItem.setEnabled(true);
			publishItem.setEnabled(true);
		}

	}

	protected void saveNonogram() {

		if (currentOpenFile != null) {
			try {
				xmlNonogramSerializer.save(currentOpenFile, currentNonogram);
				
			} catch (NullPointerException e) {
				
				logger.error("The open nonogram could not be saved because an error occured."); //$NON-NLS-1$
				
			} catch (IOException e) {
				
				logger.error("The open nonogram could not be saved because an error occured."); //$NON-NLS-1$
			}
		}
	}

	protected void openNonogram() {

		final JFileChooser fc = new JFileChooser();

		// set filters for file chooser
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".nonogram"); //$NON-NLS-1$
			}

			@Override
			public String getDescription() {
				return Messages.getString("EditorFrame.NonogramFileType"); //$NON-NLS-1$
			}
		});
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".nonopack"); //$NON-NLS-1$
			}

			@Override
			public String getDescription() {
				return Messages.getString("EditorFrame.CourseFileType"); //$NON-NLS-1$
			}
		});

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			currentOpenFile = fc.getSelectedFile();
			
			if (getExtension(currentOpenFile).equals("nonogram")) { //$NON-NLS-1$
				
				loadNonogram(currentOpenFile);
				
			} else {
				
				loadNonogramFromCourse(currentOpenFile);
			}

		}
	}

	/**
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {

		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
		
		// String extension = "";
		// int i = fileName.lastIndexOf('.');
		// int p = Math.max(fileName.lastIndexOf('/'),
		// fileName.lastIndexOf('\\');
		// if (i > p) {
		// extension = fileName.substring(i+1);
		// }
	}

	protected void loadNonogram(File file) {

		Nonogram[] n = null;

		if (file.getName().endsWith(
				"." + XMLNonogramSerializer.DEFAULT_FILE_EXTENSION)) { //$NON-NLS-1$

			try {
				n = xmlNonogramSerializer.load(file);
				
			} catch (NullPointerException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
				
			} catch (IOException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
				
			} catch (NonogramFormatException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
			}

		} else if (file.getName().endsWith(
				"." + SimpleNonogramSerializer.DEFAULT_FILE_EXTENSION)) { //$NON-NLS-1$

			SimpleNonogramSerializer simpleNonogramSerializer = new SimpleNonogramSerializer();
			
			try {
				n = simpleNonogramSerializer.load(file);
				
			} catch (NullPointerException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
				
			} catch (IOException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
				
			} catch (NonogramFormatException e) {
				
				logger.error("The chosen nonogram could not be loaded because an error occured."); //$NON-NLS-1$
			}
		}

		// choose Nonogram from read file to edit
		currentNonogram = n[0];

		finishLoading();
	}
	
	protected void loadNonogramFromCourse(File file) {
		
		Course c = null;
		
		try {
			
			c = zipCourseSerializer.load(file);
			
		} catch (NullPointerException e) {
			
			logger.error("An error occured during loading of course file."); //$NON-NLS-1$
			
		} catch (IOException e) {
			
			logger.error("An error occured during loading of course file."); //$NON-NLS-1$
			
		} catch (NonogramFormatException e) {
			
			logger.error("An error occured during loading of course file."); //$NON-NLS-1$
			
		} catch (CourseFormatException e) {
			
			logger.error("An error occured during loading of course file."); //$NON-NLS-1$
		}
		
		if (c != null) {
			
			logger.debug("Opened course view dialog to choose nonogram to edit."); //$NON-NLS-1$
			courseViewDialog = new CourseViewDialog(this, c);
			currentNonogram = courseViewDialog.getChosenNonogram();
			
			finishLoading();
		}
	}
	
	private void finishLoading() {

		// paint board only if one nonogram was chosen
		if (currentNonogram != null) {

			logger.debug("Nonogram " + currentNonogram.getName() //$NON-NLS-1$
					+ " was chosen. Board will be build."); //$NON-NLS-1$
			buildBoard();

			saveItem.setEnabled(true);
			saveAsItem.setEnabled(true);
			propertiesItem.setEnabled(true);
			publishItem.setEnabled(true);
		}
		else {
			
			logger.warn("No nonogram was chosen to be opened."); //$NON-NLS-1$
		}
	}
	
	protected void publishNonogram() {
		
		String courseName = "Testing"; //$NON-NLS-1$
		
		// ...serialize picked nonogram
		XMLNonogramSerializer ns = new XMLNonogramSerializer();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ns.save(baos, currentNonogram);
		} catch (NullPointerException e) {
			logger.error("Null pointer encountered during nonogram serializing."); //$NON-NLS-1$
		} catch (IOException e) {
			logger.error("Could not write serialized nonogram to output stream."); //$NON-NLS-1$
		}
		
		// send nonogram via network
		URL serverURL = null;
		try {
			serverURL = new URL(nonoServer + "/" + courseName + "/" //$NON-NLS-1$ //$NON-NLS-2$
					+ currentNonogram.getName());
		} catch (MalformedURLException e) {
			logger.debug("Invalid URL for NonoServer!"); //$NON-NLS-1$
		}
		ClientResource resource = new ClientResource(serverURL.toString());

		// write from ByteArrayOutputStream into Representation?!
		Representation rep = new OutputRepresentation(MediaType.TEXT_XML) {

			@Override
			public void write(OutputStream arg0) throws IOException {
				baos.writeTo(arg0);
			}
		};
		resource.put(rep);
	}

	protected void showPropertiesDialog() {

		propertyDialog.setNonogram(currentNonogram);
		propertyDialog.setVisible(true);
		currentNonogram = propertyDialog.getNonogram();
		buildBoard();

	}

	protected void showHelpDialog() {
		
		// TODO: show help dialog
	}

	protected void showAboutDialog() {

		showSplashscreen();
	}

	private void showSplashscreen() {

		// show splash screen
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SplashScreen splash = new SplashScreen(
						"/resources/icon/splashscreen_fne.png"); //$NON-NLS-1$
				splash.setVisible(true);
			}
		});
	}

}