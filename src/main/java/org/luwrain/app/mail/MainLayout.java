/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.pim.*;
import org.luwrain.pim.mail.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase implements TreeArea.ClickHandler
{
    private final App app;
    private final TreeArea foldersArea;
    private final ListArea summaryArea;
    private final ReaderArea messageArea;

        private MailFolder openedFolder = null;
    private SummaryItem[] summaryItems = new SummaryItem[0];
        private MailMessage openedMessage;


    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.foldersArea = new TreeArea(createFoldersTreeParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.UNIREF_AREA:
			{
			    final Object selected = foldersArea.selected();
			    if (selected == null || !(selected instanceof MailFolder) || !(query instanceof UniRefAreaQuery))
				return false;
			    final UniRefAreaQuery uniRefQuery = (UniRefAreaQuery)query;
			    final MailFolder folder = (MailFolder)selected;
			    try {
				final String uniRef = app.getStoring().getFolders().getUniRef(folder);
				if (uniRef == null || uniRef.trim().isEmpty())
				    return false;
				uniRefQuery.answer(uniRef);
			    }
			    catch(PimException e)
			    {
				app.getLuwrain().crash(e);
				return false;
			    }
			}
		    default:
			return super.onAreaQuery(query);
		    }
		}
	    };
	this.summaryArea = new ListArea(createSummaryParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
	this.messageArea = new ReaderArea(createMessageReaderParams()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
	    };
    }

    @Override public boolean onTreeClick(TreeArea area, Object obj)
    {
		    if (obj == null || !(obj instanceof MailFolder))
			return false;
		    final MailFolder folder = (MailFolder)obj;
		    return false;//actions.onOpenFolder(folder, summaryArea);
		}

    TreeArea.Params createFoldersTreeParams()
    {
	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new CachedTreeModel(new FoldersModel());
	params.name = app.getStrings().foldersAreaName();
	params.clickHandler = this;
	return params;
    }

    ListArea.Params createSummaryParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.name = app.getStrings().summaryAreaName();
	params.model = new SummaryListModel();
	//	params.clickHandler = clickHandler;
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (!(item instanceof SummaryItem))
			return false;
		    final SummaryItem summaryItem = (SummaryItem)item;
		    return summaryItem.type == SummaryItem.Type.SECTION;
		}
	    };
	params.transition = new ListUtils.DoubleLevelTransition(params.model){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    if (!(item instanceof SummaryItem))
			return false;
		    final SummaryItem summaryItem = (SummaryItem)item;
		    return summaryItem.type == SummaryItem.Type.SECTION;
		}
	    };
	return params;
    }

    ReaderArea.Params createMessageReaderParams()
    {
	final ReaderArea.Params params = new ReaderArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.name = app.getStrings().messageAreaName();
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(foldersArea);
    }

    private final class SummaryListModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return summaryItems.length;
	}
	@Override public Object getItem(int index)
	{
	    return summaryItems[index];
	}
	@Override public void refresh()
	{
	}
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


    /*
    void openFolder(MailFolder folder) throws PimException
    {
	NullCheck.notNull(folder, "folder");
	this.openedFolder = folder;
	final MailMessage[] messages = storing.getMessages().loadNoDeleted(folder);
	this.summaryItems = hooks.organizeSummary(messages);
    }

    void openMessage(MailMessage message)
    {
	this.openedMessage = message;
    }
    */

