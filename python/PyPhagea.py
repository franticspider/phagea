"""
    Copyright (C) 2013-2015 Simon Hickinbotham, Hywl Williams, Susan Stepney
    
    This file is part of Phagea.

    Phagea is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Phagea is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Phagea.  If not, see <http://www.gnu.org/licenses/>.

"""


"""
#need to 
#      export JAVA_HOME=/usr/lib/jvm/default-java 
#before running
#import jpype
"""
#

from jpype import *# JPackage, startJVM, shutdownJVM

#TODO: change this path...
#classpath = "-Djava.class.path=/home/sjh/Desktop/sjh/phagea/workspace/PhagEA/bin/" 
#classpath = "-Djava.class.path=/home/sjh/workspace/PhagEApype/jar/" 

#Specify the path to the phagea java classes:
classpath = "-Djava.class.path=/home/sjh/git/phagea/src/"

#Specify the path to the jvm: 
jvmpath = "/usr/local/java/jdk1.8.0_25/jre/lib/amd64/server/libjvm.so"#"/usr/lib/jvm/default-java/jre/lib/i386/client/libjvm.so"
#classpath = "-Djava.class.path='.'" 
#"-ea -Djava.class.path=C:\\Documents and Settings\\Sydney\\Desktop\\jpypeTest\\"
startJVM(jvmpath,"-ea",classpath)

# test: access the basic java functions
java.lang.System.out.println("JPype has connected to the JVM successfully (I think)")

 
import matplotlib.pyplot as plt



##########################################################
# TESTING NETWORKX
import networkx as nx
import matplotlib.cm as cmx
import matplotlib.colors as colors



import numpy as np

from numpy.core.numeric import zeros
""" there's a sqrt error in plot - this can trap it:   """
#np.seterr(invalid='raise')



"""
print '======================================='
print '==== TRYING COMMENTED-OUT NKP CODE ===='

nkpc = JClass("com.phagea.nkpLandscape")
nkp = nkpc(8,3,0.0)

nodes = nkp.getNodes()
edges = nkp.getEdges()
score = nkp.getScores()

G=nx.Graph()

G.add_nodes_from(nodes)

b = np.array(edges)
for i in range(len(b)):
    for j in range(len(b[i])):
        if b[i][j] == 1:
            G.add_edge(i,j)
            

#Select the layout


pos=nx.spring_layout(G,iterations=2000)


#Get the colormap for the range of scores:
jet = cm = plt.get_cmap('jet') 
cNorm  = colors.Normalize(vmin=nkp.getScoreMin(), vmax=nkp.getScoreMax())
scalarMap = cmx.ScalarMappable(norm=cNorm, cmap=jet)

colorList = []
for i in range(len(score)):
    colorVal = scalarMap.to_rgba(score[i])
    colorList.append(colorVal)

#Do something with the node sizes
ss = np.array(score)
for i in range(len(ss)):
    ss[i] = 30 + (12*(1+ss[i]))

##nx.draw_networkx(G, pos=None, ax=None, node_size=[ss[v] for v in G])
#nx.draw_networkx(G, pos, with_labels=False,  ax=None, node_color = colorList, node_size =  10)

#plt.xlim(-0.05,1.05)
#plt.ylim(-0.05,1.05)
#plt.axis('off')
#plt.savefig('random_geometric_graph.png')
#plt.show()

"""
##########################################################

#TODO: Not sure if I need this:
#import sys

testPkg = JPackage("com").phagea
Test = testPkg.TestPype
Test.speak("hi")
t = Test()
print t.getString()

print '=============================='
print '====STARTING PHAGEA PROPER===='
print ''


""" Create class variables that we'll get the objects from """
cfc = JClass("com.phagea.phageaConfig")
plc = JClass("com.phagea.phageaLandscape")



"""
Let's define a function that runs a simulation and plots it, given only the config and the image name
"""

