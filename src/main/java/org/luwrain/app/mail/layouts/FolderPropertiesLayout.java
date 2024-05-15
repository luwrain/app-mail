/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.mail.layouts;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.mail.*;

import org.luwrain.pim.mail.persistence.model.*;
import static org.luwrain.pim.mail.FolderProperties.*;

public final class FolderPropertiesLayout extends LayoutBase
{
    final Folder folder;
    final Data data;
    final FormArea formArea;

    public FolderPropertiesLayout(App app, Folder folder, ActionHandler closing)
    {
	super(app);
	this.data = app.getData();
	this.folder = folder;
	this.formArea = new FormArea(getControlContext(), "Свойства группы \"" + folder.getName() + "\"") ;
	formArea.addEdit("title", "Имя группы:", folder.getName());//
	formArea.addCheckbox("defaultIncoming", "Использовать для входящих писем по умолчанию:", propSet(folder.getProperties(), DEFAULT_INCOMING));
	formArea.addCheckbox("defaultIncomingLists", "Использовать для писем из списков рассылки:", propSet(folder.getProperties(), DEFAULT_MAILING_LISTS));
	formArea.addCheckbox("defaultOutgoing", "Использовать для исходящих писем:", propSet(folder.getProperties(), DEFAULT_OUTGOING));
	formArea.addCheckbox("defaultSent", "Использовать для отправленных писем:", propSet(folder.getProperties(), DEFAULT_SENT));
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
	folder.setName(title);
	folder.getProperties().setProperty(DEFAULT_INCOMING, Boolean.valueOf(formArea.getCheckboxState("defaultIncoming")).toString());
	folder.getProperties().setProperty(DEFAULT_MAILING_LISTS, Boolean.valueOf(formArea.getCheckboxState("defaultIncomingLists")).toString());
	folder.getProperties().setProperty(DEFAULT_OUTGOING, Boolean.valueOf(formArea.getCheckboxState("defaultOutgoing")).toString());
	folder.getProperties().setProperty(DEFAULT_SENT, Boolean.valueOf(formArea.getCheckboxState("defaultSent")).toString());
	this.data.folderDAO.update(folder);
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
