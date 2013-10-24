/*****************************************************************************
 * FreeNono - A free implementation of the nonogram game
 * Copyright (c) 2013 by FreeNono Development Team
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
package org.freenono.ui.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.freenono.controller.Manager;
import org.freenono.provider.CollectionFromFilesystem;
import org.freenono.provider.CollectionProvider;
import org.freenono.provider.CourseFromSeed;
import org.freenono.provider.CourseProvider;
import org.freenono.provider.NonogramFromSeed;
import org.freenono.provider.NonogramProvider;
import org.freenono.ui.Messages;
import org.freenono.ui.YesNoDialog;
import org.freenono.ui.colormodel.ColorModel;

/**
 * Shows a dialog for the user to choose a nonogram to play.
 * 
 * @author Martin Wichmann, Christian Wichmann
 */
public class NonogramChooserUI extends JDialog {

    private static final long serialVersionUID = 449003977161113952L;

    private static Logger logger = Logger.getLogger(NonogramChooserUI.class);

    private final List<CollectionProvider> nonogramProvider;
    private NonogramProvider chosenNonogram = null;
    private ColorModel colorModel;

    private JTree nonogramsTree = null;
    private DefaultTreeModel nonogramsTreeModel = null;
    private DefaultMutableTreeNode nonogramsTreeRootNode = null;

    private JPanel extraPane = null;
    private JPanel courseViewPane = null;
    private JPopupMenu popup = null;

    /**
     * Creates a dialog to chose a nonogram to play.
     * 
     * @param nonogramProvider
     *            list of collection containing nonograms to chose from
     * @param colorModel
     *            color model given by the Settings object
     */
    public NonogramChooserUI(final List<CollectionProvider> nonogramProvider,
            final ColorModel colorModel) {

        this.nonogramProvider = nonogramProvider;
        this.colorModel = colorModel;

        initialize();

        addListener();
    }

    /**
     * Initializes a dialog to chose a nonogram to play.
     */
    private void initialize() {

        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        // add buttons to dialog
        add(getButtonPane(), BorderLayout.SOUTH);

        // split dialog horizontal between treePane and the extraPane
        JSplitPane horizontalSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, getTreePane(), getExtraPane());
        horizontalSplitPane.setContinuousLayout(true);
        horizontalSplitPane.setDividerLocation(300);
        horizontalSplitPane.setDividerSize(0);
        add(horizontalSplitPane, BorderLayout.NORTH);

        // populate tree
        populateTree(nonogramProvider);

        // set course view pane to empty panel
        courseViewPane = new JPanel();
        courseViewPane.add(new JLabel(Messages
                .getString("NonogramChooserUI.ClickLeft")));
        // courseViewPane.setSize(new Dimension(675, 450));
        courseViewPane.setPreferredSize(new Dimension(650, 450));
        extraPane.add(courseViewPane);

        pack();

