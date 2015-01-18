package com.google.android.skippass;

import com.google.android.skippass.PasswordsContentProvider.Data;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

/**
 * Created by dimrub on 1/7/15.
 */
public class SelectEntryActivity extends ListActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = "SP";
  // This is the Adapter being used to display the list's data
  SimpleCursorAdapter adapter;

  // These are the Contacts rows that we will retrieve
  static final String[] PROJECTION = new String[] {PasswordsContentProvider.Data.ID,
      PasswordsContentProvider.Data.NAME, Data.PASSWORD};

  // This is the select criteria
  static final String SELECTION = ".*";

  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create a progress bar to display while the list loads
    progressBar = new ProgressBar(this);
    progressBar.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT,
        ListView.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    progressBar.setIndeterminate(true);
    getListView().setEmptyView(progressBar);

    // Must add the progress bar to the root of the layout
    ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
    root.addView(progressBar);

    // For the cursor adapter, specify which columns go into which views
    String[] fromColumns = {PasswordsContentProvider.Data.NAME,
        Data.PASSWORD};
    int[] toViews = {android.R.id.text1, android.R.id.text2};

    // Create an empty adapter we will use to display the loaded data.
    // We pass null for the cursor, then update it in onLoadFinished()
    adapter = new SimpleCursorAdapter(this,
        android.R.layout.simple_list_item_2, null,
        fromColumns, toViews, 0);
    setListAdapter(adapter);

    // Prepare the loader.  Either re-connect with an existing one,
    // or start a new one.
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    getMenuInflater().inflate(R.menu.select_entry_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_create_entry) {
      Intent intent = EditEntryActivity.createIntent(SelectEntryActivity.this, null);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // Called when a new Loader needs to be created
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // Now create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed.
    return new CursorLoader(this, PasswordsContentProvider.CONTENT_URI,
        PROJECTION, SELECTION, null, "12345");
  }

  // Called when a previously created loader has finished loading
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    // Swap the new cursor in.  (The framework will take care of closing the
    // old cursor once we return.)
    adapter.swapCursor(data);
    progressBar.setVisibility(View.GONE);
  }

  // Called when a previously created loader is reset, making the data unavailable
  public void onLoaderReset(Loader<Cursor> loader) {
    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.  We need to make sure we are no
    // longer using it.
    adapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent intent = EditEntryActivity.createIntent(SelectEntryActivity.this, "" + id);
    startActivity(intent);
  }
}
