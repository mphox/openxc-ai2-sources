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
import com.openxc.measurements.VehicleSpeed;
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
import com.google.appinventor.components.runtime.util.OnInitializeListener;

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
implements OnInitializeListener, OnNewIntentListener, OnPauseListener, OnResumeListener, Deleteable {
  private static final String TAG = "OPENXC";
  private Activity activity;

  private VehicleManager mVehicleManager;

  private String ignitionStatus = "NO READING";
  private IgnitionStatus.IgnitionPosition mIgnitionStatus;

  private String transmissionGearPosition = "NO READING";
  private TransmissionGearPosition.GearPosition mGearPosition;

  private String speed = "NO READING";
  private VehicleSpeed mVehicleSpeed;

  private Boolean driverDoorOpen = false;
  private Boolean passengerDoorOpen = false;
  private Boolean rearLeftDoorOpen = false;
  private Boolean rearRightDoorOpen = false;


  
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
      final TransmissionGearPosition status = (TransmissionGearPosition) measurement;
      final TransmissionGearPosition.GearPosition statusEnum = status.getValue().enumValue();
      String newStatus = "";

      switch(statusEnum) {
        case REVERSE:
          newStatus = "REVERSE";
          break;
        case NEUTRAL:
          newStatus = "NEUTRAL";
          break;
        case FIRST:
          newStatus = "FIRST";
          break;
        case SECOND:
          newStatus = "SECOND";
          break;
        case THIRD:
          newStatus = "THIRD";
          break;
        case FOURTH:
          newStatus = "FOURTH";
          break;
        case FIFTH:
          newStatus = "FIFTH";
          break;
        case SIXTH:
          newStatus = "SIXTH";
          break;
        case SEVENTH:
          newStatus = "SEVENTH";
          break;
        case EIGHTH:
          newStatus = "EIGHTH";
          break;
        default:
          break;
      }
      if (!transmissionGearPosition.equals(newStatus)) {
        transmissionGearPosition = newStatus;
        TransmissionGearPositionChanged();
      }
    };
  };
  
  private VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
      @Override
      public void receive(Measurement measurement) {
        mVehicleSpeed = (VehicleSpeed) measurement;
        speed = String.valueOf(mVehicleSpeed.getValue().doubleValue());
        VehicleSpeedChanged();
      };
    };

/*
  private VehicleDoorStatus.Listener mDoorListener = new VehicleDoorStatus.Listener() {
      @Override
      public void receive(Measurement measurement) {
        mDoorStatus = (VehicleDoorStatus) measurement;
        mDoorStatusEnum = mDoorStatus.getValue().enumValue();

        switch(mDoorStatusEnum) {
        case DRIVER:
          newStatus = "REVERSE";
          break;
        case PASSENGER:
          newStatus = "NEUTRAL";
          break;
        case REAR_LEFT:
          newStatus = "FIRST";
          break;
        case REAR_RIGHT:
          newStatus = "SECOND";
          break;
        default:
          break;
      }
      };
  }

  */
  
   /**
   * Creates a new OpenXC component
   * @param container  ignored (because this is a non-visible component)
   */
  public OpenXC(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    
    form.registerForOnResume(this);
    form.registerForOnPause(this); 
    form.registerForOnNewIntent(this); 
    form.registerForOnInitialize(this);     
    Log.d(TAG, "OpenXC component created");
  }

  /**
  * Return the ignition position.
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String IgnitionStatus() {;
    return ignitionStatus;
  }

  /**
  * Return the transmission
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String TransmissionGearPosition() {
    return transmissionGearPosition;
  }

  /**
  * Return the speed
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String VehicleSpeed() {
    return speed;
  }
  
  @SimpleEvent
  public void TransmissionGearPositionChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "TransmissionGearPositionChanged");
      }
    });
  }

  @SimpleEvent
  public void IgnitionStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "IgnitionStatusChanged");
      }
    });
  }
  
  @SimpleEvent
  public void VehicleSpeedChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "VehicleSpeedChanged");
      }
    });
  }

  @Override
  public void onNewIntent(Intent intent) {
  }

  @Override
  public void onPause() {
  }

  @Override
  public void onResume() {
    // When the activity returns from the background,
    // re-connect to the VehicleManager so we can receive updates.
    if(mVehicleManager == null) {
      Intent intent = new Intent(activity, VehicleManager.class);
      activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  public void onInitialize() {
    // When the activity starts up,
    // re-connect to the VehicleManager so we can receive updates.
    if(mVehicleManager == null) {
      Intent intent = new Intent(activity, VehicleManager.class);
      activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  public void onDelete() {
    activity.unbindService(mConnection);
  }
}