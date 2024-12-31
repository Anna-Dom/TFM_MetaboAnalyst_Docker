# Protocol 4: Sample Size Estimation and Power Analysis

In statistics, power is defined as the probability of detecting an effect, when the effect exists. the main challenge is how to make a reasonable estimate of the effect size they expect to see in their data. One common approach is to use data sets from a pilot study or from closely related samples obtained from public data repositories. If the pilot data sets have similar characteristics as the experiment to be conducted, the predictions should work well. This approach has been implemented in MetaboAnalyst.

In this protocol the data `human_cachezia.csv` will be used, this dataset contains metabolite concentration data from a pilot urine metabolomics study on cancer patients, group 1 is cachexic and group 2 the control.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	**Module Selection**. Click on “Power Analysis”
3. **Data Upload**. Select the "Concentrations" radio button, the option "Samples in rows (unpaired)" and upload the CSV file. Click "Submit".
4. **Data Integrity Check**. The page indicates that the data has passed all the data checks. Click "Proceed".
5. **Normalization**. Choose "Normalization by sum" to adjust for potential dilution effects (as it is a urine sample), select "Log transformation" and leave "None" in data scaling. Click "Normalize", check the results with "View Result" and click "Proceed".
6. **Power Analysis**. The page shows 4 diagnosic plots to assess if the data fits the underlying model. Click "Submit".

    - Now the power analysis results are shown. By default, the result shows the predicted powers for up to 200 smaples per group.
    - The graph has not reached a plateu yet, change the maximum sample size. Enter 800 as the maximum sample size per group, leave the FDR cutoff as 0.1, and click the “Submit” button.
    - From the graph, it can be concluded that with around 320 samples per group, the study will have enough power (0.8) based on this pilot data set.
    
7.	**Data Download**. Click the “Download” hyperlink on the bottom of navigation bar. Here you can download all the elements produced in the analysis by clicking in "Donwload.zip", save it. Click "Logout" to complete the session.

## Re-use the R History file to reproduce the analysis

The R History file used in this part of the protocol can be the one you have downloaded from the previous analysis or you can use the one located in this folder called `Rhistory_P5.R`.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	Click on “Re-use R Script” to move to the “Data Upload” page
3.	Upload the R History file into the first box and the file `human_cachezia.csv` into the second box. Click "Submit"
4.	Data Download. All the analyses have been performed and you have been redirected to the Download page directly where all the resources are available to download.

If you wish, you can perfrom further analysis with the already normalized and updated data by expanding side bar menu. The new analysis perfromed will be attached after the analysis in the R History script uploaded.
