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
package org.freenono.provider;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.freenono.model.data.DifficultyLevel;
import org.freenono.model.data.Nonogram;

/**
 * Provides a random nonogram based on a given seed.
 * 
 * @author Christian Wichmann, Martin Wichmann
 */
public class NonogramFromSeed implements NonogramProvider {

    private static Logger logger = Logger.getLogger(NonogramFromSeed.class);

    private static final int MIN_HEIGHT = 5;
    private static final int MAX_HEIGHT = 25;
    private static final int MIN_WIDTH = 5;
    private static final int MAX_WIDTH = 25;
    private int height = MIN_HEIGHT;
    private int width = MIN_WIDTH;

    private Random rng = null;
    private static int ranNonoCounter = 1;

    /**
     * Types of random nonograms.
     * 
     * @author Martin Wichmann
     */
    public enum RandomTypes {
        RANDOM, HALFNHALF, FULLRANDOM, RANDOMWAYS
    }

    private Nonogram currentNonogram = null;
    private NonogramProvider nextNonogram = null;
    private NonogramProvider previousNonogram = null;
    private CourseFromSeed course = null;
    private String seed = "";

    /**
     * Initializes a new nonogram from a given seed.
     * 
     * @param seed
     *            Seed to generate new random nonogram.
     * @param c
     *            Course which contains this nonogram.
     */
    public NonogramFromSeed(final String seed, final CourseFromSeed c) {

        plantSeed(seed);
        course = c;
    }

    @Override
    public final Nonogram fetchNonogram() {

        return generateNonogramBySeed();
    }

    @Override
    public final String getName() {

        return seed;
    }

    @Override
    public final String getDescription() {

        return "";
    }

    @Override
    public final DifficultyLevel getDifficulty() {

        return DifficultyLevel.UNDEFINED;
    }

    @Override
    public final String getAuthor() {

        return fetchNonogram().getAuthor();
    }

    @Override
    public final long getDuration() {

        return fetchNonogram().getDuration();
    }

    @Override
    public final int width() {
        return fetchNonogram().width();
    }

    @Override
    public final int height() {

        return fetchNonogram().height();
    }

    @Override
    public final String toString() {

        return getName();
    }

    /**
     * Plant seed to generate a new random nonogram. The given seed is saved in
     * the nonogramProvider and the new nonogram is generated.
     * 
     * @param seed
     *            the seed with which to generate a new random nonogram
     * @return Nonogram generated by given seed.
     */
    public final Nonogram plantSeed(final String seed) {

        this.seed = seed;
        return generateNonogramBySeed();
    }

    /**
     * Generates a new Nonogram by calculating a hash from the given text. The
     * width and height of the new nonogram is calculated by moduloing the
     * hashed value and the seed value for the random number generator results
     * from the first 64 bit of the hash.
     * 
     * @return Nonogram generated by seed to play
     */
    private Nonogram generateNonogramBySeed() {

        // get the text input by the user...
        byte[] bytesOfMessage = null;

        try {

            // TODO check if UTF-8 is the correct encoding to set?!
            bytesOfMessage = seed.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e1) {

            logger.warn("Seed input by user is not correctly encoded. UTF-8 expected!");
        }

        // ...digest byte array to hash...
        MessageDigest md = null;
        String hashFunction = "MD5";
        try {
            md = MessageDigest.getInstance(hashFunction);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Hash function " + hashFunction
                    + " not available on this system.");
        }
        byte[] thedigest = md.digest(bytesOfMessage);

        BigInteger bigintdigest = new BigInteger(thedigest);

        // ...generate long from byte array to use...
        long seedValue = bigintdigest.longValue();
        height = (bigintdigest.intValue() % MAX_HEIGHT) + MIN_HEIGHT;
        width = (bigintdigest.intValue() % MAX_WIDTH) + MIN_WIDTH;

