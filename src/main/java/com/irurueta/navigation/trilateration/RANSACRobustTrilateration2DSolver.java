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
package com.irurueta.navigation.trilateration;

import com.irurueta.geometry.Circle;
import com.irurueta.geometry.Point2D;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NavigationException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.numerical.robust.*;

import java.util.List;

/**
 * Robustly solves the trilateration problem by finding the best pairs of 2D
 * positions and distances among the provided ones using RANSAC algorithm to
 * discard outliers.
 */
@SuppressWarnings("WeakerAccess")
public class RANSACRobustTrilateration2DSolver extends RobustTrilateration2DSolver {

    /**
     * Constant defining default threshold to determine whether samples are inliers or not.
     */
    public static final double DEFAULT_THRESHOLD = 1e-2;

    /**
     * Minimum value that can be set as threshold.
     * Threshold must be strictly greater than 0.0.
     */
    public static final double MIN_THRESHOLD = 0.0;

    /**
     * Indicates that by default inliers will only be computed but not kept.
     */
    public static final boolean DEFAULT_COMPUTE_AND_KEEP_INLIERS = false;

    /**
     * Indicates that by default residuals will only be computed but not kept.
     */
    public static final boolean DEFAULT_COMPUTE_AND_KEEP_RESIDUALS = false;

    /**
     * Threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on distance between estimated position and
     * distances provided for each sample.
     */
    private double mThreshold = DEFAULT_THRESHOLD;

    /**
     * Indicates whether inliers must be computed and kept.
     */
    private boolean mComputeAndKeepInliers = DEFAULT_COMPUTE_AND_KEEP_INLIERS;

    /**
     * Indicates whether residuals must be computed and kept.
     */
    private boolean mComputeAndKeepResiduals = DEFAULT_COMPUTE_AND_KEEP_RESIDUALS;

    /**
     * Constructor.
     */
    public RANSACRobustTrilateration2DSolver() {
        super();
    }

