/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class MailApp implements Application, MonoApp, Actions
{
    static private final String STRINGS_NAME = "luwrain.mail";

    private enum Mode {
	REGULAR,
	RAW,
    };

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;

    private Mode mode = Mode.REGULAR;
    private TreeArea foldersArea;
    private TableArea summaryArea;
    private MessageArea messageArea;
    private RawMessageArea rawMessageArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, this, strings))//FIXME:Let user know what happens;
	    return false;
	createAreas();
	return true;
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    @Override public boolean deleteInSummary()
    {
	final Object o = summaryArea.getSelectedRow();
	if (o == null || !(o instanceof StoredMailMessage))
	    return false;
	final StoredMailMessage message = (StoredMailMessage)o;
	if (!base.deleteInSummary(message))
	    return true;
	summaryArea.refresh();
	clearMessageArea();
	return true;
    }

    @Override public void launchMailFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--MAIL"});
    }

    @Override public void saveAttachment(String fileName)
    {
	base.saveAttachment(fileName);
    }

    @Override public boolean makeReply(StoredMailMessage message, boolean wideReply)
    {
	if (!base.makeReply(message, wideReply))
	    luwrain.message("Во время подготовки ответа произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }

    @Override public boolean makeForward(StoredMailMessage message)
    {
	if (!base.makeForward(message))
	    luwrain.message("Во время подготовки перенаправленяи произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }

    @Override public void refreshMessages(boolean refreshTableArea)
    {
	summaryArea.refresh();
    }

    @Override public void openFolder(StoredMailFolder folder)
    {
	if (!base.openFolder(folder))
	    return;
	summaryArea.refresh();
	gotoSummary();
    }

    @Override public boolean onFolderUniRefQuery(AreaQuery query)
    {
	if (query == null || !(query instanceof ObjectUniRefQuery))
	    return false;
	final Object selected = foldersArea.selected();
	if (selected == null || !(selected instanceof FolderWrapper))
	    return false;
	return base.onFolderUniRefQuery((ObjectUniRefQuery)query, (FolderWrapper)selected);
    }

    @Override public void clearMessageArea()
    {
	base.setCurrentMessage(null);
	messageArea.show(null);
	messageArea.setHotPoint(0, 0);
	rawMessageArea.show(null);
	rawMessageArea.setHotPoint(0, 0);
	enableMessageMode(Mode.REGULAR);
    }

    @Override public void showMessage(StoredMailMessage message)
    {
	if (message == null)
	    return;
	base.setCurrentMessage(message);
	messageArea.show(message);
	messageArea.setHotPoint(0, 0);
	rawMessageArea.show(message);
	rawMessageArea.setHotPoint(0, 0);
	enableMessageMode(Mode.REGULAR);
	gotoMessage();
    }

    @Override public boolean switchToRawMessage()
    {
	System.out.println("switching");
	if (!base.hasCurrentMessage())
	    return false;
	enableMessageMode(Mode.RAW);
	gotoMessage();
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;
	final Strings s = strings;

	final TableClickHandler summaryHandler = new TableClickHandler(){
		@Override public boolean onClick(TableModel model,
						 int col,
						 int row,
						 Object obj)
		{
		    if (model == null)
			return false;
		    final Object o = model.getRow(row);
		    if (o == null || !(o instanceof StoredMailMessage))
			return false;
		    actions.showMessage((StoredMailMessage)o);
		    return true;
		}};

	final TreeArea.Params treeParams = new TreeArea.Params();
	treeParams.environment = new DefaultControlEnvironment(luwrain);
	treeParams.model = base.getFoldersModel(); 
	treeParams.name = strings.foldersAreaName();

	foldersArea = new TreeArea(treeParams) {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    actions.gotoSummary();
			    return true;
			case F9:
			    actions.launchMailFetch();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.OBJECT_UNIREF:
			return actions.onFolderUniRefQuery(query);
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override public void onClick(Object obj)
		{
		    if (obj == null || !(obj instanceof FolderWrapper))
			return;
		    final FolderWrapper wrapper = (FolderWrapper)obj;
		    actions.openFolder(wrapper.folder());
		}
	    };

	summaryArea = new TableArea(new DefaultControlEnvironment(luwrain),
				    base.getSummaryModel(), base.getSummaryAppearance(),
				    summaryHandler, strings.summaryAreaName()) { //Click handler;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case DELETE:
			    return actions.deleteInSummary();
			case TAB:
			    actions.gotoMessage();
			    return true;
			case BACKSPACE:
			    actions.gotoFolders();
			    return true;
			case F9:
			    actions.launchMailFetch();
			    return true;
			case F5:
			    if (getSelectedRow() == null)
				return false;
			    return base.makeReply((StoredMailMessage)getSelectedRow(), false);
			case F6:
			    if (getSelectedRow() == null)
				return false;
			    return base.makeForward((StoredMailMessage)getSelectedRow());
			}
		    if (event.isSpecial() && event.withShiftOnly())
			switch(event.getSpecial())
			{
			case F5:
			    if (getSelectedRow() == null)
				return false;
			    return base.makeReply((StoredMailMessage)getSelectedRow(), true);
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			actions.closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "reply"))
			{
			    if (getSelectedRow() == null)
				return false;
			    base.makeReply((StoredMailMessage)getSelectedRow(), false);
			    return true;
			}
			if (ActionEvent.isAction(event, "reply-all"))
			{
			    if (getSelectedRow() == null)
				return false;
			    base.makeReply((StoredMailMessage)getSelectedRow(), true);
			    return true;
			}
			if (ActionEvent.isAction(event, "forward"))
			{
			    if (getSelectedRow() == null)
				return false;
			    base.makeForward((StoredMailMessage)getSelectedRow());
			    return true;
			}
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[]{
			new Action("reply", "Ответить"),
			new Action("reply-all", "Ответить всем"),
			new Action("forward", "Переслать"),
		    };
		}
	    };

	messageArea = new MessageArea(luwrain, this, strings);
	rawMessageArea = new RawMessageArea(luwrain, this, strings);
    }

    @Override  public AreaLayout getAreasToShow()
    {
	System.out.println(mode);
	switch(mode)
	{
	case REGULAR:
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, messageArea);
	case RAW:
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, rawMessageArea);
	}
	return null;
    }

    @Override public void gotoFolders()
    {
	luwrain.setActiveArea(foldersArea);
    }

    @Override public void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    @Override public void gotoMessage()
    {
	switch(mode)
	{
	case REGULAR:
	    luwrain.setActiveArea(messageArea);
	    return;
	case RAW:
	    luwrain.setActiveArea(rawMessageArea);
	    return;
	}
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
    @Override public String getAppName()
    {
	return strings.appName();
    }

    private void enableMessageMode(Mode mode)
    {
	if (this.mode == mode)
	    return;
	this.mode = mode;
	luwrain.onNewAreaLayout();
    }
}
