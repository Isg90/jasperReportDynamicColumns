package dynamiccolumns;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dynamiccolumns.reportBuilder.ReportTemplateBuilder;
import dynamiccolumns.reportBuilder.model.ReportColumn;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

public class TemplateChecker {
	public static void main(String[] args) throws JRException, FileNotFoundException, IOException {
		
		ReportColumn column1 = buildColumnWithDetails("", 1, 1, "par1", java.lang.String.class);
		column1.setColumnWidth(40);
		ReportColumn column2 = new ReportColumn("Fact", 1, 2);
		
		ReportColumn c2Child1 = new ReportColumn("‡‡‡", 2, 1);
		ReportColumn c2Child2 = buildColumnWithDetails("bbb", 2, 2, "par2", java.lang.Integer.class);
		column2.getChildren().add(c2Child1);
		column2.getChildren().add(c2Child2);
		
		ReportColumn columnWithDetails = buildColumnWithDetails("aaa1", 3, 1, "par3", java.lang.Integer.class);
		columnWithDetails.setColumnWidth(40);
		c2Child1.getChildren().add(columnWithDetails);
		c2Child1.getChildren().add(buildColumnWithDetails("aaa2", 3, 2, "par4", java.lang.Integer.class));
		columnWithDetails = buildColumnWithDetails("aaa3", 3, 3, "par5", java.lang.Integer.class);
		columnWithDetails.setColumnWidth(60);
		c2Child1.getChildren().add(columnWithDetails);
		c2Child1.getChildren().add(buildColumnWithDetails("aaa4", 3, 4, "par6", java.lang.Integer.class));
		
		ReportColumn column3 = buildColumnWithDetails("Date", 1, 3, "par7", java.lang.Integer.class);
		
		ReportColumn column4 = new ReportColumn("Region", 1, 4);

		column4.getChildren().add(buildColumnWithDetails("Region1", 2, 1, "par8", java.lang.Integer.class));
		column4.getChildren().add(buildColumnWithDetails("Region2", 2, 1, "par9", java.lang.Integer.class));
		
		ArrayList<ReportColumn> columns = new ArrayList<ReportColumn>();
		columns.add(column1);
		columns.add(column2);
		columns.add(column3);
		columns.add(column4);
		
		ReportTemplateBuilder tb = new ReportTemplateBuilder(columns, "newReport", 3);
		tb.setLineColor(Color.green);
		JasperDesign jd = tb.generateReportTemplate();
		
		JasperReport jasperReport = JasperCompileManager.compileReport(jd);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ReportName", "Report title");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, generateReportData());
//		updateReportColors(jasperPrint);
		
		try (FileOutputStream baos = new FileOutputStream("C:\\Users\\igolovaschenko\\Desktop\\templateTest.pdf")) {
				JRPdfExporter pdfExporter = new JRPdfExporter();
				pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
				pdfExporter.exportReport();
		}

		try (FileOutputStream baos = new FileOutputStream("C:\\Users\\igolovaschenko\\Desktop\\templateTest.xls")) {
			JRXlsExporter xlsExporter = new JRXlsExporter();
			xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
			SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
			configuration.setOnePagePerSheet(false);
			configuration.setWhitePageBackground(false);
			xlsExporter.setConfiguration(configuration);
			xlsExporter.exportReport();
		}
	}

	private static ReportColumn buildColumnWithDetails(String colName, int colLevel, int colHOrderNum, String detailName, Class<?> classValue) {
		ReportColumn childColumn = new ReportColumn(colName, colLevel, colHOrderNum);
		childColumn.setDetailVariable(true);
		childColumn.setColumnDetailNameAndClass(detailName, classValue);
		return childColumn;
	}
	
	private static JRMapCollectionDataSource generateReportData() {
		List<Map<String, ?>> collection = new ArrayList<Map<String, ?>>();
		Map<String, Object> m1;
		for (int i = 0; i < 5; i++) {
			m1 = new HashMap<>();
			m1.put("par1", "Rus" + i);
			m1.put("par2", 0 + i*3);
			m1.put("par3", 1 + i*3);
			m1.put("par4", 2 + i*3);
			m1.put("par5", 3 + i*3);
			m1.put("par6", 4 + i*3);
			m1.put("par7", 5 + i*3);
			m1.put("par8", 6 + i*3);
			m1.put("par9", 7 + i*3);
			
			collection.add(m1);
		}
		
		JRMapCollectionDataSource ds = new JRMapCollectionDataSource(collection);
		return ds;
	}
}
