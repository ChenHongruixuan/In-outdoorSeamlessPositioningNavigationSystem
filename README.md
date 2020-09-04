# In-outdoorSeamlessPositioningNavigationSystem


## Description


## Technical Difficulty
### Indoor Positioning
#### WIFI Positioning
In online location, the server receives and matches the WiFi data sent by the client with the data in the fingerprint database, and uses the corresponding W-K-NN algorithm to match the positioning result and return them to the client.

In fact, due to the complexity of the indoor scene, the signal strength value of the same point will be affected by many indoor interference factors, so the WiFi positioning result would jump in a time series. Though the W-K-NN algorithm can reduce the beating degree of the positioning result to a certain extent, the jump can not be ignored. Therefore, the system uses Kalman filter to smooth the positioning results, and improves the beating of WiFi Positioning.

#### PDR Algorithm
The system obtains the data of the gravity sensor and orientation sensor, uses the combination of crossing-peak and crossing-valley step frequency detection algorithm, adaptive step size algorithm, and orientation detection algorithm to obtain the position increment of each step, so as to complete the positioning. Compared with the conventional PDR algorithm, it has higher accuracy and better reliability.

## Illustration

