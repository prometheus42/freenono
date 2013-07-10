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
package org.freenono.controller;

import java.awt.Color;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.freenono.event.GameEventHelper;
import org.freenono.event.ProgramControlEvent;
import org.freenono.event.ProgramControlEvent.ProgramControlType;
import org.freenono.model.game_modes.GameModeType;
import org.freenono.ui.colormodel.ColorModel;
import org.freenono.ui.colormodel.ColorModelAnalogous;
import org.freenono.controller.ControlSettings.Control;

/**
 * Stores all settings and provides getter and setter for them. For all settings
 * a default is defined.
 * 
 * @author Christian Wichmann, Markus Wichmann, Martin Wichmann
 */
public class Settings {

    /*
     * To add a new option:
     * 
     * 1) add field and default constant,
     * 
     * 2) implement getter and setter equal to the existing options,
     * 
     * 3) add option to resetSettings() method.
     * 
     * 4) add option to copy constructor.
     */

    private static Logger logger = Logger.getLogger(Settings.class);

    private GameEventHelper eventHelper;
    private ControlSettings controlSettings;
    private ColorModel currentColorModel;

    private static final boolean USE_MAX_FAIL_COUNT_DEFAULT = true;
    private boolean useMaxFailCount = true;

    private static final int MAX_FAIL_COUNT_DEFAULT = 5;
    private int maxFailCount = MAX_FAIL_COUNT_DEFAULT;

    private static final boolean USE_MAX_TIME_DEFAULT = true;
    private boolean useMaxTime = true;

    private static final long MAX_TIME_DEFAULT = 1800000;
    private long maxTime = MAX_TIME_DEFAULT;

    private static final boolean MARK_INVALID_DEFAULT = true;
    private boolean markInvalid = MARK_INVALID_DEFAULT;

    private static final boolean COUNT_MARKED_DEFAULT = false;
    private boolean countMarked = COUNT_MARKED_DEFAULT;

    private static final boolean PLAY_AUDIO_DEFAULT = false;
    private boolean playAudio = PLAY_AUDIO_DEFAULT;

    private static final boolean PLAY_MUSIC_DEFAULT = false;
    private boolean playMusic = PLAY_MUSIC_DEFAULT;

    private static final boolean PLAY_EFFECTS_DEFAULT = false;
    private boolean playEffects = PLAY_EFFECTS_DEFAULT;

    private static final boolean HIDE_PLAYFIELD_DEFAULT = true;
    private boolean hidePlayfield = HIDE_PLAYFIELD_DEFAULT;

    private static final boolean CROSS_CAPTIONS_DEFAULT = true;
    private boolean crossCaptions = CROSS_CAPTIONS_DEFAULT;

    private static final boolean MARK_COMPLETE_ROWS_COLUMNS_DEFAULT = true;
    private boolean markCompleteRowsColumns = MARK_COMPLETE_ROWS_COLUMNS_DEFAULT;

    private static final boolean SHOW_NONOGRAM_NAME_DEFAULT = false;
    private boolean showNonogramName = SHOW_NONOGRAM_NAME_DEFAULT;

    private static final GameModeType GAME_MODE_DEFAULT = GameModeType.PENALTY;
    private GameModeType gameMode = GAME_MODE_DEFAULT;

    private static final Color BASE_COLOR_DEFAULT = new Color(255, 255, 102);
    private Color baseColor = BASE_COLOR_DEFAULT;

    private static final Locale GAME_LOCALE_DEFAULT = Locale.ROOT;
    private Locale gameLocale = GAME_LOCALE_DEFAULT;

    /**
     * Default constructor building a <code>Settings</code> object with default
     * values.
     */
    public Settings() {

        currentColorModel = new ColorModelAnalogous(baseColor);
        controlSettings = new ControlSettings();
    }

    /**
     * Constructs a new copy of a given <code>Settings</code> object with all
     * values from the existing object. Only the game event helper will
     * <strong>not</strong> be copied, because only <strong>one</strong>
     * instance should be allowed to fire options changed events.
     * 
     * @param oldSettings
     *            settings to be copied into the new object.
     */
    public Settings(final Settings oldSettings) {

        /*
         * Set all options from old settings object. Because many classes
         * (String, Integer, Color, Locale) are immutable they don't have to be
         * copied themselves!
         */
        setBaseColor(oldSettings.getBaseColor());
        setCountMarked(oldSettings.getCountMarked());
        setCrossCaptions(oldSettings.getCrossCaptions());
        setGameLocale(oldSettings.getGameLocale());
        setGameMode(oldSettings.getGameMode());
        setHidePlayfield(oldSettings.getHidePlayfield());
        setMarkCompleteRowsColumns(oldSettings.getMarkCompleteRowsColumns());
        setMarkInvalid(oldSettings.getMarkInvalid());
        setMaxFailCount(oldSettings.getMaxFailCount());
        setMaxTime(oldSettings.getMaxTime());
        setPlayAudio(oldSettings.getPlayAudio());
        setPlayEffects(oldSettings.isPlayEffects());
        setPlayMusic(oldSettings.isPlayMusic());
        setShowNonogramName(oldSettings.isShowNonogramName());
        setUseMaxFailCount(oldSettings.getUseMaxFailCount());
        setUseMaxTime(oldSettings.getUseMaxTime());

        controlSettings = new ControlSettings(oldSettings.getControlSettings());
    }

