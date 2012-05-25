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
package org.freenono.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.freenono.interfaces.CourseProvider;
import org.freenono.interfaces.NonogramProvider;
import org.freenono.model.Course;
import org.freenono.model.Seed;
import org.freenono.model.Seeds;
import org.freenono.serializer.XMLSeedsSerializer;
import org.freenono.ui.Messages;
import org.restlet.Message;

/**
 * Default course for nonogram by seed. This Course provides only one random
 * nonogram generated by the given seed.
 * 
 */
public class CourseFromSeed implements CourseProvider {

	private static Logger logger = Logger.getLogger(CourseFromSeed.class);

	private List<NonogramProvider> nonogramProviderList = null;
	private String seedFile = null;
	private Seeds seedList = null;

	private XMLSeedsSerializer xmlSeedsSerializer = new XMLSeedsSerializer();

	
	public CourseFromSeed(String seedFile) {

		this.seedFile = seedFile;

		loadSeeds();
	}

	
	private void loadSeeds() {

		try {

			seedList = xmlSeedsSerializer.load(new File(seedFile));
			
		} catch (NullPointerException e) {

			logger.warn("NullPointerException when loading seeds file.");

		} catch (IOException e) {

			logger.warn("IOException when loading seeds file.");
		}

		if (seedList == null) {

			logger.warn("Seeds could not be loaded from file!");
		}

		// create nonogramProvider for all loaded seeds in seedList
		nonogramProviderList = new ArrayList<NonogramProvider>();

		if (seedList != null) {
			
			for (int i = 0; i < seedList.getNumberOfSeeds(); i++) {

				nonogramProviderList.add(new NonogramFromSeed(seedList.get(i)
						.getSeedString()));
			}
			
		} else {
			
			seedList = new Seeds();
		}
	}
	
	private void saveSeeds() {
		
		try {
			
			xmlSeedsSerializer.save(seedList, new File(seedFile));
			
		} catch (NullPointerException e) {
			
			logger.error("Could not save seeds in XML file.");
			
		} catch (IOException e) {
			
			logger.error("Could not save seeds in XML file.");
		}
	}

	
	@Override
	public List<String> getNonogramList() {

		List<String> nonogramList = new ArrayList<String>();
		
		for (int i = 0; i < seedList.getNumberOfSeeds(); i++) {
			
			nonogramList.add(seedList.get(i).getSeedString());
		}
		
		return nonogramList;
	}

	@Override
	public List<NonogramProvider> getNonogramProvider() {

		return nonogramProviderList;
	}

	@Override
	public Course fetchCourse() {

		// TODO generate default course class with embedded nonograms.
		return null;
	}

	@Override
	public String getCourseName() {

		return Messages.getString("NonogramChooserUI.NonogramBySeedText");
	}

	
	public NonogramFromSeed generateSeededNonogram(String seed) {
	
		// add new seed to seed list and save list in xml file
		seedList.addSeed(new Seed(seed, Calendar.getInstance()));
		saveSeeds();
		
		// instantiate new nonogramProvider for new seed and add it to list
		NonogramFromSeed tmp = new NonogramFromSeed(seed);
		nonogramProviderList.add(tmp);
		
		return tmp;
	}
	
	
	public String toString() {

		return getCourseName();
	}

}
