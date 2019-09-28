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

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    final Conversations conv;

    Actions(Base base)
    {
	NullCheck.notNull(base, "base");
	this.luwrain = base.luwrain;
	this.base = base;
	this.strings = base.strings;
	this.conv = new Conversations(luwrain, base, strings);
    }

    //Returns true, if the message is successfully saved in the pending queue, the sending worker will be launched
    boolean onSend(Area area, boolean useAnotherAccount)
    {
	NullCheck.notNull(area, "area");
	if (!isReadyForSending(area))
	    return false;
	try {
	    final StoredMailAccount account;
	    final StoredMailAccount defaultAccount = base.mailStoring.getAccounts().getDefault(MailAccount.Type.SMTP);
	    if (useAnotherAccount)
	    {
		account = conv.accountToSend();
		if (account == null)
		    return false;
	    }else
	    {
		if (defaultAccount == null)
		{
		    if (!conv.confirmLaunchingAccountWizard())
			return false;
		    if (!(new org.luwrain.pim.wizards.Mail(luwrain).start()))
			return false;
		    account = base.mailStoring.getAccounts().getDefault(MailAccount.Type.SMTP);
		} else
		    account = defaultAccount;
	    }
	    return base.send(account, area.constructMailMessage());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean onEditTo(Area area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editTo();
	if (res != null)
	    area.setTo(res);
	return true;
    }

    boolean onEditCc(Area area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    boolean onAttachFile(Area area)
    {
	NullCheck.notNull(area, "area");
	final File file = conv.attachment();
	if (file == null)
	    return true;
	area.addAttachment(file);
	return true;
    }

    boolean onDeleteAttachment(Area area)
    {
	NullCheck.notNull(area, "area");
	final int index = area.getHotPointY();
	if (area.getItemTypeOnLine(index) != Area.Type.STATIC)
	    return false;
	final Object obj = area.getItemObjOnLine(index);
	if (obj == null || !(obj instanceof Attachment))
	    return false;
	final Attachment a = (Attachment)obj;
	if (!conv.confirmAttachmentDeleting(a.file))
	    return true;
	area.removeAttachment(index);
	luwrain.message("Прикрепление " + a.file.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }

        private boolean isReadyForSending(Area area)
    {
	NullCheck.notNull(area, "area");
	if (area.getTo().trim().isEmpty())
	{
	    luwrain.message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);//FIXME:
	    area.focusTo();
	    return false;
	}
	if (area.getSubject().trim().isEmpty())
	{
	    luwrain.message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    area.focusSubject();
	    return false;
	}
	return true;
    }
}
