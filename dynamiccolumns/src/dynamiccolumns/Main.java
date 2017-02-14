package dynamiccolumns;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;

import net.sf.jasperreports.components.table.BaseColumn;
import net.sf.jasperreports.components.table.DesignCell;
import net.sf.jasperreports.components.table.StandardColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.components.table.WhenNoDataTypeTableEnum;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignDatasetRun;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.fill.JRTemplateElement;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

public class Main {
	private static final int REPORT_WIDTH = 555;
	public static final Color LINE_COLOR = new Color(240, 248, 255, 255);
	public static final Color TABLE_HEADER_BACK_COLOR = new Color(20, 123, 196, 255);
	
	private static final String[] RUNTIME_PAINTED_STYLES = {"columnHeader"};
	private static final int PAGE_WIDTH = 595;

	public static void main(String[] args) throws JRException {
		try {
			generateReport();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateReport() throws JRException, IOException {
		JasperDesign jasperDesign = createDesign();
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
//		JRDataSource jrDataSource = prepareDataSource();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startDate", new Date());
		params.put("endDate", new Date());
		params.put("ReportName", "No way");
		params.put("ReportData", generateReportData());
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, buildData());
//		updateReportColors(jasperPrint);
		
		try (FileOutputStream baos = new FileOutputStream("C:\\Users\\igolovaschenko\\Desktop\\dynamicReport.pdf")) {
				JRPdfExporter pdfExporter = new JRPdfExporter();
				pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
				pdfExporter.exportReport();
		}

		try (FileOutputStream baos = new FileOutputStream("C:\\Users\\igolovaschenko\\Desktop\\dynamicReport.xls")) {
			JRXlsExporter xlsExporter = new JRXlsExporter();
			xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
			SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
			configuration.setOnePagePerSheet(false);
			configuration.setWhitePageBackground(false);
			xlsExporter.setConfiguration(configuration);
			xlsExporter.exportReport();
		}
		
//		
//		JasperPrint jasperPrint = getJasperPrint(reportInput, false, pathToJasperReportFile);
//		JRRtfExporter rtfExporter = new JRRtfExporter();
//		rtfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//		rtfExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
//		rtfExporter.exportReport();
	}
	
	public static void updateReportColors(JasperPrint jasperPrint) {
		
		for(JRPrintPage page : jasperPrint.getPages()){
			for(JRPrintElement jrPrintElement : page.getElements()) {
				if (jrPrintElement instanceof JRTemplatePrintText) {
					JRTemplatePrintText textField = (JRTemplatePrintText) jrPrintElement;
					if (textField != null) {
						//cause there is no parameters in JR for inject, information about marks color we set into text hyperlinktooltip;  
						String markColor = ((JRTemplatePrintText)jrPrintElement).getHyperlinkTooltip();	
						//markColor will be null or Empty if color is not needed
						if (markColor != null && !markColor.isEmpty()) {
							int[] rgbColor = getColorArray(markColor);
							JRTemplateElement newTemplate = (JRTemplateElement) SerializationUtils.clone(textField.getTemplate());
							
							if (isStyleEqualsOneOfRuntimePaintedStyle(textField)) {
								newTemplate.setBackcolor(new Color(rgbColor[0],rgbColor[1],rgbColor[2]));
							}
							textField.setTemplate(newTemplate);
						}
					}
				}
			}
		}
	}
	
	private static int[] getColorArray(String color) {
		color = color.replace("rgb(", "").replace(")", "");
		return new int[] {
			Integer.parseInt(color.split(",")[0].trim()),
			Integer.parseInt(color.split(",")[1].trim()),
			Integer.parseInt(color.split(",")[2].trim())
		};
	}
	
	private static boolean isStyleEqualsOneOfRuntimePaintedStyle(JRTemplatePrintText textField) {
		boolean result = false;
		JRStyle style = textField.getStyle();
		if (style == null)
			return result;
		
		String styleName = textField.getStyle().getName();
		
		for (String runtimePaintedStyle : RUNTIME_PAINTED_STYLES) {
			if (styleName.contains(runtimePaintedStyle))
				return true;
		}
		
		return false;
	}
	
	private static Object generateReportData() {
		ArrayList<Data> collection = new ArrayList<Data>();
		for (int a = 0; a < 5; a++)
			collection.add(new Data("Rome is not wine"));
		
		JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(collection);
		return ds;
	}
	
	private static JRDataSource buildData(){
		List<Map<String, ?>> preparedData = new ArrayList<Map<String, ?>>();
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put("name", "First");
		map.put("value", 10);
		
		preparedData.add(map);
		map = new HashMap<String, Object>();
		map.put("name", "Second");
		map.put("value", 4);
		preparedData.add(map);
		return new JRMapCollectionDataSource(preparedData);
	}

	/**
	 * Здесь нам нужно развернуть данные, полученные из хранимой процедуры в
	 * список объектов с нужным количеством полей. В качестве примера мы просто
	 * создадим тестовые данные.
	 * 
	 * @return JRDataSource.
	 */
	private static JRDataSource prepareDataSource() {
		List<Map<String, ?>> preparedData = new ArrayList<Map<String, ?>>();
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put("name", "Первый");
		map.put("value", 10);
		// В реальности нужно будет добавлять необходимые поля, сколько нужно,
		// динамически, в зависимости от параметров и данных.
		preparedData.add(map);
		map = new HashMap<String, Object>();
		map.put("name", "Второй");
		map.put("value", 4);
		// В реальности нужно будет добавлять необходимые поля, сколько нужно,
		// динамически, в зависимости от параметров и данных.
		preparedData.add(map);
		return new JRMapCollectionDataSource(preparedData);
	}

	@SuppressWarnings("deprecation")
	public static JasperDesign createDesign() throws JRException {
//		// Эквивалентно StaticText в JasperStudio
//		JRDesignStaticText staticText = null;
//
//		// Эквивалентно TextField в JasperStudio
//		JRDesignTextField textField = null;
//
//		// Band. Details, Summary, Title и другие.
//		JRDesignBand band = null;
//
//		// Вычисляемое выражение. Для записи значений в JRDesignTextField.
//		JRDesignExpression expression = null;
//
//		// Для рисования линий.	
//		@SuppressWarnings("unused")
//		JRDesignLine line = null;
//
//		// Для добавления полей в отчёт.
		JRDesignField field = null;
//
//		// Можно создавать условные стили.
//		@SuppressWarnings("unused")
//		JRDesignConditionalStyle conditionalStyle = null;
//
//		// Рамка вокруг ячейки.
//		JRLineBox lineBox = null;
//
//		// Вычисляемое значение. Можно подсчитать, например сумму.
//		JRDesignVariable variable = null;
//
//		int x;
//		int y;
//		final int ROW_HEIGHT = 11;
//		final int COLUMN_WIDTH = 60;

		JasperDesign jasperDesign = new JasperDesign();
		jasperDesign.setName("dynamicColumns");
		
		jasperDesign.setColumnWidth(REPORT_WIDTH);
		jasperDesign.setColumnSpacing(0);
		jasperDesign.setLeftMargin(20);
		jasperDesign.setRightMargin(20);
		jasperDesign.setTopMargin(20);
		jasperDesign.setBottomMargin(20);
		
		//jasperDesign.setIgnorePagination(true);
		
		jasperDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
		
		JRDesignStyle defaultStyle = new JRDesignStyle();
		defaultStyle.setName("defaultStyle");
		defaultStyle.setBackcolor(TABLE_HEADER_BACK_COLOR);
		jasperDesign.addStyle(defaultStyle);

//		JRDesignStyle normalStyle = new JRDesignStyle();
//		normalStyle.setName("normal");
//		normalStyle.setDefault(true);
//		normalStyle.setFontName("Report");
//		normalStyle.setFontSize(8.5f);
//		lineBox = normalStyle.getLineBox();
//		lineBox.getTopPen().setLineWidth(0.5f);
//		lineBox.getRightPen().setLineWidth(0.5f);
//		lineBox.getBottomPen().setLineWidth(0.5f);
//		lineBox.getLeftPen().setLineWidth(0.5f);
//		jasperDesign.addStyle(normalStyle);
//
//		JRDesignStyle headerStyle = new JRDesignStyle();
//		headerStyle.setName("header");
//		headerStyle.setDefault(true);
//		headerStyle.setFontName("Report");
//		headerStyle.setFontSize(8.5f);
//		headerStyle.setBold(true);
//		lineBox = headerStyle.getLineBox();
//		lineBox.getTopPen().setLineWidth(0.5f);
//		lineBox.getRightPen().setLineWidth(0.5f);
//		lineBox.getBottomPen().setLineWidth(0.5f);
//		lineBox.getLeftPen().setLineWidth(0.5f);
//		jasperDesign.addStyle(headerStyle);
//
//		// Параметры отчёта
//		JRDesignParameter startDateParameter = new JRDesignParameter();
//		startDateParameter.setName("startDate");
//		startDateParameter.setValueClass(java.util.Date.class);
//		jasperDesign.addParameter(startDateParameter);
//
//		JRDesignParameter endDateParameter = new JRDesignParameter();
//		endDateParameter.setName("endDate");
//		endDateParameter.setValueClass(java.util.Date.class);
//		jasperDesign.addParameter(endDateParameter);

//		// Поля отчёта.
		field = new JRDesignField();
		field.setName("name");
		field.setValueClass(java.lang.String.class);
		jasperDesign.addField(field);
//
//		field = new JRDesignField();
//		field.setName("value");
//		field.setValueClass(java.lang.Integer.class);
//		jasperDesign.addField(field);
//		// В случае отчёта с динамическими полями пробегаемся по количеству
//		// полей и добавляем JRDesignField для каждого с уникальным именем.
//
//		// Подсчитываем сумму
//		variable = new JRDesignVariable();
//		variable.setResetType(ResetTypeEnum.REPORT);
//		variable.setValueClass(java.lang.Integer.class);
//		expression = new JRDesignExpression();
//		expression.setText("$F{value}");
//		variable.setExpression(expression);
//		variable.setCalculation(CalculationEnum.SUM);
//		expression = new JRDesignExpression();
//		expression.setText("0");
//		variable.setInitialValueExpression(expression);
//		variable.setName("summary");
//		jasperDesign.addVariable(variable);

		// Title band
		JRDesignBand band = new JRDesignBand();
		// добавляем нужные элементы в band.
		// Можно добавлять JRDesignTextField-ы и JRDesignStaticField-ы,
		// картинки и всё, что угодно. Мы пропустим для простоты.
		JRDesignStyle reportHeaderStyle = new JRDesignStyle();
		reportHeaderStyle.setName("reportHeaderStyle");
		reportHeaderStyle.setBold(true); 
		reportHeaderStyle.setBlankWhenNull(true); 
		reportHeaderStyle.setFontName("Report"); 
		reportHeaderStyle.setFontSize(14f);
		reportHeaderStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		
		JRDesignStyle tableColumnHeaderStyle = new JRDesignStyle();
		tableColumnHeaderStyle.setName("columnHeader");
		tableColumnHeaderStyle.setBold(true); 
		tableColumnHeaderStyle.setBlankWhenNull(true); 
		tableColumnHeaderStyle.setFontName("Report"); 
		tableColumnHeaderStyle.setFontSize(14f);
		tableColumnHeaderStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		tableColumnHeaderStyle.setBackcolor(TABLE_HEADER_BACK_COLOR);
		JRLineBox headerlineBox = tableColumnHeaderStyle.getLineBox();
		headerlineBox.getBottomPen().setLineWidth(1f);
		headerlineBox.getTopPen().setLineWidth(1f);
		headerlineBox.getRightPen().setLineWidth(1f);
		headerlineBox.getLeftPen().setLineWidth(1f);
		headerlineBox.getTopPen().setLineColor(LINE_COLOR);
		headerlineBox.getBottomPen().setLineColor(LINE_COLOR);
		headerlineBox.getLeftPen().setLineColor(LINE_COLOR);
		headerlineBox.getRightPen().setLineColor(LINE_COLOR);
		tableColumnHeaderStyle.setMode(ModeEnum.OPAQUE);
		
		
		jasperDesign.addStyle(tableColumnHeaderStyle);
		
		JRLineBox reportHeaderStyleBox = reportHeaderStyle.getLineBox();
		reportHeaderStyleBox.getBottomPen().setLineWidth(0f);
		reportHeaderStyleBox.getTopPen().setLineWidth(0f);
		reportHeaderStyleBox.getLeftPen().setLineWidth(0f);
		reportHeaderStyleBox.getRightPen().setLineWidth(0f);
		jasperDesign.addStyle(reportHeaderStyle);
		
		JRDesignParameter reportName = new JRDesignParameter();
		reportName.setName("ReportName");
		reportName.setValueClass(java.lang.String.class);
		jasperDesign.addParameter(reportName);
		
		JRDesignTextField reportHeader = new  JRDesignTextField();
		JRDesignExpression reportHeaderExpr = new JRDesignExpression();
		reportHeaderExpr.setText("$P{ReportName}");
		reportHeader.setStyle(reportHeaderStyle);
		reportHeader.setExpression(reportHeaderExpr);
		reportHeader.setX(0);
		reportHeader.setY(0);
		reportHeader.setStretchWithOverflow(true);
		reportHeader.setPositionType(PositionTypeEnum.FLOAT);
		reportHeader.setWidth(REPORT_WIDTH);
		reportHeader.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
		reportHeader.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		reportHeader.setHeight(30);
		reportHeader.setY(0);
		reportHeader.setX(0);
//		band.addElement(reportHeader);
//		band.setHeight(30);
//		band.setSplitType(SplitTypeEnum.STRETCH);
//		jasperDesign.setTitle(band);
		
//		band = new JRDesignBand(); band.setHeight(1);
//		jasperDesign.setPageHeader(band);
		
		//Без таблиц-хуебиц
		JRDesignBand pageHeader = new JRDesignBand();
		pageHeader.setHeight(60);
		pageHeader.setSplitType(SplitTypeEnum.STRETCH);
		int x = 0, y = 0 + 30;
		JRDesignTextField col1Header = new JRDesignTextField();
		col1Header.setStyle(tableColumnHeaderStyle);
//		col1Header.setHyperlinkTooltipExpression(new JRDesignExpression("\"rgb(20, 123, 196)\""));
		col1Header.setForecolor(Color.WHITE);
		col1Header.setHeight(30);
		col1Header.setWidth(60);
		col1Header.setX(x); x += 60;
		col1Header.setY(y); y += 30;
		col1Header.setExpression(new JRDesignExpression("\"col1Name\""));
		col1Header.setStretchWithOverflow(true);
		col1Header.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);

		pageHeader.addElement(reportHeader);
		pageHeader.addElement(col1Header);

		
		
		jasperDesign.setColumnHeader(pageHeader);
		
		JRDesignBand detail = new JRDesignBand();
		x=0; y=0;
		JRDesignTextField col1Detail = new JRDesignTextField();
		col1Detail.setForecolor(Color.BLACK);
		col1Detail.setHeight(30);
		col1Detail.setWidth(60);
		JRDesignExpression exp = new JRDesignExpression("$F{name}");
		col1Detail.setExpression(exp);
		detail.setHeight(30);
		detail.addElement(col1Detail);
		
		((JRDesignSection) jasperDesign.getDetailSection()).addBand(detail);
	
		
		// эквивалент А4
		
		jasperDesign.setPageWidth(595);
		jasperDesign.setPageHeight(842);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//Таблица и колонки
		band = new JRDesignBand();
		StandardTable table = new StandardTable();
		
		table.setWhenNoDataType(WhenNoDataTypeTableEnum.ALL_SECTIONS_NO_DETAIL);
		
		JRDesignDatasetRun designDataSetRun = new JRDesignDatasetRun();
		JRDesignExpression dataSetExpr = new JRDesignExpression();
		dataSetExpr.setText("$P{ReportData}");
		
		designDataSetRun.setDatasetName("ReportData");
		designDataSetRun.setDataSourceExpression(dataSetExpr);
		table.setDatasetRun(designDataSetRun);
		
		
		StandardColumn c1 = new StandardColumn();
		c1.setWidth(60);
		
		
		DesignCell c1Header = new DesignCell();
		c1Header.setHeight(30);
		
		JRDesignTextField c1HeaderText = new JRDesignTextField();
		c1HeaderText.setWidth(60);
		c1HeaderText.setHeight(30);
		JRDesignExpression expression1 = new JRDesignExpression();
		expression1.setText("\"Кол1\"");
		c1HeaderText.setExpression(expression1);
		c1HeaderText.setStyle(tableColumnHeaderStyle);
		c1HeaderText.setHyperlinkTooltipExpression(new JRDesignExpression("\"rgb(20, 123, 196)\""));
		c1HeaderText.getStyle().setForecolor(Color.GRAY);
		c1HeaderText.getStyle().setBackcolor(TABLE_HEADER_BACK_COLOR);
		c1Header.addElement(c1HeaderText);
		
		c1Header.setStyle(defaultStyle);
		c1Header.getLineBox().getBottomPen().setLineColor(LINE_COLOR);
		c1Header.getLineBox().getTopPen().setLineColor(LINE_COLOR);
		c1Header.getLineBox().getLeftPen().setLineColor(LINE_COLOR);
		c1Header.getLineBox().getRightPen().setLineColor(LINE_COLOR);
		
		c1.setColumnHeader(c1Header);
		c1.setTableHeader(c1Header);
		c1.getTableHeader().getStyle().setBackcolor(TABLE_HEADER_BACK_COLOR);
		c1.getTableHeader().getStyle().setBackcolor(TABLE_HEADER_BACK_COLOR);
		c1.getColumnHeader().getStyle().setBackcolor(TABLE_HEADER_BACK_COLOR);
		
		DesignCell c1Footer = new DesignCell();
		c1Footer.setHeight(0);
		c1.setColumnFooter(c1Footer);
		
		DesignCell c1Detail = new DesignCell();
		c1Detail.setHeight(30);

		
		JRDesignTextField c1DetailTextField = new JRDesignTextField();
		c1DetailTextField.setStretchWithOverflow(true);
		c1DetailTextField.setHeight(30);
		c1DetailTextField.setWidth(60);
		c1DetailTextField.setPrintWhenDetailOverflows(true);
		JRDesignExpression expression = new JRDesignExpression();
		expression.setText("$F{name}");
		c1DetailTextField.setExpression(expression);
		c1DetailTextField.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);

