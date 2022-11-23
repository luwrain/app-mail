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
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

final class FoldersModel implements TreeListArea.Model<MailFolder>
{
    static private final int
	DEFAULT_ID = 17;

    final App app;
    FoldersModel(App app) { this.app = app; }

    @Override public boolean getItems(MailFolder obj, TreeListArea.Collector<MailFolder> collector)
    {
	collector.collect(Arrays.asList(app.getStoring().getFolders().load(obj)));
	return true;
    }

    @Override public MailFolder getRoot()
    {
	return app.getStoring().getFolders().getRoot();
    }

    @Override public boolean isLeaf(MailFolder item)
    {
	return !app.getStoring().getFolders().hasSubfolders(item);
    }
}
