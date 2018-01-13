/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.controls.doctree.*;
import org.luwrain.pim.mail.*;

class App implements Application, MonoApp
{
    enum Mode {
	REGULAR,
	RAW,
    };

    private Luwrain luwrain;
    private Actions actions = null;
    private Base base = null;
    private Strings strings;

    private Mode mode = Mode.REGULAR;
    private TreeArea foldersArea;
    private TableArea summaryArea;
    private DoctreeArea messageArea;
    private RawMessageArea rawMessageArea;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(this, luwrain, strings);
	this.actions = new Actions(luwrain, strings, this);
	if (!base.init())
	    return new InitResult(InitResult.Type.FAILURE);
	createAreas();
	if (base.openDefaultFolder())
	    summaryArea.refresh();
	return new InitResult();
    }

    void saveAttachment(String fileName)
    {
	base.saveAttachment(fileName);
    }

void refreshMessages()
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

    void clearMessageArea()
    {
	base.setCurrentMessage(null);
	//	messageArea.show(null);
	//messageArea.setHotPoint(0, 0);
	rawMessageArea.show(null);
	rawMessageArea.setHotPoint(0, 0);
	enableMessageMode(Mode.REGULAR);
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
	NullCheck.notNull(base, "base");
	NullCheck.notNull(actions, "actions");

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
null,
strings.summaryAreaName()) { //Click handler;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    gotoMessage();
			    return true;
			case BACKSPACE:
			    gotoFolders();
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
		    case ACTION:
			return onSummaryAreaAction(event);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getSummaryAreaActions();
		}
	    };

messageArea = new DoctreeArea(new DefaultControlEnvironment(luwrain), new Announcement(new DefaultControlEnvironment(luwrain), (org.luwrain.controls.doctree.Strings)luwrain.i18n().getStrings(org.luwrain.controls.doctree.Strings.NAME))){

	@Override public boolean onKeyboardEvent(KeyboardEvent event)
	{
	    NullCheck.notNull(event, "event");
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case TAB:
		    gotoFolders();
		    return true;
		case BACKSPACE:
		    gotoSummary();
		    return true;
		}
	    return super.onKeyboardEvent(event);
	}

	@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
	{
	    NullCheck.notNull(event, "event");
	    if (event.getType() != EnvironmentEvent.Type.REGULAR)
		return super.onEnvironmentEvent(event);
	    switch(event.getCode())
	    {
	    case CLOSE:
		closeApp();
		return true;
	    default:
		return super.onEnvironmentEvent(event);
	    }
	}
    };

	rawMessageArea = new RawMessageArea(luwrain, this, strings);

	summaryArea.setClickHandler(				    (model, col, row, obj)->actions.onSummaryClick(base, model, col, row, obj, summaryArea, messageArea));
    }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "delete-message"))
	    return actions.onDeleteInSummary(base, summaryArea, false);
	if (ActionEvent.isAction(event, "delete-message-forever"))
	    return actions.onDeleteInSummary(base, summaryArea, false);

	if (ActionEvent.isAction(event, "reply"))
	    return actions.onSummaryReply(base, summaryArea, false);
	if (ActionEvent.isAction(event, "reply-all"))
	    return actions.onSummaryReply(base, summaryArea, true);

	    //	if (ActionEvent.isAction(event, "forward"))
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

void gotoMessage()
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

    void enableMessageMode(Mode mode)
    {
	if (this.mode == mode)
	    return;
	this.mode = mode;
	luwrain.onNewAreaLayout();
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override  public AreaLayout getAreaLayout()
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
