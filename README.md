# Stage-Drift-Correction
A set of utility programs for assisting stage drift correction by rigid body transformation in ImageJ.

"back2Origin_RigidBody.java"

 - An ImageJ plugin that applies "rigid-body" transformation to an image stack with reference to the Results Table listing the parameters. The Results Table should have rows as many as the number of the images in the active image stack and four columns "C1", "C2", "C3", and "C4", which represent the frame number, translation amount in x axis, translation amount in y axis, and rotation angle in radians, respectively, to be applied for the corresponding slice of the image stack. Currently the plugin uses bicubic interpolation for treating subpixel values.
  
  
"fiducial_Chaser.java"

 - An ImageJ plugin that tracks the displacement of fiducial markers in every slice of an image stack. When we encounter a stage drift problem, it is often the case that the shapes of the objects are continuously changing except a few "landmark" or fiducial markers which are supposed stationary to the stage. If we can isolate each of these fiducial markers in a rectangular ROI throughout the image stack, the plugin output the positions of the centre of the marker in the whole image in each slice of the image stack by adding two new columns to the current Results Table (–– so we first need to reset the Results Table by closing before starting a new analysis). We need to repetitively run the plugin so that we can collect the data of at least three fiducial markers (but we actually need more to make it work). The collected list of the marker positions could be used to calculate the parameters of the rigid body transformation in "rigidBody.py".
 - The plugin uses trial and error iteration algorithm to maximise the difference of the average intensities between the inside and the outside of a circle ROI the radius of which is specified by the user.


"rigidBody.py"

 - A python script that reads the "Results.xls" that was produced by "fiducial_Chaser.java" and saved as tab-limited text file and calculates the parameters of rigid body transformation to be directly referred in "back2Origin_RigidBody.java".
 
 -----------------------------------------------------------------------------------
 
 "landmark2Table.py"
 
 - A python script to be used for "alternative" correction method using TurboReg. It flattens the file format of "landmarks.txt" to re-feed TurboReg through an ImageJ macro "TurboRegBatchRigidBody" to produce "TableLWM.txt".
 
 
 "TurboRegBatchRigidBody.ijm"
 
 - An imageJ macro that interfaces batch transformation of the image stack with the recorded landmarks ("TableLWM.txt").
