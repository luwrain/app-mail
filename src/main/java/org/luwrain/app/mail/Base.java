
package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.util.*;
import org.luwrain.network.*;

class Base
{
    static private final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private final Luwrain luwrain;
    private final MailApp app;
    private final Strings strings;
    private final MailStoring storing;
    private StoredMailFolder currentFolder = null;
    private StoredMailMessage currentMessage;
    private TreeModelSource treeModelSource;
    private TreeArea.Model foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    Base(MailApp app, Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.app = app;
	this.luwrain = luwrain;
	this.strings = strings;
	this.storing = org.luwrain.pim.mail.Factory.getMailStoring(luwrain);
    }

    boolean init()
    {
	return storing != null;
    }

    void setCurrentMessage(StoredMailMessage message)
    {
	this.currentMessage = message;
    }

    boolean hasCurrentMessage()
    {
	return currentMessage != null;
    }

    TreeArea.Model getFoldersModel()
    {
	if (foldersModel != null)
	    return foldersModel;
	treeModelSource = new TreeModelSource(luwrain, storing, strings);
	foldersModel = new CachedTreeModel(treeModelSource);
	return foldersModel;
    }

    SummaryTableModel getSummaryModel()
    {
	if (summaryModel != null)
	    return summaryModel;
	summaryModel = new SummaryTableModel();
	return summaryModel;
    }

    SummaryTableAppearance getSummaryAppearance()
    {
	if (summaryAppearance != null)
	    return summaryAppearance;
	summaryAppearance = new SummaryTableAppearance(luwrain, strings);
	return summaryAppearance;
    }

    boolean openFolder(StoredMailFolder folder)
    {
	NullCheck.notNull(folder, "folder");
	currentFolder = folder;
	try {
	    final StoredMailMessage[] messages = storing.loadMessages(currentFolder);
	    summaryModel.setMessages(messages);
	    return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean makeReply(StoredMailMessage message, boolean wideReply)
    {
	NullCheck.notNull(message, "message");
	try {
	    if (currentMessage == null)
		return false;
	    final StoredMailMessage m = message != null?message:currentMessage;
	    String subject = m.getSubject();
	    if (!subject.toLowerCase().startsWith("re: "))
		subject = "Re: " + subject;
	    final byte[] bytes = m.getRawMail();
	    final String from = m.getFrom();
	    if (from.trim().isEmpty())
		return false;
	    String replyTo = null;
	    try {
replyTo = Utils.getReplyTo(bytes);
	}
	catch(IOException e)
	{
	    luwrain.crash(e);
	}
	    if (replyTo.trim().isEmpty())
		replyTo = from;
	    final StringBuilder newBody = new StringBuilder();
	    newBody.append(strings.replyFirstLine(Utils.getDisplayedAddress(from), m.getSentDate()) + "\n");
	    newBody.append("\n");
	    for(String s: m.getBaseContent().split("\n"))
		newBody.append(">" + s + "\n");
	    if (wideReply)
	    {
		final MailUtils utils = new MailUtils();
		try {
		    utils.load(bytes);
		}
		catch (IOException e)
		{
		    luwrain.crash(e);
		}
		luwrain.launchApp("message", new String[]{
			replyTo,
			utils.constructWideReplyCcList(true),
			subject,
			newBody.toString()
		    });
	    } else 
	    {
		luwrain.launchApp("message", new String[]{
			replyTo,
			subject,
			newBody.toString()
		    });
	    }
	    return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean makeForward(StoredMailMessage message)
    {
	NullCheck.notNull(message, "message");
	try {
	    if (currentMessage == null)
		return false;
	    final StoredMailMessage m = message != null?message:currentMessage;
	    String subject = m.getSubject();
	    if (!subject.toLowerCase().startsWith("fwd: "))
		subject = "Fwd: " + subject;
	    final byte[] bytes = m.getRawMail();
	    final String from = m.getFrom();
	    final StringBuilder newBody = new StringBuilder();
	    newBody.append("=== Пересылаемое сообщение ===\n");
	    newBody.append("ОТ: ");
	    newBody.append(m.getFrom());
	    newBody.append("\n");
	    newBody.append("Кому: ");
	    if (m.getTo().length > 0)
	    {
		final String[] values = m.getTo();
		newBody.append(values[0]);
		for(int i = 1;i < values.length;++i)
		    newBody.append("," + values[i]);
	    }
	    newBody.append("\n");
	    if (m.getCc().length > 0)
	    {
		newBody.append("Копия: ");
		final String[] values = m.getCc();
		newBody.append(values[0]);
		for(int i = 1;i < values.length;++i)
		    newBody.append("," + values[i]);
	    }
	    newBody.append("\n");
	    newBody.append("Тема: " + m.getSubject() + "\n");
	    newBody.append("Дата: " + m.getSentDate() + "\n");
	    newBody.append("\n");
	    for(String s: m.getBaseContent().split("\n"))
		newBody.append(s + "\n");
	    newBody.append("=== Конец пересылаемого сообщения ===");
	    luwrain.launchApp("message", new String[]{
		    "",
		    subject,
		    newBody.toString()
		});
	    return true;
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
	}

    boolean onFolderUniRefQuery(ObjectUniRefQuery query, StoredMailFolder folder)
    {
	NullCheck.notNull(query, "query");
	NullCheck.notNull(folder, "folder");
	try {
	    final String uniRef = storing.getFolderUniRef(folder);
	    if (uniRef == null || uniRef.trim().isEmpty())
		return false;
	    query.answer(uniRef);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
	return true;
    }

    boolean saveAttachment(String fileName)
    {
	/*
	if (currentMessage == null)
	    return false;
	File destFile = new File(luwrain.launchContext().userHomeDirAsFile(), fileName);
	destFile = Popups.file(luwrain, "Сохранение прикрепления", "Введите имя файла для сохранения прикрепления:", destFile, 0, 0);
	if (destFile == null)
	    return false;
	if (destFile.isDirectory())
	    destFile = new File(destFile, fileName);
	final org.luwrain.util.MailEssentialJavamail util = new org.luwrain.util.MailEssentialJavamail();
	try {
	    if (!util.saveAttachment(currentMessage.getRawMail(), fileName, destFile))
	    {
		luwrain.message("Целостность почтового сообщения нарушена, сохранение прикрепления невозможно", Luwrain.MESSAGE_ERROR);
		return false;
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время сохранения прикрепления произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	luwrain.message("Файл " + destFile.getAbsolutePath() + " успешно сохранён", Luwrain.MESSAGE_OK);
	*/
	return true;
    }

    boolean deleteInSummary(StoredMailMessage message)
    {
	NullCheck.notNull(message, "message");
	if (currentFolder == null)
	    return false;
	try {
	    storing.deleteMessage(message);
	summaryModel.setMessages(storing.loadMessages(currentFolder));
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    luwrain.message("Во время удаления сообщения произошла непредвиденная ошибка:" + e.getMessage());
	    return false;
	}
	return true;
    }
}
