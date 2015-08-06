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

    public MessageArea(Luwrain luwrain,
		       Actions actions,
		       Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions =  actions;
	this.strings = strings;
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
    }

    public void show(StoredMailMessage message)
    {
	try {
	    this.message = message;
	    System.out.println("1");
	    from = message.getFrom();
	    System.out.println("2");
	    to = message.getTo()[0];//FIXME:
	    System.out.println("3");
	    subject = message.getSubject();
	    System.out.println("4");
	    date = message.getSentDate().toString();
	    System.out.println("5");
	    text = message.getBaseContent().split("\n");
	    System.out.println("6");
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
	if (event == null)
	    throw new NullPointerException("event may not be null");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.TAB:
	    actions.gotoFolders();
	    return true;
	    default:
		return super.onKeyboardEvent(event);
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
