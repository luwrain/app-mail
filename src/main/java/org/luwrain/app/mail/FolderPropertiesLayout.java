/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.app.base.*;

final class FolderPropertiesLayout extends LayoutBase
{
    private final App app;
    private final MailFolder folder;
    private final Runnable closing;

    private final FormArea formArea;

    FolderPropertiesLayout(App app, MailFolder folder, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(folder, "folder");
	NullCheck.notNull(closing, "closing");
	this.app = app;
	this.folder = folder;
	this.closing = closing;
	this.formArea = new FormArea(new DefaultControlContext(app.getLuwrain()), "Свойства группы \"" + folder.getTitle() + "\"") {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event, closing))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			    return onSaveAndClose();
			}
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
	    };
	formArea.addEdit("title", "Имя группы:", folder.getTitle());//
	formArea.addCheckbox("defaultIncoming", "Использовать для входящих писем по умолчанию:", propSet(folder.getProperties(), "defaultIncoming"));
		formArea.addCheckbox("defaultIncomingLists", "Использовать для писем из списков рассылки:", propSet(folder.getProperties(), "defaultIncomingLists"));
				formArea.addCheckbox("defaultOutgoing", "Использовать для исходящих писем:", propSet(folder.getProperties(), "defaultOutgoing"));
								formArea.addCheckbox("defaultSent", "Использовать для отправленных писем:", propSet(folder.getProperties(), "defaultSent"));
    }

    private boolean onSaveAndClose()
    {
	final String title = formArea.getEnteredText("title").trim();
	if (title.isEmpty())
	{
	    app.getLuwrain().message("Название группы не может быть пустым", Luwrain.MessageType.ERROR);
	    return true;
	}
	this.folder.setTitle(title);
	this.folder.getProperties().setProperty("defaultIncoming", new Boolean(formArea.getCheckboxState("defaultIncoming")).toString());
		this.folder.getProperties().setProperty("defaultIncomingLists", new Boolean(formArea.getCheckboxState("defaultIncomingLists")).toString());
				this.folder.getProperties().setProperty("defaultOutgoing", new Boolean(formArea.getCheckboxState("defaultOutgoing")).toString());
								this.folder.getProperties().setProperty("defaultSent", new Boolean(formArea.getCheckboxState("defaultSent")).toString());
	try {
	    this.folder.save();
	}
	catch(PimException e)
	{
	app.getLuwrain().crash(e);
	return true;
    }
    closing.run();
    return true;
}

    AreaLayout getLayout()
    {
	return new AreaLayout(formArea);
    }

    static private boolean propSet(Properties props, String propName)
    {
	NullCheck.notNull(props, "props");
	NullCheck.notEmpty(propName, "propName");
	final String value = props.getProperty(propName);
	if (value == null)
	    return false;
	return value.equals("true");
    }
}
