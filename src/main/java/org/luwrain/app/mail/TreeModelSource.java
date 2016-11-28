
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
	    final StoredMailFolder root = storing.getFoldersRoot();
	    if (root == null)
		return null;
	    return new FolderWrapper(root, root.getTitle());
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
	final FolderWrapper wrapper = (FolderWrapper)obj;
	try {
	    StoredMailFolder[] folders = storing.getFolders(wrapper.folder());
	    if (folders == null || folders.length < 1)
		return new Object[0];
	    final FolderWrapper[] wrappers= new FolderWrapper[folders.length];
	    for(int i = 0;i < folders.length;++i)
		wrappers[i] = new FolderWrapper(folders[i], folders[i].getTitle());
	    return wrappers;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return new Object[0];
	}
    }
}
