/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.mail;

import java.util.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.email.*;

class Base
{
    private Luwrain luwrain;
    private Strings strings;
    private EmailStoring emailStoring;
    private FoldersTreeModel foldersModel;
    private SummaryTableModel summaryModel;
    private SummaryTableAppearance summaryAppearance;

    public void init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	//	mailStoring = luwrain.getPimManager().getMailStoring();
    }

    public FoldersTreeModel getFoldersModel()
    {
	if (foldersModel != null)
	    return foldersModel;
	foldersModel = new FoldersTreeModel(emailStoring, strings);
	return foldersModel;
    }

    public SummaryTableModel getSummaryModel()
    {
	if (summaryModel != null)
	    return summaryModel;
	summaryModel = new SummaryTableModel(emailStoring);
	return summaryModel;
    }

    public SummaryTableAppearance getSummaryAppearance()
    {
	if (summaryAppearance != null)
	    return summaryAppearance;
	summaryAppearance = new SummaryTableAppearance();
	return summaryAppearance;
    }

    /*
    public boolean isStoredMailGroup(Object obj)
    {
	return obj != null && (obj instanceof StoredMailGroup);
    }
    */

    public boolean openFolder(Object obj, TableArea summaryTable)
    {
	/*
	if (obj == null || !(obj instanceof StoredMailGroup))
	    return false;
	summaryModel.setCurrentMailGroup((StoredMailGroup)obj);
	summaryTable.refresh();//FIXME:Reset hot point position;
	return summaryModel.isValidState();
	*/
	return false;
    }
}
