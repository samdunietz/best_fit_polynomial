import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import Jama.*;

public class BestFitPolynomial
{

	public static final String FILE_LOCATION = "example.txt";
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		Scanner input = new Scanner(System.in);
		
		int degreePolynomial = getInt(input, "Degree polynomial? ", "Enter an integer.");
		while (degreePolynomial < 0)
		{
			System.out.println("Enter a positive number.");
			degreePolynomial = getInt(input, "Degree polynomial? ", "Enter an integer.");
		}
		
		Scanner dataSet = new Scanner(new File(FILE_LOCATION));
		
		// One-off Scanner for counting lines contained in file,
		// which correspond to number of points in this program.
		Scanner dataSetHowManyLines = new Scanner(new File(FILE_LOCATION)); 
		int numPoints = howManyLines(dataSetHowManyLines);
		
		// one row will give x coordinates, one y coordinates
		double[][] allPoints = new double[2][numPoints]; 
		for (int point = 0; point < numPoints; point++)
		{
			double x = dataSet.nextDouble();
			allPoints[0][point] = x; 
			double y = dataSet.nextDouble();
			allPoints[1][point] = y;
		}
		// allPoints[0] now gives array of x coordinates
		// allPoints[1] now gives array of y coordinates
		
		double[][] leftArray = calculateLeftArray(allPoints, degreePolynomial);
		Matrix leftMatrix = new Matrix(leftArray); // Matrix on the far left side
		
		double[][] rightArray= calculateRightArray(allPoints, degreePolynomial);
		Matrix rightMatrix = new Matrix(rightArray); // Matrix on the right side
		
		Matrix coefficientsSolutionMatrix = leftMatrix.solve(rightMatrix); // Right matrix on the left side
		System.out.println(solutionToString(coefficientsSolutionMatrix));
		
		double rSquared = calculateRSquared(coefficientsSolutionMatrix, allPoints);
		System.out.println("R^2: " + rSquared);	
	}
	
	public static double calculateRSquared(Matrix coefficientsSolutionMatrix, double[][] allPoints)
	{
		double sumOfErrorSquared = 0.; // sum of (squares of distances between projected points and actual points)
		double projectedY;
		double variance;
		for (int i = 0; i < allPoints[0].length; i++)
		{
			projectedY = bestFitFuction(allPoints[0][i], coefficientsSolutionMatrix);
			variance = projectedY - allPoints[1][i];
			sumOfErrorSquared += Math.pow(variance, 2);
		}
		
		double meanOfY = getMean(allPoints[1]);
		double sum_of_squares_of_distances_from_mean = 0.;
		for (double d: allPoints[1])
		{
			sum_of_squares_of_distances_from_mean += Math.pow(d - meanOfY, 2);
		}
		
		return 1 - (sumOfErrorSquared / sum_of_squares_of_distances_from_mean); 
	}

	// Let y = f(x), defined by the coefficientSolutionMatrix, be the best-fit polynomial
	// Takes double d and returns f(d).
	public static double bestFitFuction(double d, Matrix coefficientsSolutionMatrix)
	{
		double coefficient;
		double functionOutput = 0.;
		for (int i = 0; i < coefficientsSolutionMatrix.getRowDimension(); i++)
		{
			coefficient = coefficientsSolutionMatrix.get(i, 0);
			functionOutput += coefficient * Math.pow(d, (coefficientsSolutionMatrix.getRowDimension() - 1) - i); //Ex. "3x^2" 
		}
		
		return functionOutput;
	}

	// Calculate the mean of an array of doubles
	public static double getMean(double[] allPoints) 
	{
		double sum = 0;
		for (double d : allPoints)
		{
			sum += d;
		}
		
		return sum / allPoints.length;
	}

	public static double[][] calculateRightArray(double[][] allPoints, int degreePolynomial) 
	{
		double[][] rightArray = new double[degreePolynomial + 1][1];
		for (int i = degreePolynomial; i >= 0; i--)
		{
			rightArray[degreePolynomial - i][0] = rightSummation(allPoints[0], allPoints[1], i);
		}
		
		return rightArray;
	}



	public static double[][] calculateLeftArray(double[][] allPoints, int degreePolynomial) 
	{
		double[][] leftArray = new double[degreePolynomial + 1][degreePolynomial + 1]; 
		// "+ 1" because there is a coefficient for x^0 term, a constant
		// meaning that there is one more coefficient that there is degree polynomial
				 
		for (int i = degreePolynomial; i >= 0; i--)
		{
			for (int j = degreePolynomial; j >= 0; j--)
			{
				leftArray[degreePolynomial - i][degreePolynomial - j] = leftSummation(allPoints[0], i + j);
			}
		}
		
		return leftArray;
	}

	public static double rightSummation(double[] xPoints, double[] yPoints, int xRaisedTo)
	{
		double sum = 0.;
		for (int i = 0; i < xPoints.length; i++)
		{
			sum += Math.pow(xPoints[i], (double)(xRaisedTo)) * yPoints[i];
		}
		
		return sum;
	}
	
	public static double leftSummation(double[] xPoints, int raisedTo)
	{
		double sum = 0.;
		for (int i = 0; i < xPoints.length; i++)
		{
			sum += Math.pow(xPoints[i], (double)(raisedTo));
		}
		
		return sum;
	}

	// Takes Scanner, in this case for file, and returns how many lines are in Scanner
	public static int howManyLines(Scanner dataSetHowManyLines) 
	{
		int numLines = 0;
		while (dataSetHowManyLines.hasNextLine())
		{
			numLines++;
			dataSetHowManyLines.nextLine();
		}
		return numLines;
	}
	
	// Gets int from user
	public static int getInt(Scanner input, String prompt, String errorMessage)
	{
		System.out.print(prompt);
		while (!input.hasNextInt())
		{
			input.next();
			System.out.println(errorMessage);
			System.out.print(prompt);
		}
		return input.nextInt();
	}
	
	public static String solutionToString(Matrix coefficientSolutionMatrix)
	{
		String intro = "The best fit polynomial of degree " + (coefficientSolutionMatrix.getRowDimension() - 1) + " is:\n";
		String equation = "y = ";
		// For loop appends the equation to String equation
		// Note that "coefficientSolutionMatrix.getRowDimension() - 1" should equal degreePolynomial
		for (int i = coefficientSolutionMatrix.getRowDimension() - 1; i >= 0; i--)
		{
			equation += coefficientSolutionMatrix.get(coefficientSolutionMatrix.getRowDimension() - 1 - i, 0);
			if (i > 1)
			{
				equation += "x^" + i + " + ";
			}
			else if (i == 1)
			{
				equation += "x" + " + "; // So you see 3x, not 3x^1
			}
			// if i == 0, no additional x term in equation, just constant			
		}
			
		return intro + equation;
	}
}
