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

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class SummaryGroup
{
    final String title;
    final MailMessage[] messages;

    SummaryGroup(String title, MailMessage[] messages)
    {
	NullCheck.notNull(title, "title");
	NullCheck.notNullItems(messages, "messages");
	this.title = title;
	this.messages = messages;
    }

    @Override public String toString()
    {
	return title;
    }

    }
