/*
   Copyright 2012-2023 Michael Pozhidaev <msp@luwrain.org>

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

import static org.luwrain.app.mail.App.*;
import static org.luwrain.app.mail.Utils.*;

final class MainLayout extends LayoutBase implements TreeListArea.LeafClickHandler<MailFolder>, ClickHandler<SummaryItem>
{
    static private final InputEvent
	HOT_KEY_REPLY = new InputEvent('r', EnumSet.of(InputEvent.Modifiers.ALT));

    final App app;
    final TreeListArea<MailFolder> foldersArea;
    final ListArea<SummaryItem> summaryArea;
    final ReaderArea messageArea;

    private final List<SummaryItem> summaryItems = new ArrayList<>();
    private MailFolder folder = null;
    private MailMessage message = null;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	final TreeListArea.Params<MailFolder> treeParams = new TreeListArea.Params<>();
        treeParams.context = getControlContext();
	treeParams.name = app.getStrings().foldersAreaName();
	treeParams.model = new FoldersModel(app);
	treeParams.leafClickHandler = this;
	this.foldersArea = new TreeListArea<MailFolder>(treeParams) {
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
	this.foldersArea.requery();
	this.summaryArea = new ListArea<>(listParams((params)->{
		    params.name = app.getStrings().summaryAreaName();
		    params.model = new ListModel<>(summaryItems);
		    params.clickHandler = this;
		    params.appearance = new DoubleLevelAppearance<SummaryItem>(getControlContext()){
			    @Override public boolean isSectionItem(SummaryItem item) { return item.type == SummaryItem.Type.SECTION; }
			};
		    params.transition = new DoubleLevelTransition<SummaryItem>(params.model){
			    @Override public boolean isSectionItem(SummaryItem item) { return item.type == SummaryItem.Type.SECTION; }
			};
		}));
	final ReaderArea.Params messageParams = new ReaderArea.Params();
	messageParams.context = getControlContext();
	messageParams.name = app.getStrings().messageAreaName();
	this.messageArea = new ReaderArea(messageParams);
	final ActionInfo
	fetchIncomingBkg = action("fetch-incoming-bkg", app.getStrings().actionFetchIncomingBkg(), new InputEvent(InputEvent.Special.F6), ()->{	getLuwrain().runWorker(org.luwrain.pim.workers.Pop3.NAME); return true;});
	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, actions(
								       action("remove-folder", app.getStrings().actionRemoveFolder(), new InputEvent(InputEvent.Special.DELETE), this::actRemoveFolder),
								       action("new-folder", app.getStrings().actionNewFolder(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actNewFolder),
								       fetchIncomingBkg),
		      summaryArea, actions(
					   action("reply", app.getStrings().actionReply(), HOT_KEY_REPLY, this::actSummaryReply),
					   action("delete", app.getStrings().actionDeleteMessage(), new InputEvent(InputEvent.Special.DELETE), this::actDeleteMessage),
					   action("delete-forever", app.getStrings().actionDeleteMessageForever(), new InputEvent(InputEvent.Special.DELETE, EnumSet.of(InputEvent.Modifiers.SHIFT)), this::actDeleteMessage),
					   fetchIncomingBkg
					   ),
		      messageArea, actions(
					   ));
    }

    @Override public boolean onLeafClick(TreeListArea<MailFolder> area, MailFolder folder)
    {
	this.folder = folder;
	this.summaryItems.clear();
	this.summaryItems.addAll(app.getHooks().organizeSummary(app.getStoring().getMessages().load(folder, (m)->{ return m.getState() != MailMessage.State.DELETED; })));
	summaryArea.refresh();
	summaryArea.reset(false);
	setActiveArea(summaryArea);
	return true;
    }

    @Override public boolean onListClick(ListArea area, int index, SummaryItem item)
    {
	final MailMessage message = item.message;
	if (message == null)
	    return false;
	if (message.getState() == MailMessage.State.NEW)
	{
	    message.setState(MailMessage.State.READ);
	    summaryArea.refresh();
	}
	messageArea.setDocument(createDocForMessage(message, app.getStrings()), 128);
	setActiveArea(messageArea);
	return true;
    }

    private boolean actNewFolder()
    {
	final MailFolder opened = foldersArea.opened();
	if (opened == null)
	    return false;
	final String name = app.getConv().newFolderName();
	if (name == null)
	    return true;
	final int selectedIndex = foldersArea.selectedIndex();
	final MailFolder newFolder = new MailFolder();
	newFolder.setTitle(name);
	app.getMailStoring().getFolders().save(opened, newFolder, Math.max(selectedIndex, 0));
	foldersArea.requery();
	foldersArea.refresh();
	return true;
    }

    private boolean actRemoveFolder()
    {
	final MailFolder opened = foldersArea.opened();
	if (opened == null)
	    return false;
	final int selectedIndex = foldersArea.selectedIndex();
	if (selectedIndex < 0)
	    return false;
	if (!app.getConv().removeFolder())
	    return true;
	app.getStoring().getFolders().remove(opened, selectedIndex);
	foldersArea.requery();
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

    private boolean actSummaryReply()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
app.getHooks().makeReply(item.message);
return true;
    }

    private boolean actDeleteMessage()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	item.message.setState(MailMessage.State.DELETED);
	app.getStoring().getMessages().update(item.message);
	return true;
    }

    private boolean actDeleteMessageForever()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	item.message.setState(MailMessage.State.DELETED);
	app.getStoring().getMessages().delete(item.message);
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
}
