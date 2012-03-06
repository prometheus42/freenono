/*****************************************************************************
 * FreeNono - A free implementation of the nonogram game
 * Copyright (c) 2010 Markus Wichmann
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
package org.freenono;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.freenono.model.Nonogram;
import org.freenono.serializer.XMLNonogramSerializer;
import org.freenono.ui.MainUI;

import javax.swing.SwingUtilities;


public class RunUI {
	
	private static Logger logger = org.apache.log4j.Logger.getLogger(RunUI.class);
	
	public static void main(String[] args) throws Exception {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainUI ui = new MainUI(); 
				ui.setVisible(true);
			}
		});
	}
}