def plotsim(cf,outfn):
    
    #load the landscape
    pl = plc(cf)
    
    ndims = pl.getNdims();
    if ndims == 1:
        #this used to work, but no longer!
        #mylandscape = JArray(JFloat,1)(pl.get1DLandscape())
        #nowadays, we can do it like this:
        landscape = pl.get1DLandscape()
        #pl.get1DLandscape()
    else: 
        if ndims == 2:
            landscape = pl.get2DLandscape()
        else:
            print 'Ndims = ',ndims, ' ...no landscape created yet (in python)'
    
    #cf.setInitFit(pl.getNdims(),pl.getMaxValue(),pl.getMinValue())
    
    pec = JClass("com.phagea.phageaEngine")
    pe = pec(cf,pl)
    
    pe.print_stats(0)
    pe.runAlgorithm()
    
    #DO THE PLOTTING NOW:
    plt.figure()
    
    #Data for the subtitle:
    w = cf.getw()
    Rgamma = cf.getrGamma()
    Repsilon = cf.getrEpsilon()
    Theta = cf.getTheta()
    
    if ndims == 1:
        print "Preparing 1D plot"
    
        pst = 'W= %f'%cf.getw()+' Rgamma= %f'%cf.getrGamma()+' Repsilon= %f'%cf.getrEpsilon()+' Theta= %f'%cf.getTheta()
        plt.suptitle(pst)
    
        print 'Plotting Cell Histogram'
        plt.subplot(231)
        CellHist =  pe.getCellHist()
        plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')
        
        print 'Plotting Phage Histogram'
        plt.subplot(232)
        PhageHist = pe.getPhageHist()
        plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')
        
        print 'Plotting Landscape'
        plt.subplot(233)
        FitMap = pl.get1DLandscape()
        nbin = cf.getNbin()
        xx = zeros(nbin)
        print 'nbin= ',nbin,' FitMap size = ',len(FitMap)
        for i in range (0,nbin):
            xx[i] = i
        plt.plot(FitMap,xx,label = "fitness")
        
        print 'Plotting Cell Popdy'
        plt.subplot(234)
        cellpopdy = pe.getCellPopdy()
        plt.plot(cellpopdy)
        
        print 'Plotting Phage Popdy'
        plt.subplot(235)
        phagepopdy = pe.getPhagePopdy()
        plt.plot(phagepopdy)
        
        print 'Plotting Resource Dynamics'
        plt.subplot(236)
        rdy = pe.getRDy()
        plt.plot(rdy)
    
    elif ndims == 2:
        print "Preparing 2D plot"
        pst = 'w='+'{:.1e}'.format(w)+' Rgamma='+"{:.1e}".format(Rgamma)+'\n Repsilon='+"{:.1e}".format(Repsilon)+' theta='+"{:.1e}".format(Theta)
        plt.suptitle(pst)
    
    
        print 'Plotting Cell Histogram'
        plt.subplot(231)
        CellHist =  pe.getCellHist()
        plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')
        
        print 'Plotting Phage Histogram'
        plt.subplot(232)
        PhageHist = pe.getPhageHist()
        plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')
        
        print 'Plotting Landscape'
        plt.subplot(233)
        plt.imshow(landscape, origin = 'lower' ,aspect='auto', label='landscape')
        
        print 'Plotting Cell Popdy'
        plt.subplot(234)
        cellpopdy = pe.getCellPopdy()
        plt.plot(cellpopdy)
        
        print 'Plotting Phage Popdy'
        plt.subplot(235)
        phagepopdy = pe.getPhagePopdy()
        plt.plot(phagepopdy)
        
        print 'Plotting Resource Dynamics'
        plt.subplot(236)
        rdy = pe.getRDy()
        plt.plot(rdy)
    
    else:
        print "TODO: Prepare N-Dimensional plot"
        
    
    
    #newname = cfgfile.replace('.cfg', '.png')
    plt.savefig(outfn)    
    #newname = cfgfile.replace('.cfg', '.pdf')
    #plt.savefig(newname,format='pdf')    
    
    
    
    return










