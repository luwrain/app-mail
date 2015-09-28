
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.pim.mail.*;

interface Actions
{
    void gotoFolders();
    void gotoSummary();
    void gotoMessage();
    boolean makeReply(StoredMailMessage message);
    boolean makeForward(StoredMailMessage message);
    void refreshMessages(boolean refreshTableArea);
    void openFolder(StoredMailFolder folder);
    void showMessage(StoredMailMessage message);
    void closeApp();
    boolean onFolderUniRefQuery(AreaQuery query);
}
