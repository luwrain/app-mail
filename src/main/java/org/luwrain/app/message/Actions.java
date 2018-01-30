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

import org.luwrain.core.*;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    final Conversations conv;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
	this.conv = new Conversations(luwrain, base, strings);
    }

    boolean onSend(Base base, Area area, boolean useAnotherAccount)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	if (!area.isReadyForSending())
	    return false;
	return base.send(area.constructMailMessage(), useAnotherAccount);
    }

    boolean onEditTo(Area area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editTo();
	if (res != null)
	    area.setTo(res);
	return true;
    }

    boolean onEditCc(Area area)
    {
	NullCheck.notNull(area, "area");
	final String res = conv.editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    boolean onAttachFile(Area area)
    {
	NullCheck.notNull(area, "area");
	final File file = conv.attachment();
	if (file == null)
	    return true;
	area.addAttachment(file);
	return true;
    }

boolean onDeleteAttachment(Area area)
    {
	NullCheck.notNull(area, "area");
	final int index = area.getHotPointY();
	if (area.getItemTypeOnLine(index) != Area.Type.STATIC)
	    return false;
	final Object obj = area.getItemObjOnLine(index);
	if (obj == null || !(obj instanceof Attachment))
	    return false;
	final Attachment a = (Attachment)obj;
	if (!conv.confirmAttachmentDeleting(a.file))
	    return true;
	area.removeAttachment(index, a);
	luwrain.message("Прикрепление " + a.file.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }
}
