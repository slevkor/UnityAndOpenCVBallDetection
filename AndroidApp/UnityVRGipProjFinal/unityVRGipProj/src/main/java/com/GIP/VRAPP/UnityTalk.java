package com.GIP.VRAPP;

/**
 * Created by lev on 07/09/2016.
 */
public class UnityTalk {
    public static int getR(){
        return OurUnityPlayer.r;
    }
    public static int getG(){
        return OurUnityPlayer.g;
    }
    public static int getB(){
        return OurUnityPlayer.b;
    }
    public static void moveToScene2() { OurUnityPlayer.OurScene = OurUnityPlayer.SceneEnum.Second; }
    public static double getX() { return OurUnityPlayer.x ; }
    public static double getY() { return OurUnityPlayer.y ; }
    public static double getDis() { return OurUnityPlayer.Dis; }
    public static int getSpeed() { return 50; }
}
