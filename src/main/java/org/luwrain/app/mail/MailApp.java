/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class MailApp implements Application, MonoApp
{
    private enum Mode {
	REGULAR,
	RAW,
    };

    private Luwrain luwrain;
    private Base base;
    private Strings strings;

    private Mode mode = Mode.REGULAR;
    private TreeArea foldersArea;
    private TableArea summaryArea;
    private MessageArea messageArea;
    private RawMessageArea rawMessageArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	{
	    Log.error("mail", "no strings object");
	    return false;
	}
	strings = (Strings)o;
	this.luwrain = luwrain;
	base = new Base(this, luwrain, strings);
	if (!base.init())
	    return false;
	createAreas();
	return true;
    }

    private boolean deleteInSummary()
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

    void launchMailFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--MAIL"});
    }

    void saveAttachment(String fileName)
    {
	base.saveAttachment(fileName);
    }

    boolean makeReply(StoredMailMessage message, boolean wideReply)
    {
	if (!base.makeReply(message, wideReply))
	    luwrain.message("Во время подготовки ответа произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }

    boolean makeForward(StoredMailMessage message)
    {
	if (!base.makeForward(message))
	    luwrain.message("Во время подготовки перенаправленяи произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	return true;
    }

    private void refreshMessages(boolean refreshTableArea)
    {
	summaryArea.refresh();
    }

    private void openFolder(StoredMailFolder folder)
    {
	if (!base.openFolder(folder))
	    return;
	summaryArea.refresh();
	gotoSummary();
    }

    private boolean onFolderUniRefQuery(AreaQuery query)
    {
	if (query == null || !(query instanceof ObjectUniRefQuery))
	    return false;
	final Object selected = foldersArea.selected();
	if (selected == null || !(selected instanceof StoredMailFolder))
	    return false;
	return base.onFolderUniRefQuery((ObjectUniRefQuery)query, (StoredMailFolder)selected);
    }

    private void clearMessageArea()
    {
	base.setCurrentMessage(null);
	messageArea.show(null);
	messageArea.setHotPoint(0, 0);
	rawMessageArea.show(null);
	rawMessageArea.setHotPoint(0, 0);
	enableMessageMode(Mode.REGULAR);
    }

    private void showMessage(StoredMailMessage message)
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

    boolean switchToRawMessage()
    {
	if (!base.hasCurrentMessage())
	    return false;
	enableMessageMode(Mode.RAW);
	gotoMessage();
	return true;
    }

    private void createAreas()
    {
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
		    showMessage((StoredMailMessage)o);
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
			    gotoSummary();
			    return true;
			case F9:
			    launchMailFetch();
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
			closeApp();
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
			return onFolderUniRefQuery(query);
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override public void onClick(Object obj)
		{
		    if (obj == null || !(obj instanceof StoredMailFolder))
			return;
		    final StoredMailFolder folder = (StoredMailFolder)obj;
		    openFolder(folder);
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
			    return deleteInSummary();
			case TAB:
			    gotoMessage();
			    return true;
			case BACKSPACE:
			    gotoFolders();
			    return true;
			case F9:
			    launchMailFetch();
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
			closeApp();
			return true;
		    case ACTION:
			return onSummaryAreaAction(event);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return Actions.getSummaryAreaActions();
		}
	    };

	messageArea = new MessageArea(luwrain, this, strings);
	rawMessageArea = new RawMessageArea(luwrain, this, strings);
    }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "reply"))
	{
	    if (summaryArea.getSelectedRow() == null)
		return false;
	    base.makeReply((StoredMailMessage)summaryArea.getSelectedRow(), false);
	    return true;
	}
	if (ActionEvent.isAction(event, "reply-all"))
	{
	    if (summaryArea.getSelectedRow() == null)
		return false;
	    base.makeReply((StoredMailMessage)summaryArea.getSelectedRow(), true);
	    return true;
	}
	if (ActionEvent.isAction(event, "forward"))
	{
	    if (summaryArea.getSelectedRow() == null)
		return false;
	    base.makeForward((StoredMailMessage)summaryArea.getSelectedRow());
	    return true;
	}
	return false;
    }

    void gotoFolders()
    {
	luwrain.setActiveArea(foldersArea);
    }

    void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    private void gotoMessage()
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

    private void enableMessageMode(Mode mode)
    {
	if (this.mode == mode)
	    return;
	this.mode = mode;
	luwrain.onNewAreaLayout();
    }

    void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override  public AreaLayout getAreasToShow()
    {
	switch(mode)
	{
	case REGULAR:
	    return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, messageArea);
	case RAW:
	    return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, foldersArea, summaryArea, rawMessageArea);
	}
	return null;
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
