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

import com.irurueta.geometry.InhomogeneousPoint3D;
import com.irurueta.geometry.Point3D;
import com.irurueta.geometry.Sphere;
import com.irurueta.navigation.LockedException;

/**
 * Solves a Trilateration problem with an instance of a least squares optimizer.
 */
@SuppressWarnings({"WeakerAccess", "Duplicates"})
public class NonLinearLeastSquaresTrilateration3DSolver extends NonLinearLeastSquaresTrilaterationSolver<Point3D> {

    /**
     * Constructor.
     */
    public NonLinearLeastSquaresTrilateration3DSolver() {
        super();
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @throws IllegalArgumentException if either positions or distances are null, don't have the same length or their
     * length is smaller than required (3 for 2D points or 4 for 3D points) or fitter is null.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances)
            throws IllegalArgumentException {
        super(positions, distances);
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start trilateration solving.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D initialPosition) {
        super(initialPosition);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param initialPosition initial position to start trilateration solving.
     * @throws IllegalArgumentException if either positions or distances are null, don't have the same length or their
     * length is smaller than required (3 for 2D points or 4 for 3D points) or fitter is null.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances, Point3D initialPosition)
            throws IllegalArgumentException {
        super(positions, distances, initialPosition);
    }

    /**
     * Constructor.
     * @param listener listener to be notified of events raised by this instance.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(TrilaterationSolverListener<Point3D> listener) {
        super(listener);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if either positions or distances are null, don't have the same length or their
     * length is smaller than required (3 for 2D points or 4 for 3D points) or fitter is null.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(positions, distances, listener);
    }

    /**
     * Constructor.
     * @param initialPosition initial position to start trilateration solving.
     * @param listener listener to be notified of events raised by this instance.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D initialPosition,
            TrilaterationSolverListener<Point3D> listener) {
        super(initialPosition, listener);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param initialPosition initial position to start trilateration solving.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if either positions or distances are null, don't have the same length or their
     * length is smaller than required (3 for 2D points or 4 for 3D points) or fitter is null.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances, Point3D initialPosition,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(positions, distances, initialPosition, listener);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres) throws IllegalArgumentException {
        super();
        internalSetSpheres(spheres);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param initialPosition initial position to start trilateration solving.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres, Point3D initialPosition)
            throws IllegalArgumentException {
        super(initialPosition);
        internalSetSpheres(spheres);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(listener);
        internalSetSpheres(spheres);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param initialPosition initial position to start trilateration solving.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres, Point3D initialPosition,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(initialPosition, listener);
        internalSetSpheres(spheres);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @throws IllegalArgumentException if either positions, distances or standard deviations
     * are null, don't have the same length of their length is smaller than required (2 points).
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances,
            double[] distanceStandardDeviations) throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param initialPosition initial position to start trilateration solving.
     * @throws IllegalArgumentException if either positions, distances or standard deviations
     * are null, don't have the same length of their length is smaller than required (2 points).
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances,
            double[] distanceStandardDeviations, Point3D initialPosition)
            throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations, initialPosition);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if either positions, distances or standard deviations
     * are null, don't have the same length of their length is smaller than required (2 points).
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances,
            double[] distanceStandardDeviations, TrilaterationSolverListener<Point3D> listener)
            throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations, listener);
    }

    /**
     * Constructor.
     * @param positions known positions of static nodes.
     * @param distances euclidean distances from static nodes to mobile node.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param initialPosition initial position to start trilateration solving.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if either positions, distances or standard deviations
     * are null, don't have the same length of their length is smaller than required (2 points).
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Point3D[] positions, double[] distances,
            double[] distanceStandardDeviations, Point3D initialPosition,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(positions, distances, distanceStandardDeviations, initialPosition, listener);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres,
            double[] distanceStandardDeviations) throws IllegalArgumentException {
        super();
        internalSetSpheresAndStandardDeviations(spheres, distanceStandardDeviations);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param initialPosition initial position to start trilateration solving.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres,
            double[] distanceStandardDeviations, Point3D initialPosition)
            throws IllegalArgumentException {
        super(initialPosition);
        internalSetSpheresAndStandardDeviations(spheres, distanceStandardDeviations);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres,
            double[] distanceStandardDeviations,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(listener);
        internalSetSpheresAndStandardDeviations(spheres, distanceStandardDeviations);
    }

    /**
     * Constructor.
     * @param spheres spheres defining positions and distances.
     * @param distanceStandardDeviations standard deviations of provided measured distances.
     * @param initialPosition initial position to start trilateration solving.
     * @param listener listener to be notified of events raised by this instance.
     * @throws IllegalArgumentException if spheres is null or if length of spheres array is less than 2.
     */
    public NonLinearLeastSquaresTrilateration3DSolver(Sphere[] spheres,
            double[] distanceStandardDeviations, Point3D initialPosition,
            TrilaterationSolverListener<Point3D> listener) throws IllegalArgumentException {
        super(initialPosition, listener);
        internalSetSpheresAndStandardDeviations(spheres, distanceStandardDeviations);
    }

