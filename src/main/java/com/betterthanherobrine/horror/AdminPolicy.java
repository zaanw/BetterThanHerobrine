package com.betterthanherobrine.horror;
/** Nickname is an additional restriction, never an authorization grant. */
public final class AdminPolicy {
 private AdminPolicy(){}
 public static boolean allowed(String name,boolean serverPermission){return serverPermission&&"zaanw".equalsIgnoreCase(name);}
}
