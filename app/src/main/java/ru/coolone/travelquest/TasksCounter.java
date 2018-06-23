package ru.coolone.travelquest;

import android.util.Log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Counter of started tasks
 *
 * @author coolone
 * @since 23.06.18
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class TasksCounter {
    private static final String TAG = TasksCounter.class.getSimpleName();

    private final Object tasksCountLock = new Object();

    @NonNull
    private String tasksName;

    @Getter
    @Setter
    @NonNull
    TaskListener taskListener;

    @Getter
    int tasksCount;

    String getTasksName() {
        return tasksName != null && !tasksName.isEmpty()
                ? tasksName
                : toString();
    }

    public void onStartTasks(int count) {
        synchronized (tasksCountLock) {
            tasksCount += count;
        }

        Log.d(TAG, "Started " + count + " " + getTasksName() + " tasks");
    }

    public void onStartTask() {
        synchronized (tasksCountLock) {
            tasksCount++;
        }

        Log.d(TAG, "Started " + getTasksName() + " task");
    }

    public void onEndTask() {
        synchronized (tasksCountLock) {
            tasksCount--;
        }

        Log.d(TAG, "Ended " + getTasksName() + " task");

        if (tasksCount == 0) {
            Log.d(TAG, "All " + getTasksName() + " tasks ended");

            if (taskListener != null) {
                Log.d(TAG, "Listener of " + getTasksName() + " tasks not null");

                taskListener.onTasksEnded();
            }
        }
    }

    public interface TaskListener {
        void onTasksEnded();
    }
}
