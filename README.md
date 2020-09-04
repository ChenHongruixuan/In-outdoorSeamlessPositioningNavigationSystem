# In-outdoorSeamlessPositioningNavigationSystem


## Description


## Technical Difficulty
### Indoor Positioning
#### WIFI Positioning
In online location, the server receives and matches the WiFi data sent by the client with the data in the fingerprint database, and uses the corresponding W-K-NN algorithm to match the positioning results and return them to the client.

In fact, due to the complexity of the indoor scene, the signal strength value of the same point will be affected by many indoor interference factors, so the WiFi location result will jump in a time series. Although the w-k-nn algorithm can reduce the beating degree of the positioning result to a certain extent, the jump can not be ignored. Therefore, the system uses Kalman filter to smooth the positioning results, and improves the beating of WiFi Positioning.

## Illustration

