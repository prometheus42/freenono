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
package org.freenono.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.plaf.FontUIResource;

import java.awt.ComponentOrientation;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.freenono.RunUI;
import org.freenono.board.BoardPanel;
import org.freenono.board.StatusComponent;
import org.freenono.event.GameAdapter;
import org.freenono.event.ProgramControlEvent;
import org.freenono.event.QuizEvent;
import org.freenono.event.ProgramControlEvent.ProgramControlType;
import org.freenono.event.GameEventHelper;
import org.freenono.event.StateChangeEvent;
import org.freenono.model.game_modes.GameMode_Quiz;
import org.freenono.provider.CollectionProvider;
import org.freenono.provider.NonogramProvider;
import org.freenono.quiz.Question;
import org.freenono.ui.common.AboutDialog2;
import org.freenono.ui.common.FontFactory;
import org.freenono.ui.common.PropertiesLoader;
import org.freenono.ui.common.SplashScreen;
import org.freenono.ui.common.Tools;
import org.freenono.ui.explorer.NonogramChooserUI;
import org.freenono.controller.Manager;
import org.freenono.controller.Settings;

/**
 * Shows the main window for the GUI.
 * 
 * @author Christian Wichmann, Markus Wichmann
 */
public class MainUI extends JFrame {

    private static final long serialVersionUID = 3834029197472615118L;

    private static Logger logger = Logger.getLogger(MainUI.class);

    private GameAdapter gameAdapter = new GameAdapter() {

        @Override
        public void optionsChanged(final ProgramControlEvent e) {

            repaint();
        }

        @Override
        public void stateChanged(final StateChangeEvent e) {

            final boolean isSolved;

            /*
             * Calling of method handleGameEnding is done by the AWT event
             * dispatch thread. It handles the game end by showing game over
             * dialog when possible.
             * 
             * After the event thread has received the Runnable the remaining
             * event listeners waiting for game end are called.
             * 
             * Also all calls on statusBarText are made from the AWT event
             * dispatch thread because you never know where these calls come
             * from.
             */
            switch (e.getNewState()) {
            case GAME_OVER:
                isSolved = false;
                gameRunning = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        handleGameEnding(isSolved);
                    }
                });
                break;

