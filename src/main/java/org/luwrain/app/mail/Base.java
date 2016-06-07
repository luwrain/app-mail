
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

class Base
{
    static private final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private MailStoring storing;
    private StoredMailFolder currentFolder = null;
    private StoredMailMessage currentMessage;
    private TreeModelSource treeModelSource;
    private TreeArea.Model foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    boolean init(Luwrain luwrain,
			Actions actions,
			Strings strings)
    {
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	final Object obj = luwrain.getSharedObject(SHARED_OBJECT_NAME);
	if (obj == null || !(obj instanceof org.luwrain.pim.mail.Factory))
	    return false;
	final org.luwrain.pim.mail.Factory factory = (org.luwrain.pim.mail.Factory)obj;
	final Object obj2 = factory.createMailStoring();
	if (obj2 == null || !(obj2 instanceof MailStoring))
	    return false;
	storing = (MailStoring)obj2;
	return true;
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
	    String replyTo = Utils.getReplyTo(bytes);
	    if (replyTo.trim().isEmpty())
		replyTo = from;
	    final StringBuilder newBody = new StringBuilder();
	    newBody.append(strings.replyFirstLine(Utils.getDisplayedAddress(from), m.getSentDate()) + "\n");
	    newBody.append("\n");
	    for(String s: m.getBaseContent().split("\n"))
		newBody.append(">" + s + "\n");
	    if (wideReply)
	    {
		luwrain.launchApp("message", new String[]{
			replyTo,
			new MailEssentialJavamail().constructWideReplyCcList(bytes, true),
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

    boolean onFolderUniRefQuery(ObjectUniRefQuery query, FolderWrapper wrapper)
    {
	NullCheck.notNull(query, "query");
	NullCheck.notNull(wrapper, "wrapper");
	if (wrapper.folder() == null)
	    return false;
	try {
	    final String uniRef = storing.getFolderUniRef(wrapper.folder());
	    if (uniRef == null || uniRef.trim().isEmpty())
		return false;
	    query.setUniRef(uniRef);
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
