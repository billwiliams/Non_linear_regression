####
# prototyping anomaly detection using python numpy and scipy 
####
import numpy as np
import matplotlib.pyplot as plt
from sklearn import linear_model
from scipy.optimize import  curve_fit
import scipy
import datetime
import matplotlib.dates as mdates


#loading  data from the arctan csv
data = np.genfromtxt('ArcTan_Data.csv',dtype=None, delimiter=',')
#time,course,speed,x(m),Y(m) data
X= data[1:,1:5].astype(float)
#bearing data
Y=data[1:,5].astype(float)
#data for the last leg
Ymin=Y[68:145]
#date data from the csv
date=data[1:,0]
#time in seconds
ti=np.linspace(0,145,145)

#variable to hold bi to be calculated
y_fit=np.zeros(Ymin.size)
#converting the string date data to python dates
for i in range (0,Y.size):
    
    date[i]=datetime.datetime.strptime(date[i], "%d/%m/%Y %H:%M:%S")


#converting the dates to numbers that we can use in computations
dates=mdates.datestr2num(date)
#dates for the last leg
datesmin=dates[68:145]
#obtaining the time elapsed starting from zero then dividing by 100000 to normalize it for curve fitting
#since  fitting doesnt work well with large numbers
for i in range (0,Y.size):
     diff=datetime.timedelta(dates[i]-dates[0])
     ti[i]=diff.seconds
     ti[i]=ti[i]/10000


#Arctan function used to fit a curve to the data
def func2(x,B,P,Q):
    return np.degrees(np.arctan2(np.sin(np.radians(B))+P*x,np.cos(np.radians(B))+Q*x))

#finding the fitting parameters to use .ti(for the last leg) 
#fitpars2 are the fitting parameters obtained by the curve_fit using equation func2
#covmat2 are the convariant matrix
#maxfev= number of times we minimize the values.the larger the better
# The diagonals of the covariance matrix are variances
fitpars2, covmat2 = curve_fit(func2,ti[68:145],Ymin,maxfev=100000000)



#plotting dates against bearing for the last leg
plt.figure(1)
plt.plot(datesmin,Ymin,'.',color='red')

#finding the bearing bi using the fitting parameters obtained by curve_fit equation.y_fit=bi    
y_fit=func2(ti[68:145], fitpars2[0],fitpars2[1],fitpars2[2])
#plotting bi calculated for the last leg against dates
plt.plot(datesmin,y_fit,'r-',color='green')
locs, labels = plt.xticks()
plt.setp(labels, rotation=45)
#obtaining the sum of the squared errors for the leg
wholelegerros=np.sum((Ymin-y_fit)**2)



#showing the plots
plt.show()







