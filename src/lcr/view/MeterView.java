package lcr.view;

import lcr.beans.ConfigDTO;

public interface MeterView {
    void updateConnectionState(boolean isConnected, String model, String firmware, String portName);
    void updateUIFromConfig(ConfigDTO config);
}