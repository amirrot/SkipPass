package com.google.android.skippass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by dimrub on 1/8/15.
 */
public class EditEntryActivity extends Activity {
  private static final String ENTRY_ID_EXTRA = "entry_id";
  private Cursor mCursor;
  static final String[] PROJECTION = new String[] {PasswordsContentProvider.Data.USERNAME,
    PasswordsContentProvider.Data.PASSWORD};

  private long parseIntent(Intent intent) {
    if (intent.hasExtra(ENTRY_ID_EXTRA)) {
      return Long.parseLong((intent.getStringExtra(ENTRY_ID_EXTRA)), 10);
    }
    return -1;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_entry);
    // Queries the user dictionary and returns results
    long id = parseIntent(getIntent());
    mCursor = getContentResolver().query(
        UserDictionary.Words.CONTENT_URI,   // The content URI of the words table
        PROJECTION,                         // The columns to return for each row
        "id=" + id,                          // Selection criteria
        null,                               // Selection criteria
        null);                              // The sort order for the returned rows
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.edit_entry_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
  }

  public static Intent createIntent(Context context, String entryId) {
    // null entryId designates a request to create new entry.
    Intent intent = new Intent(context, EditEntryActivity.class);
    intent.putExtra(ENTRY_ID_EXTRA, entryId);
    return intent;
  }
}
