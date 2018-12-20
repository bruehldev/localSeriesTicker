package sample;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class TVSeriesService extends Service<ObservableList<TVSeries>> {
    @Override
    protected Task createTask() {
        System.out.println("TVSeriesTask.class "+TVSeriesTask.class);
        System.out.println("TVSeriesTask.State.values() "+TVSeriesTask.State.values());
        return new TVSeriesTask();
    }
}