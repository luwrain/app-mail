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

package org.luwrain.app.message;

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.pim.*;
import org.luwrain.popups.pim.*;
import org.luwrain.pim.mail.persistence.model.*;

import static org.luwrain.popups.Popups.*;

final class Conv
{
    private final App app;
    private final Luwrain luwrain;
    private final Strings strings;

    Conv(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    File attachment()
    {
	return Popups.existingFile(luwrain, strings.attachmentPopupPrefix());
    }

    boolean confirmAttachmentDeleting(File file)
    {
	NullCheck.notNull(file, "file");
	return Popups.confirmDefaultYes(luwrain, "Удаление прикрепления", "Вы действительно хотите исключить " + file.getName() + " из списка прикреплений?");
    }

    String editCc(String initial)
    {
	NullCheck.notNull(initial, "initial");
	final String[] items = App.splitAddrs(initial);
	final CcEditPopup popup;
	try {
	    popup = new CcEditPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), app.getContactsStoring(), items);
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
	final ChooseMailPopup popup;
	try {
	    popup = new ChooseMailPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), app.getContactsStoring(), app.getContactsStoring().getFolders().getRoot());
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

    Account accountToSend() throws PimException
    {
/*
	final MailAccount[] accounts = app.getMailStoring().getAccounts().load();
	final List<MailAccount> items = new ArrayList<>();
	for(MailAccount a: accounts)
	    if (a.getType() == MailAccount.Type.SMTP && a.getFlags().contains(MailAccount.Flags.ENABLED))
		items.add(a);
	if (items.isEmpty())
	    return null;
	final Object res = Popups.fixedList(luwrain, "Выберите учётную запись для отправки сообщения:", items.toArray(new Object[items.size()]));//FIXME:
	if (res == null)
	    return null;
	return (MailAccount)res;
																				*/
return null;
    }

    boolean closeModified()
    {
	return confirmDefaultNo(luwrain, strings.closeModifiedPopupName(), strings.closeModifiedPopupText());
    }
}
