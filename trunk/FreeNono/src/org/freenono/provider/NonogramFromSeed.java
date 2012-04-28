/*****************************************************************************
 * FreeNono - A free implementation of the nonogram game
 * Copyright (c) 2012 Christian Wichmann, Martin Wichmann
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
import org.freenono.interfaces.NonogramProvider;
import org.freenono.model.DifficultyLevel;
import org.freenono.model.Nonogram;

public class NonogramFromSeed implements NonogramProvider {

	private static Logger logger = Logger.getLogger(NonogramFromSeed.class);

	private int height = 5;
	private int width = 5;
	private Random rng = null;
	private static int ranNonoCounter = 1;

	/*
	 * Add new types here
	 */
	public enum RandomTypes {
		RANDOM, HALFNHALF, FULLRANDOM, RANDOMWAYS
	}

	private Nonogram currentNonogram = null;
	private String seed = "";

	@Override
	public Nonogram fetchNonogram() {
		return generateNonogramBySeed();
	}

	@Override
	public String getName() {
		return "Seed -> " + seed;
	}

	@Override
	public String getDescription() {
		return "Seed -> " + seed;
	}

	@Override
	public DifficultyLevel getDifficulty() {
		return DifficultyLevel.undefined;
	}

	@Override
	public int width() {
		return fetchNonogram().width();
	}

	@Override
	public int height() {
		return fetchNonogram().height();
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
			bytesOfMessage = seed.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
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
		int height = (bigintdigest.intValue() % 19) + 4;
		int width = (bigintdigest.intValue() % 19) + 4;

		// ..in the constructing of a new Nonogram!
		rng = new Random(seedValue);
		currentNonogram = createRandomNonogram(height, width,
				RandomTypes.FULLRANDOM);
		return currentNonogram;
	}

	/**
	 * @param seed
	 *            the seed to set
	 */
	public Nonogram plantSeed(String seed) {
		this.seed = seed;
		return generateNonogramBySeed();
	}

	public String toString() {
		return "Seed -> " + seed;
	}

	/**
	 * createRandomNonogram
	 * 
	 * @param height
	 *            Height of the to generated nonogram
	 * @param width
	 *            Width of the to generated nonogram
	 * @param type
	 *            Type of the random nonogram. Type 0 uses a random type.
	 * @return Nonogram, if one could be generated, else null.
	 */
	private Nonogram createRandomNonogram(int height, int width,
			RandomTypes type) {

		if (height <= 0) {
			this.height = 5;
		} else {
			this.height = height;
		}
		if (width <= 0) {
			width = 5;
		} else {
			this.width = width;
		}

		if (type == RandomTypes.RANDOM) {
			int tmp = RandomTypes.values().length;
			do {
				type = RandomTypes.values()[rng.nextInt(tmp)];
			} while (type == RandomTypes.RANDOM);
		}

		Nonogram ret = null;

		/*
		 * Add new types here
		 */
		switch (type) {
		case HALFNHALF:
			ret = halfnhalf();
			break;
		case FULLRANDOM:
			ret = fullRandomNono();
			break;
		case RANDOMWAYS:
			ret = randomWays();
			break;
		default:
			ret = fullRandomNono();
			break;
		}

		ranNonoCounter++;

		return ret;
	}

	/**
	 * Generates random nonogram with one half marked
	 * 
	 * @return Nonogram, if generated, else null
	 */
	private Nonogram halfnhalf() {

		String id = "";
		String name = "random " + ranNonoCounter;
		String desc = "";
		DifficultyLevel difficulty = DifficultyLevel.undefined;

		boolean field[][] = new boolean[height][width];

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
			}

		}

		Nonogram ret = null;
		try {
			ret = new Nonogram(name, desc, difficulty, field);
		} catch (NullPointerException e) {
			// e.printStackTrace(); // should not occur, since we use it correct
			// ;-)
		}

		return ret;
	}

	private Nonogram fullRandomNono() {
		String id = "";
		String name = "random " + ranNonoCounter;
		String desc = "";
		DifficultyLevel difficulty = DifficultyLevel.undefined;

		boolean field[][] = new boolean[height][width];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				field[j][i] = (rng.nextInt(2) == 0) ? true : false;
			}
		}

		// One field should at least be true, so the nonogram isn't empty
		field[rng.nextInt(height)][rng.nextInt(width)] = true;

		Nonogram ret = null;
		try {
			ret = new Nonogram(name, desc, difficulty, field);
		} catch (NullPointerException e) {
			// e.printStackTrace(); // should not occur, since we use it correct
			// ;-)
		}

		return ret;
	}

	private Nonogram randomWays() {
		String id = "";
		String name = "random " + ranNonoCounter;
		String desc = "";
		DifficultyLevel difficulty = DifficultyLevel.undefined;

		boolean field[][] = new boolean[height][width];

		int endCounter = (int) Math.ceil((height * width) / 5);
		// int endCounter = 5;
		int counter = 0;
		int hMark = rng.nextInt(height);
		int wMark = rng.nextInt(width);

		while (counter <= endCounter) {
			if (field[hMark][wMark] != true) {
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
			ret = new Nonogram(name, desc, difficulty, field);
		} catch (NullPointerException e) {
			// e.printStackTrace(); // should not occur, since we use it correct
			// ;-)
		}

		return ret;
	}

	private int mod(int x, int y) {
		int result = x % y;
		if (result < 0) {
			result += y;
		}
		return result;
	}
}
