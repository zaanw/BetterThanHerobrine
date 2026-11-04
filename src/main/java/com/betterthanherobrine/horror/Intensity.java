package com.betterthanherobrine.horror;
public enum Intensity {
 NORMAL, UNEASY, WATCHED, DISTURBED, NIGHTMARE;
 public static Intensity of(double tension){return values()[Math.clamp((int)(tension/20),0,4)];}
}