        // ..in the constructing of a new Nonogram!
        rng = new Random(seedValue);
        currentNonogram = createRandomNonogram(RandomTypes.FULLRANDOM);
        return currentNonogram;
    }

    /**
     * Creates a random nonogram.
     * 
     * @param type
     *            Type of the random nonogram. Type 0 uses a random type.
     * @return Nonogram, if one could be generated, else null.
     */
    private Nonogram createRandomNonogram(final RandomTypes type) {

        RandomTypes randomType = type;

        if (height < MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }

        if (width < MIN_WIDTH) {
            width = MIN_WIDTH;
        }

        if (randomType == RandomTypes.RANDOM) {

            int tmp = RandomTypes.values().length;

            do {

                randomType = RandomTypes.values()[rng.nextInt(tmp)];

            } while (randomType == RandomTypes.RANDOM);
        }

        Nonogram n = null;

        /*
         * Add new types here
         */
        switch (randomType) {
        case HALFNHALF:
            n = halfnhalf();
            break;
        case FULLRANDOM:
            n = fullRandomNono();
            break;
        case RANDOMWAYS:
            n = randomWays();
            break;
        default:
            n = fullRandomNono();
            break;
        }

        ranNonoCounter++;

        return n;
    }

    /**
     * Generates random nonogram with one half marked.
     * 
     * @return Nonogram, if generated, else null
     */
    private Nonogram halfnhalf() {

        String name = "random " + ranNonoCounter;
        String desc = "";
        DifficultyLevel difficulty = DifficultyLevel.UNDEFINED;

        boolean[][] field = new boolean[height][width];

        int options = rng.nextInt(4);

        if (width == 1 || height == 1) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    field[j][i] = true;
                }
            }
        } else {
            switch (options) {
            case 0:
                for (int i = 0; i < Math.floor((width / 2)); i++) {
                    for (int j = 0; j < height; j++) {
                        field[j][i] = true;
                    }
                }
                break;
            case 1:
                for (int i = (int) Math.floor((width / 2)); i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        field[j][i] = true;
                    }
                }
                break;
            case 2:
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < (int) Math.floor((height / 2)); j++) {
                        field[j][i] = true;
                    }
                }
                break;
            case 3:
                for (int i = 0; i < width; i++) {
                    for (int j = (int) Math.floor((height / 2)); j < height; j++) {
                        field[j][i] = true;
                    }
                }
                break;
            default:
                break;
            }

        }

        Nonogram ret = null;
        try {

            ret = new Nonogram(name, difficulty, field);
            ret.setDescription(desc);
            ret.setAuthor(System.getProperty("user.name"));
            // TODO Use game-wide player name as author!

        } catch (NullPointerException e) {

            logger.debug("Could not generate random nonogram (halfnhalf).");
        }

        return ret;
    }

    /**
     * Generates a fully random nonogram.
     * 
     * @return Randomly generated nonogram.
     */
    private Nonogram fullRandomNono() {

        String name = getName();
        String desc = getDescription();
        DifficultyLevel difficulty = DifficultyLevel.UNDEFINED;

        boolean[][] field = new boolean[height][width];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                field[j][i] = (rng.nextInt(2) == 0) ? true : false;
            }
        }

        // One field should at least be true, so the nonogram isn't empty
        field[rng.nextInt(height)][rng.nextInt(width)] = true;

        Nonogram ret = null;
        try {

            ret = new Nonogram(name, difficulty, field);
            ret.setDescription(desc);

        } catch (NullPointerException e) {

            logger.debug("Could not generate random nonogram (fullRandomNono).");
        }

        return ret;
    }

    /**
     * Generates a nonogram which is based on random ways through it.
     * 
     * @return Randomly generated nonogram.
     */
    private Nonogram randomWays() {

        String name = "random " + ranNonoCounter;
        String desc = "";
        DifficultyLevel difficulty = DifficultyLevel.UNDEFINED;

        boolean[][] field = new boolean[height][width];

        int endCounter = (int) Math.ceil((height * width) / 5);
        // int endCounter = 5;
        int counter = 0;
        int hMark = rng.nextInt(height);
        int wMark = rng.nextInt(width);

        while (counter <= endCounter) {
            if (!(field[hMark][wMark])) {
                field[hMark][wMark] = true;
                counter++;
            }

            int decisionCoin = rng.nextInt(5);

            switch (decisionCoin) {
            case 0: // left
                wMark = mod((wMark - 1), width);
                break;
            case 1: // right
                wMark = (wMark + 1) % width;
                break;
            case 2: // up
                hMark = mod((hMark - 1), height);
                break;
            case 3: // down
                hMark = (hMark + 1) % height;
                break;
            case 4: // new start
                hMark = rng.nextInt(height);
                wMark = rng.nextInt(width);
                break;
            default:
                break;
            }

        }

        Nonogram ret = null;
        try {

            ret = new Nonogram(name, difficulty, field);
            ret.setDescription(desc);

        } catch (NullPointerException e) {

            logger.debug("Could not generate random nonogram (randomWays).");
        }

        return ret;
    }

    /**
     * Calculates the modulo function.
     * 
     * @param x
     *            Parameter x.
     * @param y
     *            Parameter y.
     * @return Result from modulo operation.
     */
    private int mod(final int x, final int y) {

        int result = x % y;
        if (result < 0) {
            result += y;
        }
        return result;
    }

    @Override
    public final NonogramProvider getNextNonogram() {

        if (nextNonogram == null) {

            nextNonogram = course.getNextNonogram(this);
        }
        return nextNonogram;
    }

    @Override
    public final NonogramProvider getPreviousNonogram() {

        if (previousNonogram == null) {

            previousNonogram = course.getPreviousNonogram(this);
        }
        return previousNonogram;
    }
}
