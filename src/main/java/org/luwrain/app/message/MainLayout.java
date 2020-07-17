

package org.luwrain.app.message;

import java.io.*;
//import java.nio.file.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.MailMessage;

import org.luwrain.template.*;

final class MainLayout extends LayoutBase
{
    static private final String HOOKS_PREFIX = "luwrain.message.edit";

    static final String
	TO_NAME = "to",
	CC_NAME = "cc",
	SUBJECT_NAME = "subject",
ATTACHMENT = "attachment";

private final App app;
    private final FormArea formArea;
    private final MutableLinesImpl lines;
    private int attachmentCounter = 0;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	final MessageContent msg = null;
		this.lines = new MutableLinesImpl(msg.textLines());
		this.formArea = new FormArea(new DefaultControlContext(app.getLuwrain())){
		final Actions actions = actions();
				@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
					    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}

	    };
		formArea.addEdit(TO_NAME, app.getStrings().to(), msg.to);
		formArea.addEdit(CC_NAME, app.getStrings().cc(), msg.cc);
		formArea.addEdit(SUBJECT_NAME, app.getStrings().subject(), msg.subject);
		formArea.activateMultilineEdit(app.getStrings().enterMessageBelow(), lines, createEditParams(), true);
    }

    String getTo()
    {
	return formArea.getEnteredText(TO_NAME);
    }

    void setTo(String value)
    {
	NullCheck.notNull(value, "value");
	formArea.setEnteredText(value, "value");
    }

    void focusTo()
    {
	formArea.setHotPoint(0, 0);
    }

    String getCc()
    {
	return formArea.getEnteredText(CC_NAME);
    }

    void setCc(String value)
    {
	NullCheck.notNull(value, "value");
	formArea.setEnteredText(CC_NAME, value);
    }

    String getSubject()
    {
	return formArea.getEnteredText(SUBJECT_NAME);
    }

    void focusSubject()
    {
	formArea.setHotPoint(0, 2);
    }

    String getText()
    {
	return lines.getWholeText();
    }

    Attachment[] getAttachments()
    {
	final List<Attachment> res = new LinkedList();
		for(int i = 0;i < formArea.getItemCount();++i)
	{
	    if (formArea.getItemTypeOnLine(i) != FormArea.Type.STATIC)
		continue;
	    final Object o = formArea.getItemObj(i);
	    if (o == null || !(o instanceof Attachment))
		continue;
	    res.add((Attachment)o);
	}
		return res.toArray(new Attachment[res.size()]);
    }

    File[] getAttachmentFiles()
    {
	final Attachment[] attachments = getAttachments();
	final File[] res = new File[attachments.length];
	for(int i = 0;i < attachments.length;++i)
	    res[i] = attachments[i].file;
	return res;
    }

    void addAttachment(File file)
    {
	NullCheck.notNull(file, "file");
	for(Attachment a: getAttachments())
	    if (a.file.equals(file))
	    {
		app.getLuwrain().message("Файл " + file.getName() + " уже прикреплён к сообщению", Luwrain.MessageType.ERROR);//FIXME:
		return;
	    }
	final Attachment a = new Attachment(ATTACHMENT + attachmentCounter, file);
	++attachmentCounter;
	formArea.addStatic(a.name, app.getStrings().attachment(file.getName()), a);
    }

    void removeAttachment(int lineIndex)
    {
	formArea.removeItemOnLine(lineIndex);
    }

    MailMessage constructMailMessage() throws org.luwrain.pim.PimException
    {
	final MailMessage msg = new MailMessage();
	msg.setTo(Base.splitAddrs(formArea.getEnteredText(TO_NAME)));
	msg.setCc(Utils.splitAddrs(formArea.getEnteredText(CC_NAME)));
	msg.setSubject(formArea.getEnteredText(SUBJECT_NAME));
	msg.setText(getText());
	final List<String> attachments = new LinkedList();
	for(File f: getAttachmentFiles())
	    attachments.add(f.getAbsolutePath());
	msg.setAttachments(attachments.toArray(new String[attachments.size()]));
	return msg;
    }

    private MultilineEdit.Params createEditParams()
    {
	final MultilineEdit.Params params = formArea.createMultilineEditParams(new DefaultControlContext(app.getLuwrain()), lines);
	final MultilineEditCorrector corrector = (MultilineEditCorrector)params.model;
	params.model = new DirectScriptMultilineEditCorrector(params.context, corrector, HOOKS_PREFIX);
	return params;
    }
}