            case SOLVED:
                isSolved = true;
                gameRunning = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        handleGameEnding(isSolved);
                    }
                });
                break;

            case PAUSED:
                gameRunning = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        statusBarText.setText(Messages
                                .getString("MainUI.StatusBarPause"));
                    }
                });
                break;

            case RUNNING:
                gameRunning = true;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        statusBarText.setText(Messages
                                .getString("MainUI.StatusBarRunning"));
                    }
                });
                break;

            case USER_STOP:
                gameRunning = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        statusBarText.setText(Messages
                                .getString("MainUI.StatusBarStopped"));
                        pauseGlassPane.setDoPaint(false);
                    }
                });
                break;

            case NONE:
                break;

            default:
                assert false : e.getNewState();
                break;
            }
        }

        @Override
        public void askQuestion(final QuizEvent e) {

            Question question = e.getQuestion();
            AskQuestionDialog aqd = new AskQuestionDialog(MainUI.this,
                    question, settings.getColorModel());
            centerWindowOnMainScreen(aqd, 0, 0);
            aqd.setVisible(true);

            // set answer to "0" if cancel button was pushed
            String answer = aqd.getAnswer();
            if (answer == null) {
                answer = "0";
            }
            ((GameMode_Quiz) e.getSource()).checkAnswer(question, answer);
        }
    };

    private GameEventHelper eventHelper = null;
    private Settings settings = null;
    private List<CollectionProvider> nonogramProvider = null;
    private NonogramProvider lastChosenNonogram = null;
    private boolean gameRunning = false;
    private boolean windowMinimized = false;

    private Rectangle mainScreenBounds;
    private PauseGlassPane pauseGlassPane;

    private AboutDialog2 aboutDialog;
    private AboutDialog2 helpDialog;

    private JPanel contentPane = null;
    private JToolBar statusBar = null;
    private JMenuItem statusBarText = null;
    private JToolBar toolBar = null;
    private JPanel gameBoardPane = null;
    private BoardPanel boardPanel = null;
    private StatusComponent statusField = null;

    private JButton startButton = null;
    private JButton coopButton = null;
    private JButton pauseButton = null;
    private JButton stopButton = null;
    private JButton restartButton = null;
    private JButton exitButton = null;
    private JButton aboutButton = null;
    private JButton helpButton = null;
    private JButton editButton = null;
    private JButton optionsButton = null;
    private JButton statisticsButton = null;

    /**
     * Is used as glass pane for MainUI and paints when game is paused.
     * 
     * @author Christian Wichmann
     */
    private class PauseGlassPane extends JPanel {

        /*
         * TODO Fade pause glass pane in and out.
         */

        private static final long serialVersionUID = -5807935182594813623L;

        private boolean doPaint = false;

        /**
         * Default constructor.
         */
        protected PauseGlassPane() {

            super(null);

            // set glass pane panel transparent so that buttons are still usable
            // and visible
            setOpaque(false);
        }

        /**
         * Sets whether this component should be painted.
         * 
         * @param doPaint
         *            whether this component should be painted
         */
        protected void setDoPaint(final boolean doPaint) {

            this.doPaint = doPaint;
            setVisible(doPaint);
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics g) {

            if (doPaint) {

                final int x, y, width, height;

                // set coordinates depending on whether the window is in
                // wide screen mode
                if (isWindowWidescreen()) {
                    x = toolBar.getWidth();
                    y = 0;
                    width = getWidth() - toolBar.getWidth();
                    height = getHeight() - statusBar.getHeight();
                } else {
                    x = 0;
                    y = toolBar.getHeight();
                    width = getWidth();
                    height = getHeight() - toolBar.getHeight()
                            - statusBar.getHeight();
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_SPEED);
                g2.setColor(Color.BLACK);
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.5f));
                g2.fillRect(x, y, width, height);

                // paint pause sign
                final int barWidth = 35;
                final int barHeight = 150;
                final int barGap = 30;
                final int barPositionX = (width - barWidth) / 2;
                final int barPositionY = y + height / 2 - barHeight / 2;
                final int arcSize = 30;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(barPositionX - barGap, barPositionY, barWidth,
                        barHeight, arcSize, arcSize);
                g2.fillRoundRect(barPositionX + barGap, barPositionY, barWidth,
                        barHeight, arcSize, arcSize);
                g2.dispose();
            }
        }
    }

    /**
     * Initializes the main graphical user interface of FreeNono.
     * 
     * @param geh
     *            Game event helper to fire and receive events.
     * @param s
     *            Settings object.
     * @param np
     *            List of all available nonogram collections.
     */
    public MainUI(final GameEventHelper geh, final Settings s,
            final List<CollectionProvider> np) {

        super();

        this.eventHelper = geh;
        this.settings = s;
        this.nonogramProvider = np;

        eventHelper.addGameListener(gameAdapter);

        /*
         * Check for Windows Vista or Windows 7 to deactivate double buffering
         * on those systems to prevent rendering artifacts due to Aero
         * interface.
         * 
         * See also: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6873928
         * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6630702
         * https://weblogs
         * .java.net/blog/chet/archive/2006/10/java_on_vista_y.html
         */
        try {
            if (System.getProperty("os.name").contains("Windows")
                    && Double.valueOf(System.getProperty("os.version")) >= 6) {
                RepaintManager.currentManager(this).setDoubleBufferingEnabled(
                        false);
            }
        } catch (NumberFormatException e) {
            logger.warn("Could not parse os version number.");
        }

        setUIOptions();

        initialize();

        addListener();

        addKeyBindings();

        findMainScreen();

        askForPlayerName();

        if (settings.shouldSearchForUpdates()) {
            checkForUpdates();
        }
    }

    /**
     * Checks whether this application can be updated. It downloads the number
     * of the newest version from the FreeNono web page and directs the user to
     * it, if a newer version is available.
     */
    private void checkForUpdates() {

        if (!Tools.isRunningJavaWebStart()) {

            // instantiate PropertiesLoader to get newest version number
            PropertiesLoader pl = new PropertiesLoader(
                    "http://www.freenono.org/freenono.properties");

            final String newestVersion = (String) pl
                    .getValueOfProperty("freenono_newest_version");
            final String currentVersion = RunUI.class.getPackage()
                    .getSpecificationVersion();

            logger.debug("Newest version: " + newestVersion);
            logger.debug("Current version: " + currentVersion);

            if (currentVersion != null && newestVersion != null
                    && !currentVersion.equals(newestVersion)) {

                final YesNoDialog informUserOfUpdateDialog = new YesNoDialog(
                        this,
                        Messages.getString("MainUI.NewUpdateInformationTitle"),
                        settings.getColorModel().getTopColor(), settings
                                .getColorModel().getBottomColor(),
                        Messages.getString("MainUI.NewUpdateInformation"));
                centerWindowOnMainScreen(informUserOfUpdateDialog, 0, 0);
                informUserOfUpdateDialog.setVisible(true);

                if (informUserOfUpdateDialog.userChoseYes()) {

                    // load FreeNono web page into browser for user
                    Desktop desktop = null;
                    if (Desktop.isDesktopSupported()) {
                        desktop = Desktop.getDesktop();
                    }
                    if (desktop != null
                            && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            final String urlOfNewestVersion = (String) pl
                                    .getValueOfProperty("freenono_newest_version_link");
                            if (urlOfNewestVersion != null) {
                                desktop.browse(new URI(urlOfNewestVersion));
                            }
                        } catch (IOException e) {
                            logger.debug("Could not open browser to show FreeNono web page.");
                        } catch (URISyntaxException e) {
                            logger.debug("Could not open browser to show FreeNono web page.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds screen on which MainUI is shown to be available as static member of
     * MainUI <code>mainScreen</code>.
     */
    private void findMainScreen() {

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        int i = 1;
        for (GraphicsDevice screen : gs) {
            if (screen.getDefaultConfiguration().getBounds()
                    .contains(getBounds())) {
                mainScreenBounds = screen.getDefaultConfiguration().getBounds();
            }
            logger.debug("Screen " + (i++) + ": "
                    + screen.getDefaultConfiguration().getBounds());
        }

        logger.debug("Main screen: " + mainScreenBounds);
    }

    /**
     * Moves a window (e.g. a dialog or a frame) to the main screen. Main screen
     * is defined as the screen where the main window is placed.
     * 
     * @param window
     *            window to be moved to main screen
     * @param dx
     *            distance that window should be moved in horizontal direction
     * @param dy
     *            distance that window should be moved in vertical direction
     */
    public final void centerWindowOnMainScreen(final Window window,
            final int dx, final int dy) {

        int newX = mainScreenBounds.x + mainScreenBounds.width / 2;
        int newY = mainScreenBounds.y + mainScreenBounds.height / 2;

        newX -= window.getWidth() / 2;
        newY -= window.getHeight() / 2;

        newX += dx;
        newY += dy;

        window.setLocation(newX, newY);
    }

    /**
     * Asks user for the name he wants to use while playing FreeNono. This name
     * will also be used by the highscore manager.
     */
    private void askForPlayerName() {

        if (settings.shouldAskForPlayerName()) {

            // Build dialog and show it...
            final JDialog askPlayerNameDialog = new JDialog(this);
            askPlayerNameDialog.setModalityType(ModalityType.APPLICATION_MODAL);
            askPlayerNameDialog
                    .setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            askPlayerNameDialog.setResizable(false);
            askPlayerNameDialog.setAlwaysOnTop(true);
            askPlayerNameDialog.setUndecorated(true);
            askPlayerNameDialog.setTitle(Messages
                    .getString("AskPlayerNameDialog.Title"));
            askPlayerNameDialog.getContentPane().setBackground(
                    settings.getColorModel().getTopColor());
            askPlayerNameDialog.getContentPane().setForeground(
                    settings.getColorModel().getBottomColor());
            ((JPanel) askPlayerNameDialog.getContentPane())
                    .setBorder(BorderFactory.createEtchedBorder());

            GridBagLayout layout = new GridBagLayout();
            askPlayerNameDialog.getContentPane().setLayout(layout);
            GridBagConstraints c = new GridBagConstraints();
            final int inset = 10;
            c.insets = new Insets(inset, inset, inset, inset);

            JLabel askPlayerNameLabel = new JLabel(
                    Messages.getString("AskPlayerNameDialog.PlayerNameLabel"));
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            askPlayerNameDialog.add(askPlayerNameLabel, c);

            JTextField askPlayerNameField = new JTextField(
                    settings.getPlayerName());
            c.gridx = 0;
            c.gridy = 1;
            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            askPlayerNameDialog.add(askPlayerNameField, c);

            JCheckBox shouldAskCheckBox = new JCheckBox(
                    Messages.getString("AskPlayerNameDialog.AskEveryTimeLabel"),
                    settings.shouldAskForPlayerName());
            c.gridx = 0;
            c.gridy = 2;
            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            askPlayerNameDialog.add(shouldAskCheckBox, c);

            JButton okButton = new JButton(Messages.getString("OK"));
            c.gridx = 2;
            c.gridy = 2;
            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.NONE;
            askPlayerNameDialog.add(okButton, c);

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    askPlayerNameDialog.setVisible(false);
                }
            });

            askPlayerNameDialog
                    .getRootPane()
                    .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke("ESCAPE"),
                            "QuitPlayerNameDialog");
            askPlayerNameDialog
                    .getRootPane()
                    .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke("ENTER"),
                            "QuitPlayerNameDialog");
            askPlayerNameDialog.getRootPane().getActionMap()
                    .put("QuitPlayerNameDialog", new AbstractAction() {

                        private static final long serialVersionUID = 4941805525864237285L;

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            askPlayerNameDialog.setVisible(false);
                        }
                    });

            askPlayerNameDialog.pack();
            askPlayerNameDialog.setLocationRelativeTo(null);
            askPlayerNameDialog.setVisible(true);

            // ...get answers from dialog and save values in settings.
            settings.setPlayerName(askPlayerNameField.getText());
            settings.setAskForPlayerName(shouldAskCheckBox.isSelected());
        }
    }

    /**
     * Initializes MainUI with its program icon, window sizes, etc. and gets
     * content pane with all components of MainUI.
     */
    private void initialize() {

        final Dimension normalSize = new Dimension(980, 760);
        final Dimension minimumSize = new Dimension(700, 700);

        setSize(normalSize);
        setMinimumSize(minimumSize);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(new ImageIcon(getClass().getResource(
                "/resources/icon/icon_freenono.png")).getImage());
        setLocationRelativeTo(null);
        setName("mainUI");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle(Messages.getString("MainUI.Title"));

        setContentPane(buildContentPane());
        validate();

        // so that MainUI can receive key-events
        setFocusable(true);
        requestFocus();

        pauseGlassPane = new PauseGlassPane();
        setGlassPane(pauseGlassPane);
    }

    /**
     * Sets all fonts of the current look-and-feel to a given font name and font
     * size. Font style for all keys will stay the same.
     * <p>
     * Source for 'setting of all fonts':
     * http://stackoverflow.com/questions/12730230/set-the-same-font-for-
     * all-component-java
     */
    private void setUIOptions() {

        /*
         * Set font for all components.
         */
        Enumeration<Object> keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {

            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) value;
                UIManager.put(key, new FontUIResource(FontFactory
                        .createDefaultFont().deriveFont(orig.getStyle())));
            }
        }

        /*
         * Set background for all panels.
         */
        UIManager.put("Panel.background", settings.getColorModel()
                .getTopColor());
        UIManager.put("RootPane.background", settings.getColorModel()
                .getTopColor());
        UIManager.put("background", settings.getColorModel().getTopColor());
    }

    /**
     * Add listeners for this window to pause game when frame is minimized and
     * handle the exit.
     */
    private void addListener() {

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(final WindowEvent e) {
            }

            @Override
            public void windowIconified(final WindowEvent e) {
                if (gameRunning) {
                    windowMinimized = true;
                    performPause();
                }
            }

            @Override
            public void windowDeiconified(final WindowEvent e) {
                if (windowMinimized) {
                    performPause();
                    windowMinimized = false;
                }
            }

            @Override
            public void windowDeactivated(final WindowEvent e) {
            }

            @Override
            public void windowClosing(final WindowEvent e) {
                performExit();
            }

            @Override
            public void windowClosed(final WindowEvent e) {
            }

            @Override
            public void windowActivated(final WindowEvent e) {
            }
        });

        addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(final ComponentEvent e) {
            }

            @Override
            public void componentResized(final ComponentEvent e) {

                findMainScreen();

                // change layout
                if (isWindowWidescreen()) {
                    contentPane.remove(buildIconsBar());
                    contentPane.add(buildIconsBar(), BorderLayout.WEST);
                } else {
                    contentPane.remove(buildIconsBar());
                    contentPane.add(buildIconsBar(), BorderLayout.NORTH);
                }

                // set orientation according to window size
                if (isWindowWidescreen()) {
                    toolBar.setOrientation(JToolBar.VERTICAL);
                } else {
                    toolBar.setOrientation(JToolBar.HORIZONTAL);
                }

                validate();
                repaint();
            }

            @Override
            public void componentMoved(final ComponentEvent e) {

                findMainScreen();
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
            }
        });
    }

    /**
     * Add key bindings for all control buttons on the top of the window.
     */
    private void addKeyBindings() {

        JComponent rootPane = this.getRootPane();

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F1"), "Start");
        rootPane.getActionMap().put("Start", new AbstractAction() {
            private static final long serialVersionUID = 653149778238948695L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                performStart();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F2"), "Restart");
        rootPane.getActionMap().put("Restart", new AbstractAction() {
            private static final long serialVersionUID = 2909922464716273283L;

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (restartButton.isEnabled()) {
                    performRestart();
                }
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F3"), "Pause");
        rootPane.getActionMap().put("Pause", new AbstractAction() {
            private static final long serialVersionUID = -3429023602787303442L;

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (pauseButton.isEnabled()) {
                    performPause();
                }
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F4"), "Stop");
        rootPane.getActionMap().put("Stop", new AbstractAction() {
            private static final long serialVersionUID = -4991874644955600912L;

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (stopButton.isEnabled()) {
                    performStop();
                }
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F5"), "ShowOptions");
        rootPane.getActionMap().put("ShowOptions", new AbstractAction() {
            private static final long serialVersionUID = 4520522172894740522L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                showOptions();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F6"), "ShowStatistics");
        rootPane.getActionMap().put("ShowStatistics", new AbstractAction() {
            private static final long serialVersionUID = 7842336013574876417L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                showStatistics();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F7"), "ShowHelp");
        rootPane.getActionMap().put("ShowHelp", new AbstractAction() {
            private static final long serialVersionUID = -5662170020301495368L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                showHelp();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F8"), "ShowEdit");
        rootPane.getActionMap().put("ShowEdit", new AbstractAction() {
            private static final long serialVersionUID = 1578736838902924356L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                showEdit();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F9"), "ShowAbout");
        rootPane.getActionMap().put("ShowAbout", new AbstractAction() {
            private static final long serialVersionUID = -5782569581091699423L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                showAbout();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F10"), "Exit");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "Exit");
        rootPane.getActionMap().put("Exit", new AbstractAction() {
            private static final long serialVersionUID = 7710250349322747098L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                performExit();
            }
        });
    }

    /**
     * Builds content pane with all components, like icon bar and status bar.
     * 
     * @return content pane with all components.
     */
    private JPanel buildContentPane() {

        if (contentPane == null) {
            contentPane = new JPanel() {

                private static final long serialVersionUID = -375905655173204523L;

                protected void paintComponent(final Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    BufferedImage cache = null;
                    if (cache == null || cache.getHeight() != getHeight()) {
                        cache = new BufferedImage(2, getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = cache.createGraphics();

                        GradientPaint paint = new GradientPaint(0, 0, settings
                                .getColorModel().getTopColor(), 0, getHeight(),
                                Color.WHITE);
                        g2d.setPaint(paint);
                        g2d.fillRect(0, 0, 2, getHeight());
                        g2d.dispose();
                    }
                    g2.drawImage(cache, 0, 0, getWidth(), getHeight(), null);
                }
            };

            // use GridBagLayout as layout manager
            contentPane.setLayout(new BorderLayout());

            // add tool bar and status bar
            if (isWindowWidescreen()) {
                contentPane.add(buildIconsBar(), BorderLayout.WEST);
            } else {
                contentPane.add(buildIconsBar(), BorderLayout.NORTH);
            }
            contentPane.add(buildStatusBar(), BorderLayout.SOUTH);

            // add dummy panel to later insert game board into
            gameBoardPane = new JPanel();
            gameBoardPane.setLayout(new GridBagLayout());
            gameBoardPane.setOpaque(false);
            contentPane.add(gameBoardPane, BorderLayout.CENTER);
        }
        return contentPane;
    }

    /*
     * ===== Functions providing UI components =====
     */

    /**
     * Builds and initializes the status bar at the bottom of the window.
     * 
     * @return status bar component
     */
    private JToolBar buildStatusBar() {

        if (statusBar == null) {
            statusBar = new JToolBar() {

                private static final long serialVersionUID = -3717090949953624554L;

                @Override
                public void paintComponent(final Graphics g) {
                    g.setColor(settings.getColorModel().getTopColor());
                    g.fillRect(0, 0, getSize().width, getSize().height);
                    super.paintComponent(g);
                }
            };
            statusBar.setOpaque(false);
            statusBar.setBorder(BorderFactory.createEmptyBorder());
            statusBar.setBorderPainted(false);
            statusBar.setFocusable(false);
            statusBar.setFloatable(false);
            statusBar.add(getStatusBarText());
        }
        return statusBar;
    }

    /**
     * Initializes the text label within the status bar.
     * 
     * @return text label for the status bar
     */
    private JMenuItem getStatusBarText() {

        if (statusBarText == null) {
            statusBarText = new JMenuItem();
            statusBarText.setText("FreeNono...");
        }
        return statusBarText;
    }

    /**
     * Instantiates the board panel itself and the status component.
     */
    private void buildBoard() {

        // remove status field and board panel if they are already shown
        if (statusField != null) {
            statusField.removeEventHelper();
        }
        if (boardPanel != null) {
            boardPanel.removeEventHelper();
        }
        gameBoardPane.removeAll();

        // add status field and board panel
        GridBagConstraints constraints = new GridBagConstraints();
        final int insetStatusField = 50;
        constraints.insets = new Insets(insetStatusField, insetStatusField,
                insetStatusField, insetStatusField);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        statusField = new StatusComponent(settings);
        gameBoardPane.add(statusField, constraints);

        final int insetBoardPanel = 5;
        constraints.insets = new Insets(insetBoardPanel, insetBoardPanel,
                insetBoardPanel, insetBoardPanel);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.BOTH;
        boardPanel = new BoardPanel(eventHelper,
                lastChosenNonogram.fetchNonogram(), settings);
        gameBoardPane.add(boardPanel, constraints);

        // validate and layout MainUI ...
        contentPane.validate();

        // ... and let boardPanel do its layout based upon available space
        boardPanel.layoutBoard();

        // set event helper for children
        statusField.setEventHelper(eventHelper);
        boardPanel.setEventHelper(eventHelper);
    }

    /**
     * Initializes the icon bar in the main window including all icons.
     * 
     * @return icon bar with all buttons in it
     */
    private JToolBar buildIconsBar() {

        if (toolBar == null) {
            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setFocusable(false);
            toolBar.setRollover(true);
            // toolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
            toolBar.setBorder(BorderFactory.createEmptyBorder());
            toolBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            toolBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);

            toolBar.add(getStartButton());
            // toolBar.add(getCoopButton());
            toolBar.add(getRestartButton());
            toolBar.add(getPauseButton());
            toolBar.add(getStopButton());
            toolBar.add(getOptionsButton());
            toolBar.add(getStatisticsButton());
            // toolBar.add(getEditButton());
            toolBar.add(getHelpButton());
            toolBar.add(getAboutButton());
            toolBar.add(getExitButton());

            // set orientation according to window size
            if (isWindowWidescreen()) {
                toolBar.setOrientation(JToolBar.VERTICAL);
            } else {
                toolBar.setOrientation(JToolBar.HORIZONTAL);
            }
        }
        return toolBar;
    }

    /**
     * Initializes button to start a game.
     * 
     * @return button to start game
     */
    private JButton getStartButton() {

        if (startButton == null) {
            startButton = new JButton();
            startButton.setText("");
            startButton.setFocusable(false);
            startButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_start.png")));
            startButton.setToolTipText(Messages
                    .getString("MainUI.StartTooltip"));
            startButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_start2.png")));
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    performStart();
                }
            });
        }
        return startButton;
    }

    /**
     * Initializes a button for the coop mode.
     * 
     * @return button for coop mode
     */
    @SuppressWarnings("unused")
    private JButton getCoopButton() {

        if (coopButton == null) {
            coopButton = new JButton();
            coopButton.setText("");
            coopButton.setFocusable(false);
            coopButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_coop.png")));
            coopButton.setToolTipText(Messages.getString("MainUI.CoopTooltip"));
            coopButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_coop2.png")));
            coopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    handleCoop();
                }
            });
        }
        return coopButton;
    }

    /**
     * Initializes button to pause game.
     * 
     * @return Button to pause game.
     */
    private JButton getPauseButton() {
        if (pauseButton == null) {
            pauseButton = new JButton();
            pauseButton.setToolTipText(Messages
                    .getString("MainUI.PauseTooltip"));
            pauseButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_pause.png")));
            pauseButton.setText("");
            pauseButton.setEnabled(false);
            pauseButton.setFocusable(false);
            pauseButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_pause2.png")));
            pauseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    performPause();
                }
            });
        }
        return pauseButton;
    }

    /**
     * Initializes button to stop the game.
     * 
     * @return Button to stop game.
     */
    private JButton getStopButton() {

        if (stopButton == null) {
            stopButton = new JButton();
            stopButton.setToolTipText(Messages.getString("MainUI.StopTooltip"));
            stopButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_stop.png")));
            stopButton.setText("");
            stopButton.setEnabled(false);
            stopButton.setFocusable(false);
            stopButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_stop2.png")));
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    performStop();
                }
            });
        }
        return stopButton;
    }

    /**
     * Initializes a button to restart the game.
     * 
     * @return Button to restart game.
     */
    private JButton getRestartButton() {

        if (restartButton == null) {
            restartButton = new JButton();
            restartButton.setToolTipText(Messages
                    .getString("MainUI.RestartTooltip"));
            restartButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_restart.png")));
            restartButton.setText("");
            restartButton.setEnabled(false);
            restartButton.setFocusable(false);
            restartButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_restart2.png")));
            restartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    performRestart();
                }
            });
        }
        return restartButton;
    }

    /**
     * Initializes a button to exit the game.
     * 
     * @return Button to exit game.
     */
    private JButton getExitButton() {

        if (exitButton == null) {
            exitButton = new JButton();
            exitButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_exit.png")));
            exitButton.setEnabled(true);
            exitButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_exit2.png")));
            exitButton.setText("");
            exitButton.setFocusable(false);
            exitButton.setToolTipText(Messages.getString("MainUI.ExitTooltip"));
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    performExit();
                }
            });
        }
        return exitButton;
    }

    /**
     * Initializes a button for showing an about box.
     * 
     * @return Button showing an about box.
     */
    private JButton getAboutButton() {

        if (aboutButton == null) {
            aboutButton = new JButton();
            aboutButton.setEnabled(true);
            aboutButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_about2.png")));
            aboutButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_about.png")));
            aboutButton.setText("");
            aboutButton.setFocusable(false);
            aboutButton.setComponentOrientation(ComponentOrientation.UNKNOWN);
            aboutButton.setToolTipText(Messages
                    .getString("MainUI.AboutTooltip"));
            aboutButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    showAbout();
                }
            });
        }
        return aboutButton;
    }

    /**
     * Initializes a button to show the options dialog.
     * 
     * @return Button that shows the options dialog.
     */
    private JButton getOptionsButton() {

        if (optionsButton == null) {
            optionsButton = new JButton();
            optionsButton.setComponentOrientation(ComponentOrientation.UNKNOWN);
            optionsButton.setToolTipText(Messages
                    .getString("MainUI.OptionsTooltip"));
            optionsButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_config2.png")));
            optionsButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_config.png")));
            optionsButton.setText("");
            optionsButton.setEnabled(true);
            optionsButton.setFocusable(false);
            optionsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    showOptions();
                }
            });
        }
        return optionsButton;
    }

    /**
     * Initializes a button to get a help dialog.
     * 
     * @return Button for help dialog.
     */
    private JButton getHelpButton() {

        if (helpButton == null) {
            helpButton = new JButton();
            helpButton.setComponentOrientation(ComponentOrientation.UNKNOWN);
            helpButton.setToolTipText(Messages.getString("MainUI.HelpTooltip"));
            helpButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_help2.png")));
            helpButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_help.png")));
            helpButton.setText("");
            helpButton.setEnabled(true);
            helpButton.setFocusable(false);
            helpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    showHelp();
                }
            });
        }
        return helpButton;
    }

    /**
     * Initializes button to call editor.
     * 
     * @return Button to call editor.
     */
    @SuppressWarnings("unused")
    private JButton getEditButton() {

        if (editButton == null) {
            editButton = new JButton();
            editButton.setComponentOrientation(ComponentOrientation.UNKNOWN);
            editButton.setToolTipText(Messages.getString("MainUI.EditTooltip"));
            editButton.setDisabledIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_edit2.png")));
            editButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_edit.png")));
            editButton.setText("");
            editButton.setEnabled(false);
            editButton.setFocusable(false);
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    showEdit();
                }
            });
        }
        return editButton;
    }

    /**
     * Initializes button to call a statistics window.
     * 
     * @return Button to call statistics window.
     */
    private JButton getStatisticsButton() {

        if (statisticsButton == null) {
            statisticsButton = new JButton();
            statisticsButton
                    .setComponentOrientation(ComponentOrientation.UNKNOWN);
            statisticsButton.setToolTipText(Messages
                    .getString("MainUI.StatisticsTooltip"));
            statisticsButton.setDisabledIcon(new ImageIcon(getClass()
                    .getResource("/resources/icon/button_statistics2.png")));
            statisticsButton.setIcon(new ImageIcon(getClass().getResource(
                    "/resources/icon/button_statistics.png")));
            statisticsButton.setText("");
            statisticsButton.setFocusable(false);
            statisticsButton.setEnabled(false);
            statisticsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    showStatistics();
                }
            });
        }
        return statisticsButton;
    }

    /*
     * ===== Functions controlling the game flow =====
     */

    /**
     * Performs a start of a new game.
     */
    private void performStart() {

        NonogramProvider newlyChosenNonogram = null;
        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        // set busy mouse cursor
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        // get NonogramChooserUI and show it
        NonogramChooserUI nonoChooser = new NonogramChooserUI(this,
                nonogramProvider, settings.getColorModel());
        centerWindowOnMainScreen(nonoChooser, 0, 0);
        nonoChooser.setVisible(true);
        newlyChosenNonogram = nonoChooser.getChosenNonogram();
        nonoChooser.dispose();
        // NonogramExplorer nexp = new NonogramExplorer(nonogramProvider,
        // settings.getColorModel()); nexp.setVisible(true);
        // newlyChosenNonogram = nexp.getChosenNonogram();
        // nexp.dispose();

        // reset mouse cursor
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        if (lastChosenNonogram == null && newlyChosenNonogram == null) {

            /*
             * If no nonogram was played before and no nonogram was chosen,
             * disable all buttons when no new nonogram was selected.
             */

            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            restartButton.setEnabled(false);

        } else if (newlyChosenNonogram != null) {

            /*
             * Or if some new nonogram was chosen save it and start the new
             * game.
             */

            performStop();

            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);
            statisticsButton.setEnabled(true);

            lastChosenNonogram = newlyChosenNonogram;
            logger.debug("Nonogram chosen by user: " + newlyChosenNonogram);

            buildBoard();

            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.NONOGRAM_CHOSEN, lastChosenNonogram
                            .fetchNonogram()));

            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.START_GAME, lastChosenNonogram
                            .fetchNonogram()));
        } else {

            /*
             * If no new nonogram was chosen and an old nonogram was set, resume
             * game if it was paused when clicking the button.
             */

            if (resumeAfter) {
                performPause();
            }
        }
    }

    /**
     * Performs a restart of the last played nonogram.
     */
    private void performRestart() {

        performStop();

        if (lastChosenNonogram != null) {
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);

            buildBoard();

            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.RESTART_GAME, lastChosenNonogram
                            .fetchNonogram()));
        }
    }

    /**
     * Performs a pause of running game.
     */
    private void performPause() {

        if (gameRunning) {
            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.PAUSE_GAME));

            setPauseButtonToResume();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pauseGlassPane.setDoPaint(true);
                }
            });

        } else {
            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.RESUME_GAME));

            setPauseButtonToPause();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pauseGlassPane.setDoPaint(false);
                }
            });
        }
    }

    /**
     * Changes icon of pause button to pause.
     */
    private void setPauseButtonToPause() {

        pauseButton.setIcon(new ImageIcon(getClass().getResource(
                "/resources/icon/button_pause.png")));
        pauseButton.setToolTipText(Messages.getString("MainUI.PauseTooltip"));
    }

    /**
     * Changes icon of pause button to resume.
     */
    private void setPauseButtonToResume() {

        pauseButton.setIcon(new ImageIcon(getClass().getResource(
                "/resources/icon/button_resume.png")));
        pauseButton.setToolTipText(Messages.getString("MainUI.ResumeTooltip"));
    }

    /**
     * Stops the running game.
     */
    private void performStop() {

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.STOP_GAME));

        setPauseButtonToPause();

        pauseButton.setEnabled(false);
        restartButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /**
     * Exits the program.
     */
    private void performExit() {

        boolean doExit = true;

        if (gameRunning) {
            YesNoDialog askExit = new YesNoDialog(this,
                    Messages.getString("MainUI.QuestionQuitProgramTitle"),
                    settings.getColorModel().getTopColor(), settings
                            .getColorModel().getBottomColor(),
                    Messages.getString("MainUI.QuestionQuitProgram"));
            centerWindowOnMainScreen(askExit, 0, 0);
            askExit.setVisible(true);
            doExit = askExit.userChoseYes();
        }

        if (doExit) {
            eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                    ProgramControlType.QUIT_PROGRAMM));

            setVisible(false);
            dispose();
        }
    }

    /**
     * Handle coop mode and show coop dialog.
     */
    private void handleCoop() {

        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        CoopStartDialog csd = new CoopStartDialog(this, settings);
        centerWindowOnMainScreen(csd, 0, 0);
        csd.setVisible(true);

        if (resumeAfter) {
            performPause();
        }
    }

    /*
     * Functions providing organizational and statistical dialogs
     */

    /**
     * Shows a about box.
     */
    private void showAbout() {

        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.SHOW_ABOUT));

        // set path to about dialog and build it
        if (aboutDialog == null) {
            logger.debug("Building about dialog.");

            URL pathToText = null, pathToIcon = null;
            String path = "/about/about_" + Locale.getDefault().getLanguage()
                    + ".html";
            pathToText = getClass().getResource(path);

            if (pathToText == null) {
                pathToText = getClass().getResource("/about/about_en.html");
            }

            // set path to FreeNono icon
            pathToIcon = getClass().getResource(
                    "/resources/icon/icon_freenono_big.png");

            if (pathToIcon != null && pathToText != null) {
                aboutDialog = new AboutDialog2(
                        Messages.getString("MainUI.Title"), RunUI.class
                                .getPackage().getImplementationVersion(),
                        pathToText, pathToIcon, settings.getColorModel()
                                .getTopColor());
            }
        }

        if (aboutDialog != null) {
            centerWindowOnMainScreen(aboutDialog, 0, 0);
            aboutDialog.setVisible(true);
        }

        if (resumeAfter) {
            performPause();
        }
    }

    /**
     * Shows a splash screen for a given time.
     * 
     * @param timerDelay
     *            Time to show splash screen for.
     */
    @SuppressWarnings("unused")
    private void showSplashscreen(final int timerDelay) {

        // show splash screen
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SplashScreen splash = new SplashScreen(timerDelay);
                splash.setVisible(true);
            }
        });
    }

    /**
     * Opens current nonogram in editor.
     */
    private void showEdit() {

        if (lastChosenNonogram != null) {
            logger.debug("Open editor frame with nonogram: "
                    + lastChosenNonogram.fetchNonogram().getOriginPath());
        }

        try {
            Object editor = Class.forName("or.freenono.editor.EditorFrame")
                    .newInstance();
            ((JComponent) editor).setVisible(true);

        } catch (ClassNotFoundException e) {
            logger.warn("FreeNonoEditor can not be opened.");
        } catch (InstantiationException e) {
            logger.warn("FreeNonoEditor can not be opened.");
        } catch (IllegalAccessException e) {
            logger.warn("FreeNonoEditor can not be opened.");
        }
        // TODO Hand over nonogram to edit to FNE.
    }

    /**
     * Shows a statistic window.
     */
    private void showStatistics() {

        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        StatisticsViewDialog svd = new StatisticsViewDialog(this, settings);
        centerWindowOnMainScreen(svd, 0, 0);
        svd.setVisible(true);

        if (resumeAfter) {
            performPause();
        }
    }

    /**
     * Shows a help dialog.
     */
    private void showHelp() {

        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.SHOW_ABOUT));

        // set path to about dialog and build it
        if (helpDialog == null) {
            logger.debug("Building help dialog.");

            URL pathToText = null;
            String path = "/help/help_" + Locale.getDefault().getLanguage()
                    + ".html";
            pathToText = getClass().getResource(path);

            if (pathToText == null) {
                pathToText = getClass().getResource("/help/help_en.html");
            }

            if (pathToText != null) {
                helpDialog = new AboutDialog2(
                        Messages.getString("HelpDialog.Help"), null,
                        pathToText, null, settings.getColorModel()
                                .getTopColor());
            }
        }

        if (helpDialog != null) {
            centerWindowOnMainScreen(helpDialog, 0, 0);
            helpDialog.setVisible(true);
        }

        if (resumeAfter) {
            performPause();
        }
    }

    /**
     * Shows the options dialog.
     */
    private void showOptions() {

        boolean resumeAfter = false;

        if (gameRunning) {
            performPause();
            resumeAfter = true;
        }

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.SHOW_OPTIONS));
        OptionsUI optionsDialog = new OptionsUI(this, settings);
        centerWindowOnMainScreen(optionsDialog, 0, 0);
        optionsDialog.setVisible(true);

        if (optionsDialog.isProgramRestartNecessary()) {

            /*
             * Check if restart of FreeNono is necessary.
             */
            YesNoDialog askRestart = new YesNoDialog(this,
                    Messages.getString("MainUI.RestartProgramQuestionTitle"),
                    settings.getColorModel().getTopColor(), settings
                            .getColorModel().getBottomColor(),
                    Messages.getString("MainUI.RestartProgramQuestion"));
            centerWindowOnMainScreen(askRestart, 0, 0);
            askRestart.setVisible(true);

            if (askRestart.userChoseYes()) {
                // TODO Use event RESTART_PROGRAM to restart FreeNono
                // automatically.
                eventHelper.fireProgramControlEvent(new ProgramControlEvent(
                        this, ProgramControlType.QUIT_PROGRAMM));

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setVisible(false);
                        dispose();
                    }
                });
            }

        } else if (optionsDialog.isGameRestartNecessary() && resumeAfter) {

            /*
             * Check if restart of running game is necessary, if game was paused
             * stop and restart it...
             */
            YesNoDialog askRestart = new YesNoDialog(
                    this,
                    Messages.getString("MainUI.RestartRunningGameQuestionTitle"),
                    settings.getColorModel().getTopColor(), settings
                            .getColorModel().getBottomColor(), Messages
                            .getString("MainUI.RestartRunningGameQuestion"));
            centerWindowOnMainScreen(askRestart, 0, 0);
            askRestart.setVisible(true);

            if (askRestart.userChoseYes()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        performRestart();
                    }
                });
            }
        }

        if (resumeAfter) {
            performPause();
        }
    }

    /**
     * Starts a new game when player chose next nonogram pattern in GameOverUI.
     * 
     * @param nextNonogramToPlay
     *            nonogram that should be played next
     */
    private void performStartFromDialog(
            final NonogramProvider nextNonogramToPlay) {

        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
        restartButton.setEnabled(true);
        statisticsButton.setEnabled(true);

        lastChosenNonogram = nextNonogramToPlay;
        logger.debug("Nonogram chosen by user: " + nextNonogramToPlay);

        buildBoard();

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.NONOGRAM_CHOSEN, lastChosenNonogram
                        .fetchNonogram()));

        eventHelper.fireProgramControlEvent(new ProgramControlEvent(this,
                ProgramControlType.START_GAME, lastChosenNonogram
                        .fetchNonogram()));
    }

    /**
     * Handles the game ending by setting status bar text and showing a game end
     * dialog.
     * 
     * @param isSolved
     *            Whether the game was won or lost.
     */
    private void handleGameEnding(final boolean isSolved) {

        // set text for status bar
        if (isSolved) {
            statusBarText.setText(Messages.getString("MainUI.StatusBarWon"));
        } else {
            statusBarText.setText(Messages.getString("MainUI.StatusBarLost"));
        }

        pauseGlassPane.setDoPaint(false);

        // set buttons
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);

        // get previewImage and save it as file
        if (isSolved) {
            saveThumbnail(boardPanel.getPreviewImage());
        }

        // show GameOver dialog
        final GameOverUI gameOverDialog = new GameOverUI(MainUI.this,
                lastChosenNonogram, isSolved, settings);
        gameOverDialog.setVisible(true);

        // start new game if user chose new nonogram
        final NonogramProvider nextNonogram = gameOverDialog
                .getNextNonogramToPlay();
        if (nextNonogram != null) {
            logger.debug("Next nonogram from game over dialog: " + nextNonogram);
            performStartFromDialog(nextNonogram);
        }
    }

    /**
     * Checks whether the main window is in widescreen mode, meaning the aspect
     * ratio is larger than 4:3.
     * 
     * @return true, if window is widescreen
     */
    private boolean isWindowWidescreen() {

        final Dimension currentSize = getSize();
        final double aspectRatio = ((double) currentSize.width / (double) currentSize.height);

        logger.debug("Aspect ratio of main window is " + aspectRatio);

        return aspectRatio > 1.4;
    }

    /**
     * Save preview of currently played nonogram as thumbnail on disk.
     * 
     * @param preview
     *            Preview of current nonogram.
     */
    private void saveThumbnail(final BufferedImage preview) {

        /*
         * TODO Move creation and deletion of preview thumbnails to new
         * PreviewManager class.
         */

        File thumbDir = new File(Manager.DEFAULT_THUMBNAILS_PATH);

        if (!thumbDir.exists()) {
            thumbDir.mkdirs();
        }

        File thumbFile = new File(thumbDir, lastChosenNonogram.fetchNonogram()
                .getHash());

        if (!thumbFile.exists()) {
            try {
                ImageIO.write((RenderedImage) preview, "png", thumbFile);

            } catch (IOException e) {
                logger.warn("Could not write preview image to file "
                        + thumbFile);
            }

            logger.info("Preview image written to file " + thumbFile);
        }
    }

    /**
     * Removes own game listeners and all listeners from children.
     */
    public final void removeEventHelper() {

        if (eventHelper != null) {
            eventHelper.removeGameListener(gameAdapter);
        }

        if (statusField != null) {
            statusField.removeEventHelper();
        }

        if (boardPanel != null) {
            boardPanel.removeEventHelper();
        }

        this.eventHelper = null;
    }
}