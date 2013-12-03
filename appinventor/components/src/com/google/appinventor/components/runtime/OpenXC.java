// Copyright 2012 MIT All rights reserved

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.GingerbreadUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * Controller for OpenXC Component
 *
 */
@DesignerComponent(version = YaVersion.OPENXC_COMPONENT_VERSION,
    description = "<p>Non-visible component to provide NFC capabilities." +
    "For now this component supports the reading and writing of text" +
    "(if supported by the device)</p>" +
    "<p>In order to read and write text tags, the component must have its " +
    "<code>ReadMode</code> property set to True or False respectively.</p>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/openxc.png")

@SimpleObject
public class OpenXC extends AndroidNonvisibleComponent
implements OnStopListener, OnResumeListener, OnPauseListener, OnNewIntentListener, Deleteable {
  private static final String TAG = "nearfield";
  private Activity activity;

   /**
   * Creates a new OpenXC component
   * @param container  ignored (because this is a non-visible component)
   */
  public OpenXC(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    
    // register with the forms to that OnResume and OnNewIntent
    // messages get sent to this component
    form.registerForOnResume(this);
    form.registerForOnNewIntent(this);
    form.registerForOnPause(this);
    Log.d(TAG, "OpenXC component created");
  }

    @Override
  public void onNewIntent(Intent intent) {
    // TODO Auto-generated method stub
  }

  // TODO: Re-enable NFC communication if it had been disabled
  @Override
  public void onResume() {
    // TODO Auto-generated method stub
  }

  // TODO: Disable NFC communication in onPause and onDelete
  // and restore it in onResume

  public void onPause() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onDelete() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onStop() {
    // TODO Auto-generated method stub		
  }

}