    /**
     * Constructor.
     * @param listener listener to be notified of events such as when estimation
     *                 starts, ends or its progress significantly changes.
     */
    public RANSACRobustTrilateration2DSolver(
            RobustTrilaterationSolverListener<Point2D> listener) {
        super(listener);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node to be
     *                  estimated.
     * @throws IllegalArgumentException if either positions or distances are null,
     * don't have the same length or their length is smaller than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Point2D[] positions, double[] distances)
            throws IllegalArgumentException {
        super(positions, distances);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node to be
     *                  estimated.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @throws IllegalArgumentException if either positions or distances are null,
     * don't have the same length or their length is smaller than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Point2D[] positions, double[] distances,
            double[] distanceStandardDeviations) throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations);
    }

    /**
     /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param listener listener to be notified of events such as when estimation starts,
     *                 ends or its progress significantly changes.
     * @throws IllegalArgumentException if either positions, distances or
     * standard deviations are null, don't have the same length or their length is smaller
     * than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Point2D[] positions, double[] distances,
            double[] distanceStandardDeviations,
            RobustTrilaterationSolverListener<Point2D> listener)
            throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations, listener);
    }


    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param listener listener to be notified of events such as when estimation starts,
     *                 ends or its progress significantly changes.
     * @throws IllegalArgumentException if either positions or distances are null,
     * don't have the same length or their length is smaller than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Point2D[] positions, double[] distances,
            RobustTrilaterationSolverListener<Point2D> listener)
            throws IllegalArgumentException {
        super(positions, distances, listener);
    }

    /**
     * Constructor.
     * @param circles circles defining positions and distances.
     * @throws IllegalArgumentException if circles is null or if length of circles array
     * is less than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Circle[] circles)
            throws IllegalArgumentException {
        super(circles);
    }

    /**
     * Constructor.
     * @param circles circles defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @throws IllegalArgumentException if circles is null, length of circles array is less
     * than required (3 points) or don't have the same length.
     */
    public RANSACRobustTrilateration2DSolver(Circle[] circles,
            double[] distanceStandardDeviations) throws IllegalArgumentException {
        super(circles, distanceStandardDeviations);
    }

    /**
     * Constructor.
     * @param circles circles defining positions and distances.
     * @param listener listener to be notified of events such as when estimation starts,
     *                 ends or its progress significantly changes.
     * @throws IllegalArgumentException if circles is null or if length of circles array
     * is less than required (3 points).
     */
    public RANSACRobustTrilateration2DSolver(Circle[] circles,
            RobustTrilaterationSolverListener<Point2D> listener)
            throws IllegalArgumentException {
        super(circles, listener);
    }

    /**
     * Constructor.
     * @param circles circles defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param listener listener to be notified of events such as when estimation starts,
     *                 ends or its progress significantly changes.
     * @throws IllegalArgumentException if circles is null, length of circles array is less
     * than required (3 points) or don't have the same length.
     */
    public RANSACRobustTrilateration2DSolver(Circle[] circles,
            double[] distanceStandardDeviations,
            RobustTrilaterationSolverListener<Point2D> listener)
            throws IllegalArgumentException {
        super(circles, distanceStandardDeviations, listener);
    }

    /**
     * Gets threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on distance between estimated position and distances
     * provided for each sample.
     * @return threshold to determine whether samples are inliers or not.
     */
    public double getThreshold() {
        return mThreshold;
    }

    /**
     * Sets threshold to determine whether samples are inliers or not when testing possible solutions.
     * The threshold refers to the amount of error on distance between estimated position and distances
     * provided for each sample.
     * @param threshold threshold to determine whether samples are inliers or not.
     * @throws IllegalArgumentException if provided value is equal or less than zero.
     * @throws LockedException if this solver is locked.
     */
    public void setThreshold(double threshold)
            throws IllegalArgumentException, LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (threshold <= MIN_THRESHOLD) {
            throw new IllegalArgumentException();
        }
        mThreshold = threshold;
    }

    /**
     * Indicates whether inliers must be computed and kept.
     * @return true if inliers must be computed and kept, false if inliers
     * only need to be computed but not kept.
     */
    public boolean isComputeAndKeepInliersEnabled() {
        return mComputeAndKeepInliers;
    }

    /**
     * Specifies whether inliers must be computed and kept.
     * @param computeAndKeepInliers true if inliers must be computed and kept,
     *                              false if inliers only need to be computed but not kept.
     * @throws LockedException if this solver is locked.
     */
    public void setComputeAndKeepInliersEnabled(boolean computeAndKeepInliers)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mComputeAndKeepInliers = computeAndKeepInliers;
    }

    /**
     * Indicates whether residuals must be computed and kept.
     * @return true if residuals must be computed and kept, false if residuals
     * only need to be computed but not kept.
     */
    public boolean isComputeAndKeepResiduals() {
        return mComputeAndKeepResiduals;
    }

    /**
     * Specifies whether residuals must be computed and kept.
     * @param computeAndKeepResiduals true if residuals must be computed and kept,
     *                                false if residuals only need to be computed but not kept.
     * @throws LockedException if this solver is locked.
     */
    public void setComputeAndKeepResidualsEnabled(boolean computeAndKeepResiduals)
            throws LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        mComputeAndKeepResiduals = computeAndKeepResiduals;
    }

    /**
     * Solves the trilateration problem.
     * @return estimated position.
     * @throws LockedException if instance is busy solving the trilateration problem.
     * @throws NotReadyException is solver is not ready.
     * @throws RobustEstimatorException if estimation fails for any reason
     * (i.e. numerical instability, no solution available, etc).
     */
    @Override
    public Point2D solve() throws LockedException, NotReadyException, RobustEstimatorException {
        if (isLocked()) {
            throw new LockedException();
        }
        if (!isReady()) {
            throw new NotReadyException();
        }

        RANSACRobustEstimator<Point2D> innerEstimator =
                new RANSACRobustEstimator<>(new RANSACRobustEstimatorListener<Point2D>() {

                    @Override
                    public double getThreshold() {
                        return mThreshold;
                    }

                    @Override
                    public int getTotalSamples() {
                        return mDistances.length;
                    }

                    @Override
                    public int getSubsetSize() {
                        return getMinRequiredPositionsAndDistances();
                    }

                    @Override
                    public void estimatePreliminarSolutions(int[] samplesIndices, List<Point2D> solutions) {
                        try {
                            int length = samplesIndices.length;
                            int index;
                            for (int i = 0; i < length; i++) {
                                index = samplesIndices[i];
                                mInnerPositions[i] = mPositions[index];
                                mInnerDistances[i] = mDistances[index];
                            }

                            mLinearSolver.setPositionsAndDistances(mInnerPositions, mInnerDistances);
                            mLinearSolver.solve();
                            solutions.add(mLinearSolver.getEstimatedPosition());
                        } catch (NavigationException ignore) {
                            //if anything fails, no solution is added
                        }
                    }

                    @Override
                    public double computeResidual(Point2D currentEstimation, int i) {
                        return Math.abs(currentEstimation.distanceTo(mPositions[i]) - mDistances[i]);
                    }

                    @Override
                    public boolean isReady() {
                        return RANSACRobustTrilateration2DSolver.this.isReady();
                    }

                    @Override
                    public void onEstimateStart(RobustEstimator<Point2D> estimator) {
                        if (mListener != null) {
                            mListener.onSolveStart(RANSACRobustTrilateration2DSolver.this);
                        }
                    }

                    @Override
                    public void onEstimateEnd(RobustEstimator<Point2D> estimator) {
                        if (mListener != null) {
                            mListener.onSolveEnd(RANSACRobustTrilateration2DSolver.this);
                        }
                    }

                    @Override
                    public void onEstimateNextIteration(RobustEstimator<Point2D> estimator, int iteration) {
                        if (mListener != null) {
                            mListener.onSolveNextIteration(RANSACRobustTrilateration2DSolver.this,
                                    iteration);
                        }
                    }

                    @Override
                    public void onEstimateProgressChange(RobustEstimator<Point2D> estimator, float progress) {
                        if (mListener != null) {
                            mListener.onSolveProgressChange(RANSACRobustTrilateration2DSolver.this,
                                    progress);
                        }
                    }
                });

        try {
            mLocked = true;
            mInliersData = null;
            innerEstimator.setComputeAndKeepInliersEnabled(
                    mComputeAndKeepInliers || mRefineResult);
            innerEstimator.setComputeAndKeepResidualsEnabled(
                    mComputeAndKeepResiduals || mRefineResult);
            innerEstimator.setConfidence(mConfidence);
            innerEstimator.setMaxIterations(mMaxIterations);
            innerEstimator.setProgressDelta(mProgressDelta);
            Point2D result = innerEstimator.estimate();
            mInliersData = innerEstimator.getInliersData();
            return attemptRefine(result);
        } catch (com.irurueta.numerical.LockedException e) {
            throw new LockedException(e);
        } catch (com.irurueta.numerical.NotReadyException e) {
            throw new NotReadyException(e);
        } finally {
            mLocked = false;
        }
    }

    /**
     * Returns method being used for robust estimation.
     * @return method being used for robust estimation.
     */
    @Override
    public RobustEstimatorMethod getMethod() {
        return RobustEstimatorMethod.RANSAC;
    }
}