    /**
     * Reset settings to defaults.
     */
    public final void resetSettings() {

        logger.debug("Resetting settings to default.");

        setBaseColor(BASE_COLOR_DEFAULT);
        setCountMarked(COUNT_MARKED_DEFAULT);
        setCrossCaptions(CROSS_CAPTIONS_DEFAULT);
        setGameLocale(GAME_LOCALE_DEFAULT);
        setGameMode(GAME_MODE_DEFAULT);
        setHidePlayfield(HIDE_PLAYFIELD_DEFAULT);
        setMarkCompleteRowsColumns(MARK_COMPLETE_ROWS_COLUMNS_DEFAULT);
        setMarkInvalid(MARK_INVALID_DEFAULT);
        setMaxFailCount(MAX_FAIL_COUNT_DEFAULT);
        setMaxTime(MAX_TIME_DEFAULT);
        setPlayAudio(PLAY_AUDIO_DEFAULT);
        setPlayEffects(PLAY_EFFECTS_DEFAULT);
        setPlayMusic(PLAY_MUSIC_DEFAULT);
        setShowNonogramName(SHOW_NONOGRAM_NAME_DEFAULT);
        setUseMaxFailCount(USE_MAX_FAIL_COUNT_DEFAULT);
        setUseMaxTime(USE_MAX_TIME_DEFAULT);
    }

    /**
     * Getter max fail count.
     * @return maxFailCount
     */
    public final int getMaxFailCount() {
        return maxFailCount;
    }

