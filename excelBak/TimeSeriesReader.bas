Attribute VB_Name = "TimeSeriesReader"
Public Sub LoadTimeSeries()

  '
  ' Open file dialog and ask user for a JSON file
  '
  Dim fileDialog As Office.fileDialog
  Set fileDialog = Application.fileDialog(msoFileDialogFilePicker)
  fileDialog.AllowMultiSelect = False
  fileDialog.Title = "Select TimeSeries.JSON file"
  fileDialog.Filters.Add "JSON", "*.json"
  fileDialog.Filters.Add "All", "*.*"

  '
  ' Quit here is user push Cancel
  '
  If fileDialog.Show = False Then
    Exit Sub
  End If

  '
  ' Capture the user selected file and read as text
  '
  fileName = fileDialog.SelectedItems(1)

  Dim FileSystemObject As New FileSystemObject
  Dim jsonTextStream As TextStream
  Set jsonTextStream = FileSystemObject.OpenTextFile(fileName, ForReading)
  jsonText = jsonTextStream.ReadAll
  jsonTextStream.Close

  '
  ' Parse the text string into a JSON object
  '
  Set timeSeriesList = ParseJson(jsonText)

  '
  ' Loop over all TimeSeries and put each in a separate sheet
  '
  sheetNo = 1
  For Each timeSeries In timeSeriesList

    ' Clear sheet content
    Sheets(sheetNo).Cells.Delete

    ' Use log name as the sheet name
    timeSeriesName = "TimeSeries"
    If Not IsNull(timeSeries.Item("header")) And Not IsNull(timeSeries.Item("header")("name")) Then
      timeSeriesName = timeSeries.Item("header")("name")
    End If

    Sheets(sheetNo).Name = timeSeriesName

    ' Loop over the signal definitions and populate signal name
    ' (row 1) and unit (row 2). If multi-dimensional append _n
    ' to the curve name
    Set signalDefinitions = timeSeries.Item("signals")

    columnNo = 1
    For Each signalDefinition In signalDefinitions
      nDimensions = signalDefinition.Item("dimensions")
      If IsNull(nDimensions) Then
        nDimensions = 1
      End If
      signalName = signalDefinition.Item("name")
      unit = signalDefinition.Item("unit")

      columnName = signalName

      For dimension = 1 To nDimensions
        If nDimensions > 1 Then
          columnName = signalName & "_" & dimension
        End If

        Sheets(sheetNo).Cells(1, columnNo).Value = columnName
        Sheets(sheetNo).Cells(2, columnNo).Value = unit

        columnNo = columnNo + 1
      Next dimension
    Next

    ' Loop over the data rows and populate sheet rows accordingly
    Set dataRows = timeSeries.Item("data")
    rowNo = 3
    For Each dataRow In dataRows
       columnNo = 1
       For Each signalValue In dataRow
         If TypeOf signalValue Is Collection Then
           For Each signalSubValue In signalValue
             Sheets(sheetNo).Cells(rowNo, columnNo).Value = signalSubValue
             columnNo = columnNo + 1
           Next
         Else
           Sheets(sheetNo).Cells(rowNo, columnNo).Value = signalValue
           columnNo = columnNo + 1
         End If
       Next
       rowNo = rowNo + 1
    Next

    sheetNo = sheetNo + 1
  Next
End Sub
