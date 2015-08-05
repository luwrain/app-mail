/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import java.util.*;

import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class FoldersTreeModel implements TreeModel
{
private class CacheItem
{
    public FolderWrapper parent;
    public FolderWrapper[] folders = new FolderWrapper[0];

    public CacheItem(FolderWrapper parent)
    {
	this.parent = parent;
	if (parent == null)
	    throw new NullPointerException("parent may not be null");
    }
}

    private MailStoring storing;
    private Strings strings;
    private  LinkedList<CacheItem> cache = new LinkedList<CacheItem>();

    public FoldersTreeModel(MailStoring storing, Strings strings)
    {
	this.storing = storing;
	this.strings = strings;
	if (storing == null)
	    throw new NullPointerException("storing may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
    }

    @Override public Object getRoot()
    {
	try {
	    final StoredMailFolder root = storing.getFoldersRoot();
	    //	    System.out.println("root" + root);
	    return root != null?new FolderWrapper(root, strings.folderTitle(root.getTitle())):null;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    @Override public boolean isLeaf(Object node)
    {
	if (node == null || !(node instanceof FolderWrapper))
	    return true;
	final FolderWrapper wrapper = (FolderWrapper)node;
	try {
	    StoredMailFolder[] folders = storing.getFolders(wrapper.folder());
	    return folders == null || folders.length < 1;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return true;
	}
    }

    @Override public void beginChildEnumeration(Object node)
    {
	if (node == null || !(node instanceof FolderWrapper))
	    return;
	final FolderWrapper wrapper = (FolderWrapper)node;
	CacheItem newItem = null;
	for(CacheItem c: cache)
	    if (c.parent.equals(wrapper))
		newItem = c;
	if (newItem == null)
	{
	    newItem = new CacheItem(wrapper);
	    cache.add(newItem);
	}
	try {
	    final StoredMailFolder[] folders = storing.getFolders(wrapper.folder());
	    if (folders == null || folders.length < 1)
		return;
	    newItem.folders = new FolderWrapper[folders.length];
	    for(int i = 0;i < folders.length;++i)
		newItem.folders[i] = new FolderWrapper(folders[i], strings.folderTitle(folders[i].getTitle()));
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }

    public int getChildCount(Object parent)
    {
	if (parent == null || !(parent instanceof FolderWrapper))
	    return 0;
	final FolderWrapper wrapper = (FolderWrapper)parent;
	for(CacheItem c: cache)
	    if (c.parent.equals(wrapper))
		return c.folders.length;
	return 0;
    }

    @Override public Object getChild(Object parent, int index)
    {
	if (parent == null || !(parent instanceof FolderWrapper))
	    return 0;
	final FolderWrapper wrapper = (FolderWrapper)parent;
	for(CacheItem c: cache)
	    if (c.parent.equals(wrapper))
		return c.folders[index];
	return null;
    }

    public void endChildEnumeration(Object node)
    {
	//FIXME:
    }
}