    /**
     * Setter max fail count.
     * @param maxFailCount
     *            Max fail count
     */
    public final void setMaxFailCount(final int maxFailCount) {

        if (this.maxFailCount != maxFailCount) {
            this.maxFailCount = maxFailCount;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter use max time.
     * @return Use max time
     */
    @Deprecated
    public final boolean getUseMaxTime() {
        return useMaxTime;
    }

    /**
     * Setter use max time.
     * @param useMaxTime
     *            Use max time
     */
    @Deprecated
    public final void setUseMaxTime(final boolean useMaxTime) {

        if (this.useMaxTime != useMaxTime) {
            this.useMaxTime = useMaxTime;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter max time.
     * @return Max time
     */
    public final long getMaxTime() {

        return maxTime;
    }

    /**
     * Setter max time.
     * @param maxTime
     *            Max time
     */
    public final void setMaxTime(final long maxTime) {

        if (this.maxTime != maxTime) {
            this.maxTime = maxTime;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter use max fail count.
     * @return Use max fail count.
     */
    @Deprecated
    public final boolean getUseMaxFailCount() {

        return useMaxFailCount;
    }

    /**
     * Setter use max fail count.
     * @param useMaxFailCount
     *            Use max fail count
     */
    @Deprecated
    public final void setUseMaxFailCount(final boolean useMaxFailCount) {

        if (this.useMaxFailCount != useMaxFailCount) {
            this.useMaxFailCount = useMaxFailCount;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter mark invalid.
     * @return Mark invalid.
     */
    public final boolean getMarkInvalid() {

        return markInvalid;
    }

    /**
     * Setter mark invalid.
     * @param markInvalid
     *            Mark invalid
     */
    public final void setMarkInvalid(final boolean markInvalid) {

        if (this.markInvalid != markInvalid) {
            this.markInvalid = markInvalid;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter count marked.
     * @return Count marked
     */
    @Deprecated
    public final boolean getCountMarked() {

        return countMarked;
    }

    /**
     * Setter count marked.
     * @param countMarked
     *            Count marked
     */
    @Deprecated
    public final void setCountMarked(final boolean countMarked) {

        if (this.countMarked != countMarked) {
            this.countMarked = countMarked;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter play audio.
     * @return Play audio
     */
    @Deprecated
    public final boolean getPlayAudio() {

        return playAudio;
    }

    /**
     * Setter play audio.
     * @param playAudio
     *            Play audio
     */
    @Deprecated
    public final void setPlayAudio(final boolean playAudio) {

        if (this.playAudio != playAudio) {
            this.playAudio = playAudio;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets if music should be played.
     * @return True, if music should be played.
     */
    public final boolean isPlayMusic() {

        return playMusic;
    }

    /**
     * Sets whether music should be played.
     * @param playMusic
     *            If music should be played.
     */
    public final void setPlayMusic(final boolean playMusic) {

        if (this.playMusic != playMusic) {
            this.playMusic = playMusic;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets if sound effects should be played.
     * @return True, if sound effects should be played.
     */
    public final boolean isPlayEffects() {

        return playEffects;
    }

    /**
     * Sets whether sound effects should be played.
     * @param playEffects
     *            If sound effects should be played.
     */
    public final void setPlayEffects(final boolean playEffects) {

        if (this.playEffects != playEffects) {
            this.playEffects = playEffects;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }

    }

    /**
     * Gets whether to hide the play field while playing.
     * 
     * @return true, if play field should be hidden
     */
    public final boolean getHidePlayfield() {

        return hidePlayfield;
    }

    /**
     * Sets whether to hide the play field while playing.
     * 
     * @param hidePlayfield
     *            whether to hide play field
     */
    public final void setHidePlayfield(final boolean hidePlayfield) {

        if (this.hidePlayfield != hidePlayfield) {
            this.hidePlayfield = hidePlayfield;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets if caption hints should be crossed out while playing.
     * 
     * @return true, if caption hints should be crossed out
     */
    public final boolean getCrossCaptions() {

        return crossCaptions;
    }

    /**
     * Sets if caption hints should be crossed out while playing.
     * 
     * @param crossCaptions
     *            if caption hints should be crossed out
     * 
     */
    public final void setCrossCaptions(final boolean crossCaptions) {

        if (this.crossCaptions != crossCaptions) {
            this.crossCaptions = crossCaptions;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets if complete rows and columns on the board should be marked.
     * 
     * @return true, if complete rows and columns should be marked
     */
    public final boolean getMarkCompleteRowsColumns() {

        return markCompleteRowsColumns;
    }

    /**
     * Sets if complete rows and columns on the board should be marked.
     * 
     * @param markCompleteRowsColumns
     *            if complete rows and columns should be marked
     * 
     */
    public final void setMarkCompleteRowsColumns(
            final boolean markCompleteRowsColumns) {

        if (this.markCompleteRowsColumns != markCompleteRowsColumns) {
            this.markCompleteRowsColumns = markCompleteRowsColumns;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Getter show nonogram name.
     * @return Show nonogram name
     */
    public final boolean isShowNonogramName() {

        return showNonogramName;
    }

    /**
     * Setter show nonogram name.
     * @param showNonogramName
     *            Show nonogram name
     */
    public final void setShowNonogramName(final boolean showNonogramName) {

        if (this.showNonogramName != showNonogramName) {

            this.showNonogramName = showNonogramName;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * @return Current gameMode of the game.
     */
    public final GameModeType getGameMode() {

        return gameMode;
    }

    /**
     * @param gameMode
     *            The chosen gameMode to be stored.
     */
    public final void setGameMode(final GameModeType gameMode) {

        if (this.gameMode != gameMode) {
            this.gameMode = gameMode;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets key code for specific control like "left" or "mark".
     * 
     * @param ct
     *            control defining a specific function in the game
     * 
     * @return key code for given control
     * @see ControlSettings
     */
    public final Integer getKeyCodeForControl(final Control ct) {

        return controlSettings.getControl(ct);
    }

    /**
     * Sets event helper for firing events.
     * 
     * @param eventHelper
     *            game event helper
     */
    public final void setEventHelper(final GameEventHelper eventHelper) {

        this.eventHelper = eventHelper;

        controlSettings.setEventHelper(eventHelper);
    }

    /**
     * Gets control settings.
     * 
     * @return Control settings
     */
    public final ControlSettings getControlSettings() {

        return controlSettings;
    }

    /**
     * Gets base color from which all other colors are derived.
     * 
     * @return base color
     */
    public final Color getBaseColor() {

        return baseColor;
    }

    /**
     * Sets base color from which all other colors are derived.
     * 
     * @param baseColor
     *            base color to be set
     */
    public final void setBaseColor(final Color baseColor) {

        if (this.baseColor != baseColor) {

            this.baseColor = baseColor;
            this.currentColorModel = new ColorModelAnalogous(baseColor);

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

    /**
     * Gets a color model containing different colors derived from a given base
     * color.
     * 
     * @return color model including all colors that should be used.
     */
    public final ColorModel getColorModel() {

        return currentColorModel;
    }

    /**
     * Gets game locale.
     * 
     * @return game locale
     */
    public final Locale getGameLocale() {

        return gameLocale;
    }

    /**
     * Sets game locale.
     * 
     * @param gameLocale
     *            game locale
     */
    public final void setGameLocale(final Locale gameLocale) {

        if (this.gameLocale != gameLocale) {
            this.gameLocale = gameLocale;

            if (eventHelper != null) {
                eventHelper.fireOptionsChangedEvent(new ProgramControlEvent(
                        this, ProgramControlType.OPTIONS_CHANGED));
            }
        }
    }

}
