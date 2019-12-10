/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

class MessageArea extends FormArea
{
    static private final String HOOKS_PREFIX = "luwrain.message.edit";

    static final String TO_NAME = "to";
    static final String CC_NAME = "cc";
    static final String SUBJECT_NAME = "subject";
    static final String ATTACHMENT = "attachment";

    private final Luwrain luwrain;
    private final Strings strings;
    private final MutableLinesImpl lines;
    private int attachmentCounter = 0;

    MessageArea(Luwrain luwrain, Strings strings, MessageContent msg)
    {
	super(new DefaultControlContext(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(msg, "msg");
	this.luwrain = luwrain;
	this.strings = strings;
		this.lines = new MutableLinesImpl(msg.textLines());
	addEdit(TO_NAME, strings.to(), msg.to);
	addEdit(CC_NAME, strings.cc(), msg.cc);
	addEdit(SUBJECT_NAME, strings.subject(), msg.subject);

	activateMultilineEdit(strings.enterMessageBelow(), lines, createEditParams(), true);
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

    void focusTo()
    {
	setHotPoint(0, 0);
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

    void focusSubject()
    {
	setHotPoint(0, 2);
    }

    String getText()
    {
	return lines.getWholeText();
    }

    Attachment[] getAttachments()
    {
	final List<Attachment> res = new LinkedList();
		for(int i = 0;i < getItemCount();++i)
	{
	    if (getItemTypeOnLine(i) != FormArea.Type.STATIC)
		continue;
	    final Object o = getItemObj(i);
	    if (o == null || !(o instanceof Attachment))
		continue;
	    res.add((Attachment)o);
	}
		return res.toArray(new Attachment[res.size()]);
    }

    File[] getAttachmentFiles()
    {
	final Attachment[] attachments = getAttachments();
	final File[] res = new File[attachments.length];
	for(int i = 0;i < attachments.length;++i)
	    res[i] = attachments[i].file;
	return res;
    }

    void addAttachment(File file)
    {
	NullCheck.notNull(file, "file");
	for(Attachment a: getAttachments())
	    if (a.file.equals(file))
	    {
		luwrain.message("Файл " + file.getName() + " уже прикреплён к сообщению", Luwrain.MessageType.ERROR);//FIXME:
		return;
	    }
	final Attachment a = new Attachment(ATTACHMENT + attachmentCounter, file);
	++attachmentCounter;
	addStatic(a.name, strings.attachment(file.getName()), a);
    }

    void removeAttachment(int lineIndex)
    {
	removeItemOnLine(lineIndex);
    }

    MailMessage constructMailMessage() throws org.luwrain.pim.PimException
    {
	final MailMessage msg = new MailMessage();
	msg.setTo(Base.splitAddrs(getEnteredText(TO_NAME)));
	msg.setCc(Base.splitAddrs(getEnteredText(CC_NAME)));
	msg.setSubject(getEnteredText(SUBJECT_NAME));
	msg.setText(getText());
	final List<String> attachments = new LinkedList();
	for(File f: getAttachmentFiles())
	    attachments.add(f.getAbsolutePath());
	msg.setAttachments(attachments.toArray(new String[attachments.size()]));
	return msg;
    }

    private MultilineEdit.Params createEditParams()
    {
	final MultilineEdit.Params params = createMultilineEditParams(context, lines);
	final MultilineEditCorrector corrector = (MultilineEditCorrector)params.model;
	params.model = new DirectScriptMultilineEditCorrector(context, corrector, HOOKS_PREFIX);
	return params;
    }
}
