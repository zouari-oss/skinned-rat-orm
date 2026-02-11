package org.zouarioss.skinnedratorm.example.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class SystemInfo {

  public static String getPrivateIpAddress() {
    try {
      InetAddress localhost = InetAddress.getLocalHost();
      return localhost.getHostAddress();
    } catch (Exception e) {
      return "127.0.0.1";
    }
  }

  public static String getMacAddress() {
    try {
      InetAddress localhost = InetAddress.getLocalHost();
      NetworkInterface network = NetworkInterface.getByInetAddress(localhost);
      
      if (network == null) {
        return "unknown";
      }

      byte[] mac = network.getHardwareAddress();
      
      if (mac == null) {
        return "unknown";
      }

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < mac.length; i++) {
        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
      }
      
      return sb.toString();
    } catch (Exception e) {
      return "unknown";
    }
  }
}
