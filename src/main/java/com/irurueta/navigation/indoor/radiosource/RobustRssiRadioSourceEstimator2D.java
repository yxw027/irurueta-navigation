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

import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.Point2D;
import com.irurueta.navigation.NavigationException;
import com.irurueta.navigation.indoor.*;
import com.irurueta.numerical.robust.RobustEstimatorMethod;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This is an abstract class to robustly estimate 2D position, transmitted power and
 * pathloss exponent of a radio source (e.g. WiFi access point or bluetooth beacon),
 * by discarding outliers and assuming that the radio source emits isotropically
 * following the expression below:
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
 * If RssiReadings contain RSSI standard deviations, those values will be used,
 * otherwise it will be assumed an RSSI standard deviation of 1 dB.
 * Implementations of this class should be able to detect and discard outliers in
 * order to find the best solution.
 *
 * IMPORTANT: Implementations of this class can choose to estimate a
 * combination of radio source position, transmitted power and path loss
 * exponent. However enabling all three estimations usually achieves
 * inaccurate results. When using this class, estimation must be of at least
 * one parameter (position, transmitted power or path loss exponent) when
 * initial values are provided for the other two, and at most it should consist
 * of two parameters (either position and transmitted power, position and
 * path loss exponent or transmitted power and path loss exponent), providing an
 * initial value for the remaining parameter.
 *
 * @param <S> a {@link RadioSource} type.
 */
