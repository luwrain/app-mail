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

package org.luwrain.app.mail;

import java.util.*;
import org.graalvm.polyglot.*;

import org.luwrain.core.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.script.*;

import static org.luwrain.script.ScriptUtils.*;
import static org.luwrain.script.Hooks.*;
import static org.luwrain.app.mail.App.*;

final class Hooks
{
    static private final String
	SERVERS = "luwrain.mail.servers",
	REPLY = "luwrain.mail.reply",
	ORGANIZE_SUMMARY = "luwrain.mail.summary.organize";

    private final Luwrain luwrain;
    Hooks(Luwrain luwrain) { this.luwrain = luwrain; }

    List<SummaryItem> organizeSummary(MailMessage[] messages)
    {
	Log.debug("proba", "" + messages.length + " messages");
	final MessageObj [] hookObjs = new MessageObj[messages.length];
	for(int i = 0;i < messages.length;i++)
	    hookObjs[i] = new MessageObj(messages[i]);
	final Object[] args = new Object[]{getArray(hookObjs)};
	final Object[] res;
	final List<SummaryItem> items = new ArrayList<>();
	try {
	    res = asArray(provider(luwrain, ORGANIZE_SUMMARY, new Object[]{getArray(hookObjs)}));
	}
	catch(RuntimeException e)
	{
	    luwrain.crash(e);
	    return items;
	}
	if (res == null)
	{
	    Log.warning(LOG_COMPONENT, "The " + ORGANIZE_SUMMARY + " hook returned null");
	    return items;
	}
	for(Object o: res)
	{
	    if (!(o instanceof Value))
		continue;
	    final Value value = (Value)o;
	    if (value.isString())
	    {
		items.add(new SummaryItem(value.asString()));
		continue;
	    }
	    final MessageObj messageObj = value.asHostObject();
	    if (messageObj == null)
		continue;
	    items.add(new SummaryItem(messageObj.getMessage()));
	}
	return items;
    }

    void makeReply(MailMessage message)
    {
	try {
chainOfResponsibilityNoExc(luwrain, REPLY, new Object[]{new MessageObj(message)});
	}
	catch(RuntimeException e)
	{
	    luwrain.crash(e);
	}
    }

    Map<String, MailAccount> server(String mailAddr)
    {
	final Object res = provider(luwrain, SERVERS, new Object[]{mailAddr});
	if (isNull(res))
	    return null;
	final Map<String, MailAccount> accounts = new HashMap<>();
	final Object
	smtp = getMember(res, "smtp"),
	pop3 = getMember(res, "pop3");
	if (!isNull(smtp))
	    accounts.put("smtp", getAccount(smtp));
	if (!isNull(pop3))
	    accounts.put("pop3", getAccount(pop3));
	for(Map.Entry<String, MailAccount> e: accounts.entrySet())
	{
	    e.getValue().setLogin(mailAddr);
	    e.getValue().setTitle(e.getKey().toUpperCase() + " (" + mailAddr + ")");
	}
	return accounts;
    }

    static MailAccount getAccount(Object obj)
    {
	final MailAccount account = new MailAccount();
	account.setHost(asString(getMember(obj, "host")));
	account.setPort(asInt(getMember(obj, "port")));
	final EnumSet<MailAccount.Flags> flags = EnumSet.noneOf(MailAccount.Flags.class);
	final boolean
	ssl = asBoolean(getMember(obj, "ssl")),
	tls = asBoolean(getMember(obj, "tls"));
	if (ssl)
	    flags.add(MailAccount.Flags.SSL);
	if (tls)
	    flags.add(MailAccount.Flags.TLS);
	flags.add(MailAccount.Flags.ENABLED);
	flags.add(MailAccount.Flags.DEFAULT);
	account.setFlags(flags);
	return account;
    }
}
