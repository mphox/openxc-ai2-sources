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
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperStatus;
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

  private String acceleratorPedalPosition = "NO READING";
  private boolean brakePedalStatus = false;
  private String engineSpeed = "NO READING";
  private String fuelConsumed = "NO READING";
  private String fuelLevel = "NO READING";
  private boolean headlampStatus = false;
  private boolean highBeamStatus = false;
  private String ignitionStatus = "NO READING";
  private String latitude = "NO READING";
  private String longitude = "NO READING";
  private String odometer = "NO READING";
  private boolean parkingBrakeStatus = false;
  private String steeringWheelAngle = "NO READING";
  private String torqueAtTransmission = "NO READING";
  private String transmissionGearPosition = "NO READING";
  private String turnSignalStatus = "NO READING";
  //private String vehicleButtonEvent = "NO READING";
  //corresponds to vehicleDoorStatus
  private boolean driverDoorOpen = false;
  private boolean passengerDoorOpen = false;
  private boolean rearLeftDoorOpen = false;
  private boolean rearRightDoorOpen = false;
  private String vehicleSpeed = "NO READING";
  private boolean windshieldWiperStatus = false;




  private ServiceConnection mConnection = new ServiceConnection() {
    // When the VehicleManager starts up, we store a reference to it
    public void onServiceConnected(ComponentName className, IBinder service) {
        Log.d(TAG, "Bound to VehicleManager");
        mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

        try {
            // Bind all of the listeners to the vehicleManager
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAcceleratorPedalPositionListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakePedalStatusListener);
            mVehicleManager.addListener(EngineSpeed.class, mEngineSpeedListener);
            mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.addListener(FuelLevel.class, mFuelLevelListener);
            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampStatusListener);
            mVehicleManager.addListener(HighBeamStatus.class, mHighBeamStatusListener);
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
            mVehicleManager.addListener(Latitude.class, mLatitudeListener);
            mVehicleManager.addListener(Longitude.class, mLongitudeListener);
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkingBrakeStatusListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mSteeringWheelAngleListener);
            mVehicleManager.addListener(TorqueAtTransmission.class, mTorqueAtTransmissionListener);
            mVehicleManager.addListener(TransmissionGearPosition.class, mTransmissionGearListener);
            mVehicleManager.addListener(TurnSignalStatus.class, mTurnSignalStatusListener);
            //mVehicleManager.addListener(VehicleButtonEvent.class, mVehicleButonEventListener);
            mVehicleManager.addListener(VehicleDoorStatus.class, mDoorListener);
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
            mVehicleManager.addListener(WindshieldWiperStatus.class, mWindshieldWiperStatusListener);
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

  private AcceleratorPedalPosition.Listener mAcceleratorPedalPositionListener = new AcceleratorPedalPosition.Listener(){
    @Override
    public void receive(Measurement measurement){
      final AcceleratorPedalPosition mAcceleratorPedalPosition = (AcceleratorPedalPosition) measurement;
      String newAcceleratorPedalPosition = String.valueOf(mAcceleratorPedalPosition.getValue().doubleValue());
      if(!acceleratorPedalPosition.equals(newAcceleratorPedalPosition)) {
        acceleratorPedalPosition = newAcceleratorPedalPosition;
        AcceleratorPedalPositionChanged();
      };
    };
  };

  private BrakePedalStatus.Listener mBrakePedalStatusListener = new BrakePedalStatus.Listener(){
    @Override
    public void receive(Measurement measurement){
      final BrakePedalStatus mBrakePedalStatus = (BrakePedalStatus) measurement;
      boolean newBrakePedalStatus = mBrakePedalStatus.getValue().booleanValue();
      if(newBrakePedalStatus != brakePedalStatus) {
        brakePedalStatus = newBrakePedalStatus;
        BrakePedalStatusChanged();
      };
    };
  };

  private EngineSpeed.Listener mEngineSpeedListener = new EngineSpeed.Listener(){
    @Override
    public void receive(Measurement measurement){
      final EngineSpeed mEngineSpeed = (EngineSpeed) measurement;
      String newEngineSpeed = String.valueOf(mEngineSpeed.getValue().doubleValue());
      if(!engineSpeed.equals(newEngineSpeed)) {
        engineSpeed = newEngineSpeed;
        EngineSpeedChanged();
      };
    };
  };

  private FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener(){
    @Override
    public void receive(Measurement measurement){
      final FuelConsumed mFuelConsumed = (FuelConsumed) measurement;
      String newFuelConsumed = String.valueOf(mFuelConsumed.getValue().doubleValue());
      if(!fuelConsumed.equals(newFuelConsumed)) {
        fuelConsumed = newFuelConsumed;
        FuelConsumedChanged();
      };
    };
  };

  private FuelLevel.Listener mFuelLevelListener = new FuelLevel.Listener(){
    @Override
    public void receive(Measurement measurement){
      final FuelLevel mFuelLevel = (FuelLevel) measurement;
      String newFuelLevel = String.valueOf(mFuelLevel.getValue().doubleValue());
      if(!fuelLevel.equals(newFuelLevel)) {
        fuelLevel = newFuelLevel;
        FuelLevelChanged();
      };
    };
  };

  private HeadlampStatus.Listener mHeadlampStatusListener = new HeadlampStatus.Listener(){
    @Override
    public void receive(Measurement measurement){
      final HeadlampStatus mHeadlampStatus = (HeadlampStatus) measurement;
      boolean newHeadlampStatus = mHeadlampStatus.getValue().booleanValue();
      if(newHeadlampStatus != headlampStatus) {
        headlampStatus = newHeadlampStatus;
        HeadlampStatusChanged();
      };
    };
  };

  private HighBeamStatus.Listener mHighBeamStatusListener = new HighBeamStatus.Listener(){
    @Override
    public void receive(Measurement measurement){
      final HighBeamStatus mHighBeamStatus = (HighBeamStatus) measurement;
      boolean newHighBeamStatus = mHighBeamStatus.getValue().booleanValue();
      if(newHighBeamStatus != highBeamStatus) {
        highBeamStatus = newHighBeamStatus;
        HighBeamStatusChanged();
      };
    };
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

  private Latitude.Listener mLatitudeListener = new Latitude.Listener(){
    @Override
    public void receive(Measurement measurement){
      final Latitude mLatitude = (Latitude) measurement;
      String newLatitude = String.valueOf(mLatitude.getValue().doubleValue());
      if(!latitude.equals(newLatitude)) {
        latitude = newLatitude;
        LatitudeChanged();
      };
    };
  };

  private Longitude.Listener mLongitudeListener = new Longitude.Listener(){
    @Override
    public void receive(Measurement measurement){
      final Longitude mLongitude = (Longitude) measurement;
      String newLongitude = String.valueOf(mLongitude.getValue().doubleValue());
      if(!longitude.equals(newLongitude)) {
        longitude = newLongitude;
        LongitudeChanged();
      };
    };
  };

  private Odometer.Listener mOdometerListener = new Odometer.Listener(){
    @Override
    public void receive(Measurement measurement){
      final Odometer mOdometer = (Odometer) measurement;
      String newOdometer = String.valueOf(mOdometer.getValue().doubleValue());
      if(!odometer.equals(newOdometer)) {
        odometer = newOdometer;
        OdometerChanged();
      };
    };
  };

  private ParkingBrakeStatus.Listener mParkingBrakeStatusListener = new ParkingBrakeStatus.Listener(){
    @Override
    public void receive(Measurement measurement){
      final ParkingBrakeStatus mParkingBrakeStatus = (ParkingBrakeStatus) measurement;
      boolean newParkingBrakeStatus = mParkingBrakeStatus.getValue().booleanValue();
      if(newParkingBrakeStatus != parkingBrakeStatus) {
        parkingBrakeStatus = newParkingBrakeStatus;
        ParkingBrakeStatusChanged();
      };
    };
  };

  private SteeringWheelAngle.Listener mSteeringWheelAngleListener = new SteeringWheelAngle.Listener(){
    @Override
    public void receive(Measurement measurement){
      final SteeringWheelAngle mSteeringWheelAngleListener = (SteeringWheelAngle) measurement;
      String newSteeringWheelAngle = String.valueOf(mSteeringWheelAngleListener.getValue().doubleValue());
      if(!steeringWheelAngle.equals(newSteeringWheelAngle)) {
        steeringWheelAngle = newSteeringWheelAngle;
        SteeringWheelAngleChanged();
      };
    };
  };

  private TorqueAtTransmission.Listener mTorqueAtTransmissionListener = new TorqueAtTransmission.Listener(){
    @Override
    public void receive(Measurement measurement){
      final TorqueAtTransmission mTorqueAtTransmissionListener = (TorqueAtTransmission) measurement;
      String newTorqueAtTransmission = String.valueOf(mTorqueAtTransmissionListener.getValue().doubleValue());
      if(!torqueAtTransmission.equals(newTorqueAtTransmission)) {
        torqueAtTransmission = newTorqueAtTransmission;
        TorqueAtTransmissionChanged();
      };
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

  private TurnSignalStatus.Listener mTurnSignalStatusListener  = new TurnSignalStatus.Listener() {
    @Override
    public void receive(Measurement measurement) {
      final TurnSignalStatus status = (TurnSignalStatus) measurement;
      final TurnSignalStatus.TurnSignalPosition statusEnum = status.getValue().enumValue();
      String newStatus = "";

      switch(statusEnum) {
        case LEFT:
          newStatus = "LEFT";
          break;
        case OFF:
          newStatus = "OFF";
          break;
        case RIGHT:
          newStatus = "RIGHT";
          break;
        default:
          break;
      }

      if (!turnSignalStatus.equals(newStatus)) {
        turnSignalStatus = newStatus;
        TurnSignalStatusChanged();
      }
    };
  };

  private VehicleDoorStatus.Listener mDoorListener = new VehicleDoorStatus.Listener() {
    @Override
    public void receive(Measurement measurement) {
      final VehicleDoorStatus mDoorStatus = (VehicleDoorStatus) measurement;
      //
      final VehicleDoorStatus.DoorId mDoorId = mDoorStatus.getValue().enumValue();

      Boolean oldValue;

      switch(mDoorId) {
      case DRIVER:
        oldValue = driverDoorOpen;
        driverDoorOpen = mDoorStatus.getEvent().booleanValue();
        if(oldValue!=driverDoorOpen){
          if(driverDoorOpen){
            DriverDoorOpened();
          }
          else{
            DriverDoorClosed();
          }
        }
        break;
      case PASSENGER:
        oldValue = passengerDoorOpen;
        passengerDoorOpen = mDoorStatus.getEvent().booleanValue();
        if(oldValue!=passengerDoorOpen){
          if(passengerDoorOpen){
            PassengerDoorOpened();
          }
          else{
            PassengerDoorClosed();
          }
        }
        break;
      case REAR_LEFT:
        oldValue = rearLeftDoorOpen;
        rearLeftDoorOpen = mDoorStatus.getEvent().booleanValue();
        if(oldValue!=rearLeftDoorOpen){
          if(rearLeftDoorOpen){
            RearLeftDoorOpened();
          }
          else{
            RearLeftDoorClosed();
          }
        }
        break;
      case REAR_RIGHT:
        oldValue = rearRightDoorOpen;
        rearRightDoorOpen = mDoorStatus.getEvent().booleanValue();
        if(oldValue!=rearRightDoorOpen){
          if(rearRightDoorOpen){
            RearRightDoorOpened();
          }
          else{
            RearLeftDoorOpened();
          }
        }
        break;
      default:
        break;
      };
    };
  };
  
  private VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
    @Override
    public void receive(Measurement measurement) {
      final VehicleSpeed mSpeed = (VehicleSpeed) measurement;
      String newSpeed = String.valueOf(mSpeed.getValue().doubleValue());
      if (!vehicleSpeed.equals(newSpeed)) {
        vehicleSpeed = newSpeed;
        VehicleSpeedChanged();
      }
    };
  };
  
  private WindshieldWiperStatus.Listener mWindshieldWiperStatusListener = new WindshieldWiperStatus.Listener() {
    @Override
    public void receive(Measurement measurement) {
      final WindshieldWiperStatus mWindshieldWiperStatus = (WindshieldWiperStatus) measurement;
      boolean newWindshieldWiperStatus = mWindshieldWiperStatus.getValue().booleanValue();
      if(windshieldWiperStatus!=newWindshieldWiperStatus) {
        windshieldWiperStatus = newWindshieldWiperStatus;
        WindshieldWiperStatusChanged();
      }
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
    form.registerForOnPause(this);
    form.registerForOnNewIntent(this);
    form.registerForOnInitialize(this);
    Log.d(TAG, "OpenXC component created");
  }

  /**
  * Return the accelerator pedal depression as a percentage
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String AcceleratorPedalPosition() {
    return acceleratorPedalPosition;
  }

  /**
  * Return the accelerator pedal depression as a percentage
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean BrakePedalStatus() {
    return brakePedalStatus;
  }

  /**
  * Return the engine speed in RPM
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String EngineSpeed() {
    return engineSpeed;
  }

  /**
  * Return the amount of fuel consumed, in Liters,
  * since the vehicle started
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String FuelConsumed() {
    return fuelConsumed;
  }

  /**
  * Return the fuel level as a percentage
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String FuelLevel() {
    return fuelLevel;
  }

  /**
  * Return true if the headlamp is on
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean HeadlampStatus() {
    return headlampStatus;
  }

  /**
  * Return true if the high beam is on
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean HighBeamStatus() {
    return highBeamStatus;
  }

  /**
  * Return the ignition position.
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String IgnitionStatus() {
    return ignitionStatus;
  }

  /**
  * Return the car's latitude
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String Latitude() {
    return latitude;
  }

  /**
  * Return the car's longitude
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String Longitude() {
    return longitude;
  }

  /**
  * Return the car's odometer value in Kilometers
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String Odometer() {
    return odometer;
  }

  /**
  * Return the true if the parking break is on
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean ParkingBrakeStatus() {
    return parkingBrakeStatus;
  }

  /**
  * Return the steering wheel angle, 0 when the wheel is centered,
  * negative when it's turned to the left, positive when it's turned
  * to the right
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String SteeringWheelAngle() {
    return steeringWheelAngle;
  }

  /**
  * Return the torque in the transmission in NewtonMeters
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String TorqueAtTransmission() {
    return torqueAtTransmission;
  }

  /**
  * Return the transmission gear position
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String TransmissionGearPosition() {
    return transmissionGearPosition;
  }

  /**
  * Return the transmission gear position
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String TurnSignalStatus() {
    return turnSignalStatus;
  }


  /**
  * Return true if the driver door is open
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean DriverDoorOpen() {
    return driverDoorOpen;
  }

  /**
  * Return true if the driver door is open
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean PassengerDoorOpen() {
    return passengerDoorOpen;
  }

  /**
  * Return true if the driver door is open
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean RearRightDoorOpen() {
    return rearRightDoorOpen;
  }

  /**
  * Return true if the driver door is open
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean RearLeftDoorOpen() {
    return rearLeftDoorOpen;
  }

  /**
  * Return the speed
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String VehicleSpeed() {
    return vehicleSpeed;
  }

  /**
  * Return true if the windshield wipers are on
  */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean WindshieldWiperStatus() {
    return windshieldWiperStatus;
  }
  
  @SimpleEvent
  public void AcceleratorPedalPositionChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "AcceleratorPedalPositionChanged");
      }
    });
  }
  
  @SimpleEvent
  public void BrakePedalStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "BrakePedalStatusChanged");
      }
    });
  }
  
  @SimpleEvent
  public void EngineSpeedChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "EngineSpeedChanged");
      }
    });
  }
  
  @SimpleEvent
  public void FuelConsumedChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "FuelConsumedChanged");
      }
    });
  }
  
  @SimpleEvent
  public void FuelLevelChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "FuelLevelChanged");
      }
    });
  }
  
  @SimpleEvent
  public void HeadlampStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "HeadlampStatusChanged");
      }
    });
  }
  
  @SimpleEvent
  public void HighBeamStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "HighBeamStatusChanged");
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
  public void LatitudeChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "LatitudeChanged");
      }
    });
  }

  @SimpleEvent
  public void LongitudeChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "LongitudeChanged");
      }
    });
  }

  @SimpleEvent
  public void OdometerChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "OdometerChanged");
      }
    });
  }

  @SimpleEvent
  public void ParkingBrakeStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "ParkingBrakeStatusChanged");
      }
    });
  }
  
  @SimpleEvent
  public void SteeringWheelAngleChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "SteeringWheelAngleChanged");
      }
    });
  }
  
  @SimpleEvent
  public void TorqueAtTransmissionChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "TorqueAtTransmissionChanged");
      }
    });
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
  public void TurnSignalStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "TurnSignalStatusChanged");
      }
    });
  }
  

  //Door Open and Close Events
  @SimpleEvent
  public void DriverDoorOpened() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "DriverDoorOpened");
      }
    });
  }
  
  @SimpleEvent
  public void DriverDoorClosed() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "DriverDoorClosed");
      }
    });
  }

  @SimpleEvent
  public void PassengerDoorOpened() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "PassengerDoorOpened");
      }
    });
  }
  
  @SimpleEvent
  public void PassengerDoorClosed() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "PassengerDoorClosed");
      }
    });
  }

  @SimpleEvent
  public void RearRightDoorOpened() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "RearRightDoorOpened");
      }
    });
  }
  
  @SimpleEvent
  public void RearRightDoorClosed() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "RearRightDoorClosed");
      }
    });
  }

  @SimpleEvent
  public void RearLeftDoorOpened() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "RearLeftDoorOpened");
      }
    });
  }
  
  @SimpleEvent
  public void RearLeftDoorClosed() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "RearLeftDoorClosed");
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

  @SimpleEvent
  public void WindshieldWiperStatusChanged() {
    final Component comp = this;
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(comp, "WindshieldWiperStatusChanged");
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