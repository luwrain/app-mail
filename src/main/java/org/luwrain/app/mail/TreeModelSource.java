/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class TreeModelSource implements org.luwrain.controls.CachedTreeModelSource
{
    private Luwrain luwrain;
    private MailStoring storing;
    private Strings strings;

    TreeModelSource(Luwrain luwrain,
		    MailStoring storing, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(storing, "storing");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.storing = storing;
	this.strings = strings;
    }

    @Override public Object getRoot()
    {
	try {
	    return storing.getFolders().getRoot();
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	final StoredMailFolder folder = (StoredMailFolder)obj;
	try {
	    return storing.getFolders().load(folder);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return new Object[0];
	}
    }
}
