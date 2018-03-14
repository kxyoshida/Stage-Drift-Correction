import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import java.lang.Math;

import ij.measure.*;
import ij.text.*;
import ij.io.*;
import java.text.DecimalFormat;
import java.awt.geom.Point2D;

public class fiducial_Chaser implements PlugInFilter {
    // This plugin produces a result table of >=6 columns
    // from an image stack, which will be then used in
    // "rigidBody.py" to calculate the transformation 
    // consisting of translation and rotation to compensate 
    // the drift of the original image.
    // First specify a rectanglar ROI in whicn a circle or 
    // fiducial marker is contained throughout the stack.

    static double cr = 7;
    static int maxIter = 240;
    static double finalStepSize = 0.5;

    ImagePlus imp;
    //    static String RTSource = "work/fiducialPositions.txt";
    public int setup(String arg, ImagePlus imp) {
	this.imp = imp;
	return DOES_8G + DOES_16 + DOES_32;
    }


    public void run(ImageProcessor ip) {

	GenericDialog gd = new GenericDialog("fiducial_Chaser", IJ.getInstance());
	gd.addNumericField("Circle Radius:", cr, 1);
	gd.addNumericField("Max Iteration:", maxIter, 0);
	gd.addNumericField("Final Step Size", finalStepSize, 2);
	gd.showDialog();
	if (gd.wasCanceled()) 
	    return;
	
	cr=(double)gd.getNextNumber();
	maxIter=(int)gd.getNextNumber();
	finalStepSize=(double)gd.getNextNumber();

	double convR = Math.pow(finalStepSize, 1.0/maxIter);
	IJ.log("convR="+convR);

	int width = ip.getWidth();
	int height = ip.getHeight();

	int nSlices = imp.getStack().getSize();

	ResultsTable rt = ResultsTable.getResultsTable();
	if (rt == null) {
	    rt = new ResultsTable();
	}
	int nResults = rt.getCounter();
	int currentColumn = (rt.getLastColumn()+1)/2+1;

	String column_X = "x"+currentColumn;
	String column_Y = "y"+currentColumn;
	//	rt.incrementCounter();

	Rectangle rect = ip.getRoi();
	double rx = rect.getX();
	double ry = rect.getY();
	double rw = rect.getWidth();
	double rh = rect.getHeight();

	//	imp.hide();
	for (int slice=1;slice<=nSlices;slice++) {
	    IJ.showProgress(slice,nSlices);


	    // Crop the ROI.
	    imp.setSlice(slice);
	    ip = imp.getProcessor();
	    ImageProcessor wip=ip.crop();

	    // Give the initial value of the circle centre.
	    ImageStatistics is = wip.getStatistics();
	    double xi = is.xCenterOfMass;
	    double yi = is.yCenterOfMass;
	    Point2D.Double p = new Point2D.Double(xi, yi);
	    p = seekCentre(wip, p, cr, maxIter, convR);
	    rt.setValue(column_X, slice-1, rx+p.getX());
	    rt.setValue(column_Y, slice-1, ry+p.getY());
	}
	//	imp.show();
	rt.show("Results");
    }

    Point2D.Double seekCentre(ImageProcessor ip, Point2D.Double p, double ri, int maxIter, double convergeRate) {
	Point2D.Double q = p;
	double ss = 1;
	for (int m=0;m<maxIter;m++) {
	    ss = convergeRate * ss;
	    //	    IJ.log("m="+m+", stepSize="+ss+", maxIter="+maxIter+", convergeRate="+convergeRate);
	    q = tinyMove(ip,p,ri,ss);
	    //	    IJ.log(m+","+q.getX()+", "+q.getY());
	    p = q;
	}
	return q;
    }
    
    Point2D.Double tinyMove(ImageProcessor ip, Point2D.Double pi, double ri, double stepSize) {
	int tw = ip.getWidth();
	int th = ip.getHeight();
	double xi = pi.getX();
	double yi = pi.getY();
	double xcmax = xi;
	double ycmax = yi;
	double dimax = 0;
	for (int i=-1; i<2; i++) {
	    for (int j=-1; j<2; j++) {
		int nin=0;
		int nout=0;
		int iin=0;
		int iout=0;

		double xc=xi+j*stepSize;
		double yc=yi+i*stepSize;
		OvalRoi circle = new OvalRoi(xc-ri, yc-ri, ri*2, ri*2);
		ip.setRoi(circle);

		for (int y=0; y<th; y++) {
			for (int x=0; x<tw; x++) {
			    boolean inside = circle.contains(x,y);
				if (inside) {
					iin=iin+ip.getPixel(x,y);
					nin++;
				} else {
					iout=iout+ip.getPixel(x,y);
					nout++;
				}
			}
		}
		//		IJ.log("stepSize="+stepSize+", xc="+xc+", yc="+yc);
		double aviout=iout/nout;
		double aviin=iin/nin;
		double di=aviin-aviout;
		if (di>=dimax) {
			xcmax=xc;
			ycmax=yc;
			dimax=di;			
		}
	    }
	}
	Point2D.Double p = new Point2D.Double(xcmax, ycmax);
	return p;
    }
}