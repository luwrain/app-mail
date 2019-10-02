
package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;

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
	    b.append("," + items[i]);
	return new String(b);
    }

    static String[] splitLines(String str)
    {
	NullCheck.notNull(str, "str");
	if (str.isEmpty())
	    return new String[0];
	final String[] res = str.split("\n", -1);
	for(int i = 0;i < res.length;++i)
	    res[i] = res[i].replaceAll("\r", "");
	return res;
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

    static Document createDocForMessage(MailMessage message) throws PimException
    {
	NullCheck.notNull(message, "message");
	final NodeBuilder builder = new NodeBuilder();
	builder.addParagraph("ОТ: " + message.getFrom());
	builder.addParagraph("Кому: " + listToString(message.getTo()));
	builder.addParagraph("Копия: " + listToString(message.getCc()));
	builder.addParagraph("Тема: " + message.getSubject());
	builder.addParagraph("Время: " + message.getSentDate());
	builder.addParagraph("Тип данных: " + message.getContentType());
	//nodes.add(NodeFactory.newEmptyLine());
	//attachments = message.getAttachments();
	for(String line: splitLines(message.getText()))
	    if (!line.isEmpty())
		builder.addParagraph(line); else
		builder.addEmptyLine();
	final Node root = builder.newRoot(); 
	final Document doc = new Document(root);
	doc.setProperty("url", "http://localhost");
	doc.commit();
	return doc;
    }
}
