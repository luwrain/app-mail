/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;

final class ActionLists
{
    private final Strings strings;

    ActionLists(Strings strings)
    {
NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getActions()
    {
	return new Action[]{
	    new Action("send", "Отправить"),
	    new Action("send-another-account", "Отправить через учётную запись"),
	    new Action("choose-to", "Выбрать получателя из списка"),
	    new Action("choose-cc", "Выбрать получателей копии из списка"),
	    new Action("attach-file", "Прикрепить файл", new KeyboardEvent(KeyboardEvent.Special.INSERT)),//FIXME:
	};
    }
}
