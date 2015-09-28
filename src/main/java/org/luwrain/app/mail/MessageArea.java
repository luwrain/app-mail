
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class MessageArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private StoredMailMessage message;

    private String from = "";
    private String to = "";
    private String subject = "";
    private String date = "";
    private String[] text = new String[0];

    MessageArea(Luwrain luwrain, Actions actions,
		Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions =  actions;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strings");
    }

    void show(StoredMailMessage message)
    {
	try {
	    this.message = message;
	    from = message.getFrom();
	    to = "";
	    for(String s: message.getTo())
	    {
		if (!to.isEmpty())
		    to += ", ";
		to += s;
	    }
	    subject = message.getSubject();
	    date = message.getSentDate().toString();
	    text = message.getBaseContent().split("\n");
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    this.message = null;
	    to = "";
	    from = "";
	    subject = "";
	    date = "";
	    text = new String[0];
	}
	luwrain.onAreaNewContent(this);
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.TAB:
	    actions.gotoFolders();
	    return true;
	    case KeyboardEvent.BACKSPACE:
	    actions.gotoSummary();
	    return true;
	    case KeyboardEvent.F5://FIXME:Action
		if (message != null)
		return actions.makeReply(message);
		return false;
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	switch (event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.messageAreaName();
    }

    @Override public int getLineCount()
    {
	return 5 + text.length;
    }

    @Override public String getLine(int index)
    {
	switch(index)
	{
	case 0:
	    return "От: " + from;
	case 1:
	    return "Кому: " + to;
	case 2:
	    return "Тема: " + subject;
	case 3:
	    return "Дата: " + date;
	case 4:
	    return "";
	default:
	    return index - 5 < text.length?text[index - 5]:"";
	}
    }
}
