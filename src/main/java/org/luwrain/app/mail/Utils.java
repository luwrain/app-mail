
package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.pim.*;

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
}
