/*
 * Copyright (C) 2020 Alberto Irurueta Carro (alberto@irurueta.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.irurueta.navigation.inertial.calibration.noise;

import com.irurueta.navigation.inertial.calibration.IMUTimeIntervalEstimator;
import com.irurueta.units.MagneticFluxDensity;
import com.irurueta.units.MagneticFluxDensityConverter;
import com.irurueta.units.MagneticFluxDensityUnit;

/**
 * Estimates accumulated magnetometer noise variances and PSD's (Power Spectral Densities)
 * along with their average values.
 * Norms of magnetic flux density triads can be used to estimate noise levels.
 * This estimator must be used when the body where the magnetometer is attached
 * remains static on the same position and orientation with zero velocity while
 * capturing data.
 * To compute PSD's, this estimator assumes that measurement samples are obtained
 * at a constant provided rate equal to {@link #getTimeInterval()} seconds.
 * If not available, gyroscope sampling rate average can be estimated using
 * {@link IMUTimeIntervalEstimator}.
 * This estimator does NOT require the knowledge of current location and body
 * orientation.
 * This implementation of noise estimator will use the following units:
 * - Teslas (T) for magnetic flux density, average or standard deviation values.
 * - squared Teslas (T^2) for magnetic flux density variances.
 * - (T^2 * s) for magnetometer PSD (Power Spectral Density).
 * - (T * s^0.5) for magnetometer root PSD (Power Spectral Density).
 */
public class AccumulatedMagneticFluxDensityMeasurementNoiseEstimator extends
        AccumulatedMeasurementNoiseEstimator<MagneticFluxDensityUnit, MagneticFluxDensity,
                AccumulatedMagneticFluxDensityMeasurementNoiseEstimator,
                AccumulatedMagneticFluxDensityMeasurementNoiseEstimatorListener> {

    /**
     * Constructor.
     */
    public AccumulatedMagneticFluxDensityMeasurementNoiseEstimator() {
        super();
    }

    /**
     * Constructor.
     *
     * @param listener listener to handle events raised by this estimator.
     */
    public AccumulatedMagneticFluxDensityMeasurementNoiseEstimator(
            final AccumulatedMagneticFluxDensityMeasurementNoiseEstimatorListener listener) {
        super(listener);
    }

    /**
     * Gets default unit for a measurement.
     *
     * @return default unit for a measurement.
     */
    @Override
    protected MagneticFluxDensityUnit getDefaultUnit() {
        return MagneticFluxDensityUnit.TESLA;
    }

    /**
     * Creates a measurement with provided value and unit.
     *
     * @param value value to be set.
     * @param unit  unit to be set.
     * @return created measurement.
     */
    @Override
    protected MagneticFluxDensity createMeasurement(
            final double value, final MagneticFluxDensityUnit unit) {
        return new MagneticFluxDensity(value, unit);
    }

    /**
     * Converts provided measurement into default unit.
     *
     * @param value measurement to be converted.
     * @return converted value.
     */
    @Override
    protected double convertToDefaultUnit(final MagneticFluxDensity value) {
        return MagneticFluxDensityConverter.convert(value.getValue().doubleValue(),
                value.getUnit(), getDefaultUnit());
    }
}
