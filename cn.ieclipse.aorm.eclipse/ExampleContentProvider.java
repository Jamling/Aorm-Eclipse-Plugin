package org.melord.android.orm.example;

import org.melord.android.orm.Criteria;
import org.melord.android.orm.CursorUtils;
import org.melord.android.orm.Restrictions;
import org.melord.android.orm.Session;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ExampleContentProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		return null;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}

