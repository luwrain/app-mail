/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.pim.mail2.persistence.dao.*;
import org.luwrain.pim.mail2.persistence.model.*;
import org.luwrain.pim.mail2.persistence.*;

import static org.luwrain.pim.mail2.FolderProperties.*;

public final class Data
{
    public final FolderDAO folderDAO = MailPersistence.getFolderDAO();
    public final MessageDAO messageDAO = MailPersistence.getMessageDAO();
    public final AccountDAO accountDAO = MailPersistence.getAccountDAO();

    Data()
    {
	if (folderDAO.getRoot() == null)
	    createInitialFolders();
    }

    private void createInitialFolders()
    {
	final var t = "true";
	final var root = new Folder();
	root.setName("Почтовые группы");
	folderDAO.add(root);
	folderDAO.setRoot(root);
	var f = new Folder();
	f.setName("Входящие");
	f.getProperties().setProperty(DEFAULT_INCOMING, t);
	f.setParentFolderId(root.getId());
	folderDAO.add(f);
	f = new Folder();
	f.setName("Рассылки");
	f.getProperties().setProperty(DEFAULT_MAILING_LISTS, t);
	f.setParentFolderId(root.getId());
	folderDAO.add(f);
	f = new Folder();
	f.setName("Исходящие");
	f.getProperties().setProperty(DEFAULT_OUTGOING, t);
	f.setParentFolderId(root.getId());
	folderDAO.add(f);
	f = new Folder();
	f.setName("Отправленные");
	f.getProperties().setProperty(DEFAULT_SENT, t);
	f.setParentFolderId(root.getId());
	folderDAO.add(f);
	f = new Folder();
	f.setName("Черновики");
	f.setParentFolderId(root.getId());
	folderDAO.add(f);
    }
}
