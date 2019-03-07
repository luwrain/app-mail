
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
}