@SuppressWarnings("Duplicates")
public abstract class RobustRssiRadioSourceEstimator2D<S extends RadioSource> extends
        RobustRssiRadioSourceEstimator<S, Point2D> {

    /**
     * Radio source estimator used internally.
     */
    protected RssiRadioSourceEstimator2D<S> mInnerEstimator =
            new RssiRadioSourceEstimator2D<>();

    /**
     * Subset of readings used by inner estimator.
     */
    private List<RssiReadingLocated<S, Point2D>> mInnerReadings = new ArrayList<>();

    /**
     * Constructor.
     */
    public RobustRssiRadioSourceEstimator2D() {
        super();
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings signal readings belonging to the same radio source.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings) {
        super(readings);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param listener listener in charge of attending events raised by this instance.
     */
    public RobustRssiRadioSourceEstimator2D(
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings bleonging to the same radio source.
     *
     * @param readings signal readings belonging to the same radio source.
     * @param listener listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(readings, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition) {
        super(readings, initialPosition);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     */
    public RobustRssiRadioSourceEstimator2D(
            Point2D initialPosition) {
        super(initialPosition);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     */
    public RobustRssiRadioSourceEstimator2D(Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(initialPosition, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(readings, initialPosition, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's)
     */
    public RobustRssiRadioSourceEstimator2D(
            Double initialTransmittedPowerdBm) {
        super(initialTransmittedPowerdBm);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      WiFi signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm) {
        super(readings, initialTransmittedPowerdBm);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     */
    public RobustRssiRadioSourceEstimator2D(
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(initialTransmittedPowerdBm, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      WiFi signal readings belonging to the radio source point.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(readings, initialTransmittedPowerdBm, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm) {
        super(readings, initialPosition, initialTransmittedPowerdBm);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     */
    public RobustRssiRadioSourceEstimator2D(Point2D initialPosition,
            Double initialTransmittedPowerdBm) {
        super(initialPosition, initialTransmittedPowerdBm);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     */
    public RobustRssiRadioSourceEstimator2D(Point2D initialPosition,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(initialPosition, initialTransmittedPowerdBm, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(readings, initialPosition, initialTransmittedPowerdBm, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        super(readings, initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     */
    public RobustRssiRadioSourceEstimator2D(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        super(initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     */
    public RobustRssiRadioSourceEstimator2D(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Constructor.
     * Sets signal readings belonging to the same radio source.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @throws IllegalArgumentException if readings are not valid.
     */
    public RobustRssiRadioSourceEstimator2D(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        super(readings, initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, listener);
        mPreliminarySubsetSize = getMinReadings();
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param method robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>();
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>();
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>();
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>();
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>();
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings  signal readings belonging to the same radio source.
     * @param method    robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param listener  listener in charge of attending events raised by this instance.
     * @param method    robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings  signal readings belonging to the same radio source.
     * @param listener  listener in charge of attending events raised by this instance.
     * @param method    robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Double initialTransmittedPowerdBm, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D radio source power and position estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D radio source power and position estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param method        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>();
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>();
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>();
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param readings      signal readings belonging to the same radio source.
     * @param method        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param listener      listener in charge of attending events raised by this instance.
     * @param method        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param readings      signal readings belonging to the same radio source.
     * @param listener      listener in charge of attending events raised by this instance.
     * @param method        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance
     * @param method            robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of access point transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Double initialTransmittedPowerdBm,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, initialTransmittedPowerdBm);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, initialTransmittedPowerdBm);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, initialTransmittedPowerdBm,
                        listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition, initialTransmittedPowerdBm,
                        listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent, RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent,
                        listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent,
                        listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @param method                        robust estimator method.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener,
            RobustEstimatorMethod method) {
        switch (method) {
            case RANSAC:
                return new RANSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case LMedS:
                return new LMedSRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case MSAC:
                return new MSACRobustRssiRadioSourceEstimator2D<>(
                        readings, initialPosition, initialTransmittedPowerdBm,
                        initialPathLossExponent, listener);
            case PROSAC:
                return new PROSACRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent,
                        listener);
            case PROMedS:
            default:
                return new PROMedSRobustRssiRadioSourceEstimator2D<>(
                        qualityScores, readings, initialPosition,
                        initialTransmittedPowerdBm, initialPathLossExponent,
                        listener);
        }
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create() {
        return create(DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings signal readings belonging to the same radio source.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings) {
        return create(readings, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param listener listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings signal readings belonging to the same radio source.
     * @param listener listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(readings, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition) {
        return create(readings, initialPosition, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition initial position to start the estimation of radio
     *                        source position.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition) {
        return create(initialPosition, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(initialPosition, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(readings, initialPosition, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialTransmittedPowerdBm initial transmitted power to start the
     *                                   estimation of radio source transmitted power
     *                                   (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Double initialTransmittedPowerdBm) {
        return create(initialTransmittedPowerdBm, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm) {
        return create(readings, initialTransmittedPowerdBm, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(initialTransmittedPowerdBm, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(readings, initialTransmittedPowerdBm, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm) {
        return create(readings, initialPosition, initialTransmittedPowerdBm,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm) {
        return create(initialPosition, initialTransmittedPowerdBm,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(initialPosition, initialTransmittedPowerdBm, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(readings, initialPosition, initialTransmittedPowerdBm, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        return create(readings, initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        return create(initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(readings, initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores) {
        return create(qualityScores, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param readings      signal readings belonging to the same radio source.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings) {
        return create(qualityScores, readings, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param listener      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores quality scores corresponding to each provided
     *                      sample. The larger the score value the better
     *                      the quality of the sample.
     * @param readings      signal readings belonging to the same radio source.
     * @param listener      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, readings, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition) {
        return create(qualityScores, readings, initialPosition,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition) {
        return create(qualityScores, initialPosition, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, initialPosition, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores     quality scores corresponding to each provided
     *                          sample. The larger the score value the better
     *                          the quality of the sample.
     * @param readings          signal readings belonging to the same radio source.
     * @param initialPosition   initial position to start the estimation of radio
     *                          source position.
     * @param listener          listener in charge of attending events raised by this instance
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, readings, initialPosition, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Double initialTransmittedPowerdBm) {
        return create(qualityScores, initialTransmittedPowerdBm,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm) {
        return create(qualityScores, readings, initialTransmittedPowerdBm,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, initialTransmittedPowerdBm, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's)
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, readings, initialTransmittedPowerdBm, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm) {
        return create(qualityScores, readings, initialPosition,
                initialTransmittedPowerdBm, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm) {
        return create(qualityScores, initialPosition, initialTransmittedPowerdBm,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, initialPosition, initialTransmittedPowerdBm,
                listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, readings, initialPosition,
                initialTransmittedPowerdBm, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent) {
        return create(qualityScores, readings, initialPosition,
                initialTransmittedPowerdBm, initialPathLossExponent,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent) {
        return create(qualityScores, initialPosition,
                initialTransmittedPowerdBm, initialPathLossExponent,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores, Point2D initialPosition,
            Double initialTransmittedPowerdBm, double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, initialPosition, initialTransmittedPowerdBm,
                initialPathLossExponent, listener, DEFAULT_ROBUST_METHOD);
    }

    /**
     * Creates a robust 2D position radio source estimator using
     * default method.
     *
     * @param qualityScores                 quality scores corresponding to each provided
     *                                      sample. The larger the score value the better
     *                                      the quality of the sample.
     * @param readings                      signal readings belonging to the same radio source.
     * @param initialPosition               initial position to start the estimation of radio
     *                                      source position.
     * @param initialTransmittedPowerdBm    initial transmitted power to start the
     *                                      estimation of radio source transmitted power
     *                                      (expressed in dBm's).
     * @param initialPathLossExponent       initial path loss exponent. A typical value is 2.0.
     * @param listener                      listener in charge of attending events raised by this instance.
     * @return a new robust 2D position radio source estimator.
     * @throws IllegalArgumentException if readings are not valid.
     * @param <S> a {@link RadioSource} type.
     */
    public static <S extends RadioSource> RobustRssiRadioSourceEstimator2D<S> create(
            double[] qualityScores,
            List<? extends RssiReadingLocated<S, Point2D>> readings,
            Point2D initialPosition, Double initialTransmittedPowerdBm,
            double initialPathLossExponent,
            RobustRssiRadioSourceEstimatorListener<S, Point2D> listener) {
        return create(qualityScores, readings, initialPosition,
                initialTransmittedPowerdBm, initialPathLossExponent, listener,
                DEFAULT_ROBUST_METHOD);
    }

    /**
     * Gets minimum required number of readings to estimate
     * power, position and pathloss exponent.
     * This value depends on the number of parameters to
     * be estimated, but for position only, this is 3
     * readings.
     *
     * @return minimum required number of readings.
     */
    @Override
    public int getMinReadings() {
        int minReadings = 0;
        if (isPositionEstimationEnabled()) {
            minReadings += Point2D.POINT2D_INHOMOGENEOUS_COORDINATES_LENGTH;
        }
        if (isTransmittedPowerEstimationEnabled()) {
            minReadings++;
        }
        if (isPathLossEstimationEnabled()) {
            minReadings ++;
        }
        return ++minReadings;
    }

    /**
     * Gets number of dimensions of position points.
     *
     * @return always returns 2 dimensions.
     */
    @Override
    public int getNumberOfDimensions() {
        return Point2D.POINT2D_INHOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Gets estimated located radio source with estimated transmitted power.
     *
     * @return estimated located radio source with estimated transmitted power or null.
     */
    @Override
    @SuppressWarnings("unchecked")
    public RadioSourceWithPowerAndLocated<Point2D> getEstimatedRadioSource() {
        List<? extends RssiReadingLocated<S, Point2D>> readings = getReadings();
        if (readings == null || readings.isEmpty()) {
            return null;
        }
        S source = readings.get(0).getSource();

        Point2D estimatedPosition = getEstimatedPosition();
        if (estimatedPosition == null) {
            return null;
        }

        Matrix estimatedPositionCovariance = getEstimatedPositionCovariance();

        Double transmittedPowerVariance =
                getEstimatedTransmittedPowerVariance();
        Double transmittedPowerStandardDeviation = transmittedPowerVariance != null ?
                Math.sqrt(transmittedPowerVariance) : null;

        Double pathlossExponentVariance =
                getEstimatedPathLossExponentVariance();
        Double pathlossExponentStandardDeviation = pathlossExponentVariance != null ?
                Math.sqrt(pathlossExponentVariance) : null;

        if (source instanceof WifiAccessPoint) {
            WifiAccessPoint accessPoint = (WifiAccessPoint) source;
            return new WifiAccessPointWithPowerAndLocated2D(accessPoint.getBssid(),
                    source.getFrequency(), accessPoint.getSsid(),
                    getEstimatedTransmittedPowerdBm(),
                    transmittedPowerStandardDeviation,
                    getEstimatedPathLossExponent(),
                    pathlossExponentStandardDeviation,
                    estimatedPosition,
                    estimatedPositionCovariance);
        } else if(source instanceof Beacon) {
            Beacon beacon = (Beacon) source;
            return new BeaconWithPowerAndLocated2D(beacon.getIdentifiers(),
                    getEstimatedTransmittedPowerdBm(), beacon.getFrequency(),
                    beacon.getBluetoothAddress(), beacon.getBeaconTypeCode(),
                    beacon.getManufacturer(), beacon.getServiceUuid(),
                    beacon.getBluetoothName(),
                    getEstimatedPathLossExponent(),
                    transmittedPowerStandardDeviation,
                    pathlossExponentStandardDeviation,
                    estimatedPosition, estimatedPositionCovariance);
        }else {
            return null;
        }
    }

    /**
     * Solves preliminar solution for a subset of samples.
     *
     * @param samplesIndices indices of subset samples.
     * @param solutions instance where solution will be stored.
     */
    @Override
    protected void solvePreliminarSolutions(int[] samplesIndices,
            List<Solution<Point2D>> solutions) {

        try {
            int index;

            mInnerReadings.clear();
            for (int samplesIndice : samplesIndices) {
                index = samplesIndice;
                mInnerReadings.add(mReadings.get(index));
            }

            //initial transmitted power and position might or might not be available
            mInnerEstimator.setInitialTransmittedPowerdBm(
                    mInitialTransmittedPowerdBm);
            mInnerEstimator.setInitialPosition(mInitialPosition);
            mInnerEstimator.setInitialPathLossExponent(mInitialPathLossExponent);

            mInnerEstimator.setTransmittedPowerEstimationEnabled(mTransmittedPowerEstimationEnabled);
            mInnerEstimator.setPositionEstimationEnabled(mPositionEstimationEnabled);
            mInnerEstimator.setPathLossEstimationEnabled(mPathLossEstimationEnabled);

            mInnerEstimator.setReadings(mInnerReadings);

            mInnerEstimator.estimate();

            Point2D estimatedPosition = mInnerEstimator.getEstimatedPosition();
            double estimatedTransmittedPowerdBm =
                    mInnerEstimator.getEstimatedTransmittedPowerdBm();
            double estimatedPathLossExponent =
                    mInnerEstimator.getEstimatedPathLossExponent();
            solutions.add(new Solution<>(estimatedPosition,
                    estimatedTransmittedPowerdBm, estimatedPathLossExponent));
        } catch(NavigationException ignore) {
            //if anything fails, no solution is added
        }
    }

    /**
     * Attempts to refine estimated position and transmitted power contained in
     * provided solution if refinement is requested.
     * This method sets a refined result and transmitted power or provided input
     * result if refinement is not requested or has failed.
     * If refinement is enabled and it is requested to keep covariance, this method
     * will also keep covariance of refined result.
     * solution if not requested or refinement failed.
     *
     * @param result result to be refined.
     */
    protected void attemptRefine(Solution<Point2D> result) {
        Point2D initialPosition = result.getEstimatedPosition();
        double initialTransmittedPowerdBm =
                result.getEstimatedTransmittedPowerdBm();
        double initialPathLossExponent = result.getEstimatedPathLossExponent();

        if (mRefineResult && mInliersData != null) {
            BitSet inliers = mInliersData.getInliers();
            int nSamples = mReadings.size();

            mInnerReadings.clear();

            for (int i = 0; i < nSamples; i++) {
                if (inliers.get(i)) {
                    //sample is inlier
                    mInnerReadings.add(mReadings.get(i));
                }
            }

            try {
                mInnerEstimator.setInitialPosition(initialPosition);
                mInnerEstimator.setInitialTransmittedPowerdBm(initialTransmittedPowerdBm);
                mInnerEstimator.setInitialPathLossExponent(initialPathLossExponent);
                mInnerEstimator.setTransmittedPowerEstimationEnabled(mTransmittedPowerEstimationEnabled);
                mInnerEstimator.setPositionEstimationEnabled(mPositionEstimationEnabled);
                mInnerEstimator.setPathLossEstimationEnabled(mPathLossEstimationEnabled);
                mInnerEstimator.setReadings(mInnerReadings);

                mInnerEstimator.estimate();

                Matrix cov = mInnerEstimator.getEstimatedCovariance();
                if (mKeepCovariance && cov != null) {
                    //keep covariance
                    mCovariance = cov;

                    int dims = getNumberOfDimensions();
                    int pos = 0;
                    if (mPositionEstimationEnabled) {
                        //position estimation enabled
                        int d = dims -1;
                        if (mEstimatedPositionCovariance == null) {
                            mEstimatedPositionCovariance = mCovariance.
                                    getSubmatrix(0, 0, d, d);
                        } else {
                            mCovariance.getSubmatrix(0, 0, d, d,
                                    mEstimatedPositionCovariance);
                        }
                        pos += dims;
                    } else {
                        //position estimation disabled
                        mEstimatedPositionCovariance = null;
                    }

                    if (mTransmittedPowerEstimationEnabled) {
                        //transmitted power estimation enabled
                        mEstimatedTransmittedPowerVariance = mCovariance.
                                getElementAt(pos, pos);
                        pos++;
                    } else {
                        //transmitted power estimation disabled
                        mEstimatedTransmittedPowerVariance = null;
                    }

                    if (mPathLossEstimationEnabled) {
                        //pathloss exponent estimation enabled
                        mEstimatedPathLossExponentVariance = mCovariance.
                                getElementAt(pos, pos);
                    } else {
                        //pathloss exponent estimation disabled
                        mEstimatedPathLossExponentVariance = null;
                    }
                } else {
                    mCovariance = null;
                    mEstimatedPositionCovariance = null;
                    mEstimatedTransmittedPowerVariance = null;
                    mEstimatedPathLossExponentVariance = null;
                }

                mEstimatedPosition = mInnerEstimator.getEstimatedPosition();
                mEstimatedTransmittedPowerdBm =
                        mInnerEstimator.getEstimatedTransmittedPowerdBm();
                mEstimatedPathLossExponent =
                        mInnerEstimator.getEstimatedPathLossExponent();
            } catch (Exception e) {
                //refinement failed, so we return input value, and covariance
                //becomes unavailable
                mCovariance = null;
                mEstimatedPositionCovariance = null;
                mEstimatedTransmittedPowerVariance = null;
                mEstimatedPathLossExponentVariance = null;

                mEstimatedPosition = initialPosition;
                mEstimatedTransmittedPowerdBm = initialTransmittedPowerdBm;
                mEstimatedPathLossExponent = initialPathLossExponent;
            }
        } else {
            mCovariance = null;
            mEstimatedPositionCovariance = null;
            mEstimatedTransmittedPowerVariance = null;
            mEstimatedPathLossExponentVariance = null;

            mEstimatedPosition = initialPosition;
            mEstimatedTransmittedPowerdBm = initialTransmittedPowerdBm;
            mEstimatedPathLossExponent = initialPathLossExponent;
        }
    }
}