		c1Detail.addElement(c1DetailTextField);

		c1.setDetailCell(c1Detail);
		
		List<BaseColumn> columns = new ArrayList<BaseColumn>();
		columns.add(c1);
		
		table.setColumns(columns);

		
		JRDesignDataset designDataSet = new JRDesignDataset(false);
		designDataSet.setName("ReportData");
		JRDesignField fieldName = new JRDesignField();
		fieldName.setName("name");
		fieldName.setValueClass(java.lang.String.class);
		designDataSet.addField(fieldName);
		jasperDesign.addDataset(designDataSet);
		
		
		JRDesignParameter dataSetParam = new JRDesignParameter();
		dataSetParam.setName("ReportData");
		dataSetParam.setValueClass(net.sf.jasperreports.engine.data.JRBeanCollectionDataSource.class);
		jasperDesign.addParameter(dataSetParam);
		

		
		JRDesignComponentElement cElem = new JRDesignComponentElement();
		cElem.setStyle(tableColumnHeaderStyle);
		cElem.setKey("MainTable");
		cElem.setWidth(REPORT_WIDTH);
		cElem.setHeight(70);
		cElem.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
		cElem.setComponent(table);
		cElem.setComponentKey(new ComponentKey("http://jasperreports.sourceforge.net/jasperreports/components", "jr", "table"));
		
