# Protocol 2: Multivariate exploratory data analysis

In metabolomics, univariate approaches are often considered suboptimal, as they ignore correlations that are known to be present among variables. Multivariate methods are generally considered more suitable for high-dimensional “omics” data analysis. This protocol will use:

- Unsupervised methods: PCA and hierarchical clustering
- Supervised methods: PLS-DA and RF

The data that will be used in this protocol is `cow_diet.csv` and can be found in this folder. This data set contains metabolite concentrations of 39 rumen samples measured by 1H NMR from dairy cows fed with different proportions of barley grain. There are four groups—0, 15, 30, and 45—indicating the percentage of grain in the diet.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	**Module Selection**. Click on “Statistical Analysis [One Factor]” to move to the “Data Upload” page
3. **Data Upload**. Under the "A plain text file (.txt or .csv):" menu select the "Concentrations" radio button, the option "Samples in rows (upaired)" and upload the CSV file. Click "Submit".
4. **Data Integrity Check**. The page indicates that the data has passed all the data checks. Click "Proceed".
5. **Normalization**. Select the “Normalization by a pooled average sample from group” and then click “Specify.” A dialog box will appear to select a reference group; ensure that group “0” is selected from the drop-down menu, and click "Submit". Leave the Data transformation set to “none” and select “Auto scaling” for Data scaling. Click the “Normalize” and “View Result”. Click "Proceed". 




## Re-use the R History file to reproduce the analysis

The R History file used in this part of the protocol can be the one you have downloaded from the previous analysis or you can use the one located in this folder called `Rhistory_P2.R`.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	Click on “Re-use R Script” to move to the “Data Upload” page
3.	Upload the R History file into the first box and the file `cow_diet.csv` into the second box. Click "Submit"
4.	Data Download. All the analyses have been performed and you have been redirected to the Download page directly where all the resources are available to download.

If you wish, you can perfrom further analysis with the already normalized and updated data by expanding the "Statistics" menu. The new analysis perfromed will be attached after the analysis in the R History script uploaded.
