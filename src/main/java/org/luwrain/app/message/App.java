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

package org.luwrain.app.message;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

public final class App implements Application
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private ActionLists actionLists = null;
    private Actions actions = null;
    private Area messageArea;

    private final MessageContent startingMessage = new MessageContent();

    public App()
    {
    }

    public App(String to, String cc,
	       String subject, String text)
    {
	NullCheck.notNull(to, "to");
	NullCheck.notNull(cc, "cc");
	NullCheck.notNull(subject, "subject");
	NullCheck.notNull(text, "text");
	startingMessage.to = to;
	startingMessage.cc = cc;
	startingMessage.subject = subject;
	startingMessage.text = text;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null)
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actionLists = new ActionLists(luwrain, base, strings);
	this.actions = new Actions(luwrain, base, strings);
	if (!base.isReady())
	    return new InitResult(InitResult.Type.FAILURE);
	if (startingMessage.text.isEmpty())
	{
	    final Settings.PersonalInfo sett = Settings.createPersonalInfo(luwrain.getRegistry());
final String value = sett.getSignature("");
if (!value.isEmpty())
    startingMessage.text = "\n" + value;
	}
	createArea();
	return new InitResult();
    }

    private void createArea()
    {
	messageArea = new Area(luwrain,  strings, startingMessage) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case ENTER:
			{
			    final String name = getItemNameOnLine(getHotPointY());
			    if (name == null)
				return super.onInputEvent(event);
			    if (name.equals(Area.TO_NAME))
				return actions.onEditTo(this);
			    if (name.equals(Area.CC_NAME))
				return actions.onEditCc(this);
			    return super.onInputEvent(event);
			}
		    case DELETE:
			return actions.onDeleteAttachment(this);
		    case ESCAPE:
			closeApp();
			return true;
		    }
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch (event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "send"))
			{
			    if (actions.onSend(base, messageArea, false))
			    {
				luwrain.runWorker(org.luwrain.pim.workers.Smtp.NAME);
				closeApp();
			    }
			    return true;
			}
			if (ActionEvent.isAction(event, "send-another-account"))
			{
			    if (actions.onSend(base, messageArea, true))
			    {
				luwrain.runWorker(org.luwrain.pim.workers.Smtp.NAME);
				closeApp();
			    }
			    return true;
			}
			if (ActionEvent.isAction(event, "choose-to"))
			    return actions.onEditTo(messageArea);
			if (ActionEvent.isAction(event, "choose-cc"))
			    return actions.onEditCc(messageArea);
			if (ActionEvent.isAction(event, "attach-file"))
			    return actions.onAttachFile(messageArea);
			return false;
		    case OK:
				if (actions.onSend(base, messageArea, false))
				{
				    luwrain.runWorker(org.luwrain.pim.workers.Smtp.NAME);
				    closeApp();
				}
	return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    return strings.appName();
		}
    @Override public Action[] getAreaActions()
    {
	return actionLists.getActions();
    }
	    };
    }

@Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return new AreaLayout(messageArea);
    }
}