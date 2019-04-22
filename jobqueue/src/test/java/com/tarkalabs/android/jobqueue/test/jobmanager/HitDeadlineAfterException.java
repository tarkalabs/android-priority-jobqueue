package com.tarkalabs.android.jobqueue.test.jobmanager;

import com.tarkalabs.android.jobqueue.CancelReason;
import com.tarkalabs.android.jobqueue.Job;
import com.tarkalabs.android.jobqueue.JobManager;
import com.tarkalabs.android.jobqueue.Params;
import com.tarkalabs.android.jobqueue.RetryConstraint;
import com.tarkalabs.android.jobqueue.test.jobs.DummyJob;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@RunWith(RobolectricTestRunner.class)
@Config(constants = com.tarkalabs.android.jobqueue.BuildConfig.class)
public class HitDeadlineAfterException extends JobManagerTestBase {
    @Test
    public void failAndHitDeadline() throws InterruptedException {
        final JobManager jobManager = createJobManager();
        final AtomicBoolean calledShouldReRun = new AtomicBoolean(false);
        final AtomicInteger reason = new AtomicInteger();
        final DummyJob job = new DummyJob(new Params(0).overrideDeadlineToCancelInMs(100)) {
            @Override
            public void onRun() throws Throwable {
                super.onRun();
                mockTimer.incrementMs(150);
                throw new RuntimeException("why not fail");
            }

            @Override
            protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
                reason.set(cancelReason);
                super.onCancel(cancelReason, throwable);
            }

            @Override
            protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
                calledShouldReRun.set(true);
                return RetryConstraint.RETRY;
            }
        };
        waitUntilAJobIsDone(jobManager, new WaitUntilCallback() {
            @Override
            public void run() {
                jobManager.addJob(job);
            }

            @Override
            public void assertJob(Job job) {

            }
        });
        MatcherAssert.assertThat(reason.get(), CoreMatchers.is(CancelReason.REACHED_DEADLINE));
        MatcherAssert.assertThat(calledShouldReRun.get(), CoreMatchers.is(false));
    }
}
