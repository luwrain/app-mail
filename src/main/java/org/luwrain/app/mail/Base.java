/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.mail.*;
import org.luwrain.util.*;

class Base
{
    static private final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private MailStoring storing;
    private StoredMailFolder currentFolder = null;
    private StoredMailMessage currentMessage;
    private TreeModelSource treeModelSource;
    private TreeModel foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    boolean init(Luwrain luwrain,
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

    void setCurrentMessage(StoredMailMessage message)
    {
	this.currentMessage = message;
    }

    boolean hasCurrentMessage()
    {
	return currentMessage != null;
    }

    TreeModel getFoldersModel()
    {
	if (foldersModel != null)
	    return foldersModel;
	treeModelSource = new TreeModelSource(storing, strings);
	foldersModel = new CachedTreeModel(treeModelSource);
	return foldersModel;
    }

    SummaryTableModel getSummaryModel()
    {
	if (summaryModel != null)
	    return summaryModel;
	summaryModel = new SummaryTableModel();
	return summaryModel;
    }

    SummaryTableAppearance getSummaryAppearance()
    {
	if (summaryAppearance != null)
	    return summaryAppearance;
	summaryAppearance = new SummaryTableAppearance(luwrain, strings);
	return summaryAppearance;
    }

    boolean openFolder(StoredMailFolder folder)
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

    boolean makeReply(StoredMailMessage message, boolean wideReply)
    {
	try {
	    return makeReplyImpl(message, wideReply);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    private boolean makeReplyImpl(StoredMailMessage message, boolean wideReply) throws Exception
    {
	if (message == null && currentMessage == null)
	    return false;
	final StoredMailMessage m = message != null?message:currentMessage;
	String subject = m.getSubject();
	if (!subject.toLowerCase().startsWith("re: "))
	    subject = "Re: " + subject;
	System.out.println("subject " + subject);
	final byte[] bytes = m.getRawMail();
	final String from = m.getFrom();
	if (from.trim().isEmpty())
	    return false;
	System.out.println("from " + from);
	String replyTo = getReplyTo(bytes);
	if (replyTo.trim().isEmpty())
	    replyTo = from;
	System.out.println("replyto " + replyTo);
	final StringBuilder newBody = new StringBuilder();
	newBody.append(strings.replyFirstLine(getDisplayedAddress(from), m.getSentDate()) + "\n");
	System.out.println("here");
	newBody.append("\n");
	System.out.println("here2");
	for(String s: m.getBaseContent().split("\n"))
	    newBody.append(">" + s + "\n");
	if (wideReply)
	{
	    luwrain.launchApp("message", new String[]{
		    replyTo,
		    new MailEssentialJavamail().constructWideReplyCcList(bytes, true),
		    subject,
		    newBody.toString()
		});
	} else 
	{
	    System.out.println("here3");
	    luwrain.launchApp("message", new String[]{
		    replyTo,
		    subject,
		    newBody.toString()
		});
	}
	return true;
    }

    boolean makeForward(StoredMailMessage message)
    {
	try {
	    return makeForwardImpl(message);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    private boolean makeForwardImpl(StoredMailMessage message) throws Exception
    {
	if (message == null && currentMessage == null)
	    return false;
	final StoredMailMessage m = message != null?message:currentMessage;
	String subject = m.getSubject();
	if (!subject.toLowerCase().startsWith("fwd: "))
	    subject = "Fwd: " + subject;
	final byte[] bytes = m.getRawMail();
	final String from = m.getFrom();
	final StringBuilder newBody = new StringBuilder();
	newBody.append("=== Пересылаемое сообщение ===\n");

	newBody.append("ОТ: ");
	newBody.append(m.getFrom());
	newBody.append("\n");

	newBody.append("Кому: ");
	if (m.getTo().length > 0)
	{
	    final String[] values = m.getTo();
	    newBody.append(values[0]);
	    for(int i = 1;i < values.length;++i)
		newBody.append("," + values[i]);
	}
	newBody.append("\n");

	if (m.getCc().length > 0)
	{
	    newBody.append("Копия: ");
	    final String[] values = m.getCc();
	    newBody.append(values[0]);
	    for(int i = 1;i < values.length;++i)
		newBody.append("," + values[i]);
	}
	newBody.append("\n");

	newBody.append("Тема: " + m.getSubject() + "\n");
	newBody.append("Дата: " + m.getSentDate() + "\n");

	newBody.append("\n");
	for(String s: m.getBaseContent().split("\n"))
	    newBody.append(s + "\n");
	newBody.append("=== Конец пересылаемого сообщения ===");

	luwrain.launchApp("message", new String[]{
		"",
		subject,
		newBody.toString()
	    });
	return true;
    }

    static String getDisplayedAddress(String addr)
    {
	NullCheck.notNull(addr, "addr");
	if (addr.trim().isEmpty())
	    return addr;
	try {
	    final javax.mail.internet.InternetAddress inetAddr = new javax.mail.internet.InternetAddress(addr, false);
	    final String personal = inetAddr.getPersonal();
	    if (personal == null || personal.trim().isEmpty())
		return addr;
	    //	System.out.println(personal);
	    return personal;
	}
	catch (javax.mail.internet.AddressException e)
	{
	    e.printStackTrace();
	    return addr;
	}
    }

    boolean onFolderUniRefQuery(ObjectUniRefQuery query, FolderWrapper wrapper)
    {
	NullCheck.notNull(query, "query");
	NullCheck.notNull(wrapper, "wrapper");
	if (wrapper.folder() == null)
	    return false;
	try {
	    final String uniRef = storing.getFolderUniRef(wrapper.folder());
	    if (uniRef == null || uniRef.trim().isEmpty())
		return false;
	    query.setUniRef(uniRef);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    boolean saveAttachment(String fileName)
    {
	/*
	if (currentMessage == null)
	    return false;
	File destFile = new File(luwrain.launchContext().userHomeDirAsFile(), fileName);
	destFile = Popups.file(luwrain, "Сохранение прикрепления", "Введите имя файла для сохранения прикрепления:", destFile, 0, 0);
	if (destFile == null)
	    return false;
	if (destFile.isDirectory())
	    destFile = new File(destFile, fileName);
	final org.luwrain.util.MailEssentialJavamail util = new org.luwrain.util.MailEssentialJavamail();
	try {
	    if (!util.saveAttachment(currentMessage.getRawMail(), fileName, destFile))
	    {
		luwrain.message("Целостность почтового сообщения нарушена, сохранение прикрепления невозможно", Luwrain.MESSAGE_ERROR);
		return false;
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время сохранения прикрепления произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	luwrain.message("Файл " + destFile.getAbsolutePath() + " успешно сохранён", Luwrain.MESSAGE_OK);
	*/
	return true;
    }

    boolean deleteInSummary(StoredMailMessage message)
    {
	NullCheck.notNull(message, "message");
	if (currentFolder == null)
	    return false;
	try {
	    storing.deleteMessage(message);
	summaryModel.setMessages(storing.loadMessages(currentFolder));
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время удаления сообщения произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	return true;
    }

    static private String getReplyTo(byte[] bytes) throws Exception
    {
	final String[] res = new MailEssentialJavamail().getReplyTo(bytes, true);
	if (res == null || res.length < 1)
	    return "";
	return res[0];
    }
}