        // set gui options
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(DEFAULT_MODALITY_TYPE);
        setIconImage(new ImageIcon(getClass().getResource(
                "/resources/icon/icon_freenono.png")).getImage());
    }

    /**
     * Adds listeners for opening a course view when tree element is selected
     * and showing context menu when clicking with right mouse button. Also it
     * implements all key bindings for this dialog.
     */

    private void addListener() {

        nonogramsTree.getSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {

                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                                .getLastSelectedPathComponent();

                        // if nothing is selected
                        if (node == null) {
                            return;
                        }

                        /* retrieve the node that was selected */
                        Object userObject = node.getUserObject();
                        logger.debug("Object in nonogram tree selected: "
                                + userObject);

                        if (userObject instanceof CourseProvider) {

                            openCourseViewPane();
                        }
                    }
                });

        nonogramsTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {

                // show context menu at right click
                if (SwingUtilities.isRightMouseButton(e)) {

                    showPopupMenu(e.getPoint());
                }
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "Close");
        getRootPane().getActionMap().put("Close", new AbstractAction() {

            private static final long serialVersionUID = 8076217677977300825L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                performClose();
            }
        });

        nonogramsTree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ENTER"), "OpenCourse");
        nonogramsTree.getActionMap().put("OpenCourse", new AbstractAction() {

            private static final long serialVersionUID = 5184895503939248881L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                openCourseViewPane();
            }
        });

        // TODO allow to open popup menu with context menu key
        nonogramsTree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("CONTEXT_MENU"), "OpenSeedPopupMenu");
        nonogramsTree.getActionMap().put("OpenSeedPopupMenu",
                new AbstractAction() {

                    private static final long serialVersionUID = -5445786215099872334L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        showPopupMenu();
                        // showSeedPopupMenu(getMousePosition());
                    }
                });
    }

    /*
     * Methods concerning with building tree pane
     */

    /**
     * Returns a panel with a tree containing all nonogram collections.
     * 
     * @return Panel with collection tree.
     */
    private JPanel getTreePane() {

        JPanel left = new JPanel(new GridLayout());
        left.setPreferredSize(new Dimension(275, 515));
        nonogramsTreeRootNode = new DefaultMutableTreeNode(
                Messages.getString("NonogramChooserUI.FreeNono"));
        nonogramsTreeModel = new DefaultTreeModel(nonogramsTreeRootNode);
        nonogramsTree = new JTree(nonogramsTreeModel);
        nonogramsTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(nonogramsTree);
        left.add(scrollPane);

        return left;
    }

    /**
     * Populates collection tree with <strong>all</strong> nonogram collections.
     * 
     * @param collectionsList
     *            list of collection containing nonograms
     */
    private void populateTree(final List<CollectionProvider> collectionsList) {

        for (CollectionProvider collection : collectionsList) {

            populateCollection(collection);
        }
    }

    /**
     * Populates collection tree with <strong>one</strong> nonogram collections.
     * 
     * @param collection
     *            collection containing nonograms
     */
    private void populateCollection(final CollectionProvider collection) {

        NonogramTreeCollectionNode nonoRootNode = new NonogramTreeCollectionNode(
                collection);
        logger.debug("Adding provider " + collection.getProviderName()
                + " to tree.");

        nonogramsTreeModel.insertNodeInto(nonoRootNode, nonogramsTreeRootNode,
                0);

        for (CourseProvider course : collection) {

            DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(course);
            nonogramsTreeModel.insertNodeInto(dirNode, nonoRootNode,
                    nonoRootNode.getChildCount());
            logger.debug("Adding course " + course + " to tree.");
        }

        nonogramsTree.expandPath(new TreePath(nonoRootNode.getPath()));
    }

    /*
     * Methods concerning with building extra pane.
     */

    /**
     * Returns panel on the right side of this dialog.
     * 
     * @return right side panel
     */
    private JPanel getExtraPane() {

        extraPane = new JPanel();
        return extraPane;
    }

    /**
     * This method builds the panel which includes the OK and Cancel buttons. By
     * clicking on the OK button the ActionListener identifies the chosen
     * nonogram to play and saves it in the result attribute.
     * 
     * @return button panel
     */
    private JPanel getButtonPane() {

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton resetPreviewsButton = new JButton(
                Messages.getString("NonogramChooserUI.ResetPreviewButton"));
        resetPreviewsButton.setToolTipText(Messages
                .getString("NonogramChooserUI.ResetPreviewTooltip"));
        resetPreviewsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {

                /*
                 * For resetting the preview images all files in a given path
                 * are deleted. Thumbnail path is defined by the Manager class.
                 * If directories are present at this path, nothing will be done
                 * with them.
                 */

                YesNoDialog askRestart = new YesNoDialog(
                        Messages.getString("NonogramChooserUI.ResetPreviewDialogTitle"),
                        colorModel.getTopColor(),
                        colorModel.getBottomColor(),
                        Messages.getString("NonogramChooserUI.ResetPreviewDialogQuestion"));
                askRestart.setVisible(true);

                if (askRestart.userChoseYes()) {
                    File thumbDir = new File(Manager.DEFAULT_THUMBNAILS_PATH);
                    if (thumbDir.exists() && thumbDir.isDirectory()) {
                        for (File child : thumbDir.listFiles()) {
                            if (child.isFile()) {
                                child.delete();
                            }
                        }
                    }
                }
            }
        });
        resetPreviewsButton.setActionCommand("ResetPreview");
        buttonPane.add(resetPreviewsButton);

        JButton cancelButton = new JButton(
                Messages.getString("NonogramChooserUI.ButtonCancel"));
        cancelButton.setToolTipText(Messages
                .getString("NonogramChooserUI.ButtonCancelTooltip"));
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                performClose();
            }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);

        return buttonPane;
    }

    /**
     * Check which element of the tree is clicked by mouse. For a single click
     * the information for the chosen element are shown in the InfoPane.
     */
    private void openCourseViewPane() {

        DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) nonogramsTree
                .getLastSelectedPathComponent();

        if (tempNode != null) {

            Object temp = tempNode.getUserObject();

            if (tempNode == nonogramsTreeModel.getRoot()) {
                return;
            }

            // if course is chosen, set up the CourseViewPane for this
            // course
            if (temp instanceof CourseProvider) {

                if (courseViewPane != null) {
                    courseViewPane.setEnabled(false);
                    extraPane.remove(courseViewPane);
                    extraPane.validate();
                }

                courseViewPane = new CourseViewPane((CourseProvider) temp);
                extraPane.add(courseViewPane);
                pack();
            }
        }
    }

    /**
     * Show popup menu after context menu key was pressed.
     */
    private void showPopupMenu() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                .getLastSelectedPathComponent();

        Rectangle r = nonogramsTree.getPathBounds(nonogramsTree
                .getLeadSelectionPath());
        Point rp = r.getLocation();
        Point p = new Point(rp.x + r.width, rp.y + r.height);

        if (node != null) {

            if (node.getUserObject() instanceof CourseFromSeed) {
                openCourseViewPane();
                showSeedPopupMenu(p);

            } else if (node.getUserObject() instanceof CollectionFromFilesystem) {
                showFilesystemPopupMenu(p);
            }
        }
    }

    /**
     * Show popup menu after right mouse button was clicked.
     * 
     * @param point
     *            current mouse position to show context menu at.
     */
    private void showPopupMenu(final Point point) {

        nonogramsTree.setSelectionRow(nonogramsTree.getClosestRowForLocation(
                point.x, point.y));

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                .getLastSelectedPathComponent();

        if (node != null) {

            if (node.getUserObject() instanceof CourseFromSeed) {
                openCourseViewPane();
                showSeedPopupMenu(point);

            } else if (node.getUserObject() instanceof CollectionFromFilesystem) {
                showFilesystemPopupMenu(point);
            }
        }
    }

    /**
     * Shows a context menu for nonograms generated by random at a given point.
     * 
     * @param point
     *            point to show menu at
     */
    private void showSeedPopupMenu(final Point point) {

        popup = new JPopupMenu();

        JMenuItem newSeed = new JMenuItem(
                Messages.getString("NonogramChooserUI.NewSeed"));
        newSeed.addMouseListener(new MouseAdapter() {

            // ask user for seed and set seeded nongram as chosenNonogram
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1
                        && e.getClickCount() == 1) {
                    popup.setVisible(false);
                    performOK();
                }
            }
        });
        newSeed.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                popup.setVisible(false);
                performOK();
            }
        });

        JMenuItem clearSeeds = new JMenuItem(
                Messages.getString("NonogramChooserUI.ClearSeeds"));
        clearSeeds.addMouseListener(new MouseAdapter() {

            // delete all seed thumbnails for solved nonograms from default
            // directory
            @Override
            public void mousePressed(final MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1
                        && e.getClickCount() == 1) {

                    popup.setVisible(false);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                            .getLastSelectedPathComponent();
                    ((CourseFromSeed) node.getUserObject()).clearSeeds();
                    openCourseViewPane();
                }
            }
        });
        clearSeeds.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                popup.setVisible(false);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                        .getLastSelectedPathComponent();
                ((CourseFromSeed) node.getUserObject()).clearSeeds();
                openCourseViewPane();
            }
        });

        popup.add(newSeed);
        popup.add(clearSeeds);

        popup.show(nonogramsTree, point.x, point.y);
    }

    /**
     * Shows a context menu for collections coming from file system at a given
     * point.
     * 
     * @param point
     *            point to show menu at
     */
    private void showFilesystemPopupMenu(final Point point) {

        popup = new JPopupMenu();

        JMenuItem changePath = new JMenuItem("Change path...");
        changePath.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1
                        && e.getClickCount() == 1) {
                    popup.setVisible(false);
                    performOK();
                }
            }
        });
        changePath.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                popup.setVisible(false);
                performOK();
            }
        });

        popup.add(changePath);

        popup.show(nonogramsTree, point.x, point.y);
    }

    /**
     * Analyze which element of the tree was last selected when OK button was
     * pressed. If this element is of NonogramProvider the chosen Nonogram is
     * fetched by the provider and saved as result.
     */
    private void performOK() {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nonogramsTree
                .getLastSelectedPathComponent();

        if (node != null) {

            Object userObject = node.getUserObject();

            // if seed course is chosen, prepare nonogram from user input (seed)
            if (userObject instanceof CourseFromSeed) {

                askForSeed((CourseFromSeed) node.getUserObject());
            }

            // if a collection was chosen, allow user to alter path/server
            // address
            if (userObject instanceof CollectionProvider) {

                if (userObject instanceof CollectionFromFilesystem) {

                    // when it is a collection from file system, use a file
                    // chooser to select a different directory
                    CollectionFromFilesystem collection = ((CollectionFromFilesystem) userObject);

                    final JFileChooser fc = new JFileChooser();
                    fc.setCurrentDirectory(new File(collection.getRootPath()));
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                        File file = fc.getSelectedFile();

                        ((CollectionFromFilesystem) userObject)
                                .changeRootPath(file.getAbsolutePath());

                        nonogramsTreeModel.removeNodeFromParent(node);

                        populateCollection(collection);
                    }

                }
                // else if (userObject instanceof CollectionFromServer) {
                //
                // ((CollectionFromServer) userObject).changeServerURL("");
                // }
            }
        }
    }

    /**
     * Disposes of this dialog when closed or canceled by user.
     */
    private void performClose() {

        dispose();
    }

    /**
     * Asks user for input to generate a random nonogram by seed. The actual
     * generation of a random nonogram happens in a course provider.
     * 
     * @param course
     *            course provider that will generate nonogram from given input.
     */
    private void askForSeed(final CourseFromSeed course) {

        // ask user for seed
        String seed = JOptionPane.showInputDialog(this,
                Messages.getString("NonogramChooserUI.SeedLabel"),
                Messages.getString("NonogramChooserUI.RandomNonogramText"),
                JOptionPane.QUESTION_MESSAGE);

        // generate nonogram from seed and set it as chosenNonogram
        if (seed != null && !seed.isEmpty()) {
            NonogramProvider np = course.generateSeededNonogram(seed);
            chosenNonogram = (NonogramFromSeed) np;
            dispose();
        }
    }

    /**
     * Get NonogramProvider for nonogram that was chosen by user.
     * 
     * @return NonogramProvider if one is chosen, else null
     */
    public final NonogramProvider getChosenNonogram() {

        if (chosenNonogram != null) {
            return chosenNonogram;

        } else {
            if (courseViewPane instanceof CourseViewPane) {
                return ((CourseViewPane) courseViewPane).getChosenNonogram();
            }
        }

        return null;
    }
}
