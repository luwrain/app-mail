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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class ActionLists
{
    private final Luwrain luwrain;
        private final Base base;
    private final Strings strings;
    private final boolean severalAccounts;

    ActionLists(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
NullCheck.notNull(strings, "strings");
this.luwrain = luwrain;
this.base = base;
	this.strings = strings;
	this.severalAccounts = severalAccountsAvailable();
    }

    Action[] getActions()
    {
	final List<Action> res = new LinkedList();
	res.add(new Action("send", "Отправить"));
	if (severalAccounts)
	    res.add(new Action("send-another-account", "Отправить через учётную запись"));
	res.add(new Action("choose-to", "Выбрать получателя из списка"));
	res.add(new Action("choose-cc", "Выбрать получателей копии из списка"));
					res.add(new Action("attach-file", "Прикрепить файл", new KeyboardEvent(KeyboardEvent.Special.INSERT)));//FIXME:
					return res.toArray(new Action[res.size()]);
    }

    private boolean severalAccountsAvailable()
    {
	try {
	    final StoredMailAccount[] accounts = base.mailStoring.getAccounts().load();
	    int count = 0;
	    for(StoredMailAccount a: accounts)
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
