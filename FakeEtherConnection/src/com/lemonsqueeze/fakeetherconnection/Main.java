package com.lemonsqueeze.fakeetherconnection;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.*;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;

//import android.content.Context;
//import android.content.SharedPreferences;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.SupplicantState;
import android.net.DhcpInfo;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import org.apache.http.conn.util.InetAddressUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;


public class Main implements IXposedHookLoadPackage 
{
  private XSharedPreferences pref;
  private LoadPackageParam lpparam;

  // debug info: 0=quiet, 1=log function calls, 2=also dump stack traces
  private final int debug_level = 1;

  public boolean hack_enabled()
  {
      boolean master_switch = pref.getBoolean("master", true);
      boolean app_enabled = pref.getBoolean(lpparam.packageName, false);
      return (master_switch && app_enabled);
  }

  public void dump_stack_trace()
  {
      Log.d("FakeEtherConnection", Log.getStackTraceString(new Exception()));
  }

  public void log(String s)
  {
      if (debug_level < 1)
	  return;
      
      //XposedBridge.log("FakeEtherConnection: " + s);
      Log.d("FakeEtherConnection", s);
  }
    
  public void log_call(String s)
  {
      if (debug_level < 1)
	  return;
      
      //XposedBridge.log("FakeEtherConnection: " + s);
      Log.d("FakeEtherConnection", s);

      if (debug_level > 1)
	  dump_stack_trace();
  }
    
  public void doit_networkinfo(String called, MethodHookParam param) throws Exception
  {	 
//      XposedBridge.log("FakeEtherConnection:" +
//		       " master=" + master_switch +
//		       " " + lpparam.packageName + "=" + hack_enabled );
     
      if (!hack_enabled())
      {
	  log_call(called + ", hack is disabled.");
	  return;
      }
     
      // if we're already on ethernet don't interfere.
      if (param.getResult() != null)
      {
	  NetworkInfo network = (NetworkInfo) param.getResult();
	  if (network.getType() == ConnectivityManager.TYPE_ETHERNET &&
	      network.isConnected())
	  {
	      log_call(called + ", on ethernet already.");
	      return;
	  }
      }
	
      log_call(called + ", faking ethernet !");
      param.setResult(getFakeNetworkInfo());
 }
	
  public NetworkInfo	getFakeNetworkInfo() throws Exception
  {
      NetworkInfo info = createNetworkInfo(ConnectivityManager.TYPE_ETHERNET, true);
      return info;
  }

  public NetworkInfo createNetworkInfo(final int type, final boolean connected) throws Exception 
  {
      Constructor<NetworkInfo> ctor = NetworkInfo.class.getDeclaredConstructor(int.class);
      ctor.setAccessible(true);
      NetworkInfo networkInfo = ctor.newInstance(0);
      
      XposedHelpers.setIntField((Object)networkInfo, "mNetworkType", type);
      XposedHelpers.setObjectField((Object)networkInfo, "mTypeName", "ETHERNET");
      XposedHelpers.setObjectField((Object)networkInfo, "mState", NetworkInfo.State.CONNECTED);
      XposedHelpers.setObjectField((Object)networkInfo, "mDetailedState", NetworkInfo.DetailedState.CONNECTED);
      XposedHelpers.setBooleanField((Object)networkInfo, "mIsAvailable", true);
      return networkInfo;
  }

    
  @Override
  public void handleLoadPackage(final LoadPackageParam lpp) throws Throwable
  {
      lpparam = lpp;
      XposedBridge.log("FakeEtherConnection: Loaded app: " + lpparam.packageName);
	
      pref = new XSharedPreferences(Main.class.getPackage().getName(), "pref");
      
      XposedHelpers.findAndHookMethod((Class)Activity.class, "onResume", new XC_MethodHook() 
      {
	  @Override
	  protected void afterHookedMethod(MethodHookParam param) throws Throwable 
	  {  pref.reload();  }
      });	

      // *************************************************************************************
      // ConnectivityManager targets:
      //   getActiveNetworkInfo()
      //   getNetworkInfo()
      //   getAllNetworkInfo()		 
      
      // getActiveNetworkInfo()
      findAndHookMethod("android.net.ConnectivityManager", lpparam.classLoader, 
			"getActiveNetworkInfo", new XC_MethodHook() 
      {
	  @Override
	  protected void afterHookedMethod(MethodHookParam param) throws Throwable 
	  {  doit_networkinfo("getActiveNetworkInfo()", param);   }
      });
      
      // getAllNetworkInfo()
      findAndHookMethod("android.net.ConnectivityManager", lpparam.classLoader, 
			"getAllNetworkInfo", new XC_MethodHook() 
      {
	  @Override
	  protected void afterHookedMethod(MethodHookParam param) throws Throwable 
	      {
		  log_call("getAllNetworkInfo() called.");
	      }
      });
	 
      // getNetworkInfo(int)
      findAndHookMethod("android.net.ConnectivityManager", lpparam.classLoader, 
			"getNetworkInfo", int.class, new XC_MethodHook() 
      {
	  @Override
	  protected void afterHookedMethod(MethodHookParam param) throws Throwable 
	  {	 
	      int network_type = (Integer) param.args[0];
	      String called = "getNetworkInfo(" + network_type + ")";
	      
	      if (network_type == ConnectivityManager.TYPE_ETHERNET)
		  doit_networkinfo(called, param);
	      else
		  log_call(called + " called.");
	  }
      });	 


      
  }
    
}

