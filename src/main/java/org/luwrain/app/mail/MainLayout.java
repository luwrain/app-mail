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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ListArea.*;
import org.luwrain.controls.ListUtils.*;
import org.luwrain.pim.mail.*;
import org.luwrain.app.base.*;
import org.luwrain.pim.mail.persistence.model.*;
import org.luwrain.app.mail.layouts.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class MainLayout extends LayoutBase implements TreeListArea.LeafClickHandler<Folder>, ClickHandler<SummaryItem>
{
    static private final InputEvent
	HOT_KEY_REPLY = new InputEvent('r', EnumSet.of(InputEvent.Modifiers.ALT));

    final App app;
    final TreeListArea<Folder> foldersArea;
    final ListArea<SummaryItem> summaryArea;
    //    final ReaderArea messageArea;
    final NavigationArea messageArea;

    private final Data data;
    private final List<SummaryItem> summaryItems = new ArrayList<>();
    private boolean showDeleted = false;
    private Folder folder = null;
    private Message message = null;

    MainLayout(App app, Data data)
    {
	super(app);
	this.app = app;
	this.data = data;

	final TreeListArea.Params<Folder> treeParams = new TreeListArea.Params<>();
        treeParams.context = getControlContext();
	treeParams.name = app.getStrings().foldersAreaName();
	treeParams.model = new FoldersModel();
	treeParams.leafClickHandler = this;
	this.foldersArea = new TreeListArea<>(treeParams) {
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

	this.summaryArea = new ListArea<SummaryItem>(listParams((params)->{
		    params.name = app.getStrings().summaryAreaName();
		    params.model = new ListModel<>(summaryItems);
		    params.clickHandler = this;
		    params.appearance = new DoubleLevelAppearance<SummaryItem>(getControlContext()){
			    @Override public void announceNonSection(SummaryItem summaryItem) { announceSummaryMessage(summaryItem); }
			    @Override public boolean isSectionItem(SummaryItem item) { return item.type == SummaryItem.Type.SECTION; }
			};
		    params.transition = new DoubleLevelTransition<SummaryItem>(params.model){
			    @Override public boolean isSectionItem(SummaryItem item) { return item.type == SummaryItem.Type.SECTION; }
			};
		})){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case REFRESH:
			updateSummary();
			return true;
			}
		    return super.onSystemEvent(event);
		}
	    };

	/*
	final ReaderArea.Params messageParams = new ReaderArea.Params();
	messageParams.context = getControlContext();
	messageParams.name = app.getStrings().messageAreaName();
	this.messageArea = new ReaderArea(messageParams){
	*/
	messageArea = new NavigationArea(getControlContext()){
		@Override public String getAreaName() { return app.getStrings().messageAreaName(); }
		@Override public String getLine(int index) { return index < data.messageLines.size()?data.messageLines.get(index):""; }
		@Override public int getLineCount() { return data.messageLines.size() >= 1?data.messageLines.size():1; }
		@Override public void announceLine(int index, String line)
		{
		    final boolean attention = index < data.messageAttachments.size() && data.messageAttachments.get(index) != null;
		    if (line.isEmpty())
		    {
			app.setEventResponse(hint(Hint.EMPTY_LINE));
			return;
		    }
		    if (attention)
			app.setEventResponse(text(Sounds.ATTENTION, getLuwrain().getSpeakableText(line, Luwrain.SpeakableTextType.PROGRAMMING))); else
		    app.setEventResponse(text(getLuwrain().getSpeakableText(line, Luwrain.SpeakableTextType.PROGRAMMING)));
		}
		@Override public boolean onInputEvent(InputEvent event)
		{
		    if (event.isSpecial())
			switch(event.getSpecial())
			{
			case BACKSPACE:
			    setActiveArea(summaryArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}
	    };

	final ActionInfo
	fetchIncomingBkg = action("fetch-incoming-bkg", app.getStrings().actionFetchIncomingBkg(), new InputEvent(InputEvent.Special.F6), ()->{	getLuwrain().runWorker(org.luwrain.pim.workers.Pop3.NAME); return true;});

	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, actions(
								       action("remove-folder", app.getStrings().actionRemoveFolder(), new InputEvent(InputEvent.Special.DELETE), this::actRemoveFolder),
								       action("new-folder", app.getStrings().actionNewFolder(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actNewFolder),
								       fetchIncomingBkg),

		      summaryArea, actions(
					   action("reply", app.getStrings().actionReply(), HOT_KEY_REPLY, this::actSummaryReply),
					   action("mark", app.getStrings().actionMarkMessage(), new InputEvent(InputEvent.Special.INSERT), this::actMarkMessage, ()->{
						   final SummaryItem item = summaryArea.selected();
						   return item != null && item.message != null && item.message.getMetadata().getState() != MessageMetadata.State.MARKED;
					       }),
					   action("unmark", app.getStrings().actionUnmarkMessage(), new InputEvent(InputEvent.Special.INSERT), this::actUnmarkMessage, ()->{
						   final SummaryItem item = summaryArea.selected();
						   return item != null && item.message != null && item.message.getMetadata().getState() == MessageMetadata.State.MARKED;
					       }),
					   action("delete", app.getStrings().actionDeleteMessage(), new InputEvent(InputEvent.Special.DELETE), this::actDeleteMessage),
					   action("delete-forever", app.getStrings().actionDeleteMessageForever(), new InputEvent(InputEvent.Special.DELETE, EnumSet.of(InputEvent.Modifiers.CONTROL)), this::actDeleteMessageForever),
		      action("deleted-show", app.getStrings().actionDeletedShow(), new InputEvent('='), ()->{
			      showDeleted = true;
			      updateSummary();
			      getLuwrain().playSound(Sounds.OK);
			      return true;
			  }),
					   		      action("deleted-hide", app.getStrings().actionDeletedHide(), new InputEvent('-'), ()->{
			      showDeleted = false;
			      updateSummary();
			      getLuwrain().playSound(Sounds.OK);
			      return true;
			  }),
					   					   fetchIncomingBkg
					   ),

		      messageArea, actions(
					   fetchIncomingBkg
					   ));
    }

    void updateSummary()
    {
	this.summaryItems.clear();
	final var m = data.getMessagesInLocalFolder(folder.getId());
	if (!showDeleted)
	{
	    
	}
	this.summaryItems.addAll(app.getHooks().organizeSummary(m));
	summaryArea.refresh();
    }

    @Override public boolean onLeafClick(TreeListArea<Folder> area, Folder folder)
    {
	this.folder = folder;
	updateSummary();
	summaryArea.reset(false);
	setActiveArea(summaryArea);
	return true;
    }

    private boolean actNewFolder()
    {
	final var opened = foldersArea.opened();
	if (opened == null)
	    return false;
	final String name = app.getConv().newFolderName();
	if (name == null)
	    return true;
	final int selectedIndex = foldersArea.selectedIndex();
	final var newFolder = new Folder();
	newFolder.setName(name);
	newFolder.setParentFolderId(opened.getId());
	data.folderDAO.add(newFolder);
    foldersArea.requery();
    foldersArea.refresh();
	return true;
    }

    private boolean actRemoveFolder()
    {
	final Folder opened = foldersArea.opened();
	if (opened == null)
	    return false;
	final int selectedIndex = foldersArea.selectedIndex();
	if (selectedIndex < 0)
	    return false;
	if (!app.getConv().removeFolder())
	    return true;
	//	app.getStoring().getFolders().remove(opened, selectedIndex);
	foldersArea.requery();
	foldersArea.refresh();
	return true;
    }

    private boolean onFolderProps()
    {
	final var folder = foldersArea.selected();
	if (folder == null)
	    return false;
	final FolderPropertiesLayout propsLayout = new FolderPropertiesLayout(app, folder, ()->{
		app.setAreaLayout(MainLayout.this);
		foldersArea.refresh();
		app.getLuwrain().announceActiveArea();
		return true;
	    });
	app.setAreaLayout(propsLayout);
	app.getLuwrain().announceActiveArea();
	return true;
    }

    @Override public boolean onListClick(ListArea area, int index, SummaryItem item)
    {
	final var message = item.message;
	if (message == null)
	    return false;
	if (message.getMetadata().getState() == MessageMetadata.State.NEW)
	{
	    message.getMetadata().setState(MessageMetadata.State.READ);
	    data.messageDAO.update(message.getMetadata());
	    summaryArea.refresh();
	}
	data.setMessage(message);
	messageArea.redraw();
	messageArea.setHotPoint(0, 0);
	setActiveArea(messageArea);
	return true;
    }

    
    private boolean actMarkMessage()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	item.message.getMetadata().setState(MessageMetadata.State.MARKED);
	data.messageDAO.update(item.message.getMetadata());
	app.setEventResponse(text(Sounds.SELECTED, app.getStrings().messageMarked()));
	return true;
    }

    private boolean actUnmarkMessage()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	item.message.getMetadata().setState(MessageMetadata.State.READ);
	data.messageDAO.update(item.message.getMetadata());
	app.setEventResponse(text(Sounds.SELECTED, app.getStrings().messageUnmarked()));
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
	final var item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	item.message.getMetadata().setState(MessageMetadata.State.DELETED);
	data.messageDAO.update(item.message.getMetadata());
	//FIXME:not delete for showing deleted
	summaryItems.remove(item);
	summaryArea.refresh();
	final var newSelected = summaryArea.selected();
	if (newSelected != null)
	    app.setEventResponse(text(Sounds.OK, newSelected.title)); else
	    app.setEventResponse(hint(Hint.NO_ITEMS_BELOW));
	return true;
    }

        private boolean actDeleteMessageForever()
    {
	final SummaryItem item = summaryArea.selected();
	if (item == null || item.message == null)
	    return false;
	if (!app.getConv().deleteMessageForever())
	    return true;
	//FIXME:deleting raw message
	data.messageDAO.delete(item.message.getMetadata());
	updateSummary();
	return true;
    }


    private void announceSummaryMessage(SummaryItem summaryItem)
    {
	final Message m = summaryItem.message;
	if (m == null)
	    return;
	final String announcement = (m.getMetadata().getTitle() != null && !m.getMetadata().getTitle().isEmpty())?m.getMetadata().getTitle().trim():m.getMetadata().getFromAddr();
	if (m.getMetadata().getState() == null)
	{
	    app.setEventResponse(listItem(Sounds.LIST_ITEM, announcement, Suggestions.CLICKABLE_LIST_ITEM));
	    return;
	}
	switch(m.getMetadata().getState())
	{
	case NEW:
	    app.setEventResponse(listItem(Sounds.ATTENTION, announcement, Suggestions.CLICKABLE_LIST_ITEM)); 
	    break;
	case READ:
	    app.setEventResponse(listItem(Sounds.LIST_ITEM, announcement, Suggestions.CLICKABLE_LIST_ITEM));
	    break;
	case MARKED:
	    app.setEventResponse(listItem(Sounds.SELECTED, announcement, Suggestions.CLICKABLE_LIST_ITEM));
	    break;
	default:
	    app.setEventResponse(listItem(Sounds.LIST_ITEM, announcement, Suggestions.CLICKABLE_LIST_ITEM));
	}
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

    final class FoldersModel implements TreeListArea.Model<Folder>
{
    @Override public boolean getItems(Folder folder, TreeListArea.Collector<Folder> collector)
    {
	collector.collect(data.folderDAO.getChildFolders(folder));
	return true;
    }
    @Override public Folder getRoot() { return data.folderDAO.getRoot(); }
    @Override public boolean isLeaf(Folder folder) { return data.folderDAO.getChildFolders(folder).size() == 0; }
}
}
