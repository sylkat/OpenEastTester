package et431.observer;

import et431.beans.ConfigDTO;
import et431.beans.MeasurementDTO;

public interface MeasurementObserver {
    void onMeasurementReceived(MeasurementDTO dto);
    void updateUIFromConfig(ConfigDTO config);
    void onDisconnected();
}