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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class MessageArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private StoredMailMessage message;

    private String[] headers = new String[0];
    private String[] attachments = new String[0];
    private String[] text = new String[0];

    MessageArea(Luwrain luwrain, Actions actions,
		Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions =  actions;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strings");
    }

    void show(StoredMailMessage message)
    {
	if (message == null)
	{
	    this.message = null;
	    headers = new String[0];
	    attachments = new String[0];
	    text = new String[0];
	    luwrain.onAreaNewContent(this);
	    return;
	}
	try {
	    this.message = message;
	    final LinkedList<String> headersList = new LinkedList<String>();
	    headersList.add("ОТ: " + message.getFrom());
	    headersList.add("Кому: " + prepareList(message.getTo()));
	    headersList.add("Копия: " + prepareList(message.getCc()));
	    headersList.add("Тема: " + message.getSubject());
	    headersList.add("Время: " + strings.messageSentDate(message.getSentDate()));
	    headersList.add("Тип данных: " + message.getMimeContentType());
	    headers = headersList.toArray(new String[headersList.size()]);
	    headers = headersList.toArray(new String[headersList.size()]);
	    text = prepareText(message.getBaseContent());
	    attachments = message.getAttachments();
	    if (attachments == null)
		attachments = new String[0];
	    //	    System.out.println("attachments:" + attachments.length);
	    luwrain.onAreaNewContent(this);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    this.message = null;
	    attachments = new String[0];
	    headers = new String[0];
	    text = new String[0];
	    luwrain.onAreaNewContent(this);
	}
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.TAB:
	    actions.gotoFolders();
	    return true;
	    case KeyboardEvent.BACKSPACE:
	    actions.gotoSummary();
	    return true;
	    case KeyboardEvent.F9:
		actions.launchMailFetch();
		return true;
	    case KeyboardEvent.ENTER:
		return onEnter();
	    case KeyboardEvent.F5://FIXME:Action
		return actions.makeReply(null, false);
	    case KeyboardEvent.F6://FIXME:Action
		return actions.makeForward(null);
	    }
	if (event.isCommand() && event.withShiftOnly())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.F5://FIXME:Action
		return actions.makeReply(null, true);
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	case EnvironmentEvent.ACTION:
	    if (ActionEvent.isAction(event, "raw-mode"))
	    {
		if (!actions.switchToRawMessage())
		    luwrain.message("Невозможно переключиться к сырому виду сообщения", Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    if (ActionEvent.isAction(event, "reply"))
	    {
		actions.makeReply(null, false);
		return true;
	    }
	    if (ActionEvent.isAction(event, "reply-all"))
	    {
		actions.makeReply(null, true);
		return true;
	    }
	    if (ActionEvent.isAction(event, "forward"))
	    {
		actions.makeForward(null);
		return true;
	    }
	    return false;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	    new Action("raw-mode", "Показать сырой вид"),
	};
    }

    @Override public String getAreaName()
    {
	return strings.messageAreaName();
    }

    @Override public int getLineCount()
    {
	int res = headers.length + text.length + 2;
	if (attachments != null && attachments.length > 0)
	    res += (attachments.length + 1);
return res;
    }

    @Override public String getLine(int index)
    {
	if (index < 0)
	    return "";
	if (index < headers.length)
	    return headers[index];
	if (index == headers.length)
	    return "";
	int offset = headers.length + 1;
	if (attachments != null && attachments.length > 0)
{
    if (index - offset < attachments.length)
	return "Прикреплённый файл: " + attachments[index - offset];
    if (index - offset == attachments.length)
	return "";
    offset += (attachments.length + 1);
}
	if (index - offset < text.length)
	    return text[index - offset];
	return "";
    }

    @Override public void introduceLine(int index, String text)
    {
	if (attachments != null && attachments.length > 0 &&
index >= headers.length + 1 && index < headers.length + attachments.length + 1)
	    luwrain.playSound(Sounds.NEW_LIST_ITEM);
	if (text == null || text.trim().isEmpty())
	    luwrain.hint(Hints.EMPTY_LINE); else
	    luwrain.say(text);
    }

    private boolean onEnter()
    {
	if (attachments == null || attachments.length < 1)
	    return false;
	if (getHotPointY() <= headers.length)
	    return false;
	final int index = getHotPointY() - headers.length - 1;
	if (index >= attachments.length)
	    return false;
	if (attachments[index] == null || attachments[index].isEmpty())
	    return false;
	actions.saveAttachment(attachments[index]);
	return true;
    }

    static private String prepareList(String[] items)
    {
	if (items == null || items.length < 1)
	    return "";
	final StringBuilder b = new StringBuilder();
	b.append(items[0]);
	for(int i = 1;i < items.length;++i)
	    b.append("," + items[i]);
	return b.toString();
    }

    static private String[] prepareText(String str)
    {
	if (str == null || str.trim().isEmpty())
	    return new String[0];
	final String[] res = str.split("\n");
	for(int i = 0;i < res.length;++i)
	    res[i] = res[i].replaceAll("\r", "");
	return res;
    }
}
