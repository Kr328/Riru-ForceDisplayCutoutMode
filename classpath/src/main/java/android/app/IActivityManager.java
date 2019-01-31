package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import java.util.List;

public interface IActivityManager extends IInterface {
    public static class Stub extends Binder implements IActivityManager {
        public static IActivityManager asInterface(IBinder binder) {
            throw new IllegalArgumentException("Unsupported");
        }

        @Override
        public IBinder asBinder() {
            throw new IllegalArgumentException("Unsupported");
        }

        @Override
        public void registerTaskStackListener(ITaskStackListener listener) {
            throw new IllegalArgumentException("Stub!");
        }

        @Override
        public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) {
            throw new IllegalArgumentException("Stub!");
        }
    }

    public void registerTaskStackListener(ITaskStackListener listener);
    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum);
}
