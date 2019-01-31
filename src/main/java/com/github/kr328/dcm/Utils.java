package com.github.kr328.dcm;

import android.view.WindowManager;

import java.io.*;

public class Utils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static String readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer);
    }

    static void writeFile(File file ,String data) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(data.getBytes());

        outputStream.close();
    }

    static String cutoutModeIntToString(int mode) {
        switch (mode) {
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT:
                return "Default";
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER:
                return "Never";
            case WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES:
                return "ShortEdges";
        }
        return "Unknown";
    }

    static int stringToCutoutModeInt(String str) {
        switch (str) {
            case "Default":
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            case "Never":
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            case "ShortEdges":
                return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
    }
}
