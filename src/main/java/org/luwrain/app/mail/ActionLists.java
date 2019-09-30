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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

final class ActionLists
{
    private final Luwrain luwrain;
    private final Strings strings;

    ActionLists(Base base)
    {
	NullCheck.notNull(base, "base");
	this.luwrain = base.luwrain;
	this.strings = base.strings;
    }

    Action[] getSummaryAreaActions()
    {
	return new Action[]{
	    new Action("reply", strings.actionReply(), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("reply-all", strings.actionReplyAll()),
	    new Action("forward", strings.actionForward()),
	    new Action("delete-message", strings.actionDeleteMessage(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
	};
    }

    Action[] getRawMessageActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	};
    }
}
