# Protocol 1: Data uploading, processing, and normalization

MetaboAnalyst accepts multiple file types, pre-processed or not depending on the module to be used. This protocol describes how to upload multiple NMR peak list files to MetaboAnalyst for data processing, normalization, and data quality checking. The data that will be used comes from a NMR metabolomic study used to evaluate renal damage from urine samples, contains 50 peak list files, 25 from renal patients and 25 from healthy controls.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	**Module Selection**. Click on “Statistical Analysis [One Factor]” to move to the “Data Upload” page
3.	**Data Upload**. Under the “A compressed file (.zip):” menu, specify the “NMR peak list”. Click “+ Choose” and select the `nmr_peaks.zip` file present in this folder. Click “Submit”.
    - MetaboAnalyst has performed a peak list alignment, the result of this process can be seen in the next screen. Click “Proceed”.
5.	**Data Integrity Check**. This is performed to ensure that the data meets the basic requirements for the downstream analyses. The results indicate that 17.7% of the data values in this data set are missing, in this case no action will be taken. Click “Proceed” to continue. 
6.	**Low quality data filtering**. The purpose of this step is to remove noise or non-informative variables. Select “Interquantile Range (IQR)” as the mechanism to perform data filtering, click "Submit". Click “Proceed” to move to the next step.
7.	**Data Normalization**. Aims reduce systematic bias and to improve overall data consistency. Select “Normalization by sum” and “Auto scaling”, keep “None” for data transformation. Click “Normalize” to start the process. Once it has finished click “View results” to view a graphical summary of the effect of data normalization. Click “Proceed”.
8.	**Data Overview and Outlier detection**. Will use PCA to perform this step. Click “Principal Component Analysis (PCA)” under the title "Chemometrics Analysis", this should show the PCA results in mutli-tab panels. Click the “2D Scores Plot” tab to view the score plot between PC1 and PC2 and check “Display sample names” and click “Update”. Sample C004 might be an outlier, it will be removed from the dataset.
9.	**Data Editor**. In the left side bar, expand “Processing” moneu and click “Data Editor”. Move to the “Edit Samples” tab and locate the sample C004, select it and click the button with the right arrow (add) to move it to the exclude list. Click “Submit”. 
10. Perfrom steps 7 and 8 again to show that the sample has been removed.
11.	**Data Download**. Click the “Download” hyperlink on the bottom of navigation bar. Here you can download all the elements produced in the analysis by clicking in "Donwload.zip", save it. Click "Logout" to complete the session.

## Re-use the R History file to reproduce the analysis

The R History file used in this part of the protocol can be the one you have downloaded from the previous analysis or you can use the one located in this folder called `Rhistory_P1.R`.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	Click on “Re-use R Script” to move to the “Data Upload” page
3.	Upload the R History file into the first box and the file `nmr_peaks.zip` into the second box. Click "Submit"
4.	Data Download. All the analyses have been performed and you have been redirected to the Download page directly where all the resources are available to download.

If you wish, you can perfrom further analysis with the already normalized and updated data by expanding the "Statistics" menu. The new analysis perfromed will be attached after the analysis in the R History script uploaded.