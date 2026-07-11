package lcr.observer;

import lcr.beans.ConfigDTO;
import lcr.beans.MeasurementDTO;

/**
 * Observer interface defining the lifecycle callbacks for dispatching instrument
 * telemetry updates, hardware configuration shifts, and connection tear-down events
 * to listening UI presentation components.
 * * @author sylkat
 */
public interface MeasurementObserver {

    /**
     * Invoked immediately when a new async streaming measurement payload data pack
     * is parsed and processed from the hardware serial stream.
     * * @param dto the tracking data transfer object containing live component metrics
     */
    void onMeasurementReceived(MeasurementDTO dto);

    /**
     * Invoked when the instrument registers finish synchronization routines, forcing
     * the display layout controls to update and match the current hardware state bounds.
     * * @param config the structural configuration snapshot payload containing current active parameters
     */
    void updateUIFromConfig(ConfigDTO config);

    /**
     * Invoked gracefully when the underlying hardware link drops or is explicitly
     * closed, triggering the interface presentation layer to lock down or reset.
     */
    void onDisconnected();
}