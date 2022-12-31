/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;
import org.luwrain.io.json.*;

import static org.luwrain.pim.mail.BinaryMessage.*;

public final class App extends AppBase<Strings>
{
    final Message message;
    private MailStoring mailStoring = null;
    private ContactsStoring contactsStoring = null;
    private Conv conv = null;
    private MainLayout mainLayout = null;

    public App()
    {
	this(null);
    }

    public App(Message message)
    {
	super(Strings.NAME, Strings.class, "luwrain.message");
	this.message = message != null?message:new Message();
    }

    @Override protected AreaLayout onAppInit()
    {
	this.mailStoring = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
	this.contactsStoring = org.luwrain.pim.Connections.getContactsStoring(getLuwrain(), true);
	if (mailStoring == null || contactsStoring == null)
	    return null;
	this.conv = new Conv(this);
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return mainLayout.getAreaLayout();
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    boolean send(MailMessage message, boolean useAnotherAccount)
    {
	    if (useAnotherAccount)
	    {
		final MailAccount account = conv.accountToSend();
		if (account == null)
		    return false;
		send(account, message);
		return true;
	    } //useAnotherAccount
	    final MailAccount account;
	    final MailAccount defaultAccount = mailStoring.getAccounts().getDefault(MailAccount.Type.SMTP);
	    if (defaultAccount == null)
		account = conv.accountToSend(); else
		account = defaultAccount;
	    send(account, message);
	    return true;
    }

    private void send(MailAccount account, MailMessage message)
    {
	message.setFrom(getFromLine(account));
	if (message.getFrom().trim().isEmpty())
	    throw new PimException("No sender address");//FIXME:
	final MessageSendingData sendingData = new MessageSendingData();
	sendingData.setAccountId(mailStoring.getAccounts().getId(account));
	message.setExtInfo(sendingData.toString());
	fillMessageData(message);
	final MailFolder folder = mailStoring.getFolders().findFirstByProperty(MailFolders.PROP_DEFAULT_OUTGOING, "true");
	if (folder == null)
	    throw new PimException("Unable to prepare a folder for pending messages");
	mailStoring.getMessages().save(folder, message);
		    	    getLuwrain().runWorker(org.luwrain.pim.workers.Smtp.NAME);
    }

    private void fillMessageData(MailMessage message)
    {
	message.setSentDate(new Date());
	message.setContentType("text/plain; charset=utf-8");//FIXME:
	final Map<String, String> headers = new HashMap<>();
	headers.put("User-Agent", getUserAgent());
	try {
	message.setRawMessage(toByteArray(message, headers));
	}
	catch(IOException e)
	{
	    throw new PimException(e);
	}
    }

    Conv getConv() { return this.conv; }
    ContactsStoring getContactsStoring() { return this.contactsStoring; }
    MailStoring getMailStoring() { return this.mailStoring; }

    private String getFromLine(MailAccount account)
    {
	final org.luwrain.core.Settings.PersonalInfo sett = org.luwrain.core.Settings.createPersonalInfo(getLuwrain().getRegistry());
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

    private String getUserAgent()
    {
	final String ver = getLuwrain().getProperty("luwrain.version");
	if (!ver.isEmpty())
	    return "LUWRAIN v" + ver;
	return "LUWRAIN";
    }

    static String[] splitAddrs(String line)
    {
	NullCheck.notNull(line, "line");
	if (line.trim().isEmpty())
	    return new String[0];
	final List<String> res = new ArrayList<>();
	final String[] lines = line.split(",", -1);
	for(String s: lines)
	    if (!s.trim().isEmpty())
		res.add(s.trim());
	return res.toArray(new String[res.size()]);
    }
}
