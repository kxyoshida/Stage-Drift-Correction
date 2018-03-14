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


public class back2Origin_RigidBody implements PlugInFilter {
    // "RigidBody.txt" should be calculated for control points
    //  using a python script "rigidBody.py".

    ImagePlus imp;
    //    static String RTSource = "work/RigidBody.txt";
    public int setup(String arg, ImagePlus imp) {
	this.imp = imp;
	return DOES_8G + DOES_16 + DOES_32;
    }


    public void run(ImageProcessor ip) {
	int w = ip.getWidth();
	int h = ip.getHeight();

	String title = imp.getShortTitle();
	title = "rb-"+title;
	ImageStack stack = imp.getStack();
	int nSlices = stack.getSize();

	ResultsTable rt = ResultsTable.getResultsTable();
	if (rt == null) {
	    IJ.error("Can't open Results Table.");
	    return;
	}
	int nResults = rt.getCounter();
	double ax1[] = new double[nResults];
	double ay1[] = new double[nResults];
	double th[] = new double[nResults];
	
	for (int i = 0; i < nResults; i++) {
	    int fr = (int)rt.getValue("C1",i);
	    ax1[i] = (double)rt.getValue("C2", i);
	    ay1[i]= (double)rt.getValue("C3", i);
	    th[i]= (double)rt.getValue("C4", i);

	}

	FloatProcessor mip=new FloatProcessor(w,h);
	ImageStack mis=new ImageStack(w,h);
	ip = stack.getProcessor(1).convertToFloat();
	mis.addSlice(title+"_fr"+1, ip.duplicate());
	
	for (int i=1;i<nSlices;i++) {
	    IJ.showProgress(i,nSlices);
	    //	    imp.setSlice(i+1);
	    //	    ip = imp.getProcessor();
	    //	    ImageProcessor wip=ip.crop();

	    ip = stack.getProcessor(i+1).convertToFloat();
	    double mpx = 0;
	    // Sweep the coordinates of the destination image after affine transformation.
	    for (int y=0;y<h;y++) {
		for (int x=0;x<w;x++) {
		    double mx = ax1[i]+x*Math.cos(th[i])-y*Math.sin(th[i]);
		    double my = ay1[i]+x*Math.sin(th[i])+y*Math.cos(th[i]);
		    if (mx>=0 && mx<w && my>=0 && my<h) {
			mpx=ip.getBicubicInterpolatedPixel(mx,my,ip);
		    }
		    mip.putPixelValue(x,y,mpx);
		}
	    }
	    mis.addSlice(title+"_fr"+(i+1), mip.duplicate());
	}
	ImagePlus mimp=new ImagePlus(title, mis);
	mimp.show();
    }
}