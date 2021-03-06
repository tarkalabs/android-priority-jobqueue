package com.birbit.android.jobqueue.persistentQueue.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.birbit.android.jobqueue.JobHolder;

/**
 * Helper class for {@link SqliteJobQueue} to handle database connection
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    /*package*/ static final SqlHelper.Property SCHEDULE_REQUESTED_AT_NS = new SqlHelper.Property("schedule_requested_at_ns", "long", 12);
    /*package*/ static final String JOB_HOLDER_TABLE_NAME = "job_holder";
    /*package*/ static final String JOB_TAGS_TABLE_NAME = "job_holder_tags";
    /*package*/ static final String JOB_DEPENDEE_TAGS_TABLE_NAME = "job_holder_dependee_tags";
    /*package*/ static final SqlHelper.Property INSERTION_ORDER_COLUMN = new SqlHelper.Property("insertionOrder", "integer", 0);
    /*package*/ static final SqlHelper.Property ID_COLUMN = new SqlHelper.Property("_id", "text", 1, null, true);
    /*package*/ static final SqlHelper.Property PRIORITY_COLUMN = new SqlHelper.Property("priority", "integer", 2);
    /*package*/ static final SqlHelper.Property GROUP_ID_COLUMN = new SqlHelper.Property("group_id", "text", 3);
    /*package*/ static final SqlHelper.Property RUN_COUNT_COLUMN = new SqlHelper.Property("run_count", "integer", 4);
    /*package*/ static final SqlHelper.Property CREATED_NS_COLUMN = new SqlHelper.Property("created_ns", "long", 5);
    /*package*/ static final SqlHelper.Property DELAY_UNTIL_NS_COLUMN = new SqlHelper.Property("delay_until_ns", "long", 6);
    /*package*/ static final SqlHelper.Property RUNNING_SESSION_ID_COLUMN = new SqlHelper.Property("running_session_id", "long", 7);
    /*package*/ static final SqlHelper.Property REQUIRED_NETWORK_TYPE_COLUMN = new SqlHelper.Property("network_type", "integer", 8);
    /*package*/ static final SqlHelper.Property DEADLINE_COLUMN = new SqlHelper.Property("deadline", "integer", 9);
    /*package*/ static final SqlHelper.Property CANCEL_ON_DEADLINE_COLUMN = new SqlHelper.Property("cancel_on_deadline", "integer", 10);
    /*package*/ static final SqlHelper.Property CANCELLED_COLUMN = new SqlHelper.Property("cancelled", "integer", 11);
    /*package*/ static final int COLUMN_COUNT = 13;

    /*package*/ static final SqlHelper.Property TAGS_ID_COLUMN = new SqlHelper.Property("_id", "integer", 0);
    /*package*/ static final SqlHelper.Property TAGS_JOB_ID_COLUMN = new SqlHelper.Property("job_id", "text", 1, new SqlHelper.ForeignKey(JOB_HOLDER_TABLE_NAME, ID_COLUMN.columnName));
    /*package*/ static final SqlHelper.Property TAGS_NAME_COLUMN = new SqlHelper.Property("tag_name", "text", 2);

    /*package*/ static final SqlHelper.Property DEPENDEE_TAGS_ID_COLUMN = new SqlHelper.Property("_id", "integer", 0);
    /*package*/ static final SqlHelper.Property DEPENDEE_TAGS_JOB_ID_COLUMN = new SqlHelper.Property("job_id", "text", 1, new SqlHelper.ForeignKey(JOB_HOLDER_TABLE_NAME, ID_COLUMN.columnName));
    /*package*/ static final SqlHelper.Property DEPENDEE_TAGS_NAME_COLUMN = new SqlHelper.Property("tag_name", "text", 2);
    private static final int DB_VERSION = 14;
    /*package*/ static final int TAGS_COLUMN_COUNT = 3;
    /*package*/ static final int DEPENDEE_TAGS_COLUMN_COUNT = 3;

    static final String TAG_INDEX_NAME = "TAG_NAME_INDEX";
    static final String DEPENDEE_TAG_INDEX_NAME = "DEPENDEE_TAG_INDEX_NAME";

    public DbOpenHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createQuery = SqlHelper.create(JOB_HOLDER_TABLE_NAME,
                INSERTION_ORDER_COLUMN,
                ID_COLUMN,
                PRIORITY_COLUMN,
                GROUP_ID_COLUMN,
                RUN_COUNT_COLUMN,
                CREATED_NS_COLUMN,
                DELAY_UNTIL_NS_COLUMN,
                RUNNING_SESSION_ID_COLUMN,
                REQUIRED_NETWORK_TYPE_COLUMN,
                DEADLINE_COLUMN,
                CANCEL_ON_DEADLINE_COLUMN,
                CANCELLED_COLUMN,
                SCHEDULE_REQUESTED_AT_NS
        );
        sqLiteDatabase.execSQL(createQuery);
        String createTagsQuery = SqlHelper.create(JOB_TAGS_TABLE_NAME,
                TAGS_ID_COLUMN,
                TAGS_JOB_ID_COLUMN,
                TAGS_NAME_COLUMN);
        sqLiteDatabase.execSQL(createTagsQuery);

        sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS " + TAG_INDEX_NAME + " ON "
                + JOB_TAGS_TABLE_NAME + "(" + DbOpenHelper.TAGS_NAME_COLUMN.columnName + ")");

        createDependeeTagsTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion >= 11) {
            if (oldVersion == 11) {
                addCancelColumn(sqLiteDatabase);
                oldVersion++;
            }
            if (oldVersion == 12) {
                createDependeeTagsTable(sqLiteDatabase);
                oldVersion++;
            }
            if (oldVersion == 13) {
                addScheduleRequestedAtNsColumn(sqLiteDatabase);
                oldVersion++;
            }
        } else {
            sqLiteDatabase.execSQL(SqlHelper.drop(JOB_HOLDER_TABLE_NAME));
            sqLiteDatabase.execSQL(SqlHelper.drop(JOB_TAGS_TABLE_NAME));
            sqLiteDatabase.execSQL(SqlHelper.drop(JOB_DEPENDEE_TAGS_TABLE_NAME));
            sqLiteDatabase.execSQL("DROP INDEX IF EXISTS " + TAG_INDEX_NAME);
            sqLiteDatabase.execSQL("DROP INDEX IF EXISTS " + DEPENDEE_TAG_INDEX_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    private void addCancelColumn(SQLiteDatabase sqLiteDatabase) {
        String query = "ALTER TABLE " + JOB_HOLDER_TABLE_NAME + " ADD COLUMN "
                + CANCELLED_COLUMN.columnName + " " + CANCELLED_COLUMN.type;
        sqLiteDatabase.execSQL(query);
    }

    private void createDependeeTagsTable(SQLiteDatabase sqLiteDatabase) {
        String createDependeeTagsQuery = SqlHelper.create(JOB_DEPENDEE_TAGS_TABLE_NAME,
                DEPENDEE_TAGS_ID_COLUMN,
                DEPENDEE_TAGS_JOB_ID_COLUMN,
                DEPENDEE_TAGS_NAME_COLUMN);
        sqLiteDatabase.execSQL(createDependeeTagsQuery);
        sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS " + DEPENDEE_TAG_INDEX_NAME + " ON "
                + JOB_DEPENDEE_TAGS_TABLE_NAME + "(" + DbOpenHelper.DEPENDEE_TAGS_NAME_COLUMN.columnName + ")");
    }

    private void addScheduleRequestedAtNsColumn(SQLiteDatabase sqLiteDatabase) {
        String query = "ALTER TABLE " + JOB_HOLDER_TABLE_NAME + " ADD COLUMN "
                + SCHEDULE_REQUESTED_AT_NS.columnName + " " + SCHEDULE_REQUESTED_AT_NS.type
                + " DEFAULT " + JobHolder.DEFAULT_SCHEDULE_REQUEST_AT_NS_VALUE;
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }
}