		band.setSplitType(SplitTypeEnum.STRETCH);
		band.setHeight(80);
		band.addElement(cElem);
		
//		((JRDesignSection) jasperDesign.getDetailSection()).addBand(band);
		
//		c1Header.addElement(element);
//		c1.setWidth(50); c1.setColumnHeader(header);
//		StandardColumnGroup g = new StandardColumnGroup();
//		g.setWidth(60);
//		StandardColumn g2 = new StandardColumn();
//		g2.setWidth(30);
//		g2.setDetailCell(detail);
//		DesignCell gHeader = new DesignCell();
//		g2.setColumnHeader();
//		StandardColumn g3 = new StandardColumn();
//		g3.setWidth(30);
//		g.addGroupHeader(new );
//		table.addColumn(column);
		
		
		
		// Заголовки колонок.
//		x = 0;
//		y = 0;
//		band = new JRDesignBand();
//		band.setHeight(ROW_HEIGHT);
//		staticText = new JRDesignStaticText();
//		staticText.setX(x);
//		staticText.setY(y);
//		staticText.setWidth(COLUMN_WIDTH);
//		staticText.setHeight(ROW_HEIGHT);
//		staticText.setStyle(headerStyle);
//		staticText.setText("Название");
//		band.addElement(staticText);
//		x += staticText.getWidth();
//
//		staticText = new JRDesignStaticText();
//		staticText.setX(x);
//		staticText.setY(y);
//		staticText.setWidth(COLUMN_WIDTH);
//		staticText.setHeight(ROW_HEIGHT);
//		staticText.setStyle(headerStyle);
//		staticText.setText("Значение");
//		band.addElement(staticText);
//		x += staticText.getWidth();
//		jasperDesign.setColumnHeader(band);
//
//		// Detail band (данные)
//		band = new JRDesignBand();
//		band.setHeight(ROW_HEIGHT);
//		x = 0;
//		y = 0;
//		textField = new JRDesignTextField();
//		textField.setX(x);
//		textField.setY(y);
//		textField.setWidth(COLUMN_WIDTH);
//		textField.setHeight(ROW_HEIGHT);
//		expression = new JRDesignExpression();
//		expression.setText("$F{name}");
//		textField.setExpression(expression);
//		textField.setStyle(normalStyle);
//		band.addElement(textField);
//		x += textField.getWidth();
//
//		textField = new JRDesignTextField();
//		textField.setX(x);
//		textField.setY(y);
//		textField.setWidth(COLUMN_WIDTH);
//		textField.setHeight(ROW_HEIGHT);
//		expression = new JRDesignExpression();
//		expression.setText("$F{value}");
//		textField.setExpression(expression);
//		textField.setStyle(normalStyle);
//		band.addElement(textField);
//		x += textField.getWidth();
//		// DetailsBand добавляется немного странно, да...
//		((JRDesignSection) jasperDesign.getDetailSection()).addBand(band);

//		// Column footer
//		band = new JRDesignBand();
//		jasperDesign.setColumnFooter(band);
//
//		// Подвал страницы
//		band = new JRDesignBand();
//		jasperDesign.setPageFooter(band);
//
//		// Summary band
//		band = new JRDesignBand();
//		band.setHeight(ROW_HEIGHT);
//		x = 0;
//		y = 0;
//
//		staticText = new JRDesignStaticText();
//		staticText.setX(x);
//		staticText.setY(y);
//		staticText.setWidth(COLUMN_WIDTH);
//		staticText.setHeight(ROW_HEIGHT);
//		staticText.setStyle(headerStyle);
//		staticText.setText("ИТОГО:");
//		band.addElement(staticText);
//		x += staticText.getWidth();
//
//		textField = new JRDesignTextField();
//		textField.setX(x);
//		textField.setY(y);
//		textField.setWidth(COLUMN_WIDTH);
//		textField.setHeight(ROW_HEIGHT);
//		expression = new JRDesignExpression();
//		expression.setText("$V{summary}");
//		textField.setExpression(expression);
//		textField.setStyle(headerStyle);
//		band.addElement(textField);
//		x += textField.getWidth();
//		jasperDesign.setSummary(band);
		return jasperDesign;
	}
	
	public static class Data {
		private String name;
		
		public Data(){}
		public Data(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
