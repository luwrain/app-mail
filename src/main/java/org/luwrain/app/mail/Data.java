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

import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.persistence.dao.*;
import org.luwrain.pim.mail.persistence.model.*;
import org.luwrain.pim.mail.persistence.*;

import static org.luwrain.pim.mail.FolderProperties.*;
import static org.luwrain.app.mail.App.*;
import static org.luwrain.util.TextUtils.*;

public final class Data
{
    public final Strings strings;
    public final FolderDAO folderDAO = MailPersistence.getFolderDAO();
    public final MessageDAO messageDAO = MailPersistence.getMessageDAO();
    public final AccountDAO accountDAO = MailPersistence.getAccountDAO();
    final File userSettingsFile;
    final List<String> messageLines = new ArrayList<>();
    final List<MessageContentItem> messageAttachments = new ArrayList<>();

    Data(Strings strings, File userSettingsFile)
    {
	this.strings = strings;
	this.userSettingsFile = userSettingsFile;
	if (folderDAO.getRoot() == null)
	    createInitialFolders();
	/*
	if (accountDAO.getAll().isEmpty() && userSettingsFile != null )
	    createInitialAccounts();
	*/
    }

    List<Message> getMessagesInLocalFolder(int folderId)
    {
	final var mm = messageDAO.getByFolderId(folderId);
		final var m = new ArrayList<Message>();
	m.ensureCapacity(mm.size());
	mm.forEach(i -> m.add(new Message(i)));
	return m;
    }

    void setMessage(Message message)
    {
	messageLines.clear();
	messageAttachments.clear();
	messageLines.add(strings.messageAreaFrom() + " " + ((message.getMetadata().getFromAddr() != null && !message.getMetadata().getFromAddr().trim().isEmpty())?message.getMetadata().getFromAddr().trim():"(без отправителя)"));//FIXME:
	messageAttachments.add(null);
	messageLines.add(strings.messageAreaSubject() + " " + ((message.getMetadata().getSubject() != null && !message.getMetadata().getSubject().trim().isEmpty())?message.getMetadata().getSubject().trim():"(без темы)"));//FIXME:
	messageAttachments.add(null);
	MessageContentItem plain = null, plainAlternative = null;
	for(var i: message.getContentItems())
	{
	    final boolean isTextPlain = i.getContentType().toLowerCase().startsWith("text/plain");
	    if (isTextPlain)
	    {
		if (i.isAlternative())
		    plainAlternative = i; else
		    plain = i;
	    }
	    if (i.getDisposition() != null && i.getDisposition().toLowerCase().startsWith("attachment"))
	    {
		messageLines.add("" + i.getFileName()); //FIXME: something if empty 
		messageAttachments.add(i);
	    }
	}
	
	    if (plain == null)
		plain = plainAlternative;
	    if (plain != null && plain.getText() != null)
		for(var s: splitLinesAnySeparator(plain.getText()))
								   messageLines.add(s.trim());
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
	    log.error("Unable to load user accounts settings", e);
	}
    }
}
