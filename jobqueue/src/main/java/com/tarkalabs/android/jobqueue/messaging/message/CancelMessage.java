package com.tarkalabs.android.jobqueue.messaging.message;

import com.tarkalabs.android.jobqueue.messaging.Message;
import com.tarkalabs.android.jobqueue.messaging.Type;
import com.tarkalabs.android.jobqueue.CancelResult;
import com.tarkalabs.android.jobqueue.TagConstraint;

public class CancelMessage extends Message {
    private TagConstraint constraint;
    private String[] tags;
    private CancelResult.AsyncCancelCallback callback;

    public CancelMessage() {
        super(Type.CANCEL);
    }

    @Override
    protected void onRecycled() {

    }

    public TagConstraint getConstraint() {
        return constraint;
    }

    public void setConstraint(TagConstraint constraint) {
        this.constraint = constraint;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public CancelResult.AsyncCancelCallback getCallback() {
        return callback;
    }

    public void setCallback(CancelResult.AsyncCancelCallback callback) {
        this.callback = callback;
    }
}
