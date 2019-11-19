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
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;
import org.luwrain.popups.pim.*;

final class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;

    Conversations(Base base)
    {
	NullCheck.notNull(base, "base");
	this.base = base;
	this.luwrain = base.luwrain;
	this.strings = base.strings;
    }

    File attachment()
    {
	return Popups.existingFile(luwrain, strings.attachmentPopupName(), strings.attachmentPopupPrefix());
    }

    boolean confirmAttachmentDeleting(File file)
    {
	NullCheck.notNull(file, "file");
	return Popups.confirmDefaultYes(luwrain, "Удаление прикрепления", "Вы действительно хотите исключить " + file.getName() + " из списка прикреплений?");
    }

    String editCc(String initial)
    {
	NullCheck.notNull(initial, "initial");
	final String[] items = Base.splitAddrs(initial);
	final CcEditPopup popup;
	try {
	    popup = new CcEditPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), base.contactsStoring, items);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	final String[] newItems = popup.result();
	NullCheck.notNullItems(newItems, "newItems");
	if (newItems.length == 0)
	    return "";
	final StringBuilder b = new StringBuilder();
	b.append((String)newItems[0]);
	for(int i = 1;i < newItems.length;++i)
	    b.append("," + (String)newItems[i]);
	return b.toString();
    }

    String editTo()
    {
	NullCheck.notNull(base, "base");
	final ChooseMailPopup popup;
	try {
	    popup = new ChooseMailPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), base.contactsStoring, base.contactsStoring.getFolders().getRoot());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	return popup.result();
    }

    boolean confirmLaunchingAccountWizard()
    {
	return Popups.confirmDefaultYes(luwrain, "Отправление сообщения", "Учётные записи для отправления почты отсутствуют. Вы хотите добавить новую сейчас?");//FIXME:
    }

    MailAccount accountToSend() throws PimException
    {
	final MailAccount[] accounts = base.mailStoring.getAccounts().load();
	final List items = new LinkedList();
	for(MailAccount a: accounts)
	    if (a.getType() == MailAccount.Type.SMTP && a.getFlags().contains(MailAccount.Flags.ENABLED))
		items.add(a);
	if (items.isEmpty())
	    return null;
	final Object res = Popups.fixedList(luwrain, "Выберите учётную запись для отправки сообщения:", items.toArray(new Object[items.size()]));//FIXME:
	if (res == null)
	    return null;
	return (MailAccount)res;
    }
}
