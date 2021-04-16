/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.script.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.script.*;

final class SummaryItem
{
    enum Type {
	SECTION,
	MESSAGE,
    };

    final Type type;
    final String title;
    final MailMessage message;

    SummaryItem(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (obj instanceof String)
	{
	    this.type = Type.SECTION;
	    this.title = obj.toString();
	    this.message = null;
	    return;
	}
	final Object messageObj = ScriptUtils.getMember(obj, "message");
	if (messageObj != null && messageObj instanceof MessageHookObject)
	{
	    final MessageHookObject messageHookObj = (MessageHookObject)messageObj;
	    this.type = Type.MESSAGE;
	    this.message = messageHookObj.getNativeMessageObj();
	    final Object titleObj = ScriptUtils.getMember(obj, "title");
	    if (titleObj != null)
		this.title = titleObj.toString(); else
		this.title = obj.toString();
	    return;
	}
	this.type = Type.SECTION;
	this.message = null;
	final Object titleObj = ScriptUtils.getMember(obj, "title");
	if (titleObj != null)
	    this.title = titleObj.toString(); else
	    this.title = obj.toString();
    }

    @Override public String toString()
    {
	return title;
    }
}
