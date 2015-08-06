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
import org.luwrain.pim.mail.*;

class SummaryTableModel implements TableModel
{
    private StoredMailMessage[] messages;//null value with existing group means invalid state, empty content should be a valid array with zero length;

    public SummaryTableModel()
    {
    }

    public void setMessages(StoredMailMessage[] messages)
    {
	this.messages = messages;
    }

    /*
    public boolean isValidState()
    {
	return mailFolder  == null || messages != null;
    }
    */

    @Override public int getRowCount()
    {
	return messages != null?messages.length:0;
    }

    @Override public int getColCount()
    {
	return 3;
    }

    @Override public Object getCell(int col, int row)
    {
	if (messages == null || row >= messages.length)
	    return null;
	final StoredMailMessage message = messages[row];
	try {
	    switch (col)
	    {
	    case 0:
		return message.getFrom();
	    case 1:
		return message.getSubject();
	    case 2:
		return message.getSentDate();
	    default:
		return "#InvalidColumn " + col + "!#";
	    }
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return "#StoringError!#";
	}
    }

    @Override public Object getRow(int index)
    {
	return (messages != null && index < messages.length)?messages[index]:null;
    }

    @Override public Object getCol(int index)
    {
	return "Column";//FIXME:
    }

    @Override public void refresh()
    {
	//We have a custom handler of refresh event in the corresponding area;
    }
}
