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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * Factory class for vending standard <code>Font</code> objects. Wherever
 * possible, this factory will hand out references to shared <code>Font</code>
 * instances.
 * <p>
 * Create methods return a reference to a shared <code>Font</code> object which
 * is ok because Font is immutable. The given object can then be adjusted by the
 * <code>deriveFont()</code> method.
 * 
 * @author Christian Wichmann
 */
public final class FontFactory {

    private static Logger logger = Logger.getLogger(FontFactory.class);

    private static String europeBaseFont;
    private static String japaneseBaseFont;
    private static String baseFont;

    /**
     * Initializes the base font that will be used as basis for all other
     * specific fonts.
     */
    static {

        // register shipped fonts
        registerFonts();

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        ge.preferLocaleFonts();

        // set base font depending on locale
        europeBaseFont = "Linux Biolinum";

        for (String s : ge.getAvailableFontFamilyNames()) {

            if ("梅UIゴシック".equals(s)) {
                japaneseBaseFont = "梅UIゴシック";
                break;
            }
            japaneseBaseFont = "MS UI Gothic";
        }

        // set font depending on locale
        if (Locale.getDefault().equals(Locale.JAPANESE)) {
            baseFont = japaneseBaseFont;

        } else {
            baseFont = europeBaseFont;
        }
    }

    private static final int LCD_SIZE = 28;
    private static final String LCD_FONT = "LCDMono2";
    private static final int LCD_STYLE = Font.PLAIN;

    private static final int TEXT_SIZE = 14;
    private static final String TEXT_FONT = baseFont;
    private static final int TEXT_STYLE = Font.PLAIN;

    private static final int SPLASHSCREEN_SIZE = 14;
    private static final String SPLASHSCREEN_FONT = baseFont;
    private static final int SPLASHSCREEN_STYLE = Font.PLAIN;

    private static final int TILE_SIZE = 10;
    private static final String TILE_FONT = baseFont;
    private static final int TILE_STYLE = Font.PLAIN;

    private static final String ABOUT_NAME_FONT = baseFont;
    private static final int ABOUT_NAME_STYLE = Font.BOLD;
    private static final int ABOUT_NAME_SIZE = 24;

    private static final String ABOUT_VERSION_FONT = baseFont;
    private static final int ABOUT_VERSION_STYLE = Font.ITALIC;
    private static final int ABOUT_VERSION_SIZE = 16;

    private static Font sharedLcdFont;
    private static Font sharedTextFont;
    private static Font sharedDefaultFont;
    private static Font sharedSplashscreenFont;
    private static Font sharedTileFont;
    private static Font sharedAboutNameFont;
    private static Font sharedAboutVersionFont;

    static {

        sharedLcdFont = new Font(LCD_FONT, LCD_STYLE, LCD_SIZE);
        sharedTextFont = new Font(TEXT_FONT, TEXT_STYLE, TEXT_SIZE);
        sharedDefaultFont = new Font(TEXT_FONT, TEXT_STYLE, TEXT_SIZE);
        sharedSplashscreenFont = new Font(SPLASHSCREEN_FONT,
                SPLASHSCREEN_STYLE, SPLASHSCREEN_SIZE);
        sharedTileFont = new Font(TILE_FONT, TILE_STYLE, TILE_SIZE);
        sharedAboutNameFont = new Font(ABOUT_NAME_FONT, ABOUT_NAME_STYLE,
                ABOUT_NAME_SIZE);
        sharedAboutVersionFont = new Font(ABOUT_VERSION_FONT,
                ABOUT_VERSION_STYLE, ABOUT_VERSION_SIZE);
    }

    /**
     * Don't let anyone instantiate this class. Private constructor initializing
     * all necessary fonts.
     */
    private FontFactory() {

    }

    /**
     * Register all fonts included in FreeNono to be used in the frames and
     * dialogs.
     */
    private static void registerFonts() {

        // add new font
        try {
            Font fontLcd = Font
                    .createFont(
                            Font.TRUETYPE_FONT,
                            FontFactory.class
                                    .getResourceAsStream("/resources/fonts/LCDMono.TTF")); //$NON-NLS-1$

            Font fontDefault = Font
                    .createFont(
                            Font.TRUETYPE_FONT,
                            FontFactory.class
                                    .getResourceAsStream("/resources/fonts/LinuxBiolinum.ttf")); //$NON-NLS-1$

            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
                    fontLcd);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
                    fontDefault);

        } catch (FontFormatException e) {

            logger.error("Unable to load font file because of a wrong font file format!");

        } catch (IOException e) {

            logger.error("Could not load font file from filesystem.");
        }
    }

    /**
     * Creates a lcd like font for displaying game information like game time or
     * fail count.
     * 
     * @return lcd like font
     */
    public static Font createLcdFont() {

        return sharedLcdFont;
    }

    /**
     * Creates a default font for all other purposes.
     * 
     * @return default font
     */
    public static Font createDefaultFont() {

        return sharedDefaultFont;
    }

    /**
     * Creates a font for displaying textual information, e.g. in dialog boxes
     * etc.
     * 
     * @return text font
     */
    public static Font createTextFont() {

        return sharedTextFont;
    }

    /**
     * Creates a font for use in the splash screen.
     * 
     * @return splash screen font
     */
    public static Font createSplashscreenFont() {

        return sharedSplashscreenFont;
    }

    /**
     * Creates a font for use in the splash screen.
     * 
     * @param size
     *            size font should have
     * @return splash screen font
     */
    public static Font createTileFont(final int size) {

        return sharedTileFont.deriveFont((float) size);
    }

    /**
     * Creates a font for use in the about box.
     * 
     * @return about dialog font
     */
    public static Font createAboutNameFont() {

        return sharedAboutNameFont;
    }

    /**
     * Creates a font for use in the about box.
     * 
     * @return about dialog font
     */
    public static Font createAboutVersionFont() {

        return sharedAboutVersionFont;
    }
}
