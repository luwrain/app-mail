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

package org.luwrain.app.mail;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.script.*;
import org.luwrain.script.hooks.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.script.*;

final class Hooks
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;

    static private final String ORGANIZE_SUMMARY_HOOK_NAME = "luwrain.mail.summary.organize";

    private final Luwrain luwrain;

    Hooks(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    Object[] organizeSummary(MailMessage[] messages)
    {
	NullCheck.notNullItems(messages, "messages");
	final MessageHookObject[] hookObjs = new MessageHookObject[messages.length];
	for(int i = 0;i < messages.length;i++)
	    hookObjs[i] = new MessageHookObject(messages[i]);
	final Object[] args = new Object[]{ScriptUtils.createReadOnlyArray(hookObjs)};
	final Object res;
	try {
	    res = new ProviderHook(luwrain).run(ORGANIZE_SUMMARY_HOOK_NAME, args);
	}
	catch(RuntimeException e)
	{
	    Log.error(LOG_COMPONENT, "unable to run the " + ORGANIZE_SUMMARY_HOOK_NAME + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return new Object[0];
	}
	if (res == null)
	    return new Object[0];
	final List array = ScriptUtils.getArray(res);
	if (array == null)
	    return new Object[0];
	return array.toArray(new Object[array.size()]);
    }

}
