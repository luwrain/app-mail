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
import org.luwrain.pim.email.*;

class Base
{
    private static final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private Luwrain luwrain;
    private Strings strings;
    private EmailStoring storing;
    private StoredEmailFolder currentFolder = null;
    private FoldersTreeModel foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    public boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	final Object obj = luwrain.getSharedObject(SHARED_OBJECT_NAME);
	if (obj == null || !(obj instanceof org.luwrain.pim.email.Factory))
	    return false;
	final org.luwrain.pim.email.Factory factory = (org.luwrain.pim.email.Factory)obj;
	final Object obj2 = factory.createEmailStoring();
	if (obj2 == null || !(obj2 instanceof EmailStoring))
	    return false;
	storing = (EmailStoring)obj2;
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
	summaryModel = new SummaryTableModel(storing);
	return summaryModel;
    }

    public SummaryTableAppearance getSummaryAppearance()
    {
	if (summaryAppearance != null)
	    return summaryAppearance;
	summaryAppearance = new SummaryTableAppearance();
	return summaryAppearance;
    }

    /*
    public boolean isStoredMailGroup(Object obj)
    {
	return obj != null && (obj instanceof StoredMailGroup);
    }
    */

    public boolean openFolder(StoredEmailFolder folder)
    {
	if (folder == null)
	    return false;
	currentFolder = folder;
	try {
	System.out.println("reading folder " + folder.getTitle());
	} catch(Exception e) {}
	try {
	    final StoredEmailMessage[] messages = storing.loadMessages(currentFolder);
	    for(StoredEmailMessage m: messages)
		System.out.println(m.getSubject());
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
	//	summaryModel.setCurrentMailGroup((StoredMailGroup)obj);
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
	final EmailEssentialJavamail mail = new EmailEssentialJavamail();
	final File[] files = file.listFiles();
	for(File f: files)
	{
	    if (f.isDirectory())
		continue;
	    try {
		final EmailMessage message = mail.loadEmailFromFile(new FileInputStream(f.getAbsolutePath()));
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
