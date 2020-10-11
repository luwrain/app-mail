/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.contacts.*;

public final class App extends AppBase<Strings>
{
        private MailStoring mailStoring = null;
    private ContactsStoring contactsStoring = null;
    private MainLayout mainLayout = null;

    private final MessageContent startingMessage = new MessageContent();

    public App()
    {
	super(Strings.NAME, Strings.class);
    }

    public App(String to, String cc, String subject, String text)
    {
	super(Strings.NAME, Strings.class);
	NullCheck.notNull(to, "to");
	NullCheck.notNull(cc, "cc");
	NullCheck.notNull(subject, "subject");
	NullCheck.notNull(text, "text");
	/*
	startingMessage.to = to;
	startingMessage.cc = cc;
	startingMessage.subject = subject;
	startingMessage.text = text;
	*/
    }

    @Override protected boolean onAppInit()
    {

	this.mailStoring = org.luwrain.pim.Connections.getMailStoring(getLuwrain(), true);
	this.contactsStoring = org.luwrain.pim.Connections.getContactsStoring(getLuwrain(), true);
	if (mailStoring == null || contactsStoring == null)
	    return false;
	this.mainLayout = new MainLayout(this);
	return true;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

        boolean send(MailAccount account, MailMessage msg) throws PimException
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
	headers.put("User-Agent", getUserAgentStr());
	msg.setRawMessage(mailStoring.getMessages().toByteArray(msg, headers));
	final MailFolder folder = getFolderForPending();
	if (folder == null)
	    throw new RuntimeException("Unable to prepare a folder for pending messages");
	mailStoring.getMessages().save(folder, msg);
	return true;
    }

    ContactsStoring getContactsStoring()
    {
	return this.contactsStoring;
    }

    MailStoring getMailStoring()
    {
	return this.mailStoring;
    }


    

    private String prepareFromLine(MailAccount account) throws PimException
    {
	NullCheck.notNull(account, "account");
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

    private MailFolder getFolderForPending()
    {
	final org.luwrain.pim.mail.Settings sett = org.luwrain.pim.mail.Settings.create(getLuwrain().getRegistry());
	final String uniRef = sett.getFolderPending("");
	if (uniRef.trim().isEmpty())
	    return null;
	try {
	    return mailStoring.getFolders().loadByUniRef(uniRef);
	}
	catch (PimException e)
	{
	    getLuwrain().crash(e);
	    return null;
	}
    }

    private String getUserAgentStr()
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
	final List<String> res = new LinkedList();
	final String[] lines = line.split(",", -1);
	for(String s: lines)
	    if (!s.trim().isEmpty())
		res.add(s.trim());
	return res.toArray(new String[res.size()]);
    }


}
