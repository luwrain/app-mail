
package org.luwrain.app.mail;

import org.luwrain.core.NullCheck;
import org.luwrain.pim.mail.*;

class FolderWrapper
{
    private StoredMailFolder folder;
    private String title;

    FolderWrapper(StoredMailFolder folder, String title)
    {
	NullCheck.notNull(folder, "folder");
	NullCheck.notNull(title, "title");
	this.folder = folder;
	this.title = title;
    }

    StoredMailFolder folder()
    {
	return folder;
    }

    String title()
    {
	return title;
    }

    @Override public String toString()
    {
	return title;
    }

    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof FolderWrapper))
	    return false;
	final FolderWrapper wrapper = (FolderWrapper)o;
	return folder.equals(wrapper.folder);
    }
}
