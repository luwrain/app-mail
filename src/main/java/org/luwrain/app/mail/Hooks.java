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

import org.luwrain.core.*;
import org.luwrain.pim.mail.*;

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

    Map<String, MailAccount> server(String mailAddr)
    {
	final Object res = provider(luwrain, SERVERS, new Object[]{mailAddr});
	if (isNull(res))
	    return null;
	final Map<String, MailAccount> accounts = new HashMap<>();
	final Object
	smtp = getMember(res, "smtp");
	if (!isNull(smtp))
	    accounts.put("smtp", getAccount(smtp));
	return accounts;
    }

    void organizeSummary()
    {
	/*
	final MessageObj [] hookObjs = new MessageObj[messages.length];
	for(int i = 0;i < messages.length;i++)
	    hookObjs[i] = new MessageObj(messages[i]);
	final Object[] args = new Object[]{getArray(hookObjs)};
	final Object res;
	final List<SummaryItem> items = new ArrayList<>();
	try {
	    res = getHooks().provider(getLuwrain(), App.HOOK_ORGANIZE_SUMMARY, args);
	}
	catch(RuntimeException e)
	{
	    Log.error(LOG_COMPONENT, "unable to run the " + App.HOOK_ORGANIZE_SUMMARY + ": " + e.getClass().getName() + ": " + e.getMessage());
	    app.crash(e);
	    return items;
	}
	if (res == null)
	    return items;
	final Object[] array = asArray(res);
	if (array == null)
	    return items;
	for(Object o: array)
	    items.add(new SummaryItem(o));
	return items;
	*/
    }

    boolean makeReply(MailMessage message)
    {
	/*
	NullCheck.notNull(message, "message");
	final Object[] args = new Object[]{new MessageHookObject(message)};
	try {
	    return new ChainOfResponsibilityHook(luwrain).run(REPLY_HOOK_NAME, args);
	}
	catch(RuntimeException e)
	{
	    Log.error(LOG_COMPONENT, "unable to run the " + REPLY_HOOK_NAME + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return false;
	}
	*/
	return false;
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
	account.setFlags(flags);
	return account;
    }
}
