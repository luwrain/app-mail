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

package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.util.*;

class Utils
{
    static String listToString(String[] items)
    {
	NullCheck.notNullItems(items, "items");
	if (items.length == 0)
	    return "";
	final StringBuilder b = new StringBuilder();
	b.append(items[0]);
	for(int i = 1;i < items.length;++i)
	    b.append(", ").append(items[i]);
	return new String(b);
    }

    static String[] splitLines(String str)
    {
	NullCheck.notNull(str, "str");
	if (str.isEmpty())
	    return new String[0];
	return FileUtils.universalLineSplitting(str);
    }

    static String getReplyTo(byte[] bytes) throws PimException, java.io.IOException
    {
	return "";
	/*
	final MailUtils utils = new MailUtils(bytes);
	final String[] res = utils.getReplyTo(true);
	if (res == null || res.length < 1)
	    return "";
	return res[0];
	*/
    }

    static Document createDocForMessage(MailMessage message, Strings strings) throws PimException
    {
	NullCheck.notNull(message, "message");
	final NodeBuilder builder = new NodeBuilder();
	builder.addParagraph(strings.messageAreaFrom() + " " + message.getFrom());
	builder.addParagraph(strings.messageAreaTo() + " " + listToString(message.getTo()));
	builder.addParagraph(strings.messageAreaCc() + " " + listToString(message.getCc()));
	builder.addParagraph(strings.messageAreaSubject() + " " + message.getSubject());
	builder.addParagraph(strings.messageAreaDate() + " " + message.getSentDate());
	builder.addParagraph(strings.messageAreaContentType() + " " + message.getContentType());
	if (message.getAttachments().length > 0)
	{
	    		builder.addEmptyLine();
	for(String a: message.getAttachments())
	    builder.addParagraph(strings.messageAreaAttachment() + " " + a);
	}
			builder.addEmptyLine();
	for(String line: splitLines(message.getText()))
	    if (!line.trim().isEmpty())
		builder.addParagraph(line); else
		builder.addEmptyLine();
	final Document doc = new Document(builder.newRoot());
	doc.commit();
	return doc;
    }
}
