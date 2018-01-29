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

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.PimException;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;
import org.luwrain.network.*;

final class Base
{
    static private final String USER_AGENT_HEADER_NAME = "User-Agent";
    static private final String USER_AGENT_HEADER_VALUE_BASE = "LUWRAIN mail";

    private final Luwrain luwrain;
    private final Strings strings;
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

    boolean isReady()
    {
	return mailStoring != null && contactsStoring != null;
    }

    boolean send(MailMessage msg, boolean fromAnotherAccount)
    {
	NullCheck.notNull(msg, "msg");
	try {
	    final StoredMailAccount account;
	    if (fromAnotherAccount)
		account = chooseAccountToSend(); else
		account = getDefaultAccount();
	    if (account == null)
		return false;
	    msg.from = prepareFromLine(account);
	    if (msg.from == null || msg.from.trim().isEmpty())
		throw new IllegalArgumentException("No sender address");//FIXME:
	    msg.sentDate = new Date();
	    msg.bcc = new String[0];
	    msg.mimeContentType = "text/plain; charset=utf-8";//FIXME:
	    msg.extInfo = mailStoring.getAccounts().getUniRef(account);
	    //	    final MailUtils mail = new MailUtils();
	    final Map<String, String> headers = new HashMap();
	    headers.put(USER_AGENT_HEADER_NAME, getUserAgentStr());
	    msg.rawMail = mailStoring.getMessages().toByteArray(msg, headers);
	    final StoredMailFolder folder = getFolderForPending();
	    if (folder == null)
		throw new IllegalArgumentException("Unable to prepare a folder for pending messages");
	    mailStoring.getMessages().save(folder, msg);
	    return true;
	}
	catch(PimException | IOException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    private String prepareFromLine(StoredMailAccount account) throws PimException, UnsupportedEncodingException
    {
	NullCheck.notNull(account, "account");
	final org.luwrain.core.Settings.PersonalInfo settings = org.luwrain.core.Settings.createPersonalInfo(luwrain.getRegistry());
	String mail = settings.getDefaultMailAddress("").trim();
	String name = settings.getFullName("").trim();
	if (account.getSubstName() != null && !account.getSubstName().trim().isEmpty())
	    name = account.getSubstName().trim();
	if (account.getSubstAddress() != null && !account.getSubstAddress().trim().isEmpty())
	    mail = account.getSubstAddress().trim();
	//	return MailAddress.makeEncodedAddress(name, mail);
	return "fixme";
    }

    private StoredMailAccount chooseAccountToSend() throws PimException
    {
	final StoredMailAccount[] accounts = mailStoring.getAccounts().load();
	final LinkedList items = new LinkedList();
	for(StoredMailAccount a: accounts)
	    if (a.getType() == MailAccount.Type.SMTP)
		items.add(a);
	if (items.isEmpty())
	{
	    luwrain.message("Отсутствуют учётные записи для отправки почты", Luwrain.MessageType.ERROR);
	    return null;
	}
	final Object res = Popups.fixedList(luwrain, "Выберите учётную запись для отправки сообщения", items.toArray(new Object[items.size()]));
	if (res == null)
	    return null;
	return (StoredMailAccount)res;
    }

    private StoredMailAccount getDefaultAccount() throws PimException
    {
	final StoredMailAccount[] accounts = mailStoring.getAccounts().load();
	for(StoredMailAccount a: accounts)
	    if (a.getType() == MailAccount.Type.SMTP &&
		a.getFlags().contains(MailAccount.Flags.DEFAULT))
		return a;
	luwrain.message("Отсутствует выбранный по умолчанию сервер исходящей почты", Luwrain.MessageType.ERROR);
	return null;
    }

    private StoredMailFolder getFolderForPending()
    {
	final org.luwrain.settings.mail.Settings.MailFolders settings = org.luwrain.settings.mail.Settings.createMailFolders(luwrain.getRegistry());
	try {
	    return mailStoring.getFolders().loadByUniRef(settings.getFolderPending(""));
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
    }

    private String constructUserName()
    {
	final org.luwrain.core.Settings.PersonalInfo settings = org.luwrain.core.Settings.createPersonalInfo(luwrain.getRegistry());
	final String name = settings.getFullName("").trim();
	final String addr = settings.getDefaultMailAddress("").trim();
	return name + " <" + addr + ">";
    }

    private String getUserAgentStr()
    {
	return USER_AGENT_HEADER_VALUE_BASE;//FIXME:
    }

        static String[] splitAddrs(String line)
    {
	NullCheck.notNull(line, "line");
	if (line.trim().isEmpty())
	    return new String[0];
	final LinkedList<String> res = new LinkedList<String>();
	final String[] lines = line.split(",", -1);
	for(String s: lines)
	    if (!s.trim().isEmpty())
		res.add(s.trim());
	return res.toArray(new String[res.size()]);
    }
}
