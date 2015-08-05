/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;

class SummaryTableAppearance implements TableAppearance
{
    @Override public void introduceRow(TableModel model,
			     int index,
			     int flags)
    {
	if (model == null || index >= model.getRowCount())
	    return;
	/*
	Object obj = model.getRow(index);
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return;
	StoredMailMessage message = (StoredMailMessage)obj;
	Speech.say(message.getSubject());
	*/
    }

    @Override public int getInitialHotPointX(TableModel model)
    {
	return 2;
    }

    @Override public String getCellText(TableModel model,
		       int col,
		       int row)
    {
	if (model == null)
	    return "#NO MODEL#";
	/*
	Object cell = model.getCell(col, row);
	return cell != null?cell.toString():"";
	*/
	return null;
    }

    @Override public String getRowPrefix(TableModel model, int index)
    {
	return "  ";
    }

    @Override public int getColWidth(TableModel model, int  colIndex)
    {
	return 20;
    }
}
