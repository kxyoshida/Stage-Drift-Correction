# Last modified on 2017/11/07
#
#
from numpy import *
from scipy import *
from scipy import optimize

def rigidBody(cp):

    nframes = size(cp, axis=0)
    nCP = size(cp, axis=1)/2
    if nCP<3:
        print "Too few control points! Require 3 points at least!"

    X=zeros((nframes,nCP))
    Y=zeros((nframes,nCP))    
    for n in r_[0:nCP]:
        X[:,n]=cp[:,2*n]
        Y[:,n]=cp[:,2*n+1]        
             
    # x1 = cp[:,0]
    # y1 = cp[:,1]
    # x2 = cp[:,2]
    # y2 = cp[:,3]
    # x3 = cp[:,4]
    # y3 = cp[:,5]
    # x4 = cp[:,6]
    # y4 = cp[:,7]
    # x5 = cp[:,8]
    # y5 = cp[:,9]
    

    T = zeros((nframes,4))

    def fitfunc(p,x0,y0,x1,y1):
        ax1=p[0]
        axx=cos(p[2])
        axy=-sin(p[2])
        ay1=p[1]
        ayx=sin(p[2])
        ayy=cos(p[2])
        residual = 0
        for i in r_[1:size(x1)]:
            residual = residual + (x1[i]-(ax1+axx*x0[i]+axy*y0[i]))**2 + (y1[i]-(ay1+ayx*x0[i]+ayy*y0[i]))**2
        return residual
    
    for fr in r_[1:nframes]:
        p0=zeros(3)
        x0 = X[0,:]
        y0 = Y[0,:]
        x1 = X[fr,:]
        y1 = Y[fr,:]        
        res = optimize.minimize(fitfunc, p0[:], args=(x0,y0,x1,y1))
        
        T[fr,:] = r_[fr,res.x]

    return T

def main():
    cp=genfromtxt("Results.xls", skip_header=1)

    Ts = rigidBody(cp[:,1:])
    savetxt("RigidBody.txt",Ts, fmt='%i\t%10.5f\t%10.5f\t%10.5f')    
    #    savetxt("RigidBody.txt",Ts, fmt='%i\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f\t%10.5f')    
    
if __name__ == '__main__':
    main()

