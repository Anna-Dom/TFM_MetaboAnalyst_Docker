# Protocol 1: Identification of significant variables

In metabolomics, it is often assumed that most observed changes are a result of physiological variations and only a small proportion of these changes are associated with the experimental condition of interest. This protocol describes how to use MetaboAnalyst to perfrom three types of analyses:

1. Identifying variables that are significantly different among different conditions
2. Identifying variables that show particular patterns of change under different conditions
3. Identifying variables that are significantly associated with other known biomarkers or features of interests

The data that will be used in this protocol is `cow_diet.csv` and can be found in this folder. This data set contains metabolite concentrations of 39 rumen samples measured by 1H NMR from dairy cows fed with different proportions of barley grain. There are four groups—0, 15, 30, and 45—indicating the percentage of grain in the diet.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	**Module Selection**. Click on “Statistical Analysis [One Factor]” to move to the “Data Upload” page
3. **Data Upload**. Under the "A plain text file (.txt or .csv):" menu select the "Concentrations" radio button, the option "Samples in rows (upaired)" and upload the CSV file. Click "Submit".
4. **Data Integrity Check**. The page indicates that the data has passed all the data checks. Click "Proceed".
5. **Normalization**. Select the “Normalization by a pooled average sample from group” and then click “Specify.” A dialog box will appear to select a reference group; ensure that group “0” is selected from the drop-down menu, and click "Submit". Leave the Data transformation set to “none” and select “Auto scaling” for Data scaling. Click the “Normalize” and “View Result”. Click "Proceed". 

**Identifying variables that are significantly different among different conditions**

6. Click the "One-way Analysis of Variance (ANOVA)" the points highlighted in purple are the significant compounds selected based on the default p-value threshold (0.05). Click on one of the points to see a boxplot showing its concentration values in each group. The ANOVA results show that at least two of the groups differ significantly, but a post-hoc test needs to ber performed.
7. There are two small icons that appear near the top right corner of the ANOVA graph. Click on the multi-column table icon. Choosen the post-hoc test "cccc". This allows one to see the “Feature Details View” of the ANOVA result table. 
    - The table shows p-values, the -log10 of the p-values and the false discovery rate (FDR) djusted p-values (based on Benjamini–Hochberg procedure) 
    - The post-hoc results are reported in group pairs separated by semicolons to indicate that the corresponding groups are significantly different. For example, the post-hoc analysis for the compound 3-PP shows “0−15; 0−30; 0−45; 15−30; 15−45,” meaning that each group is significantly different from other groups except groups 30 and 45, based on the Fisher’s LSD.
8. Click on "View" in the 3-PP row to view boxplot summaries using both original and normalized concentrations, the plot shows a declining concentration of 3-PP with an increasing grain percentage in the diet. 

**Identification of variables showing a particular pattern of change**

9. Click the “PatternHunter” hyperlink on the navigation tree. This functionality allows to use a a pattern match function based on a predefined pattern or a user-defined pattern. Check the "a custom profile" in the radio buttons after “Define a pattern using.” and enter the pattern “3-2-1-3” from the drop-down list. This search for compounds exhibiting a concentration decrease in the first three groups (0%, 15%, 30% grain), followed by an increase in the last group (45% grain). Click "Submit"

    - The bar graph shows compounds positively correlated in light pink and negatively correlated in light blue. Notice that Butyrate is ranked at the top of the list. 

10. Click on the table icon on the top right corner of the plot to access the "Feature Details View" on the ranked list of important compounds identified by the PatternHunter. 
11. Click the "view" button on the "Butyrate" row to show the box plot summary of its concentration change, notice it fits the desired pattern.

**Identification of variables that significantly associate with a known biomarker**

12. Knowing that elevated levels of "Endotoxin" are associated with certain inflammatory responses, we will try to identify other metabolites significantly associated with this compound. 
13. Click "PatternHunter" again. Select the “Spearman rank correlation” as a distance measure. Select the radio button "a feature of interest", select "Endotoxin" from the drop-down menu. Click "Submit".
    - The plot shows that Alanine is most positively correlated with Endotoxin levels, while 3-PP is most negatively correlated with Endotoxin levels.


11.	**Data Download**. Click the “Download” hyperlink on the bottom of navigation bar. Here you can download all the elements produced in the analysis by clicking in "Donwload.zip", save it. Click "Logout" to complete the session.


## Re-use the R History file to reproduce the analysis

The R History file used in this part of the protocol can be the one you have downloaded from the previous analysis or you can use the one located in this folder called `Rhistory_P2.R`.

1.	Move to the Module Selection page by clicking “Click here to start”
2.	Click on “Re-use R Script” to move to the “Data Upload” page
3.	Upload the R History file into the first box and the file `cow_diet.csv` into the second box. Click "Submit"
4.	Data Download. All the analyses have been performed and you have been redirected to the Download page directly where all the resources are available to download.

If you wish, you can perfrom further analysis with the already normalized and updated data by expanding the "Statistics" menu. The new analysis perfromed will be attached after the analysis in the R History script uploaded.
