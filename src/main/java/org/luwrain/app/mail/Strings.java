
package org.luwrain.app.mail;

import java.util.Date;

public interface Strings
{
    static final String NAME = "luwrain.mail";

    String appName();
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
    //    String passedTimeBrief(Date date);
    String messageSentDate(Date date);
    String replyFirstLine(String sender, Date sentDate);
}
