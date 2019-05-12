package com.irurueta.navigation.indoor.position;

import com.irurueta.geometry.Point2D;
import com.irurueta.navigation.indoor.Fingerprint;
import com.irurueta.navigation.indoor.RadioSource;
import com.irurueta.navigation.indoor.RadioSourceLocated;
import com.irurueta.navigation.indoor.Reading;

import java.util.List;

public class SequentialRobustMixedPositionEstimator2D extends
        SequentialRobustMixedPositionEstimator<Point2D> {

    /**
     * Constructor.
     */
    public SequentialRobustMixedPositionEstimator2D() {
        super();
    }

    /**
     * Constructor.
     *
     * @param sources located radio sources used for lateration.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(
            List<? extends RadioSourceLocated<Point2D>> sources) {
        super(sources);
    }

    /**
     * Constructor.
     *
     * @param fingerprint fingerprint containing RSSI readings at an unknown location
     *                    for provided located radio sources.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator2D(
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(fingerprint);
    }

    /**
     * Constructor.
     *
     * @param sources       located radio sources used for lateration.
     * @param fingerprint   fingerprint containing reagins at an unknown location
     *                      for provided located radio sources.
     * @throws IllegalArgumentException if either provided sources or fingerprint is
     *                                  null or the number of provided sources is less
     *                                  than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(
            List<? extends RadioSourceLocated<Point2D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sources, fingerprint);
    }

    /**
     * Constructor.
     *
     * @param listener listener in charge of handling events.
     */
    public SequentialRobustMixedPositionEstimator2D(
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(listener);
    }

    /**
     * Constructor.
     *
     * @param sources   located radio sources used for lateration.
     * @param listener  listener in charge of handling events.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required
     *                                  minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(
            List<? extends RadioSourceLocated<Point2D>> sources,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sources, listener);
    }

    /**
     * Constructor.
     *
     * @param fingerprint   fingerprint containing readings at an unknown location for
     *                      provided located radio sources.
     * @param listener      listener in charge of handling events.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator2D(
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(fingerprint, listener);
    }

    /**
     * Constructor.
     *
     * @param sources       located radio sources used for lateration.
     * @param fingerprint   fingerprint containing readings at an unknown location
     *                      for provided located radio sources.
     * @param listener      listener in charge of handling events.
     * @throws IllegalArgumentException if either provided sources or fingerprint is
     *                                  null or the number of provided sources is less
     *                                  than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(
            List<? extends RadioSourceLocated<Point2D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sources, fingerprint, listener);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to
     *                                          readings within provided fingerprint.
     *                                          The larger the score the better the
     *                                          quality of the reading.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores) {
        super(sourceQualityScores, fingerprintReadingQualityScores);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point2D>> sources) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sourceQualityScores, fingerprintReadingQualityScores, fingerprint);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @throws IllegalArgumentException if either provided sources or fingerprint is null
     * or the number of provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point2D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                fingerprint);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param listener                          listener in charge of handling events.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, listener);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if provided sources is null or the number of
     *                                  provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point2D>> sources,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                listener);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to
     *                                          readings within provided fingerprint.
     *                                          The larger the score the better the
     *                                          quality of the reading.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located
     *                                          radio sources.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if provided fingerprint is null.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, fingerprint,
                listener);
    }

    /**
     * Constructor.
     *
     * @param sourceQualityScores               quality scores corresponding to each
     *                                          provided located radio source. The
     *                                          larger the score value the better the
     *                                          quality of the radio source.
     * @param fingerprintReadingQualityScores   quality scores corresponding to readings
     *                                          within provided fingerprint. The larger
     *                                          the score the better the quality of the
     *                                          reading.
     * @param sources                           located radio sources used for
     *                                          lateration.
     * @param fingerprint                       fingerprint containing readings at an
     *                                          unknown location for provided located radio
     *                                          sources.
     * @param listener                          listener in charge of handling events.
     * @throws IllegalArgumentException if either provided sources or fingerprint is null
     * or the number of provided sources is less than the required minimum.
     */
    public SequentialRobustMixedPositionEstimator2D(double[] sourceQualityScores,
            double[] fingerprintReadingQualityScores,
            List<? extends RadioSourceLocated<Point2D>> sources,
            Fingerprint<? extends RadioSource, ? extends Reading<? extends RadioSource>> fingerprint,
            SequentialRobustMixedPositionEstimatorListener<Point2D> listener) {
        super(sourceQualityScores, fingerprintReadingQualityScores, sources,
                fingerprint, listener);
    }

    /**
     * Gets number of dimesnions of provided and estimated points.
     *
     * @return number of dimensions of provided and estimated points.
     */
    @Override
    public int getNumberOfDimensions() {
        return Point2D.POINT2D_INHOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Gets minimum required number of located radio sources to perform lateration.
     *
     * @return minimum required number of located radio sources to perform
     * lateration.
     */
    @Override
    public int getMinRequiredSources() {
        return Point2D.POINT2D_HOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Builds ranging internal estimator.
     */
    @Override
    protected void buildRangingEstimator() {
        mRangingEstimator = RobustRangingPositionEstimator2D.create(
                mRangingRobustMethod);
    }

    /**
     * Builds RSSI internal estimator.
     */
    @Override
    protected void buildRssiEstimator() {
        mRssiEstimator = RobustRssiPositionEstimator2D.create(mRssiRobustMethod);
    }
}
