/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;
import org.luwrain.io.json.*;
import org.luwrain.app.base.*;
import org.luwrain .util.*;
import org.luwrain.nlp.*;
import org.luwrain.io.json.*;

final class MainLayout extends LayoutBase
{
    final App app;
    final MessageArea messageArea;
    final FormSpellChecking spellChecking;
    boolean modified = false;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	this.spellChecking =new FormSpellChecking(getLuwrain());
	final var sett = org.luwrain.core.Settings.createPersonalInfo(app.getLuwrain().getRegistry());
	final List<String> text = new ArrayList<>();
	if (app.message.getText() != null)
	    text.addAll(app.message.getText());
	text.add("");
	text.addAll(Arrays.asList(TextUtils.splitLinesAnySeparator(sett.getSignature(""))));
	final MessageArea.Params params = new MessageArea.Params();
	params.context = getControlContext();
	params.name = app.getStrings().appName();
	params.text = text.toArray(new String[text.size()]);
	params.to = app.message.getTo() != null?app.message.getTo().trim():"";
	params.subject = app.message.getSubject() != null?app.message.getSubject().trim():"";
	if (app.message.getAttachments() != null)
	    params.attachments = app.message.getAttachments().toArray(new String[app.message.getAttachments().size()]);
	this.messageArea = new MessageArea(params){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			    return app.send(getMailMessage(), false);
			    			case IDLE:
			    return onIdle();
			}
		    return super.onSystemEvent(event);
		}
	    };
	messageArea.getMultilineEditChangeListeners().add((area, event, lines, hotPoint)->{ modified = true; });
	messageArea.getMultilineEditChangeListeners().add(spellChecking);
	setAreaLayout(messageArea, actions(
					   action("sent", app.getStrings().actionSend(), ()->app.send(getMailMessage(), false)),
					   action("attach", app.getStrings().actionAttachFile(), new InputEvent(InputEvent.Special.INSERT), this::actAttachFile),
					   action("delete-attachment", app.getStrings().actionDeleteAttachment(), this::actDeleteAttachment)
					   ));
    }

    private boolean actEditTo()
    {
	final String res = app.getConv().editTo();
	if (res != null)
	    messageArea.setTo(res);
	return true;
    }

    private boolean actEditCc(MessageArea area)
    {
	final String res = app.getConv().editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    private boolean actAttachFile()
    {
	final File file = app.getConv().attachment();
	if (file == null)
	    return true;
	messageArea.addAttachment(file.getAbsoluteFile());
	return true;
    }

    private boolean actDeleteAttachment()
    {
	final int index = messageArea.getHotPointY();
	final MessageArea.Attachment a = messageArea.getAttachmentByLineIndex(index);
	if (a == null)
	    return false;
	if (!app.getConv().confirmAttachmentDeleting(a.getFile()))
	    return true;
	messageArea.removeAttachmentByLineIndex(index);
	app.message("Прикрепление " + a.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }

        private boolean onIdle()
    {
	if (!messageArea.isHotPointInMultilineEdit())
	    return false;
	final MarkedLines lines = messageArea.getMultilineEditContent();
	final int
	x = messageArea.getMultilineEditHotPoint().getHotPointX(),
	y = messageArea.getMultilineEditHotPoint().getHotPointY();
	if (y >= lines.getLineCount())
	    return true;
	final LineMarks marks = lines.getLineMarks(y);
	if (marks == null)
	    return  true;
	final LineMarks.Mark[] atPoint = marks.findAtPos(x);
	if (atPoint == null || atPoint.length == 0)
	    return true;
	for(LineMarks.Mark m: atPoint)
	{
	    if (m.getMarkObject() == null || !(m.getMarkObject() instanceof SpellProblem))
		continue;
	    final SpellProblem p = (SpellProblem)m.getMarkObject();
	    app.message(p.getComment(), Luwrain.MessageType.ANNOUNCEMENT);
	    return true;
	}
	return true;
    }

    private boolean isReadyForSending()
    {
	if (messageArea.getTo().trim().isEmpty())
	{
	    app.message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);//FIXME:
	    messageArea.focusTo();
	    return false;
	}
	if (messageArea.getSubject().trim().isEmpty())
	{
	    app.message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    messageArea.focusSubject();
	    return false;
	}
	return true;
    }

    private org.luwrain.pim.mail.Message getMailMessage()
    {
	final var msg = new org.luwrain.pim.mail.Message();
	/*
	msg.setTo(App.splitAddrs(messageArea.getTo()));
	msg.setCc(App.splitAddrs(messageArea.getCc()));
	msg.setSubject(messageArea.getSubject());
	msg.setText(messageArea.getText("\n"));
	final MessageContentType contentType = new MessageContentType();
	contentType.setType(MessageContentType.PLAIN);
	contentType.setCharset("UTF-8");
	contentType.setEncoding(MessageContentType.BASE64);
	msg.setContentType(contentType.toString());
	final List<String> a = new ArrayList<>();
	for(File f: messageArea.getAttachmentFiles())
	    a.add(f.getAbsolutePath());
	msg.setAttachments(a.toArray(new String[a.size()]));
	*/
	return msg;
    }
}
