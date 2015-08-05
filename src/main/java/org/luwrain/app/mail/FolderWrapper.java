
package org.luwrain.app.mail;

import org.luwrain.pim.mail.*;

class FolderWrapper
{
    private StoredMailFolder folder;
    private String title;

    public FolderWrapper(StoredMailFolder folder, String title)
    {
	this.folder = folder;
	this.title = title;
	if (folder == null)
	    throw new NullPointerException("folder may not be null");
	if (title == null)
	    throw new NullPointerException("title may not be null");
    }

    public StoredMailFolder folder()
    {
	return folder;
    }

    public String title()
    {
	return title;
    }

    @Override public String toString()
    {
	//	System.out.println(title);
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
