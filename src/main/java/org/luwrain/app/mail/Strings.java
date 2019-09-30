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

import java.util.Date;

public interface Strings
{
    static final String NAME = "luwrain.mail";

    String appName();
    String foldersAreaName();
    String summaryAreaName();
    String messageAreaName();
    String readPrefix();
    String markedPrefix();
    String deletedPrefix();
    String emptySummaryArea();
    String lastSummaryLine();
    String firstSummaryLine();
    String replyFirstLine(String sender, Date sentDate);
    String columnFrom();
    String columnSubject();
    String columnSentDate();
    String actionDeleteMessage();

    String actionReply();
    String actionReplyAll();
    String actionForward();
}
