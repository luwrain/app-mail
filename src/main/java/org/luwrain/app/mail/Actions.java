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
import org.luwrain.pim.mail.*;

interface Actions
{
    void gotoFolders();
    void gotoSummary();
    void gotoMessage();

    //Returns false is operation isn't possible at all, shows error message if some errors occurred but returns true anyway
    boolean makeReply(StoredMailMessage message, boolean wideReply);
    boolean makeForward(StoredMailMessage message);
    boolean switchToRawMessage();
    void refreshMessages(boolean refreshTableArea);
    void openFolder(StoredMailFolder folder);
    void showMessage(StoredMailMessage message);
    void closeApp();
    boolean onFolderUniRefQuery(AreaQuery query);
    void saveAttachment(String fileName);
    void launchMailFetch();
    boolean deleteInSummary();
    void clearMessageArea();
}
