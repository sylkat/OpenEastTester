package lcr.observer;

import lcr.beans.ConfigDTO;
import lcr.beans.MeasurementDTO;

public interface MeasurementObserver {
    void onMeasurementReceived(MeasurementDTO dto);
    void updateUIFromConfig(ConfigDTO config);
    void onDisconnected();
}