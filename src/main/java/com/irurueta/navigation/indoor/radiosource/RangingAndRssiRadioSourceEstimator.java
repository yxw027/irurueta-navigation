/*
 * Copyright (C) 2018 Alberto Irurueta Carro (alberto@irurueta.com)
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
package com.irurueta.navigation.indoor.radiosource;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.Point;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.navigation.indoor.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimates position, transmitted power and path loss exponent of a
 * radio source (e.g. WiFi access point or bluetooth beacon) assuming
 * that the ranging data is available to obtain position with greater
 * accuracy and that the radio source emits isotropically following the
 * expression below:
 * Pr = Pt*Gt*Gr*lambda^2 / (4*pi*d)^2,
 * where Pr is the received power (expressed in mW),
 * Gt is the Gain of the transmission antena
 * Gr is the Gain of the receiver antena
 * d is the distance between emitter and receiver
 * and lambda is the wavelength and is equal to: lambda = c / f,
 * where c is the speed of light
 * and f is the carrier frequency of the radio signal.
 * Because usually information about the antena of the radio source cannot be
 * retrieved (because many measurements are made on unkown devices where
 * physical access is not possible), this implementation will estimate the
 * equivalent transmitted power as: Pte = Pt * Gt * Gr.
 * If Readings contain RSSI standard deviations, those values will be used,
 * otherwise it will be asumed an RSSI standard deviation of 1 dB.
 *
 * Although RssiRadioSourceEstimator can estimate the same parameters of a radio
 * source, when ranging measures are available along with RSSI measurements,
 * implementations of this class should be preferred instead as they can provide
 * greater accuracy.
 *
 * Notice that implementations of this class assume that when retrieving the
 * covariance of all parameters the cross correlation among position
 * terms and pathloss or transmitted power is zero.
 *
 * @param <S> a {@link RadioSource} type.
 * @param <P> a {@link Point} type.
 */
