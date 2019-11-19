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

package org.luwrain.app.message;

import java.io.File;

public interface Strings
{
    static final String NAME = "luwrain.message";

    String actionAttachFile();
    String actionChooseCc();
    String actionDeleteAttachment();
    String actionSend();
    String actionSendAnotherAccount();
    String appName();
    String attachmentPopupName();
    String attachmentPopupPrefix();
    String attachment(String f);
    String cc();
    String enterMessageBelow();
    String subject();
    String to();
}
