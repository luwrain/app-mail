/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class RawMessageArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private StoredMailMessage message;
    private String[] content = new String[0];

    RawMessageArea(Luwrain luwrain, Actions actions,
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

    boolean show(StoredMailMessage message)
    {
	if (message == null)
	{
	    this.message = null;
	    content = new String[0];
	luwrain.onAreaNewContent(this);
	    return true;
	}
	try {
	    final String str = new String(message.getRawMail(), "US-ASCII");
	    System.out.println("strlen " + str.length());
	content = str.split("\n");
	this.message = message;
	luwrain.onAreaNewContent(this);
	return true;
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    content = new String[0];
	    this.message = null;
	luwrain.onAreaNewContent(this);
	    return false;
	}
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.F9:
		actions.launchMailFetch();
		return true;
	    case KeyboardEvent.TAB:
	    actions.gotoFolders();
	    return true;
	    case KeyboardEvent.BACKSPACE:
	    actions.gotoSummary();
	    return true;
	    case KeyboardEvent.F5://FIXME:Action
		    return actions.makeReply(null, false);
	    case KeyboardEvent.F6://FIXME:Action
		return actions.makeForward(null);
	    }
	if (event.isCommand() && event.withShiftOnly())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.F5://FIXME:Action
		    return actions.makeReply(null, true);
}
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	case EnvironmentEvent.ACTION:
	    if (ActionEvent.isAction(event, "reply"))
	    {
		actions.makeReply(null, false);
		return true;
	    }
	    if (ActionEvent.isAction(event, "reply-all"))
	    {
		actions.makeReply(null, true);
		return true;
	    }
	    if (ActionEvent.isAction(event, "forward"))
	    {
		actions.makeForward(null);
		return true;
	    }
	    return false;


	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	};
    }

    @Override public String getAreaName()
    {
	return strings.messageAreaName();//FIXME:
    }

    @Override public int getLineCount()
    {
	return content.length > 1?content.length:1;
    }

    @Override public String getLine(int index)
    {
	return index < content.length?content[index]:"";
    }
}
