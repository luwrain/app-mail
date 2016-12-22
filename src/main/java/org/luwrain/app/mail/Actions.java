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
import org.luwrain.doctree.*;
import org.luwrain.controls.doctree.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

class Actions
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final MailApp app;

    Actions(Luwrain luwrain, Strings strings, MailApp app)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(app, "app");
	this.luwrain = luwrain;
	this.strings = strings;
	this.app = app;
    }


    Action[] getSummaryAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить", new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	    new Action("delete-message", strings.actionDeleteMessage(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
	};
    }

    boolean onSummaryClick(Base base, TableArea.Model model,
			   int col, int row, Object obj,
			   TableArea summaryArea, 			   DoctreeArea messageArea)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(model, "model");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(messageArea, "messageArea");
	final Object o = model.getRow(row);
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	try {
	    if (message.getState() == MailMessage.State.NEW)
	    {
		message.setState(MailMessage.State.READ);
		summaryArea.refresh();
	    }
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	}
	base.setCurrentMessage(message);
	messageArea.setDocument(base.prepareDocumentForCurrentMessage(), 512);
app.	enableMessageMode(MailApp.Mode.REGULAR);
	app.gotoMessage();
		    return true;
    }

    boolean onDeleteInSummary(Base base, TableArea summaryArea, boolean deleteForever)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.getSelectedRow();
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	if (!base.deleteInSummary(message, deleteForever))
	    return true;
	summaryArea.refresh();
	app.clearMessageArea();
	return true;
    }

    boolean onSummaryReply(Base base, TableArea summaryArea, boolean wideReply)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.getSelectedRow();
	if (obj == null || !(obj instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)obj;
	return base.makeReply(message, wideReply);
    }

    /*
    boolean makeForward(StoredMailMessage message)
    {
	if (!base.makeForward(message))
	    luwrain.message("Во время подготовки перенаправленяи произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }
    */



    void onLaunchMailFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--MAIL"});
    }
}
