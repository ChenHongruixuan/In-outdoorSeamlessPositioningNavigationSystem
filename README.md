# IndoorOutdoorSeamlessPositioningNavigationSystem


## Description
We developed this indoor and outdoor seamless positioning and navigation system under the Android system by using the built-in multi-sensors of mobile phones.

## Algorithm
### Indoor Positioning
#### WIFI Positioning
In online location, the server receives and matches the WiFi data sent by the client with the data in the fingerprint database, and uses the corresponding W-K-NN algorithm to match the positioning result and return them to the client.
In fact, due to the complexity of the indoor scene, the signal strength value of the same point will be affected by many indoor interference factors, so the WiFi positioning result would jump in a time series. Though the W-K-NN algorithm can reduce the beating degree of the positioning result to a certain extent, the jump can not be ignored. Therefore, the system uses Kalman filter to smooth the positioning results, and eliminates the beating of WiFi Positioning.

#### PDR Algorithm
The system obtains the data of the gravity sensor and orientation sensor, uses the combination of crossing-peak and crossing-valley step frequency detection algorithm, adaptive step size algorithm, and orientation detection algorithm to obtain the position increment of each step, so as to complete the positioning. Compared with the conventional PDR algorithm, it has higher accuracy and better reliability.

#### NFC Positioning
By writing the position of some indoor key points and areas with instable WiFi signals into the NFC tag and paste them in the corresponding position. When the user enters the indoor area and reaches the position with the tag, the device can be pasted near the tag to read the position information stored in the tag and corrected position. Compared with WiFi positioning, NFC tag is used for position correction with high accuracy.

#### Indoor Map
<div align="center">
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/Client/室内地图.png" height=25% width=25% >
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/Client/室内地图格式.png" height=25% width=25% >
</div>
The indoor map is stored in the disk of the server in the form of custom XML file, and there is corresponding index in the database. When the client receives the indoor map, the parsing module parses the content of the map, instantiates the indoor map object after parsing, and the drawing module draws the indoor map according to the object rendering.

## Illustration
### Client


### Data Acquisition Tool
<div align="center">
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/DataAT/1.png" height=18% width=18% >
 
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/DataAT/2.png" height=18% width=18% >
 
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/DataAT/3.png" height=18% width=18% >
 
<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/DataAT/4.png" height=18% width=18% >

<img src="https://github.com/I-Hope-Peace/In-outdoorSeamlessPositioningNavigationSystem/blob/master/ScreenShots/DataAT/6.png" height=18% width=18% >
 </div>
<p align="center">Left to right: main, magnetic field data acquisition, acceleration data acquisition, WiFi data acquisition, NFC</p>

## Q & A
For any questions, please do not hesitate to contact me (Qschrx@gmail.com).
