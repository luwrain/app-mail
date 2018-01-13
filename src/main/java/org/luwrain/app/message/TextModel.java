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

import org.luwrain.core.*;
import org.luwrain.controls.*;

class TextModel extends MultilineEditModelTranslator
{
    private final int maxLineLen = 60;

    TextModel(MutableLines lines, HotPointControl hotPoint)
    {
	super(lines, hotPoint);
    }

    @Override public  void insertChars(int pos , int lineIndex, String str)
    {
	super.insertChars(pos, lineIndex, str);
	processLine(lineIndex);
    }

    private void processLine(int index)
    {

	final String line = getLine(index);
	if (line == null || line.length() <= maxLineLen)
	    return;
	int pos = maxLineLen;
	while(pos >= 0 && !Character.isSpace(line.charAt(pos)))
	    --pos;
	if (pos < 0)
	{
	    pos = maxLineLen;
	    while (pos < line.length() && !Character.isSpace(line.charAt(pos)))
		++pos;
	    if (pos >= line.length())//There are no spaces in the line at all
		return;
	}
	if (pos + 1 >= line.length())
	    return;
	splitLines(pos, index);
	while(true)
	{
	final String newLine = getLine(index + 1);
	if (newLine.isEmpty() || !Character.isSpace(newLine.charAt(0)))
	    break;
	deleteChar(0, index + 1);
	}
	processLine(index + 1);

    }
}
