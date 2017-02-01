package uk.org.potentialdifference.stillapp;

import com.burnweb.rnpermissions.RNPermissionsPackage;
import android.app.Application;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.RNFetchBlob.RNFetchBlobPackage;
import com.lwansbrough.RCTCamera.RCTCameraPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.pusherman.networkinfo.RNNetworkInfoPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.corbt.keepawake.KCKeepAwakePackage;
import fr.bamlab.rncameraroll.CameraRollPackage;
import com.dieam.reactnativepushnotification.ReactNativePushNotificationPackage;


import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    protected boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNFetchBlobPackage(),
          new RCTCameraPackage(),
          new RNNetworkInfoPackage(),
          new KCKeepAwakePackage(),
          new RNDeviceInfo(),
          new CameraRollPackage(),
          new RNPermissionsPackage(),
          new ReactNativePushNotificationPackage()
      );
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
      return mReactNativeHost;
  }
}
