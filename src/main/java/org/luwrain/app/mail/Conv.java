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

import org.luwrain.core.*;
import static org.luwrain.popups.Popups.*;

final class Conv
{
    final Luwrain luwrain;
    final Strings strings;

    Conv(App app)
    {
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    String newFolderName() { return textNotEmpty(luwrain, strings.newFolderNamePopupName(), strings.newFolderNamePopupPrefix(), ""); }
    boolean removeFolder() { return confirmDefaultNo(luwrain, strings.removeFolderPopupName(), strings.removeFolderPopupText()); }
    boolean deleteMessageForever() { return confirmDefaultYes(luwrain, strings.deleteMessageForeverPopupName(), strings.deleteMessageForeverPopupText()); }
}
