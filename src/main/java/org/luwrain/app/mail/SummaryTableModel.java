
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.network.*;

class SummaryTableModel implements TableArea.Model
{
    private final Luwrain luwrain;
    private final Strings strings;
    private StoredMailMessage[] messages;//null value with existing group means invalid state, empty content should be a valid array with zero length;

    SummaryTableModel(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    void setMessages(StoredMailMessage[] messages)
    {
	this.messages = messages;
    }

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
	if (messages == null || 
	    row < 0 || row >= messages.length)
	    return null;
	final StoredMailMessage message = messages[row];
	try {
	    switch (col)
	    {
	    case 0:
		return MailUtils.extractNameFromAddr(message.getFrom());
	    case 1:
		return message.getSubject();
	    case 2:
		return message.getSentDate();//FIXME:
	    default:
		return "#InvalidColumn " + col + "!#";
	    }
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return "#StoringError:" + e.getMessage() + "#";
	}
    }

    @Override public Object getRow(int index)
    {
	return (messages != null && index < messages.length)?messages[index]:null;
    }

    @Override public Object getCol(int index)
    {
	switch(index)
	{
	case 0:
	    return strings.columnFrom();
	case 1:
	    return strings.columnSubject();
	case 2:
	    return strings.columnSentDate();
	default:
	    return "";
	}
    }

    @Override public void refresh()
    {
	//We have a custom handler of refresh event in the corresponding area;
    }
}
