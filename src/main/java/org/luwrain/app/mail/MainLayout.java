/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ListArea.*;
import org.luwrain.controls.ListUtils.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.pim.mail.script.*;
import org.luwrain.app.base.*;

import static org.luwrain.script.ScriptUtils.*;
import static org.luwrain.core.DefaultEventResponse.*;

final class MainLayout extends LayoutBase implements TreeArea.ClickHandler, ClickHandler<SummaryItem>
{
    static private final String
	LOG_COMPONENT = App.LOG_COMPONENT;

    private final App app;
    final TreeArea foldersArea;
    final ListArea<SummaryItem> summaryArea;
    final ReaderArea messageArea;

    private final List<SummaryItem> summaryItems = new ArrayList<>();
    private MailFolder folder = null;
    private MailMessage message = null;

    MainLayout(App app)
    {
	super(app);
	this.app = app;

	this.foldersArea = new TreeArea(treeParams((params)->{
		    params.model = new CachedTreeModel(new FoldersModel());
		    params.name = app.getStrings().foldersAreaName();
		    params.clickHandler = this;
		})) {
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case PROPERTIES:
			    return onFolderProps();
			}
		    return super.onSystemEvent(event);
		}
	    };

	this.summaryArea = new ListArea<>(listParams((params)->{
		    params.name = app.getStrings().summaryAreaName();
		    params.model = new ListModel<>(summaryItems);
		    params.clickHandler = this;
		    params.appearance = new DoubleLevelAppearance<SummaryItem>(getControlContext()){
			    @Override public boolean isSectionItem(SummaryItem item)
			    {
				return item.type == SummaryItem.Type.SECTION;
			    }
			};
		    params.transition = new DoubleLevelTransition<SummaryItem>(params.model){
			    @Override public boolean isSectionItem(SummaryItem item)
			    {
				return item.type == SummaryItem.Type.SECTION;
			    }
			};
		}));

	final ActionInfo
	fetchIncomingBkg = action("fetch-incoming-bkg", app.getStrings().actionFetchIncomingBkg(), new InputEvent(InputEvent.Special.F6), app::fetchIncomingBkg);

	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, actions(
								       action("new-folder", "Новая группа", new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actNewFolder),
								       fetchIncomingBkg),

		      summaryArea, actions(
					   fetchIncomingBkg
					   ));
	messageArea = null;
    }

    private boolean actNewFolder()
    {
	final Object obj = foldersArea.selected();
	if (obj == null || !(obj instanceof MailFolder))
	    return true;
	final MailFolder folder = (MailFolder)obj;
	final MailFolder newFolder = new MailFolder();
	newFolder.setTitle("Новая группа");//FIXME:
	try {
	    app.getMailStoring().getFolders().save(folder, newFolder);
	}
	catch(PimException e)
	{
	    app.getLuwrain().crash(e);
	    return true;
	}
	foldersArea.refresh();
	return true;
    }

    private boolean onFolderProps()
    {
	final Object obj = foldersArea.selected();
	if (!(obj instanceof MailFolder))
	    return false;
	final FolderPropertiesLayout propsLayout = new FolderPropertiesLayout(app, (MailFolder)obj, ()->{
		app.layout(getAreaLayout());
		foldersArea.refresh();
		app.getLuwrain().announceActiveArea();
	    });
	app.layout(propsLayout.getLayout());
	app.getLuwrain().announceActiveArea();
	return true;
    }

    @Override public boolean onTreeClick(TreeArea area, Object obj)
    {
	if (obj == null || !(obj instanceof MailFolder))
	    return false;
	this.folder = (MailFolder)obj;
	try {
	    final MailMessage[] messages = app.getStoring().getMessages().loadNoDeleted(folder);
	    this.summaryItems.clear();
	    this.summaryItems.addAll(organizeSummary(messages));
	}
	catch(PimException e)
	{
	    app.getLuwrain().crash(e);
	    return true;
	}
	summaryArea.refresh();
	summaryArea.reset(false);
	app.getLuwrain().setActiveArea(summaryArea);
	return true;
    }

    @Override public boolean onListClick(ListArea area, int index, SummaryItem item)
    {
	final MailMessage message = item.message;
	if (message == null)
	    return false;
	try {
	    if (message.getState() == MailMessage.State.NEW)
	    {
		message.setState(MailMessage.State.READ);
		summaryArea.refresh();
	    }
	    messageArea.setDocument(Utils.createDocForMessage(message, app.getStrings()), 128);
	    return true;
	}
	catch(PimException e)
	{
	    app.getLuwrain().crash(e);
	    return true;
	}
    }

    private boolean actDeleteMessage(ListArea summaryArea, boolean deleteForever)
    {
	/*
	  NullCheck.notNull(summaryArea, "summaryArea");
	final Object o = summaryArea.selected();
	if (o == null || !(o instanceof SummaryItem))
	    return false;
	final SummaryItem item = (SummaryItem)o;
	if (item.message == null)
	    return false;
	try {
	    if (deleteForever)
		base.storing.getMessages().delete(item.message); else
		item.message.setState(MailMessage.State.DELETED);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	*/
	return true;
    }

    private boolean actReply(ListArea summaryArea)
    {
	/*
	  NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof SummaryItem))
	    return false;
	final SummaryItem summaryItem = (SummaryItem)obj;
	if (summaryItem.message == null)
	    return false;

	return base.hooks.makeReply(summaryItem.message);
	*/
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

    private String[] getCcExcludeAddrs()
    {
	/*
	final org.luwrain.core.Settings.PersonalInfo sett = org.luwrain.core.Settings.createPersonalInfo(luwrain.getRegistry());
	final String addr = sett.getDefaultMailAddress("");
	if (addr.trim().isEmpty())
	    return new String[0];
	return new String[]{addr};
	*/
	return null;
    }

    private List<SummaryItem> organizeSummary(MailMessage[] messages)
    {
	final MessageObj [] hookObjs = new MessageObj[messages.length];
	for(int i = 0;i < messages.length;i++)
	    hookObjs[i] = new MessageObj(messages[i]);
	final Object[] args = new Object[]{getArray(hookObjs)};
	final Object res;
	final List<SummaryItem> items = new ArrayList<>();
	try {
	    res = getHooks().provider(getLuwrain(), App.HOOK_ORGANIZE_SUMMARY, args);
	}
	catch(RuntimeException e)
	{
	    Log.error(LOG_COMPONENT, "unable to run the " + App.HOOK_ORGANIZE_SUMMARY + ": " + e.getClass().getName() + ": " + e.getMessage());
	    app.crash(e);
	    return items;
	}
	if (res == null)
	    return items;
	final Object[] array = asArray(res);
	if (array == null)
	    return items;
	for(Object o: array)
	    items.add(new SummaryItem(o));
	return items;
    }

    private class FoldersModel implements org.luwrain.controls.CachedTreeModelSource
    {
	@Override public Object getRoot()
	{
	    try {
		return app.getStoring().getFolders().getRoot();
	    }
	    catch (PimException e)
	    {
		app.getLuwrain().crash(e);
		return null;
	    }
	}
	@Override public Object[] getChildObjs(Object obj)
	{
	    NullCheck.notNull(obj, "obj");
	    final MailFolder folder = (MailFolder)obj;
	    try {
		return app.getStoring().getFolders().load(folder);
	    }
	    catch(PimException e)
	    {
		app.getLuwrain().crash(e);
		return new Object[0];
	    }
	}
    }
}
