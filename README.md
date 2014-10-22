FakeEtherConnection
==================

This app is like FakeWifiConnection, but for ethernet:  
It makes android apps believe Ethernet is connected.

Handy in situations where there is a connection, but some app won't do its thing unless it's on ethernet.

No app is faked by default. Open FakeEtherConnection app to enable/disable hack (master switch) and select which apps to fake. Changes take effect immediately (background apps need a reboot).


Install
-------

- Install Xposed Framework.  
  Get installer from http://repo.xposed.info/  
  Open Xposed Installer->Framework->Install  
  Reboot

- Install [FakeEtherConnection.apk](https://raw.github.com/lemonsqueeze/FakeEtherConnection/master/bin/FakeEtherConnection.apk)  
  Open Xposed Installer->Modules, tick FakeEtherConnection  
  Reboot

- If you have FakeWifiConnection installed, make sure it's turned off.

- Open FakeEtherConnection app to change settings.

Debugging
---------

`logcat | grep FakeEtherConnection`

Also check Xposed logs:

`logcat | grep Xposed`

Credits
-------

- rovo89 for awesome Xposed Framework
- UI code by hamzahrmalik (Force Fast Scroll)

Released under [GNU GPL License](https://raw.github.com/lemonsqueeze/FakeEtherConnection/master/LICENSE).
