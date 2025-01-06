# Open TimeSeries.JSON files in MS/Excel

With the macro available here, files in the JSON Well Log Format can be opened in Excel in two simple steps:


# Open JSON Well Log files in MS/Excel

With the macro available here, files in the JSON Well Log Format can
be opened in Excel in two simple steps:

<img hspace="20" width="1000" src="../www/images/excel3.png">


## Installation

Installing the JSON macro and adding the function to the Excel ribbon can
be done by the following simple procedure:

1. Save [JsonConverter.bas](JsonConverter.bas) and [JsonWellLogFormatReader.bas](JsonWellLogFormatReader.bas) to local disk

2. Open Microsoft Excel

The following few steps are necessary to make the macro available _across worksheets_:

1. Make sure the Developer tab is enabled (File -> Options -> Customize ribbons)



3. Select the **Developer** ribbon:

   <img hspace="40" width="601" src="../www/images/excel4.png">

4. Click the **Record Macro** button to open the _Record Macro_ dialog:

   <img hspace="40" width="601" src="../www/images/excel5.png">

5. For _Store macro in:_ select **Personal Macro Workbook** and click **OK**

Now we import the macro itself:

6. From the _Developer_ ribbon, select **Visual Basic** to open the _Visual Basic for Applications_ window:

   <img hspace="40" width="608" src="../www/images/excel6.png">

7. Select **Tools->References...** and tick **Microsoft Scripting Runtime**:

   <img hspace="40" width="608" src="../www/images/excel7.png">

8. Make sure the project explorer is visible by selecting **View->Project Explorer**:

   <img hspace="40" width="608" src="../www/images/excel8.png">

9. Expand the **VBAProject (PERSONAL.XLSB)** node in the Project Explorer:

   <img hspace="40" width="608" src="../www/images/excel9.png">

10. Right click the **Module1** entry and select **Import File...**:

    <img hspace="40" width="608" src="../www/images/excel10.png">

11. Select the **JsonConverter.bas** file and click **Open**

12. Right click the **Module1** entry a second time and select **Import File...**

13. This time, select the **JsonWellLogFormatReader.bas** file and click **Open**

14. Select **File->Save PERSONAL.XLSB** to save it all

    <img hspace="40" width="608" src="../www/images/excel11.png">

15. **Close** the Visual Basic for Application window


Now the JSON Well Log Format macro is installed and can be used, but we improve
the usability by putting it directly on a ribbon:

16. Right click the ribbon and select **Customize the Ribbon...** to open _Excel Options_:

    <img hspace="40" width="608" src="../www/images/excel12.png">

17. In the _Choose commands from_, select **Macros**:

    <img hspace="40" width="435" src="../www/images/excel13.png">

18. The function can be put in any ribbon, we will choose the _Data_ ribbon

19. Select the **Get External Data** group and click the **New Group** button to add a new group after the selected one:

    <img hspace="40" width="435" src="../www/images/excel14.png">

20. Click **Rename...** to give the new group a descriptive name, for instance "JSON", and click **OK**:

    <img hspace="40" width="535" src="../www/images/excel16.png">

21. Select the **PERSONAL.XLSB!LoadJsonWellLogFormat** macro on the left and click **Add >>>**:

    <img hspace="40" width="435" src="../www/images/excel17.png">

22. Select the **PERSONAL.XLSB!LoadJsonWellLogFormat** entry on the right and click **Rename...**:

    <img hspace="40" width="557" src="../www/images/excel18.png">

23. Give it a descriptive name like **Open Well Log**, choose an icon and click **OK**

24. Click **OK** to close the _Excel Options_ window

The macro is now installed and available from the _Data_ ribbon as shown in the
initial image.
