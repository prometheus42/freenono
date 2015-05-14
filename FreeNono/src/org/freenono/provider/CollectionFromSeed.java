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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.freenono.provider.NonogramFromSeed.RandomTypes;
import org.freenono.ui.common.Tools;

/**
 * Provides a collection of random nonograms generated by seeds from user.
 *
 * @author Christian Wichmann
 */
public class CollectionFromSeed implements CollectionProvider, Iterable<CourseProvider> {

    private String providerName = null;
    private List<String> courseList = null;
    private List<CourseProvider> courseProviderList = null;

    public static final String DEFAULT_SEEDS_FILE = System.getProperty("user.home") + Tools.FILE_SEPARATOR + ".FreeNono"
            + Tools.FILE_SEPARATOR + "seeds.xml";

    /**
     * Initializes a collection containing only nonograms that are randomly generated. The nonograms
     * can come from a file that stores all random nonogram seeds ever used or can be generated
     * after user inputs a seed.
     *
     * @param name
     *            name of this provider
     */
    public CollectionFromSeed(final String name) {

        this.providerName = name;

        courseProviderList = new ArrayList<CourseProvider>();
        courseProviderList.add(new CourseFromSeed(DEFAULT_SEEDS_FILE, RandomTypes.FULLRANDOM));
        courseProviderList.add(new CourseFromSeed(DEFAULT_SEEDS_FILE, RandomTypes.RANDOMWAYS));
        courseProviderList.add(new CourseFromSeed(DEFAULT_SEEDS_FILE, RandomTypes.CIRCLES));
        courseList = new ArrayList<String>();
        courseList.add("FullRandom");
        courseList.add("RandomWays");
        courseList.add("Circles");
    }

    @Override
    public final List<String> getCourseList() {

        return courseList;
    }

    @Override
    public final String getProviderName() {

        return providerName;
    }

    @Override
    public final List<CourseProvider> getCourseProvider() {

        return Collections.unmodifiableList(courseProviderList);
    }

    @Override
    public final void setProviderName(final String name) {

        this.providerName = name;
    }

    @Override
    public final String toString() {

        return providerName;
    }

    @Override
    public final int getNumberOfNonograms() {

        int n = 0;

        for (final CourseProvider cp : courseProviderList) {

            n += cp.getNumberOfNonograms();
        }

        return n;
    }

    @Override
    public final Iterator<CourseProvider> iterator() {

        return Collections.unmodifiableList(courseProviderList).iterator();
    }

    @Override
    public final void addCollectionListener(final CollectionListener l) {

        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public final void removeCollectionListener(final CollectionListener l) {

        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
