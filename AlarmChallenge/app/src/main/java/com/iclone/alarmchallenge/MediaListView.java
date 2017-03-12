

package com.iClone.AlarmChallenge;

import java.util.ArrayList;
import java.util.Arrays;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;


public class MediaListView extends ListView implements OnItemClickListener {
  public interface OnItemPickListener {
    void onItemPick(Uri uri, String name);
  }

  protected static int DEFAULT_TONE_INDEX = -69;

  private Cursor cursor;
  private Cursor staticCursor;
  private MediaPlayer mPlayer;
  private ViewFlipper flipper;
  private Activity cursorManager;
  private Uri contentUri;
  private String nameColumn;
  private String sortOrder;
  private OnItemPickListener listener;

  private String selectedName;
  private Uri selectedUri;

  public MediaListView(Context context) {
    this(context, null);
  }

  public MediaListView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MediaListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setChoiceMode(CHOICE_MODE_SINGLE);
    setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (flipper == null || flipper.getDisplayedChild() == 0) {
          return false;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
          if (event.getAction() == KeyEvent.ACTION_UP) {
            if (mPlayer != null) {
              mPlayer.stop();
            }
            flipper.setInAnimation(getContext(), R.anim.slide_in_right);
            flipper.setOutAnimation(getContext(), R.anim.slide_out_right);
            flipper.showPrevious();
          }
          return true;
        }
        return false;
      }
    });
  }

  public void setMediaPlayer(MediaPlayer mPlayer) {
    this.mPlayer = mPlayer;
  }

  protected MediaPlayer getMediaPlayer() {
    return mPlayer;
  }

  public void addToFlipper(ViewFlipper flipper) {
    this.flipper = flipper;
    flipper.setAnimateFirstView(false);
    flipper.addView(this);
  }

  protected ViewFlipper getFlipper() {
    return flipper;
  }

  public void setCursorManager(Activity activity) {
    this.cursorManager = activity;
  }

  protected void manageCursor(Cursor cursor) {
    cursorManager.startManagingCursor(cursor);
  }

  protected void query(Uri contentUri, String nameColumn, String selection,
      int rowResId, String[] displayColumns, int[] resIDs) {
    this.nameColumn = nameColumn;
    final ArrayList<String> queryColumns =
      new ArrayList<>(displayColumns.length + 1);
    queryColumns.addAll(Arrays.asList(displayColumns));
    
    
    if (!queryColumns.contains(BaseColumns._ID)) {
      queryColumns.add(BaseColumns._ID);
    }

      Cursor dbCursor;

      if (ContextCompat.checkSelfPermission(getContext(),
              Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED) {
          dbCursor = getContext().getContentResolver().query(
                  contentUri, queryColumns.toArray(new String[queryColumns.size()]),
                  selection, null, sortOrder);
      } else {
          dbCursor = new MatrixCursor(queryColumns.toArray(new String[queryColumns.size()]));
      }

    if (staticCursor != null) {
      Cursor[] cursors = new Cursor[] { staticCursor, dbCursor };
      cursor = new MergeCursor(cursors);
    } else {
      cursor = dbCursor;
    }
    manageCursor(cursor);

    this.contentUri = contentUri;

    final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        getContext(), rowResId, cursor, displayColumns, resIDs, 1);
    
    adapter.setViewBinder(new ViewBinder() {
      @Override
      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (view.getVisibility() == View.VISIBLE && view instanceof TextView) {
          TextView text = (TextView) view;
          if (isItemChecked(cursor.getPosition())) {
            text.setTypeface(Typeface.DEFAULT_BOLD);
          } else {
            text.setTypeface(Typeface.DEFAULT);
          }
        }
        
        return false;
      }});
    setAdapter(adapter);
    setOnItemClickListener(this);
  }

  public void overrideSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  protected void includeStaticCursor(Cursor cursor) {
    staticCursor = cursor;
  }

  
  
  public String getLastSelectedName() {
    return selectedName;
  }

  public Uri getLastSelectedUri() {
    return selectedUri;
  }

  public void setMediaPickListener(OnItemPickListener listener) {
    this.listener = listener;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    setItemChecked(position, true);
    cursor.moveToPosition(position);
    selectedName = cursor.getString(cursor.getColumnIndex(nameColumn));
    final int toneIndex = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
    if (toneIndex == DEFAULT_TONE_INDEX) {
      selectedUri = AlarmUtil.getDefaultAlarmUri();
    } else {
      selectedUri = Uri.withAppendedPath(contentUri, "" + toneIndex);
    }
    if (listener != null) {
      listener.onItemPick(selectedUri, selectedName);
    }
  }
}