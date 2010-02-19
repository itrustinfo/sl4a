/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.savedInstanceState See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.google.ase;

import java.io.IOException;

import com.google.ase.interpreter.InterpreterUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;

/**
 * A text editor for scripts.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptEditor extends Activity {

  private static String TAG = "ScriptEditor";
  private EditText mNameText;
  private EditText mContentText;

  private static enum MenuId {
    SAVE, SAVE_AND_RUN, HELP;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum RequestCode {
    RPC_HELP
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.editor);
    CustomWindowTitle.buildWindowTitle(this);

    // TODO(damonkohler): Rename these views.
    mNameText = (EditText) findViewById(R.id.title);
    mContentText = (EditText) findViewById(R.id.body);

    Intent intent = getIntent();
    String name = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    if (name != null) {
      mNameText.setText(name);
    }
    String content = intent.getStringExtra(Constants.EXTRA_SCRIPT_CONTENT);
    if (content == null && name != null) {
      try {
        content = ScriptStorageAdapter.readScript(name);
        mContentText.setText(content);
      } catch (IOException e) {
        Log.e(TAG, "Failed to read script.", e);
      }
    } else if (content != null) {
      mContentText.setText(content);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MenuId.SAVE.getId(), 0, "Save");
    menu.add(0, MenuId.SAVE_AND_RUN.getId(), 0, "Save & Run");
    menu.add(0, MenuId.HELP.getId(), 0, "Help");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == MenuId.SAVE.getId()) {
      save();
      finish();
    } else if (item.getItemId() == MenuId.SAVE_AND_RUN.getId()) {
      save();
      startActivity(IntentBuilders.buildLaunchIntent(mNameText.getText().toString()));
      finish();
    } else if (item.getItemId() == MenuId.HELP.getId()) {
      Intent intent = new Intent(this, ApiBrowser.class);
      intent.putExtra(Constants.EXTRA_INTERPRETER_NAME,
          InterpreterUtils.getInterpreterForScript(mNameText.getText().toString()).getName());
      intent.putExtra(Constants.EXTRA_SCRIPT_TEXT, mContentText.getText().toString());
      startActivityForResult(intent, RequestCode.RPC_HELP.ordinal());
    }
    return super.onOptionsItemSelected(item);
  }

  private void save() {
    ScriptStorageAdapter.writeScript(mNameText.getText().toString(),
        mContentText.getText().toString());
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    RequestCode request = RequestCode.values()[requestCode];
    if (resultCode == RESULT_OK) {
      switch (request) {
        case RPC_HELP:
          String rpcText = data.getStringExtra(Constants.EXTRA_RPC_HELP_TEXT);
          mContentText.append(rpcText);
          break;
        default:
          break;
      }
    } else {
      switch (request) {
        case RPC_HELP:
          break;
        default:
          break;
      }
    }
  }

}
