
package org.luwrain.app.mail;

import org.luwrain.core.NullCheck;
import org.luwrain.pim.mail.*;

class TreeModelSource implements org.luwrain.controls.CachedTreeModelSource
{
    private MailStoring storing;
    private Strings strings;

    public TreeModelSource(MailStoring storing, Strings strings)
    {
	this.storing = storing;
	this.strings = strings;
	NullCheck.notNull(storing, "storing");
	NullCheck.notNull(strings, "strings");
    }

    @Override public Object getRoot()
    {
	try {
	    final StoredMailFolder root = storing.getFoldersRoot();
	    if (root == null)
		return null;
	    return new FolderWrapper(root, strings.folderTitle(root.getTitle()));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	if (obj == null || !(obj instanceof FolderWrapper))
	    return new Object[0];
	final FolderWrapper wrapper = (FolderWrapper)obj;
	try {
	    StoredMailFolder[] folders = storing.getFolders(wrapper.folder());
	    if (folders == null || folders.length < 1)
		return new Object[0];
	    final FolderWrapper[] wrappers= new FolderWrapper[folders.length];
	    for(int i = 0;i < folders.length;++i)
		wrappers[i] = new FolderWrapper(folders[i], strings.folderTitle(folders[i].getTitle()));
	    return wrappers;
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return new Object[0];
	}
    }
}
