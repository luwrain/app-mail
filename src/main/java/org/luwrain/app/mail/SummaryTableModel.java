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
import org.luwrain.pim.email.*;

class SummaryTableModel implements TableModel
{
    private EmailStoring storing;
    private StoredEmailFolder mailFolder;
    private StoredEmailMessage[] messages;//null value with existing group means invalid state, empty content should be a valid array with zero length;

    public SummaryTableModel(EmailStoring storing)
    {
	this.storing = storing;
	if (storing == null)
	    throw new NullPointerException("storing may not be null");
    }

    public void setCurrentMailFolder(StoredEmailFolder mailFolder)
    {
	this.mailFolder = mailFolder;
    }

    public boolean isValidState()
    {
	return mailFolder  == null || messages != null;
    }

    public int getRowCount()
    {
	return messages != null?messages.length:0;
    }

    public int getColCount()
    {
	return 3;
    }

    @Override public Object getCell(int col, int row)
    {
	if (messages == null || row >= messages.length)
	    return null;
	return "FIXME:";//messages[row].getSubject();//FIXME:
    }

    public Object getRow(int index)
    {
	if (messages == null || index >= messages.length)
	    return null;
	return messages[index];
    }

    public Object getCol(int index)
    {
	return "Column";//FIXME:
    }

    public void refresh()
    {
	if (storing == null || mailFolder == null)
	{
	    messages = null;
	    return;
	}
	try {
	    //FIXME:	    messages = mailStoring.loadMessagesFromGroup(mailFolder);
	    messages = new StoredEmailMessage[0];
	}
	catch(Exception e)
	{
	    //	    Log.error("mail", "loading messages from group" + mailFolder.getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    messages = null;
	}
    }
}
