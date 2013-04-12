/*****************************************************************************
 * Nonotector - Detector to import nonograms from scanned images
 * Copyright (c) 2013 Christian Wichmann
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
package org.freenono.nonotector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.freenono.model.DifficultyLevel;
import org.freenono.model.Nonogram;
import org.freenono.serializer.XMLNonogramSerializer;
import org.freenono.serializer.ZipCourseSerializer;


public class NonogramStore {

	private static String creator;
	private static String description;
	private static DifficultyLevel difficulty = DifficultyLevel.undefined;
	
	private static List<Nonogram> nonograms = new ArrayList<Nonogram>();
	
	private static XMLNonogramSerializer xmlNonogramSerializer = new XMLNonogramSerializer();
	private static ZipCourseSerializer zipCourseSerializer = new ZipCourseSerializer();
	
	
	public NonogramStore() {
		
		// TODO load properties from file when instantiated and save them at the end!
	}
	
	public static void addNonogram(String title, boolean[][] data) {
		
		Nonogram n = new Nonogram(title, getDifficulty(), data);
		
		n.setAuthor(getCreator());
		
		nonograms.add(n);
	}
	
	public static void saveNonogramsToFile(File destinationDirectory) {

		File currentFile;
		
		try {
			for (Nonogram nonogram : nonograms) {
				
				// TODO strip name and check for invalid chars 
				currentFile = new File(destinationDirectory, nonogram.getName()+".nonogram");
				xmlNonogramSerializer.save(currentFile, nonogram);
			}

		} catch (NullPointerException e) {

		} catch (IOException e) {
			
		}
	}

	public static String getCreator() {
		
		return creator;
	}

	public static void setCreator(String creator) {
		
		NonogramStore.creator = creator;
	}

	public static String getDescription() {
		
		return description;
	}

	public static void setDescription(String description) {
		
		NonogramStore.description = description;
	}

	public static DifficultyLevel getDifficulty() {
		
		return difficulty;
	}

	public static void setDifficulty(DifficultyLevel difficulty) {
		
		NonogramStore.difficulty = difficulty;
	}
}
