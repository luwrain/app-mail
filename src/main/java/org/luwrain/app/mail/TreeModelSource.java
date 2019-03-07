
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
