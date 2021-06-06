package me.rookie.launcher;

import android.content.Context;
import android.util.Log;

import me.rookie.annotation.ILauncherLoader;
import me.rookie.annotation.LauncherMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LauncherManager {

    private static List<LauncherMeta>              sLauncherMetaList      = new ArrayList<>();
    private static Map<String, List<LauncherMeta>> classifiedLauncherMeta = new HashMap<>();

    public static void init(Context context) {
        //auto register by asm
//        loadLauncher("me.rookie.wfcx.LauncherLoader_module_wfcx");
    }

    private static void loadLauncher(String className) {
        try {
            Class<?> cls = Class.forName(className);
            Class<?>[] interfaces = cls.getInterfaces();
            if (Arrays.asList(interfaces).contains(ILauncherLoader.class)) {
                Object o = cls.newInstance();
                loadLauncher((ILauncherLoader) o);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static void classify() {

    }

    public static void loadLauncher(ILauncherLoader launcherLoader) {
        launcherLoader.load(sLauncherMetaList);
        Log.e("LauncherManager", sLauncherMetaList.toString());
    }

    public static void loadLauncher(LauncherMeta launcherMeta) {
        sLauncherMetaList.add(launcherMeta);
    }

    public static List<LauncherMeta> getLauncherMetaList() {
        return sLauncherMetaList;
    }
}
