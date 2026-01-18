/*
System Management (SM)
Copyright (C) 2026  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.sm.optimization.assignment;

import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.linearsolver.MPObjective;
import jm.com.dpbennett.sm.optimization.core.OrToolsBootstrap;

/**
 *
 * @author Desmond Bennett
 */

/*
Maximize Z = 3x1 + 5x2 ,

subject to the restrictions:
        x <= 4
        2y <= 12
        3x + 2y <= 18

and
        x >= 0, y >= 0

 */
public class WyndorGlass {

    public static void main(String[] args) {
        // Load native libraries
        OrToolsBootstrap.init();

        // Create the solver (Linear Programming)
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            System.err.println("Could not create solver GLOP");
            return;
        }

        // Decision variables: x >= 0, y >= 0
        MPVariable x = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "x");
        MPVariable y = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "y");

        // Constraints
        // x <= 4
        MPConstraint c1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 4.0, "c1");
        c1.setCoefficient(x, 1);

        // 2y <= 12
        MPConstraint c2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 12.0, "c2");
        c2.setCoefficient(y, 2);

        // 3x + 2y <= 18
        MPConstraint c3 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 18.0, "c3");
        c3.setCoefficient(x, 3);
        c3.setCoefficient(y, 2);

        // Objective: Maximize Z = 3x + 5y
        MPObjective objective = solver.objective();
        objective.setCoefficient(x, 3);
        objective.setCoefficient(y, 5);
        objective.setMaximization();

        // Solve
        MPSolver.ResultStatus resultStatus = solver.solve();

        // Output results
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Optimal solution found:");
            System.out.println("x = " + x.solutionValue());
            System.out.println("y = " + y.solutionValue());
            System.out.println("Maximum Z = " + objective.value());
        } else {
            System.out.println("No optimal solution found.");
        }
    }
}
