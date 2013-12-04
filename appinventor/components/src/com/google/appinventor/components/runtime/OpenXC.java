// Copyright 2012 MIT All rights reserved

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.VehicleServiceException;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
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
@UsesLibraries(libraries = "openxc.jar")
public class OpenXC extends AndroidNonvisibleComponent
implements OnStopListener, OnResumeListener, OnPauseListener, OnNewIntentListener, Deleteable {
  private static final String TAG = "OPENXC";
  private Activity activity;


  private String ignitionStatus;
  private IgnitionStatus.IgnitionPosition mIgnitionStatus;

  private String transmissionGearPosition;
  private TransmissionGearPosition.GearPosition mGearPosition;




  private IgnitionStatus.Listener mIgnitionStatusListener  = new IgnitionStatus.Listener() {
    @Override
    public void receive(Measurement measurement) {
      Log.d(TAG, "received Ignition Status:" + (IgnitionStatus) measurement); 
      //setTransmissionGearPosition()
    };
  };

  private TransmissionGearPosition.Listener mTransmissionGearListener = new TransmissionGearPosition.Listener() {
    @Override
    public void receive(Measurement measurement) {
      Log.d(TAG, "received Transmission Gear Position:" + (TransmissionGearPosition) measurement); 
    };
  };
  

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
    Log.d(TAG, "component created");
  }

  /**
  * Return the ignition position.
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String IgnitionStatus() {
    Log.d(TAG, "String message method stared");
    return ignitionStatus;
  }

  /**
  * Return the transmission
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String TransmissionGearPosition() {
    Log.d(TAG, "String message method stared");
    return transmissionGearPosition;
  }

  @SimpleEvent
  public void TransmissionGearPositionChanged() {
    Log.d(TAG, "String message method stared");
    EventDispatcher.dispatchEvent(this, "TrnsmissionGearPositionChanged");
  }

  @SimpleEvent
  public void IgnitionStatusChanged() {
    Log.d(TAG, "String message method stared");
    EventDispatcher.dispatchEvent(this, "IgnitionStatusChanged");
  }


  @Override
  public void onNewIntent(Intent intent) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub
  }

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