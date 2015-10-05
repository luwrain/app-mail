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

import java.util.Date;

public interface Strings
{
    String appName();
    String folderTitle(String src);
    String foldersAreaName();
    String summaryAreaName();
    String messageAreaName();
    String noMailStoring();
    String errorOpeningFolder();
    String mailFoldersRoot();
    String readPrefix();
    String markedPrefix();
    String emptySummaryArea();
    String lastSummaryLine();
    String firstSummaryLine();
    String passedTimeBrief(Date date);
    String messageSentDate(Date date);
    String replyFirstLine(String sender, Date sentDate);
}