@SuppressWarnings({"WeakerAccess", "Duplicates"})
public abstract class RangingAndRssiRadioSourceEstimator<S extends RadioSource, P extends Point<P>>
        extends RadioSourceEstimator<P, RangingAndRssiReadingLocated<S, P>,
        RangingAndRssiRadioSourceEstimatorListener<S, P>> {

    /**
     * Speed of light expressed in meters per second (m/s).
     */
    public static final double SPEED_OF_LIGHT = 299792458.0;

    /**
     * Default exponent typically used on free space for path loss propagation in
     * terms of distance. This value is used for free space environments.
     */
    public static final double DEFAULT_PATH_LOSS_EXPONENT = 2.0;

    /**
     * Indicates whether radio source transmitted power estimation is enabled or not by
     * default. Typically this data is required for WiFi Access points, but it is already
     * provided for Beacons (and hence its estimation is not needed).
     */
    public static final boolean DEFAULT_TRANSMITTED_POWER_ESTIMATION_ENABLED = true;

    /**
     * Indicates whether path loss estimation is enabled or not by default.
     */
    public static final boolean DEFAULT_PATHLOSS_ESTIMATION_ENABLED = false;

    /**
     * Indicates that by default position covariances of readings must be taken into account to increase
     * the amount of standard deviation of each ranging measure by the amount of position standard deviation
     * assuming that both measures are statistically independent.
     */
    public static final boolean DEFAULT_USE_READING_POSITION_COVARIANCES = true;

    /**
     * RSSI radio source estimator.
     */
    protected RssiRadioSourceEstimator<S, P> mRssiInnerEstimator;

    /**
     * Ranging radio source estimator.
     */
    protected RangingRadioSourceEstimator<S, P> mRangingInnerEstimator;

    /**
     * Indicates whether transmitted power estimation is enabled or not.
     */
    protected boolean mTransmittedPowerEstimationEnabled = DEFAULT_TRANSMITTED_POWER_ESTIMATION_ENABLED;

    /**
     * Indicates whether path loss estimation is enabled or not.
     */
    protected boolean mPathLossEstimationEnabled = DEFAULT_PATHLOSS_ESTIMATION_ENABLED;

    /**
     * Estimated transmitted power expressed in dBm's.
     */
    private double mEstimatedTransmittedPowerdBm;

    /**
     * Estimated exponent typically used on free space for path loss propagation in
     * terms of distance.
     * On different environments path loss exponent might have different values:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     * If path loss exponent estimation is not enabled, this value will always be equal to
     * {@link #DEFAULT_PATH_LOSS_EXPONENT}
     */
    private double mEstimatedPathLossExponent = DEFAULT_PATH_LOSS_EXPONENT;

    /**
     * Variance of estimated transmitted power.
     * This value will only be available when transmitted power
     * estimation is enabled.
     */
    private Double mEstimatedTransmittedPowerVariance;

    /**
     * Variance of estimated path loss exponent.
     * This value will only be available when pathloss
     * exponent estimation is enabled.
     */
    private Double mEstimatedPathLossExponentVariance;

    /**
     * Initial transmitted power to start the estimation of radio source
     * transmitted power.
     * If not defined, average value of received power readings will be used.
     */
    private Double mInitialTransmittedPowerdBm;

    /**
     * Initial position to start the estimation of radio source position.
     * If not defined, centroid of provided readings will be used.
     */
    private P mInitialPosition;

    /**
     * Initial exponent typically used on free space for path loss propagation in
     * terms of distance.
     * On different environments path loss exponent might have different values:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     *
     * If path loss exponent estimation is enabled, estimation will start at this
     * value and will converge to the most appropriate value.
     * If path loss exponent estimation is disabled, this value will be assumed
     * to be exact and the estimated path loss exponent will be equal to this
     * value.
     */
    private double mInitialPathLossExponent = DEFAULT_PATH_LOSS_EXPONENT;

    /**
     * Indicates whether position covariances of readings must be taken into account to increase
     * the amount of standard deviation of each ranging measure by the amount of position standard deviation
     * assuming that both measures are statistically independent.
     */
    private boolean mUseReadingPositionCovariances = DEFAULT_USE_READING_POSITION_COVARIANCES;

    /**
     * Indicates whether an homogeneous linear solver is used to estimate an initial
     * position for the internal ranging radio source estimator.
     */
    private boolean mUseHomogeneousRangingLinearSolver =
            RangingRadioSourceEstimator.DEFAULT_USE_HOMOGENEOUS_LINEAR_SOLVER;

    /**
     * Constructor.
     */
    public RangingAndRssiRadioSourceEstimator() {
        super();
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same
     *                 radio sources.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings) {
        super(readings);
    }

    /**
     * Constructor.
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RangingAndRssiRadioSourceEstimator(
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(listener);
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(readings, listener);
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition) {
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition) {
        super(readings);
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(listener);
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(readings, listener);
        mInitialPosition = initialPosition;
    }

    /**
     * Constructor.
     * @param initialTransmittedPowerDbm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     */
    public RangingAndRssiRadioSourceEstimator(
            Double initialTransmittedPowerDbm) {
        mInitialTransmittedPowerdBm = initialTransmittedPowerDbm;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            Double initialTransmittedPowerdBm) {
        super(readings);
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RangingAndRssiRadioSourceEstimator(Double initialTransmittedPowerdBm,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(listener);
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            Double initialTransmittedPowerdBm,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(readings, listener);
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm) {
        super(readings);
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition,
            Double initialTransmittedPowerdBm) {
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition,
            Double initialTransmittedPowerdBm,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(listener);
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        super(readings, listener);
        mInitialPosition = initialPosition;
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param initialPathLossExponent initial path loss exponent. A typical value is 2.0.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        this(readings, initialPosition, initialTransmittedPowerdBm);
        mInitialPathLossExponent = initialPathLossExponent;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param initialPathLossExponent initial path loss exponent. A typical value is 2.0.
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent) {
        this(initialPosition, initialTransmittedPowerdBm);
        mInitialPathLossExponent = initialPathLossExponent;
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param initialPathLossExponent initial path loss exponent. A typical value is 2.0.
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RangingAndRssiRadioSourceEstimator(P initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        this(initialPosition, initialTransmittedPowerdBm, listener);
        mInitialPathLossExponent = initialPathLossExponent;
    }

    /**
     * Constructor.
     * Sets radio signal readings belonging to the same radio source.
     * @param readings radio signal readings belonging to the same radio source.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @param initialPathLossExponent initial path loss exponent. A typical value is 2.0.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RangingAndRssiRadioSourceEstimator(
            List<? extends RangingAndRssiReadingLocated<S, P>> readings,
            P initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RangingAndRssiRadioSourceEstimatorListener<S, P> listener) {
        this(readings, initialPosition, initialTransmittedPowerdBm, listener);
        mInitialPathLossExponent = initialPathLossExponent;
    }

    /**
     * Gets initial transmitted power to start the estimation of radio source
     * transmitted power (expressed in dBm's).
     * If not defined, average value of received power readings will be used.
     *
     * If transmitted power estimation is enabled, estimation will start at this
     * value and will converte the most appropriate value.
     * If transmitted power estimation is disabled, this value will be assumed to be
     * exact and the estimated transmitted power will be equal to this value
     * (converted to dBm's).
     * @return initial transmitted power to start the estimation of radio source
     * transmitted power.
     */
    public Double getInitialTransmittedPowerdBm() {
        return mInitialTransmittedPowerdBm;
    }

    /**
     * Sets initial transmitted power to start the estimation of radio source
     * transmitted power (expressed in dBm's).
     * If not defined, average value of received power readings will be used.
     *
     * If transmitted power estimation is enabled, estimation will start at this
     * value and will converte the most appropriate value.
     * If transmitted power estimation is disabled, this value will be assumed to be
     * exact and the estimated transmitted power will be equal to this value
     * (converted to dBm's).
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted
     *                                   power.
     * @throws LockedException if estimator is locked.
     */
    public void setInitialTransmittedPowerdBm(Double initialTransmittedPowerdBm)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mInitialTransmittedPowerdBm = initialTransmittedPowerdBm;
    }

    /**
     * Gets initial transmitted power to start the estimation of radio source
     * transmitted power (expressed in mW).
     * If not defined, average value of received power readings will be used.
     *
     * If transmitted power estimation is enabled, estimation will start at this
     * value and will converte the most appropriate value.
     * If transmitted power estimation is disabled, this value will be assumed to be
     * exact and the estimated transmitted power will be equal to this value
     * (converted to dBm's).
     * @return initial transmitted power to start the estimation of radio source
     * transmitted power.
     */
    public Double getInitialTransmittedPower() {
        return mInitialTransmittedPowerdBm != null ?
                Utils.dBmToPower(mInitialTransmittedPowerdBm) : null;
    }

    /**
     * Sets initial transmitted power to start the estimation of radio source
     * transmitted power (expressed in mW).
     * If not defined, average value of received power readings will be used.
     *
     * If transmitted power estimation is enabled, estimation will start at this
     * value and will converte the most appropriate value.
     * If transmitted power estimation is disabled, this value will be assumed to be
     * exact and the estimated transmitted power will be equal to this value
     * (converted to dBm's).
     * @param initialTransmittedPower initial transmitted power to start the
     *                                estimation of radio source transmitted power.
     * @throws LockedException if estimator is locked.
     * @throws IllegalArgumentException if provided value is negative.
     */
    public void setInitialTransmittedPower(Double initialTransmittedPower)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (initialTransmittedPower != null) {
            if (initialTransmittedPower < 0.0) {
                throw new IllegalArgumentException();
            }
            mInitialTransmittedPowerdBm = Utils.powerTodBm(
                    initialTransmittedPower);
        } else {
            mInitialTransmittedPowerdBm = null;
        }
    }

    /**
     * Indicates whether transmitted power estimation is enabled or not.
     * @return true if transmitted power estimation is enabled, false otherwise.
     */
    public boolean isTransmittedPowerEstimationEnabled() {
        return mTransmittedPowerEstimationEnabled;
    }

    /**
     * Specifies whether transmitted power estimation is enabled or not.
     * @param transmittedPowerEstimationEnabled true if transmitted power estimation is enabled,
     *                                          false otherwise.
     * @throws LockedException if estimator is locked.
     */
    public void setTransmittedPowerEstimationEnabled(boolean transmittedPowerEstimationEnabled)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mTransmittedPowerEstimationEnabled = transmittedPowerEstimationEnabled;
    }

    /**
     * Gets initial position to start the estimation of radio source position.
     * If not defined, centroid of provided readings will be used.
     *
     * If position estimation is enabled, estimation will start at this value
     * and will converge to the most appropriate value.
     * If position estimation is disabled, this value will be assumed to
     * be exact and the estimated position will be equal to this value.
     * @return initial position to start the estimation of radio source position.
     */
    public P getInitialPosition() {
        return mInitialPosition;
    }

    /**
     * Sets initial position to start the estimation of radio source position.
     * If not defined, centroid of provided fingerprints will be used.
     *
     * If position estimation is enabled, estimation will start at this value
     * and will converge to the most appropriate value.
     * If position estimation is disabled, this value will be assumed to
     * be exact and the estimated position will be equal to this value.
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @throws LockedException if estimator is locked.
     */
    public void setInitialPosition(P initialPosition) throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mInitialPosition = initialPosition;
    }

    /**
     * Gets initial exponent typically used on free space for path loss propagation
     * in terms of distance.
     * On different environments path loss exponent might have different value:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     *
     * If path loss exponent estimation is enabled, estimation will start at this
     * value and will converge to the most appropriate value.
     * If path loss exponent estimation is disabled, this value will be assumed
     * to be exact and the estimated path loss exponent will be equal to this
     * value.
     * @return initial path loss exponent.
     */
    public double getInitialPathLossExponent() {
        return mInitialPathLossExponent;
    }

    /**
     * Sets initial exponent typically used on free space for path loss propagation
     * in terms of distance.
     * On different environments path loss exponent might have different value:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     *
     * If path loss exponent estimation is enabled, estimation will start at this
     * value and will converge to the most appropriate value.
     * If path loss exponent estimation is disabled, this value will be assumed
     * to be exact and the estimated path loss exponent will be equal to this
     * value.
     * @param initialPathLossExponent initial path loss exponent.
     * @throws LockedException if estimator is locked.
     */
    public void setInitialPathLossExponent(double initialPathLossExponent)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mInitialPathLossExponent = initialPathLossExponent;
    }

    /**
     * Indicates whether path loss estimation is enabled or not.
     * @return true if path loss estimation is enabled, false otherwise.
     */
    public boolean isPathLossEstimationEnabled() {
        return mPathLossEstimationEnabled;
    }

    /**
     * Specifies whether path loss estimation is enabled or not.
     * @param pathLossEstimationEnabled true if path loss estimation is enabled,
     *                                  false otherwise.
     * @throws LockedException if estimator is locked.
     */
    public void setPathLossEstimationEnabled(boolean pathLossEstimationEnabled)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mPathLossEstimationEnabled = pathLossEstimationEnabled;
    }

    /**
     * Indicates whether position covariances of readings must be taken into account to increase
     * the amount of standard deviation of each ranging measure by the amount of position standard
     * deviation assuming that both measures are statistically independent.
     * @return true to take into account reading position covariances, false otherwise.
     */
    public boolean getUseReadingPositionCovariance() {
        return mUseReadingPositionCovariances;
    }

    /**
     * Specifies whether position covariances of readings must be taken into account to increase
     * the amount of standard deviation of each ranging measure by the amount of position standard
     * deviation assuming that both measures are statistically independent.
     * @param useReadingPositionCovariances true to take into account reading position covariances, false
     *                                      otherwise.
     * @throws LockedException if estimator is locked.
     */
    public void setUseReadingPositionCovariances(boolean useReadingPositionCovariances)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mUseReadingPositionCovariances = useReadingPositionCovariances;
    }

    /**
     * Indicates whether an homogeneous linear solver is used to estimate an initial
     * position for the internal ranging radio source estimator.
     * @return true if homogeneous linear solver is used, false if an inhomogeneous linear
     * one is used instead.
     */
    public boolean isHomogeneousRangingLinearSolverUsed() {
        return mUseHomogeneousRangingLinearSolver;
    }

    /**
     * Specifies whether an homogeneous linear solver is used to estimate an initial
     * position for the internal ranging radio source estimator.
     * @param useHomogeneousLinearSolver true if homogeneous linear solver is used, false
     *                                   if an inhomogeneous linear one is used instead.
     * @throws LockedException if estimator is locked.
     */
    public void setHomogeneousRangingLinearSolverUsed(boolean useHomogeneousLinearSolver)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }

        mUseHomogeneousRangingLinearSolver = useHomogeneousLinearSolver;
    }

    /**
     * Gets minimum required number of readings to estimate
     * power, position and pathloss exponent.
     * This value depends on the number of parameters to
     * be estimated, but for position only, this is 3
     * readings.
     * @return minimum required number of readings.
     * @throws IllegalStateException if inner RSSI estimator is busy.
     */
    @Override
    public int getMinReadings() {
        createInnerEstimatorsIfNeeded();

        int result = getNumberOfDimensions();
        if (mRssiInnerEstimator != null &&
                (mTransmittedPowerEstimationEnabled || mPathLossEstimationEnabled)) {
            try {
                mRssiInnerEstimator.setPositionEstimationEnabled(false);
                mRssiInnerEstimator.setTransmittedPowerEstimationEnabled(
                        mTransmittedPowerEstimationEnabled);
                mRssiInnerEstimator.setPathLossEstimationEnabled(
                        mPathLossEstimationEnabled);
            } catch (LockedException e) {
                throw new IllegalStateException(e);
            }

            result += mRssiInnerEstimator.getMinReadings();
        } else {
            result++;
        }

        return result;
    }

    /**
     * Gets estimated radio sourceposition.
     * @return estimated radio source position.
     */
    public P getEstimatedPosition() {
        return mRangingInnerEstimator.getEstimatedPosition();
    }

    /**
     * Indicates whether this instance is ready to start the estimation.
     * @return true if this instance is ready, false otherwise.
     */
    @Override
    public boolean isReady() {
        //if transmitted power estimation is disabled, an initial transmitted power must be provided
        return !(!mTransmittedPowerEstimationEnabled && mInitialTransmittedPowerdBm == null) &&
                //readings must also be valid
                areValidReadings(mReadings);
    }

    /**
     * Estimate position, transmitted power and path loss exponent.
     * @throws RadioSourceEstimationException if estimation fails.
     * @throws NotReadyException if estimator is not ready.
     * @throws LockedException if estimator is locked.
     */
    @Override
    public void estimate() throws RadioSourceEstimationException, NotReadyException,
            LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (!isReady()) {
            throw new NotReadyException();
        }

        try {
            mLocked = true;

            if (mListener != null) {
                mListener.onEstimateStart(this);
            }

            createInnerEstimatorsIfNeeded();

            List<RangingReadingLocated<S, P>> rangingReadings = new ArrayList<>();
            List<RssiReadingLocated<S, P>> rssiReadings = new ArrayList<>();
            for (RangingAndRssiReadingLocated<S, P> reading : mReadings) {
                rangingReadings.add(createRangingReading(reading));
                rssiReadings.add(createRssiReading(reading));
            }

            //estimate position using ranging data
            mRangingInnerEstimator.setUseReadingPositionCovariances(
                    mUseReadingPositionCovariances);
            mRangingInnerEstimator.setHomogeneousLinearSolverUsed(
                    mUseHomogeneousRangingLinearSolver);
            mRangingInnerEstimator.setReadings(rangingReadings);
            mRangingInnerEstimator.setInitialPosition(mInitialPosition);

            mRangingInnerEstimator.estimate();

            mEstimatedPositionCoordinates =
                    mRangingInnerEstimator.getEstimatedPositionCoordinates();
            mEstimatedPositionCovariance =
                    mRangingInnerEstimator.getEstimatedPositionCovariance();
            P estimatedPosition = mRangingInnerEstimator.getEstimatedPosition();

            //estimate transmitted power and/or pathloss if enabled
            if (mTransmittedPowerEstimationEnabled || mPathLossEstimationEnabled) {
                mRssiInnerEstimator.setPositionEstimationEnabled(false);
                mRssiInnerEstimator.setInitialPosition(estimatedPosition);

                mRssiInnerEstimator.setTransmittedPowerEstimationEnabled(
                        mTransmittedPowerEstimationEnabled);
                mRssiInnerEstimator.setInitialTransmittedPowerdBm(
                        mInitialTransmittedPowerdBm);

                mRssiInnerEstimator.setPathLossEstimationEnabled(
                        mPathLossEstimationEnabled);
                mRssiInnerEstimator.setInitialPathLossExponent(
                        mInitialPathLossExponent);

                mRssiInnerEstimator.setReadings(rssiReadings);

                mRssiInnerEstimator.estimate();

                if (mTransmittedPowerEstimationEnabled) {
                    //transmitted power estimation enabled
                    mEstimatedTransmittedPowerdBm =
                            mRssiInnerEstimator.getEstimatedTransmittedPowerdBm();
                    mEstimatedTransmittedPowerVariance =
                            mRssiInnerEstimator.getEstimatedTransmittedPowerVariance();
                } else {
                    //transmitted power estimation disabled
                    if (mInitialTransmittedPowerdBm != null) {
                        mEstimatedTransmittedPowerdBm = mInitialTransmittedPowerdBm;
                    }
                    mEstimatedTransmittedPowerVariance = null;
                }

                if (mPathLossEstimationEnabled) {
                    //pathloss exponent estimation enabled
                    mEstimatedPathLossExponent =
                            mRssiInnerEstimator.getEstimatedPathLossExponent();
                    mEstimatedPathLossExponentVariance =
                            mRssiInnerEstimator.getEstimatedPathLossExponentVariance();
                } else {
                    //pathloss exponent estimation disabled
                    mEstimatedPathLossExponent = mInitialPathLossExponent;
                    mEstimatedPathLossExponentVariance = null;
                }

                //build covariance matrix
                Matrix rssiCov = mRssiInnerEstimator.getEstimatedCovariance();
                if (mEstimatedPositionCovariance != null && rssiCov != null) {
                    int dims = getNumberOfDimensions();
                    int n = dims;
                    if (mTransmittedPowerEstimationEnabled) {
                        n++;
                    }
                    if (mPathLossEstimationEnabled) {
                        n++;
                    }

                    int dimsMinus1 = dims - 1;
                    int nMinus1 = n - 1;
                    mEstimatedCovariance = new Matrix(n, n);
                    mEstimatedCovariance.setSubmatrix(0, 0,
                            dimsMinus1, dimsMinus1,
                            mEstimatedPositionCovariance);
                    mEstimatedCovariance.setSubmatrix(dims, dims,
                            nMinus1, nMinus1, rssiCov);
                } else {
                    mEstimatedCovariance = null;
                }

            } else {
                mEstimatedCovariance = mEstimatedPositionCovariance;
                if (mInitialTransmittedPowerdBm != null) {
                    mEstimatedTransmittedPowerdBm = mInitialTransmittedPowerdBm;
                }
                mEstimatedTransmittedPowerVariance = null;

                mEstimatedPathLossExponent = mInitialPathLossExponent;
                mEstimatedPathLossExponentVariance = null;
            }

            if (mListener != null) {
                mListener.onEstimateEnd(this);
            }

        } catch (AlgebraException e) {
            throw new RadioSourceEstimationException(e);
        } finally {
            mLocked = false;
        }
    }

    /**
     * Gets estimated transmitted power expressed in milli watts (mW).
     * @return estimated transmitted power expressed in milli watts.
     */
    public double getEstimatedTransmittedPower() {
        return Utils.dBmToPower(mEstimatedTransmittedPowerdBm);
    }

    /**
     * Gets estimated transmitted power expressed in dBm's.
     * @return estimated transmitted power expressed in dBm's.
     */
    public double getEstimatedTransmittedPowerdBm() {
        return mEstimatedTransmittedPowerdBm;
    }

    /**
     * Gets estimated exponent typically used on free space for path loss propagation in
     * terms of distance.
     * On different environments path loss exponent might have different values:
     * - Free space: 2.0
     * - Urban Area: 2.7 to 3.5
     * - Suburban Area: 3 to 5
     * - Indoor (line-of-sight): 1.6 to 1.8
     * If path loss exponent estimation is not enabled, this value will always be equal to
     * {@link #DEFAULT_PATH_LOSS_EXPONENT}
     * @return estimated path loss exponent.
     */
    public double getEstimatedPathLossExponent() {
        return mEstimatedPathLossExponent;
    }

    /**
     * Gets estimated transmitted power variance.
     * This value will only be available when transmitted power
     * estimation is enabled.
     * @return estimated transmitted power variance or null.
     */
    public Double getEstimatedTransmittedPowerVariance() {
        return mEstimatedTransmittedPowerVariance;
    }

    /**
     * Gets estimated path loss exponent variance.
     * This value will only be available when pathloss
     * exponent estimation is enabled.
     * @return estimated path loss exponent variance or null.
     */
    public Double getEstimatedPathLossExponentVariance() {
        return mEstimatedPathLossExponentVariance;
    }

    /**
     * Creates inner estimators if needed.
     */
    protected abstract void createInnerEstimatorsIfNeeded();

    /**
     * Creates a ranging reading from a ranging and RSSI reading.
     * @param reading input reading to convert from.
     * @return a ranging reading containing only the ranging data of input reading.
     */
    private RangingReadingLocated<S, P> createRangingReading(RangingAndRssiReadingLocated<S, P> reading) {
        return new RangingReadingLocated<>(reading.getSource(),
                reading.getDistance(), reading.getPosition(),
                reading.getDistanceStandardDeviation(),
                reading.getPositionCovariance());
    }

    /**
     * Creates an RSSI reading from a ranging and RSSI reading.
     * @param reading input reading to convert from.
     * @return an RSSI reading containing only the RSSI data of input reading.
     */
    private RssiReadingLocated<S, P> createRssiReading(RangingAndRssiReadingLocated<S, P> reading) {
        return new RssiReadingLocated<>(reading.getSource(),
                reading.getRssi(), reading.getPosition(),
                reading.getRssiStandardDeviation(),
                reading.getPositionCovariance());
    }
}
