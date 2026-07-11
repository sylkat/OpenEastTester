package test;
import lcr.business.ETMeter;
import lcr.beans.Measurement;
import lcr.enums.*;

public class Test {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Uso:");
            System.out.println("java Test <Puerto>");
            System.out.println("Ejemplo:");
            System.out.println("java Test COM3");
            System.out.println("java Test /dev/ttyACM0");
            return;
        }

        try {

            ETMeter meter = new ETMeter(args[0]);

            System.out.println("Conectando...");
            meter.connect();

            System.out.println("Conexion OK");
            System.out.println();

            // -------------------------------------------------
            // FETCH
            // -------------------------------------------------

            Measurement m = meter.fetch();

            System.out.println("Measurement");
            System.out.println("Primary   : " + m.getPrimaryValue());
            System.out.println("Secondary : " + m.getSecondaryValue());
            System.out.println();

            // -------------------------------------------------
            // FREQUENCY
            // -------------------------------------------------

            System.out.println("Frequency : " + meter.getFrequency());

            meter.setFrequency(Frequency.HZ1000);

            System.out.println("Frequency : " + meter.getFrequency());
            System.out.println();

            // -------------------------------------------------
            // VOLTAGE
            // -------------------------------------------------

            System.out.println("Voltage : " + meter.getVoltage());

            meter.setVoltage(Voltage.MV600);

            System.out.println("Voltage : " + meter.getVoltage());
            System.out.println();

            // -------------------------------------------------
            // APERTURE
            // -------------------------------------------------

            System.out.println("Aperture : " + meter.getAperture());

            meter.setAperture(Aperture.FAST);

            System.out.println("Aperture : " + meter.getAperture());
            System.out.println();

            // -------------------------------------------------
            // PRIMARY
            // -------------------------------------------------

            System.out.println("Primary : " + meter.getPrimaryParameter());

            meter.setPrimaryParameter(PrimaryParameter.R);

            System.out.println("Primary : " + meter.getPrimaryParameter());
            System.out.println();

            // -------------------------------------------------
            // SECONDARY
            // -------------------------------------------------

            System.out.println("Secondary : " + meter.getSecondaryParameter());

            meter.setSecondaryParameter(SecondaryParameter.D);

            System.out.println("Secondary : " + meter.getSecondaryParameter());
            System.out.println();

            // -------------------------------------------------
            // SERIES/PARALLEL
            // -------------------------------------------------

            System.out.println("Mode : " + meter.getSeriesMode());

            meter.setSeriesMode(SeriesMode.SER);

            System.out.println("Mode : " + meter.getSeriesMode());
            System.out.println();

            // -------------------------------------------------
            // AUTO RANGE
            // -------------------------------------------------

            System.out.println("Auto Range : " + meter.isAutoRange());

            meter.setAutoRange(true);

            System.out.println("Auto Range : " + meter.isAutoRange());
            System.out.println();

            // -------------------------------------------------
            // RANGE
            // -------------------------------------------------

            meter.setAutoRange(false);

            System.out.println("Range : " + meter.getRange());

            meter.setRange(Range.R1000);

            System.out.println("Range : " + meter.getRange());
            System.out.println();

            // -------------------------------------------------
            // BIAS
            // -------------------------------------------------

            System.out.println("Bias : " + meter.getBiasVoltage());

            meter.setBiasVoltage(BiasVoltage.MV100);

            System.out.println("Bias : " + meter.getBiasVoltage());
            System.out.println();

            meter.disconnect();

            System.out.println("Disconnected.");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}