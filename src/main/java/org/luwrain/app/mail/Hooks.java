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

package org.luwrain.app.mail;

import java.util.*;
import org.graalvm.polyglot.proxy.*;

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
	SUMMARY = "luwrain.mail.summary";

    private final Luwrain luwrain;
    private final MailObj mailObj;

    Hooks(Luwrain luwrain)
    {
	this.luwrain = luwrain;
	this.mailObj = new MailObj(luwrain);
    }

    List<SummaryItem> organizeSummary(List<Message> messages)
    {
	final var m = new ArrayList<MessageObj>();
	messages.forEach(mm -> m.add(new MessageObj(mailObj, mm)));
	final List<Object> res;
		    	final var items = new ArrayList<SummaryItem>();
	try {
	    res = asListOfNativeObjects(provider(luwrain, SUMMARY, new Object[]{
			mailObj,
			ProxyArray.fromArray((Object[])m.toArray(new MessageObj[m.size()]))
		    }));
	}
	catch(Exception ex)
	{
	    log.error("The " + SUMMARY + " hook failed", ex);
	    luwrain.crash(ex);
	    return items;
	}
	if (res == null)
	{
	    log.warn("The " + SUMMARY + " hook returned null");
	    return items;
	}
	for(Object o: res)
	{
	    if (o instanceof String s)
	    {
		items.add(new SummaryItem(s));
		continue;
	    }
		if (o instanceof MessageObj mm)
		{
	    items.add(new SummaryItem(mm.message));
	    continue;
	}
	    }
	return items;
    }

    void makeReply(Message message)
    {
	/*
	try {
chainOfResponsibilityNoExc(luwrain, REPLY, new Object[]{new MessageObj(message)});
	}
	catch(RuntimeException e)
	{
	    luwrain.crash(e);
	}
	*/
    }
}
