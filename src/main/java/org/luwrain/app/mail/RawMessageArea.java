/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class RawMessageArea extends NavigationArea
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final MailApp app;
    private StoredMailMessage message;
    private String[] content = new String[0];

    RawMessageArea(Luwrain luwrain, MailApp app,
		Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.app = app;
	this.strings = strings;
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
	    final String str = new String(message.getRawMessage(), "US-ASCII");
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
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case TAB:
app.gotoFolders();
	    return true;
	    case BACKSPACE:
app.gotoSummary();
	    return true;
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case CLOSE:
app.closeApp();
	    return true;
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
