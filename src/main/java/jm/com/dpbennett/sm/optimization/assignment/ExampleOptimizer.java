/*
System Management (SM)
Copyright (C) 2025  D P Bennett & Associates Limited

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
public class ExampleOptimizer {

    public int optimize(int[][] costMatrix) {
        OrToolsBootstrap.init();

        return 0;
    }

    public static void main(String[] args) {
       
        OrToolsBootstrap.init();

        System.out.println("Google OR-Tools version: " + OrToolsVersion.getVersionString());

        // Create the linear solver with the GLOP backend.
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            System.out.println("Could not create solver GLOP");
            return;
        }

        // Create the variables x and y.
        MPVariable x = solver.makeNumVar(0.0, 1.0, "x");
        MPVariable y = solver.makeNumVar(0.0, 2.0, "y");

        System.out.println("Number of variables = " + solver.numVariables());

        double infinity = Double.POSITIVE_INFINITY;
        // Create a linear constraint, x + y <= 2.
        MPConstraint ct = solver.makeConstraint(-infinity, 2.0, "ct");
        ct.setCoefficient(x, 1);
        ct.setCoefficient(y, 1);

        System.out.println("Number of constraints = " + solver.numConstraints());

        // Create the objective function, 3 * x + y.
        MPObjective objective = solver.objective();
        objective.setCoefficient(x, 3);
        objective.setCoefficient(y, 1);
        objective.setMaximization();

        System.out.println("Solving with " + solver.solverVersion());
        final MPSolver.ResultStatus resultStatus = solver.solve();

        System.out.println("Status: " + resultStatus);
        if (resultStatus != MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("The problem does not have an optimal solution!");
            if (resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                System.out.println("A potentially suboptimal solution was found");
            } else {
                System.out.println("The solver could not solve the problem.");
                return;
            }
        }

        System.out.println("Solution:");
        System.out.println("Objective value = " + objective.value());
        System.out.println("x = " + x.solutionValue());
        System.out.println("y = " + y.solutionValue());

        System.out.println("Advanced usage:");
        System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
        System.out.println("Problem solved in " + solver.iterations() + " iterations");

    }

}
