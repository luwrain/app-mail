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

package org.luwrain.app.message;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;

import static org.luwrain.pim.mail.persistence.MailPersistence.*;
import org.luwrain.pim.mail.persistence.model.*;
import org.luwrain.pim.mail.persistence.dao.*;

public final class App extends AppBase<Strings>
{
    final org.luwrain.io.json.Message message;
    private org.luwrain.pim.mail.Settings sett = null;
    private FolderDAO folderDAO = null;
    private ContactsStoring contactsStoring = null;
    private Conv conv = null;
    private MainLayout mainLayout = null;

    public App()
    {
	this(null);
    }

    public App(org.luwrain.io.json.Message message)
    {
	super(Strings.NAME, Strings.class, "luwrain.message");
	this.message = message != null?message:new org.luwrain.io.json.Message();
    }

    @Override protected AreaLayout onAppInit()
    {
	this.sett = org.luwrain.pim.mail.Settings.create(getLuwrain().getRegistry());
//	this.mailStoring = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
	this.folderDAO = getFolderDAO();
	this.contactsStoring = org.luwrain.pim.Connections.getContactsStoring(getLuwrain(), true);
	if (contactsStoring == null)
	    return null;
	this.conv = new Conv(this);
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return mainLayout.getAreaLayout();
    }

    @Override public boolean onEscape()
    {
	if (this.mainLayout.modified)
	    if (!conv.closeModified())
	    return true;
	closeApp();
	return true;
    }

    boolean send(org.luwrain.pim.mail.Message message, boolean useAnotherAccount)
    {
	/*
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
	*/
	return true;
    }

    private void send(Account account, Message message)
    {
	/*
notNull(account, "account");
notNull(message, "message");
	message.setFrom(getFromLine(account));
	if (message.getFrom().trim().isEmpty())
	    throw new PimException("No sender address");//FIXME:
	final MessageSendingData sendingData = new MessageSendingData();
	sendingData.setAccountId(mailStoring.getAccounts().getId(account));
	message.setExtInfo(sendingData.toString());
	fillMessageData(message);
	final Folder folder = folderDAO.findFirstByProperty(DEFAULT_OUTGOING, "true");
	if (folder == null)
	    throw new PimException("Unable to prepare a folder for pending messages");
	mailStoring.getMessages().save(folder, message);
	getLuwrain().runWorker(org.luwrain.pim.workers.Smtp.NAME);
	closeApp();
	*/
    }

    private void fillMessageData(Message message)
    {
	/*
	message.setSentDate(new Date());
	final Map<String, String> headers = new HashMap<>();
	headers.put("User-Agent", getUserAgent());
	try {
	    message.setRawMessage(toByteArray(message, headers));
	}
	catch(IOException e)
	{
	    throw new PimException(e);
	}
	*/
    }

    Conv getConv() { return this.conv; }
    ContactsStoring getContactsStoring() { return this.contactsStoring; }
//    MailStoring getMailStoring() { return this.mailStoring; }

    private String getFromLine(Account account)
    {
	final org.luwrain.core.Settings.PersonalInfo sett = org.luwrain.core.Settings.createPersonalInfo(getLuwrain().getRegistry());
	final String personal;
	final String addr;
	if (account != null && account.getSubstName() != null && !account.getSubstName().trim().isEmpty())
	    personal = account.getSubstName().trim(); else
	    personal = sett.getFullName("").trim();
	if (account != null && account.getSubstAddress() != null && !account.getSubstAddress().trim().isEmpty())
	    addr = account.getSubstAddress().trim(); else
	    addr = sett.getDefaultMailAddress("").trim();
	return "FIXME";//mailStoring.combinePersonalAndAddr(personal, addr);
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
