/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class ActionLists
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;
    private final boolean severalAccounts;

    ActionLists( Base base)
    {
	NullCheck.notNull(base, "base");
	this.base = base;
	this.luwrain = base.luwrain;
	this.strings = base.strings;
	this.severalAccounts = severalAccountsAvailable();
    }

    Action[] getActions(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final boolean hasDeleteAttachment;
	final int index = area.getHotPointY();
	if (area.getItemTypeOnLine(index) != MessageArea.Type.STATIC)
	    hasDeleteAttachment = false; else
	{
	    final Object obj = area.getItemObj(index);
	    hasDeleteAttachment = obj != null && obj instanceof Attachment;
	}
	final List<Action> res = new LinkedList();
	res.add(new Action("send", strings.actionSend()));
	if (severalAccounts)
	    res.add(new Action("send-another-account", strings.actionSendAnotherAccount()));
	res.add(new Action("choose-to", strings.actionChooseTo()));
	res.add(new Action("choose-cc", strings.actionChooseCc()));
	res.add(new Action("attach-file", strings.actionAttachFile(), new KeyboardEvent(KeyboardEvent.Special.INSERT)));
	if (hasDeleteAttachment)
	    res.add(new Action("delete-attachment", strings.actionDeleteAttachment(), new KeyboardEvent(KeyboardEvent.Special.DELETE)));
	return res.toArray(new Action[res.size()]);
    }

    private boolean severalAccountsAvailable()
    {
	try {
	    final MailAccount[] accounts = base.mailStoring.getAccounts().load();
	    int count = 0;
	    for(MailAccount a: accounts)
	    {
		if (a.getType() != MailAccount.Type.SMTP)
		    continue;
		if (!a.getFlags().contains(MailAccount.Flags.ENABLED))
		    continue;
		++count;
	    }
	    return count >= 2;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }
}
