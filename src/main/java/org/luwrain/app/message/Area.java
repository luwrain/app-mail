/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.message;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.MailMessage;
import org.luwrain.network.MailUtils;

class Area extends FormArea
{
    static final String TO_NAME = "to";
    static final String CC_NAME = "cc";
    static final String SUBJECT_NAME = "subject";
    static final String ATTACHMENT = "attachment";

    private final Luwrain luwrain;
    private final Strings strings;

    private final MutableLinesImpl lines = new MutableLinesImpl();
    private final Vector<Attachment> attachments = new Vector<Attachment>();
    private int attachmentCounter = 0;

Area(Luwrain luwrain, Strings strings, MessageContent msg)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(msg, "msg");
	this.luwrain = luwrain;
	this.strings = strings;
	addEdit(TO_NAME, strings.to(), msg.to);
	addEdit(CC_NAME, strings.cc(), msg.cc);
	addEdit(SUBJECT_NAME, strings.subject(), msg.subject);
	activateMultilineEdit(strings.enterMessageBelow(), createMultilineEditModel(msg.textLines()), true);
    }

    String getTo()
    {
	return getEnteredText(TO_NAME);
    }

    void setTo(String value)
    {
	NullCheck.notNull(value, "value");
	setEnteredText(value, "value");
    }

    String getCc()
    {
	return getEnteredText(CC_NAME);
    }

void setCc(String value)
    {
	NullCheck.notNull(value, "value");
	setEnteredText(CC_NAME, value);
    }

    String getSubject()
    {
	return getEnteredText(SUBJECT_NAME);
    }

    String getText()
    {
	return lines.getWholeText();
    }

    Path [] getAttachments()
    {
	final Path[] res = new Path[attachments.size()];
	for(int i = 0;i < attachments.size();++i)
	    res[i] = attachments.get(i).path;
	return res;
    }

    @Override public String getAreaName()
    {
	return strings.appName();
    }

    void addAttachment(File file)
    {
	NullCheck.notNull(file, "file");
	for(Attachment a: attachments)
	    if (a.path.equals(file.toPath()))
	    {
		luwrain.message("Файл " + file.getName() + " уже прикреплён к сообщению", Luwrain.MessageType.ERROR);
		return;
	    }
	final Attachment a = new Attachment(ATTACHMENT + attachmentCounter, file.toPath());
	++attachmentCounter;
	attachments.add(a);
	addStatic(a.name, strings.attachment(file), a);
    }

    void removeAttachment(int lineIndex, Attachment attachment)
    {
	NullCheck.notNull(attachment, "attachment");
	removeItemOnLine(lineIndex);
	int k;
	for(k = 0;k < attachments.size();++k)
	    if (attachments.get(k).name.equals(attachment.name))
		break;
	if (k >= attachments.size())//Should never happen
	    return;
	attachments.remove(k);
    }


    MailMessage constructMailMessage()
    {
	final MailMessage msg = new MailMessage();
	msg.to = Utils.splitAddrs(getEnteredText(TO_NAME));
	msg.cc = Utils.splitAddrs(getEnteredText(CC_NAME));
	msg.subject = getEnteredText(SUBJECT_NAME);
	msg.baseContent = getText();
	final LinkedList<String> attachments = new LinkedList<String>();
	for(int i = 0;i < getItemCount();++i)
	{
	    if (getItemTypeOnLine(i) != FormArea.Type.STATIC)
		continue;
	    final Object o = getItemObjOnLine(i);
	    if (o == null || !(o instanceof Attachment))
		continue;
	    final Attachment a = (Attachment)o;
	    attachments.add(a.path.toString());
	}
	msg.attachments = attachments.toArray(new String[attachments.size()]);
	return msg;
    }

    boolean isReadyForSending()
    {
	if (getEnteredText(TO_NAME).trim().isEmpty())
	{
	    luwrain.message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);
	    setHotPoint(0, 0);
	    return false;
	}
	if (getEnteredText(SUBJECT_NAME).trim().isEmpty())
	{
	    luwrain.message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    setHotPoint(0, 2);
	    return false;
	}
	return true;
    }



    private MultilineEdit.Model createMultilineEditModel(String[] initialText)
    {
	NullCheck.notNullItems(initialText, "initialText");
	lines.setLines(initialText);
	return new TextModel(lines, getMultilineEditHotPointControl());
    }
}
