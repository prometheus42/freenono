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
package org.freenono.ui.highscore;

import javax.swing.table.AbstractTableModel;


/**
 * Implements a table model for highscore table.
 * 
 * @author Christian Wichmann
 */
public class HighscoreTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 8693248331576638831L;

	private String[][] rowData = { { "Japan", "245" }, { "USA", "240" },
			{ "Italien", "220" }, { "Spanien", "217" },
			{ "Türkei", "215" }, { "England", "214" },
			{ "Frankreich", "190" }, { "Griechenland", "185" },
			{ "Deutschland", "180" }, { "Portugal", "170" } };
	private String[] columnNames = { "Land",
			"Durchschnittliche Sehdauer pro Tag in Minuten" };
	
	@Override
	public int getRowCount() {
		
		return rowData.length;
	}

	@Override
	public int getColumnCount() {
		
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		return rowData[rowIndex][columnIndex];
	}

	@Override
	public String getColumnName(int columnIndex) {

		return columnNames[columnIndex];
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		if (rowIndex == 2 && columnIndex == 0)
			return true;
		else
			return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		//Object oldValue = getValueAt(rowIndex, columnIndex);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		return String.class;
	}

}