    /**
     * Gets spheres defined by provided positions and distances.
     * @return spheres defined by provided positions and distances.
     */
    public Sphere[] getSpheres() {
        if (mPositions == null) {
            return null;
        }

        Sphere[] result = new Sphere[mPositions.length];

        for (int i = 0; i < mPositions.length; i++) {
            result[i] = new Sphere(mPositions[i], mDistances[i]);
        }
        return result;
    }

    /**
     * Sets spheres defining positions and euclidean distances.
     * @param spheres spheres defining positions and distances.
     * @throws IllegalArgumentException if spheres is null or length of array of spheres
     * is less than 2.
     * @throws LockedException if instance is busy solving the trilateration problem.
     */
    public void setSpheres(Sphere[] spheres) throws IllegalArgumentException, LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        internalSetSpheres(spheres);
    }

    /**
     * Sets spheres defining positions and euclidean distances along with the standard
     * deviations of provided spheres radii.
     * @param spheres spheres defining positions and distances.
     * @param radiusStandardDeviations standard deviations of circles radii.
     * @throws IllegalArgumentException if spheres is null, length of arrays is less than
     * 2 or don't have the same length.
     * @throws LockedException if instance is busy solving the trilateration problem.
     */
    public void setSpheresAndStandardDeviations(Sphere[] spheres, double[] radiusStandardDeviations)
            throws IllegalArgumentException, LockedException {
        if (isLocked()) {
            throw new LockedException();
        }
        internalSetSpheresAndStandardDeviations(spheres, radiusStandardDeviations);
    }

    /**
     * Gets number of dimensions of provided points.
     * @return always returns 3 dimensions.
     */
    @Override
    public int getNumberOfDimensions() {
        return Point3D.POINT3D_INHOMOGENEOUS_COORDINATES_LENGTH;
    }

    /**
     * Gets estimated position.
     * @return estimated position.
     */
    @Override
    public Point3D getEstimatedPosition() {
        if (mEstimatedPositionCoordinates == null) {
            return null;
        }

        InhomogeneousPoint3D position = new InhomogeneousPoint3D();
        getEstimatedPosition(position);
        return position;
    }

    /**
     * Internally sets spheres defining positions and euclidean distances.
     * @param spheres spheres defining positions and distances.
     * @throws IllegalArgumentException if spheres is null or length of array of spheres
     * is less than 2.
     */
    public void internalSetSpheres(Sphere[] spheres) throws IllegalArgumentException {
        if (spheres == null || spheres.length < MIN_POINTS) {
            throw new IllegalArgumentException();
        }

        Point3D[] positions = new Point3D[spheres.length];
        double[] distances = new double[spheres.length];
        for (int i = 0; i < spheres.length; i++) {
            Sphere sphere = spheres[i];
            positions[i] = sphere.getCenter();
            distances[i] = sphere.getRadius();
        }

        internalSetPositionsAndDistances(positions, distances);
    }

    /**
     * Internally sets spheres defining positions and euclidean distances along with the standard
     * deviations of provided spheres radii.
     * @param spheres spheres defining positions and distances.
     * @param radiusStandardDeviations standard deviations of circles radii.
     * @throws IllegalArgumentException if spheres is null, length of arrays is less than
     * 2 or don't have the same length.
     */
    private void internalSetSpheresAndStandardDeviations(Sphere[] spheres, double[] radiusStandardDeviations)
            throws IllegalArgumentException {
        if (spheres == null || spheres.length < MIN_POINTS) {
            throw new IllegalArgumentException();
        }

        if (radiusStandardDeviations == null) {
            throw new IllegalArgumentException();
        }

        if (radiusStandardDeviations.length != spheres.length) {
            throw new IllegalArgumentException();
        }

        Point3D[] positions = new Point3D[spheres.length];
        double[] distances = new double[spheres.length];
        for (int i = 0; i < spheres.length; i++) {
            Sphere sphere = spheres[i];
            positions[i] = sphere.getCenter();
            distances[i] = sphere.getRadius();
        }

        internalSetPositionsDistancesAndStandardDeviations(positions, distances,
                radiusStandardDeviations);

    }
}
