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

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.*;
import org.luwrain.pim.*;
import org.luwrain.pim.contacts.*;
import org.luwrain.popups.pim.*;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
    }

    Action[] getActions()
    {
	return new Action[]{
	    new Action("send", "Отправить"),
	    new Action("send-another-account", "Отправить через учётную запись"),
	    new Action("choose-to", "Выбрать получателя из списка"),
	    new Action("choose-cc", "Выбрать получателей копии из списка"),
	    new Action("attach-file", "Прикрепить файл", new KeyboardEvent(KeyboardEvent.Special.INSERT)),//FIXME:
	};
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
	final String res = selectFromContacts(base);
	if (res != null)
	    area.setTo(res);
	return true;
    }

    boolean onEditCc(Area area)
    {
	NullCheck.notNull(area, "area");
	final String res = editCcList(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    boolean onInsert(Area area)
    {
	NullCheck.notNull(area, "area");
	final File file = Popups.path(luwrain, strings.attachmentPopupName(), strings.attachmentPopupPrefix(), (fileToCheck,announcement)->{
		return true;
	    });
	if (file == null)
	    return true;
	area.addAttachment(file);
	return true;
    }

boolean onDelete(Area area)
    {
	NullCheck.notNull(area, "area");
	final int index = area.getHotPointY();
	if (area.getItemTypeOnLine(index) != Area.Type.STATIC)
	    return false;
	final Object obj = area.getItemObjOnLine(index);
	if (obj == null || !(obj instanceof Attachment))
	    return false;
	final Attachment a = (Attachment)obj;
	if (!Popups.confirmDefaultNo(luwrain, "Удаление прикрепления", "Вы действительно хотите исключить " + a.path.toString() + " из списка прикреплений?"))
	    return true;
	area.removeAttachment(index, a);
	luwrain.message("Прикрепление " + a.path.toString() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }


    private String editCcList(String initial)
    {
	NullCheck.notNull(initial, "initial");
	final String[] items = Utils.splitAddrs(initial);
	final CcEditPopup popup;
	try {
	    popup = new CcEditPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), base.getContactsStoring(), items);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	final String[] newItems = popup.result();
	NullCheck.notNullItems(newItems, "newItems");
	if (newItems.length == 0)
	    return "";
	final StringBuilder b = new StringBuilder();
	b.append((String)newItems[0]);
	for(int i = 1;i < newItems.length;++i)
	    b.append("," + (String)newItems[i]);
	return b.toString();
    }

    String selectFromContacts(Base base)
    {
	NullCheck.notNull(base, "base");
	final ChooseMailPopup popup;
	try {
	    popup = new ChooseMailPopup(luwrain, org.luwrain.popups.pim.Strings.create(luwrain), base.getContactsStoring(), base.getContactsStoring().getFolders().getRoot());
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return null;
	}
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	return popup.result();
    }

    private boolean mayRemove(Object item)
    {
	final YesNoPopup popup = new YesNoPopup(luwrain, "Удаление адреса получателя", "Вы действительно хотите удалить получателя копии \"" + item.toString() + "\"?", false, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return false;
	return popup.result();
    }
}
