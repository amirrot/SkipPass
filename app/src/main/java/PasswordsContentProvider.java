package com.google.android.skippass;

import com.google.android.skippass.passwords_file.Password;
import com.google.android.skippass.passwords_file.SignedUser;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class PasswordsContentProvider extends ContentProvider {

  public static class Data {
    public static final String ID = "_ID";
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String PASSWORD = "Password";
    public static final String USERNAME = "Username";
    public static final String ADDITIONAL_DATA1 = "Additional1";
    public static final String ADDITIONAL_DATA2 = "Additional2";
    public static final String ADDITIONAL_DATA3 = "Additional3";
    public static final String COMMENT = "Comment";
    public static final String PAST_PASSWORDS = "Comment";
    public static final String FILLERS = "Fillers";
  }

  private static final String PASSWORDS_FILENAME = "skippass_dat2";

  private static final String PROVIDER_NAME = "com.google.android.skippass.Passwords";
  private static final String URL = "content://" + PROVIDER_NAME + "/passwords";
  public static final Uri CONTENT_URI =  Uri.parse(URL);

  private SignedUser signedUser;

  private static final Object MUTEX = new Object();

  private SignedUser getSignedUser(String password) throws IncorrectPassword {
    synchronized (MUTEX) {
      if (signedUser == null) {
        InputStream is;
        try {
          is = getContext().openFileInput(PASSWORDS_FILENAME);
        } catch (FileNotFoundException e) {
          signedUser = new SignedUser(password);
          writeUser();
          return signedUser;
        }
        try {
          signedUser = new SignedUser(is, password);
        } catch (Throwable e) {
          e.printStackTrace();
          throw new IncorrectPassword();
        }
      }
      return signedUser;
    }
  }

  private void writeUser() {
    OutputStream os;
    try {
      os = getContext().openFileOutput(PASSWORDS_FILENAME, Context.MODE_PRIVATE);
      signedUser.writeToDisk(os);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] wtf1, String masterKey) {
    // selection == REGEXP matching partially on entire entry
    // projection == list of fields to provide
    SignedUser su;
    try {
      su = getSignedUser(masterKey);
    } catch (IncorrectPassword incorrectPassword) {
      incorrectPassword.printStackTrace();
      return null;
    }
    MatrixCursor res = new MatrixCursor(projection);
    for (Password pwd : su.getPasswords()) {
      if (pwd.match(selection)) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {
          String projectionField = projection[i];
          if (projectionField == Data.ID) {
            row[i] = "" + pwd.id;
          } else if (projectionField == Data.NAME) {
            row[i] = pwd.name;
          } else if (projectionField == Data.PASSWORD) {
            row[i] = pwd.password;
          } else {
            row[i] = null;
          }
        }
        res.addRow(row);
      }
    }
    return res;
  }

  @Override
  public String getType(Uri uri) {
    return "vnd.android.cursor.item/com.google.android.skippass.Passwords.passwords";
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(Uri uri, String s, String[] strings) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }

  private class IncorrectPassword extends Throwable {

  }
}
