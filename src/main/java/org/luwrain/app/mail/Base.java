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
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.mail.*;

class Base
{
    private static final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private MailStoring storing;
    private StoredMailFolder currentFolder = null;
    private FoldersTreeModel foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    public boolean init(Luwrain luwrain,
			Actions actions,
			Strings strings)
    {
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	final Object obj = luwrain.getSharedObject(SHARED_OBJECT_NAME);
	if (obj == null || !(obj instanceof org.luwrain.pim.mail.Factory))
	    return false;
	final org.luwrain.pim.mail.Factory factory = (org.luwrain.pim.mail.Factory)obj;
	final Object obj2 = factory.createMailStoring();
	if (obj2 == null || !(obj2 instanceof MailStoring))
	    return false;
	storing = (MailStoring)obj2;
	return true;
    }

    public FoldersTreeModel getFoldersModel()
    {
	if (foldersModel != null)
	    return foldersModel;
	foldersModel = new FoldersTreeModel(storing, strings);
	return foldersModel;
    }

    public SummaryTableModel getSummaryModel()
    {
	if (summaryModel != null)
	    return summaryModel;
	summaryModel = new SummaryTableModel();
	return summaryModel;
    }

    public SummaryTableAppearance getSummaryAppearance()
    {
	if (summaryAppearance != null)
	    return summaryAppearance;
	summaryAppearance = new SummaryTableAppearance(luwrain);
	return summaryAppearance;
    }

    /*
    public boolean isStoredMailGroup(Object obj)
    {
	return obj != null && (obj instanceof StoredMailGroup);
    }
    */

    public boolean openFolder(StoredMailFolder folder)
    {
	if (folder == null)
	    return false;
	currentFolder = folder;
	try {
	    final StoredMailMessage[] messages = storing.loadMessages(currentFolder);
	    summaryModel.setMessages(messages);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    public boolean insertMessages()
    {
	if (currentFolder == null)
	    return false;
	final File file = Popups.file(luwrain, "Добавление сообщений", "Выберите каталог с файлами сообщений для добавления:",
				      luwrain.launchContext().userHomeDirAsFile(), FilePopup.DIRECTORY, 0);
	if (file == null)
	    return true;
	final MailEssentialJavamail mail = new MailEssentialJavamail();
	final File[] files = file.listFiles();
	for(File f: files)
	{
	    if (f.isDirectory())
		continue;
	    try {
		final MailMessage message = mail.loadMailFromFile(new FileInputStream(f.getAbsolutePath()));
		if (message == null)
		    continue;
		storing.saveMessage(currentFolder, message);
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
	return true;
    }
}
