/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.network.*;

class SummaryTableAppearance implements TableArea.Appearance
{
    private final Luwrain luwrain;
    private final Strings strings;

    SummaryTableAppearance(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    @Override public void announceRow(TableArea.Model model,
				      int index, int flags)
    {
	NullCheck.notNull(model, "model");
	if (index < 0 || index >= model.getRowCount())
	    return;
	final Object obj = model.getRow(index);
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return;
	final StoredMailMessage message = (StoredMailMessage)obj;
	final String line;
	try {
	    final String prefix;
	    final MailMessage.State state = message.getState();
	    switch(state)
	    {
	    case READ:
		prefix = strings.readPrefix();
		break;
	    case MARKED:
		prefix = strings.markedPrefix();
		break;
	    case DELETED:
		prefix = strings.deletedPrefix();
		break;
	    default:
		prefix = "";
	    }
	    line = prefix + " " + MailUtils.extractNameFromAddr(message.getFrom()) + ":" + message.getSubject() + " " + luwrain.i18n().getPastTimeBrief(message.getSentDate());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return;
	}
	luwrain.playSound(Sounds.LIST_ITEM);
	luwrain.say(line);
    }

    @Override public int getInitialHotPointX(TableArea.Model model)
    {
	return 2;
    }

    @Override public String getCellText(TableArea.Model model, int col, int row)
    {
	NullCheck.notNull(model, "model");
	final Object cell = model.getCell(col, row);
	NullCheck.notNull(cell, "cell");
	return cell.toString();
    }

    @Override public String getRowPrefix(TableArea.Model model, int index)
    {
	return "  ";//FIXME:
    }

    @Override public int getColWidth(TableArea.Model model, int  colIndex)
    {
	return 15;
    }
}
