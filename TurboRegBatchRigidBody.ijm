macro "TurboRegBatchRigidBody" {
      current=getTitle();
      w=getWidth();
      h=getHeight();
      nFrames=nSlices;
      newImage("Registered", "32-bit", w, h, nFrames);
      setBatchMode(true);
      for (frame=1; frame<=nFrames; frame++) {
      	  selectWindow(current);
      	  setSlice(frame);
     	   run("Duplicate...", "title=temp");
     	    lmsx1=getResult("C1", frame-1);
     	    lmsy1=getResult("C2", frame-1);
     	    lmsx2=getResult("C3", frame-1);
      	    lmsy2=getResult("C4", frame-1);
	    lmsx3=getResult("C5", frame-1);
	    lmsy3=getResult("C6", frame-1);
	    run("TurboReg ", "-transform -window temp 512 512 -rigidBody "+lmsx1+" "+lmsy1+" 256.0 256.0 "+lmsx2+" "+lmsy2+" 256.0 80.0 "+lmsx3+" "+lmsy3+" 256.0 432.0 -showOutput");
	    selectWindow("temp");
	    close();
	    selectWindow("Output");
	    run("Select All");
	    run("Copy");
	    selectWindow("Registered");
	    setSlice(frame);
	    run("Paste");
	    selectWindow("Output");
	    close();
	}
	setBatchMode(false);
}
