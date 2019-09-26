/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.controls.reader.*;
import org.luwrain.pim.mail.*;

final class App implements Application, MonoApp
{
    enum Mode { REGULAR, RAW };

    private Luwrain luwrain = null;
    private Actions actions = null;
    private Base base = null;
    private Strings strings = null;

    private Mode mode = Mode.REGULAR;
    private TreeArea foldersArea = null;
    private ListArea summaryArea = null;
    private ReaderArea messageArea = null;
    private RawMessageArea rawMessageArea = null;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(this, luwrain, strings);
	this.actions = new Actions(base, this);
	if (base.storing == null)
	    return new InitResult(InitResult.Type.FAILURE);
	createAreas();
	if (base.openDefaultFolder())
	    summaryArea.refresh();
	return new InitResult();
    }

    private void createAreas()
    {
	this.foldersArea = new TreeArea(base.createFoldersTreeParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.UNIREF_AREA:
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
		    actions.openFolder(folder, summaryArea);
		}
	    };

	this.summaryArea = new ListArea(base.createSummaryParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			case BACKSPACE:
			    AreaLayoutHelper.activatePrevArea(luwrain, getAreaLayout(), this);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			return false;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getSummaryAreaActions();
		}
	    };

	final ReaderArea.Params messageParams = new ReaderArea.Params();
	messageParams.context = new DefaultControlContext(luwrain);
	this.messageArea = new ReaderArea(messageParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			case BACKSPACE:
			    return AreaLayoutHelper.activatePrevArea(luwrain, getAreaLayout(), this);
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };

	this.rawMessageArea = new RawMessageArea(luwrain, this, strings){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return AreaLayoutHelper.activateNextArea(luwrain, getAreaLayout(), this);
			case BACKSPACE:
			    return AreaLayoutHelper.activatePrevArea(luwrain, getAreaLayout(), this);
			}
		    return super.onInputEvent(event);
		}
	    };
    }

    void saveAttachment(String fileName)
    {
	base.saveAttachment(fileName);
    }

    void refreshMessages()
    {
	summaryArea.refresh();
    }

    private boolean onFolderUniRefQuery(AreaQuery query)
    {
	if (query == null || !(query instanceof UniRefAreaQuery))
	    return false;
	final Object selected = foldersArea.selected();
	if (selected == null || !(selected instanceof StoredMailFolder))
	    return false;
	return base.onFolderUniRefQuery((UniRefAreaQuery)query, (StoredMailFolder)selected);
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
	return true;
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
