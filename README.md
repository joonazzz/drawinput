# Drawinput - Handwriting recognition input for Android

This a project to demonstrate handwriting recognition input. Key technologies
used in this project:

1. RBF SVM from LIBSVM
2. Simple preprocessing
    * duplicate point removal
    * character data resampled to 12 points / char
    * normalization of data
3. Simple feature extraction
    * 4 features per point
    * normalized (x,y) of pen coordinates
    * sin(dx/dy) and cos(dx/dy) to estimate pen direction
4. SVM models trained with data from Unipen database
    * different models for numbers, small abc, big abc and special characters
    * 15 samples / char for numbers
    * 30 samples / char for abc and ABC models
    * X samples / char for special characters





