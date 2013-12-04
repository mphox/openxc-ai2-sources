// Copyright 2012 MIT All rights reserved

package com.google.appinventor.components.runtime;

import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    description = "<p>Non-visible component to provide access to OpenXC data." +
    "For now this component supports reading data as a string and listener" +
    "blocks for when data of a particular type is changed.",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/openxc.png")

@SimpleObject
@UsesLibraries(libraries = "openxc.jar," + "compatibility-v13-18.jar," + "guava-14.0.1.jar," + "jackson-core-2.2.3.jar," + "protobuf-java-2.5.0.jar")
public class OpenXC extends AndroidNonvisibleComponent
implements OnNewIntentListener, OnPauseListener, OnResumeListener, Deleteable {
  private static final String TAG = "OPENXC";
  private Activity activity;

  private VehicleManager mVehicleManager;

  private String ignitionStatus = "NO READING";
  private IgnitionStatus.IgnitionPosition mIgnitionStatus;

  private String transmissionGearPosition;
  private TransmissionGearPosition.GearPosition mGearPosition;

  private String speed = "NO READING";
  private VehicleSpeed mVehicleSpeed;
  
  private ServiceConnection mConnection = new ServiceConnection() {
    // When the VehicleManager starts up, we store a reference to it
    public void onServiceConnected(ComponentName className, IBinder service) {
        Log.d(TAG, "Bound to VehicleManager");
        mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

        try {
            // Bind all of the listeners to the vehicleManager
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
            mVehicleManager.addListener(TransmissionGearPosition.class, mTransmissionGearListener);
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
        } catch (VehicleServiceException e) {
            e.printStackTrace();
        } catch (UnrecognizedMeasurementTypeException e) {
            e.printStackTrace();
        }
    }

    // Called when the connection with the service disconnects unexpectedly
    public void onServiceDisconnected(ComponentName className) {
        Log.d(TAG, "VehicleManager Service disconnected unexpectedly");
        mVehicleManager = null;
    }
  };

  private IgnitionStatus.Listener mIgnitionStatusListener  = new IgnitionStatus.Listener() {
    @Override
    public void receive(Measurement measurement) {
      final IgnitionStatus status = (IgnitionStatus) measurement;
      final IgnitionStatus.IgnitionPosition statusEnum = status.getValue().enumValue();
      String newStatus = "";
      
      Log.d(TAG, "Received Ignition Status:" + status); 

      switch(statusEnum) {
        case ACCESSORY:
          newStatus = "ACCESSORY";
          break;
        case OFF:
          newStatus = "OFF";
          break;
        case RUN:
          newStatus = "RUN";
          break;
        case START:
          newStatus = "START";
          break;
        default:
          break;
      } 

      if (!ignitionStatus.equals(newStatus)) {
        ignitionStatus = newStatus;
        IgnitionStatusChanged();
      } 
    };
  };

  private TransmissionGearPosition.Listener mTransmissionGearListener = new TransmissionGearPosition.Listener() {
    @Override
    public void receive(Measurement measurement) {
      Log.d(TAG, "received Transmission Gear Position:" + (TransmissionGearPosition) measurement); 
    };
  };
  
  private VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
      @Override
      public void receive(Measurement measurement) {
        Log.d(TAG, "received vehicle speed:" + ((VehicleSpeed) measurement).getValue().doubleValue()); 
        mVehicleSpeed = (VehicleSpeed) measurement;
        speed = mVehicleSpeed.getValue().doubleValue();
        VehicleSpeedChanged();
      };
    };

   /**
   * Creates a new OpenXC component
   * @param container  ignored (because this is a non-visible component)
   */
  public OpenXC(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    
    form.registerForOnResume(this);
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

  /**
  * Return the speed
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String VehicleSpeed() {
    Log.d(TAG, "String message method stared");
    return speed;
  }
  
  @SimpleEvent
  public void TransmissionGearPositionChanged() {
    Log.d(TAG, "String message method stared");
    EventDispatcher.dispatchEvent(this, "TransmissionGearPositionChanged");
  }

  @SimpleEvent
  public void IgnitionStatusChanged() {
    Log.d(TAG, "IGNITION");
    //EventDispatcher.dispatchEvent(this, "IgnitionStatusChanged");
  }
  
  @SimpleEvent
  public void VehicleSpeedChanged() {
    Log.d(TAG, "String message method stared");
    EventDispatcher.dispatchEvent(this, "VehicleSpeedChanged");
  }



  @Override
  public void onNewIntent(Intent intent) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onPause() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onResume() {
    // When the activity starts up or returns from the background,
    // re-connect to the VehicleManager so we can receive updates.
    if(mVehicleManager == null) {
        Intent intent = new Intent(activity, VehicleManager.class);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  public void onDelete() {
    
  }
}