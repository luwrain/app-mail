/*
   Copyright 2012-2023 Michael Pozhidaev <msp@luwrain.org>

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

import static org.luwrain.pim.mail.MailFolders.*;

final class FolderPropertiesLayout extends LayoutBase
{
    final MailFolder folder;
    final FormArea formArea;

    FolderPropertiesLayout(App app, MailFolder folder, ActionHandler closing)
    {
	super(app);
	this.folder = folder;
	this.formArea = new FormArea(getControlContext(), "Свойства группы \"" + folder.getTitle() + "\"") ;
	formArea.addEdit("title", "Имя группы:", folder.getTitle());//
	formArea.addCheckbox("defaultIncoming", "Использовать для входящих писем по умолчанию:", propSet(folder.getProperties(), "defaultIncoming"));
	formArea.addCheckbox("defaultIncomingLists", "Использовать для писем из списков рассылки:", propSet(folder.getProperties(), "defaultIncomingLists"));
	formArea.addCheckbox("defaultOutgoing", "Использовать для исходящих писем:", propSet(folder.getProperties(), "defaultOutgoing"));
	formArea.addCheckbox("defaultSent", "Использовать для отправленных писем:", propSet(folder.getProperties(), "defaultSent"));
	setCloseHandler(closing);
	setOkHandler(()->{
		if (!save())
		    return true;
		return closing.onAction();
	    });
	setAreaLayout(formArea, null);
    }

    private boolean save()
    {
	final var title = formArea.getEnteredText("title").trim();
	if (title.isEmpty())
	{
	    app.getLuwrain().message("Название группы не может быть пустым", Luwrain.MessageType.ERROR);
	    return false;
	}
	this.folder.setTitle(title);
	this.folder.getProperties().setProperty(PROP_DEFAULT_INCOMING, new Boolean(formArea.getCheckboxState("defaultIncoming")).toString());
	this.folder.getProperties().setProperty(PROP_DEFAULT_MAILING_LISTS, new Boolean(formArea.getCheckboxState("defaultIncomingLists")).toString());
	this.folder.getProperties().setProperty(PROP_DEFAULT_OUTGOING, new Boolean(formArea.getCheckboxState("defaultOutgoing")).toString());
	this.folder.getProperties().setProperty(PROP_DEFAULT_SENT, new Boolean(formArea.getCheckboxState("defaultSent")).toString());
	return true;
    }

    static private boolean propSet(Properties props, String propName)
    {
	final String value = props.getProperty(propName);
	if (value == null)
	    return false;
	return value.equals("true");
    }
}
