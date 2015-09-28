
package org.luwrain.app.mail;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.pim.mail.*;

class Base
{
    static private final String SHARED_OBJECT_NAME = "luwrain.pim.mail";

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private MailStoring storing;
    private StoredMailFolder currentFolder = null;
    private TreeModelSource treeModelSource;
    private TreeModel foldersModel;
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

    TreeModel getFoldersModel()
    {
	if (foldersModel != null)
	    return foldersModel;
	treeModelSource = new TreeModelSource(storing, strings);
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

    /*
    public boolean isStoredMailGroup(Object obj)
    {
	return obj != null && (obj instanceof StoredMailGroup);
    }
    */

    boolean openFolder(StoredMailFolder folder)
    {
	if (folder == null)
	    return false;
	currentFolder = folder;
	try {
	    final StoredMailMessage[] messages = storing.loadMessages(currentFolder);
	    summaryModel.setMessages(messages);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    boolean insertMessages()
    {
	if (currentFolder == null)
	    return false;
	final File file = Popups.file(luwrain, "Добавление сообщений", "Выберите каталог с файлами сообщений для добавления:",
				      luwrain.launchContext().userHomeDirAsFile(), FilePopup.DIRECTORY, 0);
	if (file == null)
	    return true;
	final MailEssentialJavamail mail = new MailEssentialJavamail();
	final File[] files = file.listFiles();
	for(File f: files)
	{
	    if (f.isDirectory())
		continue;
	    try {
		final MailMessage message = mail.loadMailFromFile(new FileInputStream(f.getAbsolutePath()));
		if (message == null)
		    continue;
		storing.saveMessage(currentFolder, message);
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
	return true;
    }

    void makeReply(StoredMailMessage message) throws Exception
    {
	NullCheck.notNull(message, "message");
	final StringBuilder newBody = new StringBuilder();
	newBody.append("writes:\n\n");
	for(String s: message.getBaseContent().split("\n"))
	    newBody.append(">" + s + "\n");

	luwrain.launchApp("message", new String[]{
		"mail@mail.ru",
		"subject",
		newBody.toString()
});
    }

    static String getDisplaiedAddress(String addr)
    {
	NullCheck.notNull(addr, "addr");
	if (addr.trim().isEmpty())
	    return addr;
	try {
	    final javax.mail.internet.InternetAddress inetAddr = new javax.mail.internet.InternetAddress(addr, false);
	final String personal = inetAddr.getPersonal();
	if (personal == null || personal.trim().isEmpty())
	    return addr;
	//	System.out.println(personal);
	return personal;
	}
	catch (javax.mail.internet.AddressException e)
	{
	    e.printStackTrace();
	    return addr;
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
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
	}
}
