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
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.PimException;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;

final class Base extends Utils
{
    static private final String USER_AGENT_HEADER_NAME = "User-Agent";

    final Luwrain luwrain;
    final Strings strings;
    final MailStoring mailStoring;
    final ContactsStoring contactsStoring;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.mailStoring = org.luwrain.pim.Connections.getMailStoring(luwrain, true);
	this.contactsStoring = org.luwrain.pim.Connections.getContactsStoring(luwrain, true);
    }

    boolean send(StoredMailAccount account, MailMessage msg) throws PimException
    {
	NullCheck.notNull(account, "account");
	NullCheck.notNull(msg, "msg");
	msg.setFrom(prepareFromLine(account));
	if (msg.getFrom().trim().isEmpty())
	    throw new RuntimeException("No sender address");//FIXME:
	msg.setSentDate(new Date());
	msg.setContentType("text/plain; charset=utf-8");//FIXME:
	msg.setExtInfo(mailStoring.getAccounts().getUniRef(account));
	final Map<String, String> headers = new HashMap();
	headers.put(USER_AGENT_HEADER_NAME, getUserAgentStr());
	msg.setRawMessage(mailStoring.getMessages().toByteArray(msg, headers));
	final MailFolder folder = getFolderForPending();
	if (folder == null)
	    throw new RuntimeException("Unable to prepare a folder for pending messages");
	mailStoring.getMessages().save(folder, msg);
	return true;
    }

        boolean isReady()
    {
	return mailStoring != null && contactsStoring != null;
    }

    private String prepareFromLine(StoredMailAccount account) throws PimException
    {
	NullCheck.notNull(account, "account");
	final org.luwrain.core.Settings.PersonalInfo sett = org.luwrain.core.Settings.createPersonalInfo(luwrain.getRegistry());
	final String personal;
	final String addr;
	if (!account.getSubstName().trim().isEmpty())
	    personal = account.getSubstName().trim(); else
personal = sett.getFullName("").trim();
		if (!account.getSubstAddress().trim().isEmpty())
	    addr = account.getSubstAddress().trim(); else
addr = sett.getDefaultMailAddress("").trim();
	return mailStoring.combinePersonalAndAddr(personal, addr);
    }

    private MailFolder getFolderForPending()
    {
	final org.luwrain.settings.mail.Settings.MailFolders sett = org.luwrain.settings.mail.Settings.createMailFolders(luwrain.getRegistry());
	final String uniRef = sett.getFolderPending("");
	if (uniRef.trim().isEmpty())
	    return null;
	try {
	    return mailStoring.getFolders().loadByUniRef(uniRef);
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
    }

    private String getUserAgentStr()
    {
	final String ver = luwrain.getProperty("luwrain.version");
	if (!ver.isEmpty())
	    return "LUWRAIN v" + ver;
	return "LUWRAIN";
	    }

}
