package com.github.kr328.dcm;

import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

class ConfigStorage {
    ConfigStorage() {
        loadConfigs();

        //noinspection ResultOfMethodCallIgnored
        new File(Global.CONFIG_PATH).mkdirs();
    }

    void put(String pkg, int mode) {
        if ( mode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT )
            configs.remove(pkg);
        else
            configs.put(pkg ,mode);

        try {
            if ( mode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT )
                //noinspection ResultOfMethodCallIgnored
                new File(Global.CONFIG_PATH + "/" + pkg).delete();
            else
                Utils.writeFile(new File(Global.CONFIG_PATH + "/" + pkg)
                        ,Utils.cutoutModeIntToString(mode));
        } catch (IOException e) {
            Log.w(Global.TAG ,"Save config " + pkg + " failure." ,e);
        }
    }

    int get(String pkg, int defaultMode) {
        return configs.getOrDefault(pkg ,defaultMode);
    }

    private void loadConfigs() {
        File configDirectory = new File(Global.CONFIG_PATH);

        File[] configFiles = configDirectory.listFiles();
        if ( configFiles == null )
            return;

        for ( File f : configFiles ) {
            try {
                String data = Utils.readFile(f);
                int mode = Utils.stringToCutoutModeInt(data);
                if ( mode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT )
                    configs.put(f.getName() ,mode);
            } catch (IOException ignored) {}
        }
    }

    private Hashtable<String ,Integer> configs = new Hashtable<>();
}
