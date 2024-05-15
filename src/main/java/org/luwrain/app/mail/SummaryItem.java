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

import org.luwrain.pim.mail.*;

final class SummaryItem
{
    enum Type {SECTION, MESSAGE};

    final Type type;
    final String title;
    final Message message;

    SummaryItem(String sectName)
    {
	this.type = Type.SECTION;
	this.title = sectName;
	this.message = null;
    }

    SummaryItem(Message message)
    {
	this.type = Type.MESSAGE;
	this.title = message.getMetadata().getFromAddr();
	this.message = message;
    }

    @Override public String toString()
    {
	return title != null?title:"";
    }
}