print '=================================='
print '==== STARTING NEW PHAGEA 2D EXPTS ===='

#Load the config
cfgfile = sys.argv[1]
cf = cfc(cfgfile)

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(False)
cf.setRescaling(False)
cf.setReplenish(False)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRFFF.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(False)
cf.setRescaling(False)
cf.setReplenish(True)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRFFT.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(False)
cf.setRescaling(True)
cf.setReplenish(False)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRFTF.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(False)
cf.setRescaling(True)
cf.setReplenish(True)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRFTT.png")

cf.setRandInit(True)
cf.setRescaling(False)
cf.setReplenish(False)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRTFF.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(True)
cf.setRescaling(False)
cf.setReplenish(True)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRTFT.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(True)
cf.setRescaling(True)
cf.setReplenish(False)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRTTF.png")

#CREATE THE DIFFERENT CONFIGURATIONS WE ARE INVESTIGATING
cf.setRandInit(True)
cf.setRescaling(True)
cf.setReplenish(True)
plotsim(cf,"/home/sjh/Desktop/Dropbox/Hywl/2015/runs/outfilenRTTT.png")








"""
#load the landscape
pl = plc(cf)

ndims = pl.getNdims();
if ndims == 1:
    #this used to work, but no longer!
    #mylandscape = JArray(JFloat,1)(pl.get1DLandscape())
    #nowadays, we can do it like this:
    landscape = pl.get1DLandscape()
    #pl.get1DLandscape()
else: 
    if ndims == 2:
        landscape = pl.get2DLandscape()
    else:
        print 'Ndims = ',ndims, ' ...no landscape created yet (in python)'

#cf.setInitFit(pl.getNdims(),pl.getMaxValue(),pl.getMinValue())

pec = JClass("com.phagea.phageaEngine")
pe = pec(cf,pl)

pe.print_stats(0)
pe.runAlgorithm()

#DO THE PLOTTING NOW:
plt.figure()

#Data for the subtitle:
w = cf.getw()
Rgamma = cf.getrGamma()
Repsilon = cf.getrEpsilon()
Theta = cf.getTheta()

if ndims == 1:
    print "Preparing 1D plot"

    pst = 'W= %f'%cf.getw()+' Rgamma= %f'%cf.getrGamma()+' Repsilon= %f'%cf.getrEpsilon()+' Theta= %f'%cf.getTheta()
    plt.suptitle(pst)

    print 'Plotting Cell Histogram'
    plt.subplot(231)
    CellHist =  pe.getCellHist()
    plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')
    
    print 'Plotting Phage Histogram'
    plt.subplot(232)
    PhageHist = pe.getPhageHist()
    plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')
    
    print 'Plotting Landscape'
    plt.subplot(233)
    FitMap = pl.get1DLandscape()
    nbin = cf.getNbin()
    xx = zeros(nbin)
    print 'nbin= ',nbin,' FitMap size = ',len(FitMap)
    for i in range (0,nbin):
        xx[i] = i
    plt.plot(FitMap,xx,label = "fitness")
    
    print 'Plotting Cell Popdy'
    plt.subplot(234)
    cellpopdy = pe.getCellPopdy()
    plt.plot(cellpopdy)
    
    print 'Plotting Phage Popdy'
    plt.subplot(235)
    phagepopdy = pe.getPhagePopdy()
    plt.plot(phagepopdy)
    
    print 'Plotting Resource Dynamics'
    plt.subplot(236)
    rdy = pe.getRDy()
    plt.plot(rdy)

elif ndims == 2:
    print "Preparing 2D plot"
    pst = 'w='+'{:.1e}'.format(w)+' Rgamma='+"{:.1e}".format(Rgamma)+'\n Repsilon='+"{:.1e}".format(Repsilon)+' theta='+"{:.1e}".format(Theta)
    plt.suptitle(pst)


    print 'Plotting Cell Histogram'
    plt.subplot(231)
    CellHist =  pe.getCellHist()
    plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')
    
    print 'Plotting Phage Histogram'
    plt.subplot(232)
    PhageHist = pe.getPhageHist()
    plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')
    
    print 'Plotting Landscape'
    plt.subplot(233)
    plt.imshow(landscape, origin = 'lower' ,aspect='auto', label='landscape')
    
    print 'Plotting Cell Popdy'
    plt.subplot(234)
    cellpopdy = pe.getCellPopdy()
    plt.plot(cellpopdy)
    
    print 'Plotting Phage Popdy'
    plt.subplot(235)
    phagepopdy = pe.getPhagePopdy()
    plt.plot(phagepopdy)
    
    print 'Plotting Resource Dynamics'
    plt.subplot(236)
    rdy = pe.getRDy()
    plt.plot(rdy)

else:
    print "TODO: Prepare N-Dimensional plot"
    


newname = cfgfile.replace('.cfg', '.png')
plt.savefig(newname)    
newname = cfgfile.replace('.cfg', '.pdf')
plt.savefig(newname,format='pdf')    

#plt.show()




shutdownJVM()











#==========================================================================================================================

print '=================================='
print '==== STARTING OLD PHAGEA 2D EXPTS ===='


#pl = plc("CONE")
#pl = plc("SOMBRERO")
#pl = plc("THREEHILL")
pl = plc("RASTRIGIN")

print 'landscape type = '+pl.getTypeString()

#        //javastuff:
#        phageaConfig config = new phageaConfig();
#        config.setStandardParams();
cf = cfc(pl.getNdims())


resc = cf.getRescaling()

#//java:
#phageaLandscape landscape = new phageaLandscape(config.type.name(), config.rescaling);

cf.setStandardParams()
cf.setPhageCount(0)

Sombland = JArray(JFloat,2)(pl.get2DLandscape(cf.getRescaling()))
import matplotlib.pyplot as plt

plt.figure(2)
plt.imshow(Sombland, origin = 'lower' ,aspect='auto', label='cell')
plt.show()


#########################################################

#from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import matplotlib.pyplot as plt
import numpy as np

bignum = 100
mat = Sombland #np.random.random((bignum, bignum))
X, Y = np.mgrid[:bignum, :bignum]

fig = plt.figure()
ax = fig.add_subplot(1,1,1, projection='3d')
surf = ax.plot_surface(X,Y,mat)
plt.show()

##########################################################


print '=================================='
print '==== FINISHED PHAGEA 2D EXPTS ===='


#Mimicing the java call sequence:
#        /** Load what we are doing from config */
#        phageaConfig config = new phageaConfig(args[0]);
#        String name = config.getTypeName();


cfgfile = sys.argv[1]
cf = cfc(cfgfile)
typen = cf.getTypeName()

#        
#        phageaLandscape landscape = new phageaLandscape(name);
#

#pl = plc(cf)
pl = plc(cf,2)

#            landscape.findNKPMaxMin();
pl.findNKPMaxMin();

# we'll need some variables for nkp 

G = []
pos = []
plotNKP = False


if typen in ['NKP']:
    if cf.getNKPN() < 14:
        plotNKP = True

ndims = pl.getNdims();
if ndims == 1:
    landscape = JArray(JFloat,1)(pl.get1DLandscape())
else: 
    if ndims == 2:
        landscape = JArray(JFloat,2)(pl.get2DLandscape())
    else:
        if plotNKP:
            landscape = JArray(JFloat,1)(pl.get1DnkpLandscape())

cf.setInitFit(pl.getNdims(),pl.getMaxValue(),pl.getMinValue())

pec = JClass("com.phagea.phageaEngine")
pe = pec(cf,pl)
#pe.print_stats(0)
pe.runAlgorithm()

print 'Replenished ' , pe.getReplenCount() , 'times'

#=======================



if plotNKP:
    
    nodes = JArray(JInt,1)(pl.getNKPNodes())
    score = JArray(JFloat,1)(pl.getNKPScores())
    edges = JArray(JInt,2)(pl.getNKPEdges())
    
    G=nx.Graph()
    
    G.add_nodes_from(nodes)
    
    b = np.array(edges)
    for i in range(len(b)):
        for j in range(len(b[i])):
            if b[i][j] == 1:
                G.add_edge(i,j)
                
    #import math
    #kval = 1.5/math.sqrt(len(nodes))
    pos=nx.spring_layout(G)#,k=kval) 

#so far, we can't get the graphviz layouts for high N to work...
    #import pygraphviz
    #from networkx import graphviz_layout
    #print "networkx version "+nx.__version__
    
    #pos=nx.graphviz_layout(G,"sfdp")










w = cf.getw()
Rgamma = cf.getrGamma()
Repsilon = cf.getrEpsilon()
Theta = cf.getTheta()

if(ndims <=2):
    CellHist =  JArray(JInt,2)(pe.getCellHist())
    PhageHist = JArray(JInt,2)(pe.getPhageHist())
    
if(plotNKP):
    CellHist =  JArray(JInt,2)(pe.getCellHist())
    PhageHist = JArray(JInt,2)(pe.getPhageHist())


#PhageHist = pe.getPhageHist()
#cpRatioHist = pe.getcpRatioHist()



#nbin = cf.getNbin()
#xx = zeros(nbin)
#for i in range (0,nbin):
#    xx[i] = i

#FitMap = pe.getFitMap()

cellpopdy = JArray(JInt,1)(pe.getCellPopdy())
phagepopdy = JArray(JInt,1)(pe.getPhagePopdy())
rdy = JArray(JFloat,1)(pe.getRDy())

#Reprint the configuration for checking
print("")
print("=====================================")
print("CONFIGURATION:")
cfgstr = cf.printConfig()
print cfgstr
print("=====================================")
print("")

plt.figure(2)

mynodesize = 40

###CREATE THE GRAPHIC FOR THE HOST EVOLUTION OVER THE LANDSCAPE###
#pst = 'w='+'{:.1e}'.format(w)+' Rgamma='+"{:.1e}".format(Rgamma)+'\n Repsilon='+"{:.1e}".format(Repsilon)+' theta='+"{:.1e}".format(Theta)
pst = 'N= %d'%cf.getNKPN()+' K= %d'%cf.getNKPK()+' P='+"%0.2f"%cf.getNKPP()+'\n  sigma^2='+"{:.1e}".format(cf.getSigma2())
plt.suptitle(pst)
plt.subplot(231)
if plotNKP:
    #plt.ylim(0,pl.getNKPdim())
    #plt.ylim(0,pl.getNKPdim())
    ###try to draw the network
    jet = cm = plt.get_cmap('jet') 
    cNorm  = colors.Normalize(vmin=0, vmax=cf.getT())
    scalarMap = cmx.ScalarMappable(norm=cNorm, cmap=jet)
    colorListh = []
    sizeListh = []
    for i in range(len(CellHist)):
        colorVal = scalarMap.to_rgba(CellHist[i][0])
        colorListh.append(colorVal)
        sizeVal = 2 * mynodesize * CellHist[i][0] / cf.getT()
        sizeListh.append(sizeVal)
    nx.draw_networkx(G, pos, with_labels=False,  linewidths=0, ax=None, node_color = colorListh, node_size = sizeListh)
    
    plt.xlim(-0.05,1.05)
    plt.ylim(-0.05,1.05)
    plt.axis('off')
else:
    if(ndims <= 2):
        plt.imshow(CellHist, origin = 'lower' ,aspect='auto', label='cell')

plt.subplot(232)



if plotNKP:
    #plt.ylim(0,pl.getNKPdim())
    ###try to draw the network
    jet = cm = plt.get_cmap('jet') 
    cNorm  = colors.Normalize(vmin=0, vmax=cf.getT())
    scalarMap = cmx.ScalarMappable(norm=cNorm, cmap=jet)
    colorListp = []
    sizeListp = []
    for i in range(len(score)):
        colorVal = scalarMap.to_rgba(PhageHist[i][0])
        colorListp.append(colorVal)
        sizeVal = 2 * mynodesize * PhageHist[i][0] / cf.getT()
        sizeListp.append(sizeVal)
    nx.draw_networkx(G, pos, with_labels=False,  linewidths=0, ax=None, node_color = colorListp, node_size = sizeListp)
    
    plt.xlim(-0.05,1.05)
    plt.ylim(-0.05,1.05)
    plt.axis('off')
else:
    if(ndims <= 2):
        plt.imshow(PhageHist, origin = 'lower' ,aspect='auto', label='phage')

plt.subplot(233)


### PLOT THE LANDSCAPE - DIFFERENT STRATEGIES FOR DIFFERENT DIMENSIONS...
if ndims == 1:
    FitMap = JArray(JFloat,1)(pl.get1DLandscape())
    nbin = cf.getNbin()
    xx = zeros(nbin)
    print 'nbin= ',nbin,' FitMap size = ',len(FitMap)
    for i in range (0,nbin):
        xx[i] = i
    plt.plot(FitMap,xx,label = "fitness")
else:
    if ndims == 2:
        plt.imshow(landscape, origin = 'lower' ,aspect='auto', label='landscape')
    else:
        if plotNKP:
            ###Assume we have an NKP Landscape for now
            #nbin = len(landscape)
            #xx = zeros(nbin)
            #for i in range (0,nbin):
            #    xx[i] = i
                
            #plt.ylim(0,nbin)
            #plt.plot(landscape,xx,label = "fitness")
            
            
            ###try to draw the network
            jet = cm = plt.get_cmap('jet') 
            cNorm  = colors.Normalize(vmin=pl.getNKPScoreMin(), vmax=pl.getNKPScoreMax())
            scalarMap = cmx.ScalarMappable(norm=cNorm, cmap=jet)
            colorList = []
            sizeList = []
            for i in range(len(score)):
                colorVal = scalarMap.to_rgba(score[i])
                colorList.append(colorVal)
                sizeVal = 2 * mynodesize * (score[i]-min(score)) / (max(score)-min(score))
                sizeList.append(sizeVal)
            nx.draw_networkx(G, pos, with_labels=False, linewidths=0, ax=None, node_color = colorList, node_size = sizeList)
            
            plt.xlim(-0.05,1.05)
            plt.ylim(-0.05,1.05)
            plt.axis('off')
            #plt.savefig('random_geometric_graph.png')
            #plt.show()


#plt.imshow(landscape, origin = 'lower' ,aspect='auto', label='landscape')
#plt.imshow(cpRatioHist, origin = 'lower' ,aspect='auto', label='phage/cell ratio')

#plt.subplot(244)
#plt.plot(FitMap,xx,label = "fitness")

plt.subplot(234)
###To set the y axis range, uncomment this:
#plt.ylim((600,1600))

plt.plot(cellpopdy)

plt.subplot(235)
plt.plot(phagepopdy)

plt.subplot(236)
###uncomment this if you want to look at resource dynamics:
#plt.plot(rdy)

fmax = JArray(JFloat,1)(pe.getFMaxT())
fmin = JArray(JFloat,1)(pe.getFMinT())

plt.plot(fmax)
plt.plot(fmin)

if cf.getRescaling():
    obsmax = JArray(JFloat,1)(pe.getObsMaxT())
    obsmin = JArray(JFloat,1)(pe.getObsMinT())
    plt.plot(obsmax)
    plt.plot(obsmin)



#plt.savefig("fig.png")
#plt.plot(fmax)
#plt.plot(fmin)


plt.show()

"""


#shutdownJVM()

