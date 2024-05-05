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
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.pim.mail2.persistence.dao.*;
import org.luwrain.pim.mail2.persistence.model.*;
import org.luwrain.pim.mail2.persistence.*;

import static org.luwrain.pim.mail2.FolderProperties.*;
import static org.luwrain.app.mail.App.*;

public final class Data
{
    public final FolderDAO folderDAO = MailPersistence.getFolderDAO();
    public final MessageDAO messageDAO = MailPersistence.getMessageDAO();
    public final AccountDAO accountDAO = MailPersistence.getAccountDAO();
    final File userSettingsFile;

    Data(File userSettingsFile)
    {
	this.userSettingsFile = userSettingsFile;
	if (folderDAO.getRoot() == null)
	    createInitialFolders();
	if (accountDAO.getAll().isEmpty() && userSettingsFile != null )
	    createInitialAccounts();
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

    private void createInitialAccounts()
    {
	if (!userSettingsFile.exists())
	    return;
	log.debug("User settings file is " + userSettingsFile.getAbsolutePath());
	try {
	    final var p = new Properties();
	    try (final var r = new BufferedReader(new InputStreamReader(new FileInputStream(userSettingsFile), "UTF-8"))) {
		p.load(r);
	    }
	    final String
	    pop3Host = p.getProperty("pop3.host"),
	    pop3Port = p.getProperty("pop3.port"),
	    pop3Login = p.getProperty("pop3.login"),
	    pop3Passwd = p.getProperty("pop3.passwd");
	    if (pop3Host != null && !pop3Host.trim().isEmpty() &&
		pop3Login != null && !pop3Login.trim().isEmpty())
	    {
		final var a = new Account();
		a.setName("Automatically created from default settings POP3 account  on " + pop3Host.trim());
		a.setType(Account.Type.POP3);
		a.setHost(pop3Host.trim());
		a.setPort((pop3Port != null && !pop3Port.trim().isEmpty())?Integer.parseInt(pop3Port.trim()):110);
		a.setPasswd(pop3Passwd.trim());
		a.setLogin(pop3Login.trim());
		a.setEnabled(true);
		a.setDefaultAccount(false);
		a.setLeaveMessages(true);
		a.setTrustedHosts("*");
		a.setSsl(true);
		accountDAO.add(a);
		log.debug("Added the POP3 account: host=" + a.getHost() + ", login=" + a.getLogin() + ", port=" + a.getPort());
	    }
	}
	catch(Exception e)
	{
	    log.error("Unable to load user accounts settings: " + e.getMessage());
	}
    }
}
