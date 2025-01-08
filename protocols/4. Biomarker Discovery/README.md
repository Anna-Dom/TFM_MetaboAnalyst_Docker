# Protocol 4: Biomarker Analysis

These types of studies manly focus on identifying metabolite biomarkers for disease diagnosis, predictive markers, prognostic markers, and markers of exposure. MetaboAnalyst offers a ROC curve based approach for biomarker analysis.

The data that will be used in this protocol is `plasma_nmr.csv` and can be found in this folder. This data set contains metabolite concentrations of 90 human plasma samples (60 healthy pregnant controls and 30 pregnant patients with pre-eclampsia) measured by NMR.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	**Module Selection**. Click on “Biomarker Analysis” to move to the “Data Upload” page
3. **Data Upload**. Under the "From your local file" menu select the "Concentrations" radio button, the option "Samples in rows" and upload the CSV file. Click "Submit".
4. **Data Integrity Check**. The page indicates that the data has passed all the data checks. Click "Proceed".
5. **Normalization**. Keep "None" in Normalization, check "Compute and include metabolite ratios" set to Top 20, select "Log transformation" for data transformation and select "Auto scaling" for data scaling. Click "Normalize" and "Proceed"
6. **Classical univariate ROC curve analysis**. This will perform a ROC-curve analysis for each feature in the dataset and shows the AUC. To view the corresponding ROC curve for each row click "View" or "Details". The confidence interval is computed using 500 bootstrap replications.
7.	**Data Download**. Click the “Download” hyperlink on the bottom of navigation bar. Here you can download all the elements produced in the analysis by clicking in "Donwload.zip", save it. Click "Logout" to complete the session.


## Re-use the R History file to reproduce the analysis

The R History file used in this part of the protocol can be the one you have downloaded from the previous analysis or you can use the one located in this folder called `Rhistory_P4.R`.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	Click on “Re-use R Script” to move to the “Data Upload” page
3.	Upload the R History file into the first box and the file `plasma_nmr.csv` into the second box. Click "Submit"
4.	Data Download. All the analyses have been performed and you have been redirected to the Download page directly where all the resources are available to download.

If you wish, you can perfrom further analysis with the already normalized and updated data by expanding side bar menu. The new analysis perfromed will be attached after the analysis in the R History script uploaded.
