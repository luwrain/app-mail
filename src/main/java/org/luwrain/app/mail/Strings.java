/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

i   This file is part of LUWRAIN.

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
    
    String actionNewFolder();
    String newFolderNamePopupName();
    String newFolderNamePopupPrefix();

    String actionRemoveFolder();
    String removeFolderPopupName();
    String removeFolderPopupText();

    
    String messageAreaAttachment();
    String messageAreaCc();
    String messageAreaContentType();
    String messageAreaDate();
    String messageAreaFrom();
    String messageAreaName();
    String messageAreaSubject();
    String messageAreaTo();
    String summaryAreaName();
    String actionFetchIncomingBkg();

    String wizardIntro();
    String wizardContinue();
    String wizardMailAddr();
    String wizardMailAddrIsEmpty();
    String wizardMailAddrIsInvalid();
    String wizardPasswordAnnouncement();
    String wizardPasswordIntro();
    String wizardPassword();
    String wizardPasswordIsEmpty();
}
