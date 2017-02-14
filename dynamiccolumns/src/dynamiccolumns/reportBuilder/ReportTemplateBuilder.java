package dynamiccolumns.reportBuilder;

import java.awt.Color;
import java.util.ArrayList;

import dynamiccolumns.reportBuilder.model.ReportColumn;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;

public class ReportTemplateBuilder {

	private HorizontalTextAlignEnum columnHeaderHorizontalAlign = HorizontalTextAlignEnum.CENTER;
	private VerticalTextAlignEnum columnHeaderVerticalAlign = VerticalTextAlignEnum.MIDDLE;
	private final String FONT_NAME = "Report";
	private final int REPORT_PADDING = 20;
	private final int MIN_A4_PAGE_WIDTH = 595;
	private final int MIN_A4_PAGE_HEIGHT = 842;
	private int rowHeight = 25;
	private int columnWidth = 100;
	private int columnHeaderHeight = 25;
	private int columnDetailHeight = 20;
	private int maxColumnLevel = 1;
	
	private Color headerBackColor = new Color(20, 123, 196);
	private Color headerForeColor = Color.white;
	private boolean isNeedToColorDetailFieldsByValue = false;
	
	private int x = 0, y = 0;
	
	private Color lineColor = new Color(125, 190, 227); /*new Color(0,0,0); */
	private ArrayList<ReportColumn> columns = new ArrayList<>(0);
	private String listName = "Лист";
	private JasperDesign jd = new JasperDesign();
	private int numberOfColumns = 0;
	private int reportWidth = 0;
	private int reportHeight = 0;
	private int reportTitleHeight = 30;
	private VerticalTextAlignEnum columnDetailVerticalAlign = VerticalTextAlignEnum.MIDDLE;
	private HorizontalTextAlignEnum columnDetailHorizontalAlign = HorizontalTextAlignEnum.CENTER;

	public ReportTemplateBuilder(ArrayList<ReportColumn> columns, String listName, int maxColumnLevel) {
		this.columns = columns;
		this.listName = listName;
		this.maxColumnLevel = maxColumnLevel;
	}
	
	public JasperDesign generateReportTemplate() throws JRException {
		jd.setName(listName);
		jd.setColumnSpacing(0);
		jd.setLeftMargin(REPORT_PADDING);
		jd.setRightMargin(REPORT_PADDING);
		jd.setTopMargin(REPORT_PADDING);
		jd.setBottomMargin(REPORT_PADDING);
		jd.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
		
		buildReportMeat();
		
		setReportSize();
		
		return jd;
	}

	private void buildReportMeat() throws JRException {
		JRDesignStyle reportHeaderStyle = updateStyleBox(
				buildReportStyle("reportHeaderStyle", true, 14f, HorizontalTextAlignEnum.CENTER), lineColor, 0f);
		jd.addStyle(reportHeaderStyle);
		
		addReportParameter("ReportName", java.lang.String.class);
		
		JRDesignBand columnHeadersBand = new JRDesignBand();
		JRDesignBand columnDetailsBand = new JRDesignBand();
		
		JRDesignTextField reportHeader = buildTextField(reportHeaderStyle, "$P{ReportName}", VerticalTextAlignEnum.MIDDLE, HorizontalTextAlignEnum.CENTER, true, 14f);
		columnHeadersBand.addElement(reportHeader);
		reportHeader.setHeight(reportTitleHeight);
		reportHeader.setX(0); reportHeader.setY(0);
		
		y += reportTitleHeight;
		
		buildColumnHeaders(columnHeadersBand);
		reportWidth = x; //set report width by summary headers width
		columnHeadersBand.setHeight(reportTitleHeight + columnHeaderHeight * maxColumnLevel); 
		columnHeadersBand.setSplitType(SplitTypeEnum.STRETCH);
		reportHeight += reportTitleHeight + columnHeaderHeight * maxColumnLevel;
		
		JRDesignStyle columnDetailStyle = updateStyleBox(buildReportStyle("columnDetail", false, 12f, columnHeaderHorizontalAlign), lineColor, 1f);
		jd.addStyle(columnDetailStyle);
		
		x = 0; //start build details from left to right
		buildColumnDetails(columnDetailsBand, columns, columnDetailStyle);
		columnDetailsBand.setHeight(columnDetailHeight);
		
		jd.setColumnHeader(columnHeadersBand);
		((JRDesignSection) jd.getDetailSection()).addBand(columnDetailsBand);
		
		//after all meat set reportHeader width and jd.PageWidth
		reportHeader.setWidth(reportWidth);
		jd.setPageWidth(reportWidth);
		
	}

	private void buildColumnHeaders(JRDesignBand columnHeadersBand) throws JRException {
		x = 0;
		
		JRDesignStyle columnHeaderStyle = updateStyleBox(buildReportStyle("columnHeader", false, 12f, columnHeaderHorizontalAlign), lineColor, 1f);
		
		columnHeaderStyle.setMode(ModeEnum.OPAQUE); //without it element will never be painted

		columnHeaderStyle.setBackcolor(headerBackColor);
		columnHeaderStyle.setForecolor(headerForeColor);
		jd.addStyle(columnHeaderStyle);
		
		for (ReportColumn column : columns) {
			if (column.getLevel() == 1) {
				
				JRDesignTextField colHeader = buildTextField(columnHeaderStyle, "\"" + (column.getName().isEmpty() ? " " : column.getName()) + "\"", columnHeaderVerticalAlign, columnHeaderHorizontalAlign, true, column.getFontSize());
				
				if (column.getChildren().isEmpty()) {
					int columnCustomWidth = column.getColumnWidth();
					colHeader.setWidth(columnCustomWidth != 0 ? columnCustomWidth : columnWidth);
					colHeader.setHeight(columnHeaderHeight * maxColumnLevel);
					colHeader.setX(x);
					colHeader.setY(y);
					
					numberOfColumns++;
					
					x += (columnCustomWidth != 0 ? columnCustomWidth : columnWidth);
				} else {
					int resultColumnWidth = buildColumnHeaderChildren(column.getChildren(), columnHeadersBand, x, columnHeaderStyle);
					
					colHeader.setWidth(resultColumnWidth);
					colHeader.setHeight(columnHeaderHeight);
					colHeader.setX(x);
					colHeader.setY(y);
					
					x += resultColumnWidth;
				}
				
				columnHeadersBand.addElement(colHeader);
			}

		}
		
	}

	private int buildColumnHeaderChildren(ArrayList<ReportColumn> children, JRDesignBand columnHeadersBand, int rowX, JRDesignStyle columnHeaderStyle) {
		int width = 0; //summary children width
		
		for (ReportColumn column : children) {
			
			if (column.isHeaderRendered()) //do not render that is already rendered
				continue;
			
			JRDesignTextField colHeader = buildTextField(columnHeaderStyle, "\"" + (column.getName().isEmpty() ? " " : column.getName()) + "\"", columnHeaderVerticalAlign, columnHeaderHorizontalAlign, true, column.getFontSize());
			
			if (column.getChildren().isEmpty()) { //last level
				
				int columnCustomWidth = column.getColumnWidth();
				colHeader.setWidth(columnCustomWidth != 0 ? columnCustomWidth : columnWidth);
				
				colHeader.setHeight(columnHeaderHeight * (maxColumnLevel - column.getLevel() + 1));
				colHeader.setX(rowX + width);
				colHeader.setY(y + columnHeaderHeight * (column.getLevel() - 1) );
				
				numberOfColumns++;
				
				width += (columnCustomWidth != 0 ? columnCustomWidth : columnWidth);
			} else {
				int childrenSummaryWidth = buildColumnHeaderChildren(column.getChildren(), columnHeadersBand, x, columnHeaderStyle);
				
				colHeader.setWidth(childrenSummaryWidth);
				colHeader.setHeight(columnHeaderHeight);
				colHeader.setX(rowX + width);
				colHeader.setY(y + columnHeaderHeight * (column.getLevel() -1) );
				
				width += childrenSummaryWidth;
			}
			
			columnHeadersBand.addElement(colHeader);
			column.setHeaderRendered(true);
		}
		
		return width;
	}
	
	private void buildColumnDetails(JRDesignBand columnDetailsBand, ArrayList<ReportColumn> columns, JRDesignStyle columnDetailStyle) throws JRException {
		for (ReportColumn column : columns) {
			if (column.getChildren().isEmpty()) {
				
				String expressionName = " "; 
				
				JRDesignStyle style = columnDetailStyle;
				
				if (!column.getColumnDetailName().isEmpty()) {
					if (column.isDetailVariable()) {
						
						addReportField(column.getColumnDetailName(), column.getColumnDetailClass());											
						
						if (column.getOwnDetailStyle() != null) {
							style = column.getOwnDetailStyle();
							jd.addStyle(style);
						}
						
						expressionName = "$F{" + column.getColumnDetailName() + "}";
					} else {
						expressionName = "\"" + column.getColumnDetailName() + "\"";
					}
				}
				
				JRDesignTextField colDetail = buildTextField(style, expressionName, columnDetailVerticalAlign, columnDetailHorizontalAlign, true, column.getFontSize());
				colDetail.setMode(ModeEnum.OPAQUE);
				
				int resultColumnWidth = 0;
				if (column.getColumnWidth() != 0)
					resultColumnWidth = column.getColumnWidth();
				else 
					resultColumnWidth = columnWidth;
					
				colDetail.setY(0);
				colDetail.setX(x);
				colDetail.setWidth(resultColumnWidth);
				colDetail.setHeight(columnDetailHeight);
				
				x += resultColumnWidth;
				columnDetailsBand.addElement(colDetail);
			} else {
				buildColumnDetails(columnDetailsBand, column.getChildren(), columnDetailStyle);
			}
		}
	}

	private JRDesignTextField buildTextField(JRDesignStyle style, String expressionName, VerticalTextAlignEnum vALign, HorizontalTextAlignEnum hAlign, boolean stretchWithOverflow, float fontSize) {
		JRDesignTextField tField = new  JRDesignTextField();
		JRDesignExpression expr = new JRDesignExpression();
		expr.setText(expressionName);
		tField.setStyle(style);
		tField.setExpression(expr);
		tField.setStretchWithOverflow(stretchWithOverflow);
		tField.setPositionType(PositionTypeEnum.FLOAT);
		tField.setVerticalTextAlign(vALign);
		tField.setHorizontalTextAlign(hAlign);
		tField.setFontSize(fontSize);
		return tField;
	}

	public JRDesignField addReportField(String name, Class<?> classValue) throws JRException {
		JRDesignField field = new JRDesignField();
		field.setName(name);
		field.setValueClass(classValue);
		jd.addField(field);
		return field;
	}

	public void addReportParameter(String name, Class<?> classValue) throws JRException {
		JRDesignParameter reportName = new JRDesignParameter();
		reportName.setName(name);
		reportName.setValueClass(classValue);
		jd.addParameter(reportName);
	}

	private JRDesignStyle buildReportStyle(String name, boolean bold, float fontSize, HorizontalTextAlignEnum align) {
		JRDesignStyle style = new JRDesignStyle();
		style.setName(name);
		style.setBold(bold); 
		style.setBlankWhenNull(true); 
		style.setFontName(FONT_NAME);
		style.setFontSize(fontSize);
		style.setHorizontalTextAlign(align);
		return style;
	}
	
	private JRDesignStyle updateStyleBox(JRDesignStyle style, Color lineColor, float lineWidth) {
		style.getLineBox().getBottomPen().setLineColor(lineColor);
		style.getLineBox().getTopPen().setLineColor(lineColor);
		style.getLineBox().getLeftPen().setLineColor(lineColor);
		style.getLineBox().getRightPen().setLineColor(lineColor);
		style.getLineBox().getBottomPen().setLineWidth(lineWidth);
		style.getLineBox().getTopPen().setLineWidth(lineWidth);
		style.getLineBox().getLeftPen().setLineWidth(lineWidth);
		style.getLineBox().getRightPen().setLineWidth(lineWidth);
		return style;
	}

	private void setReportSize() {
		jd.setPageWidth(reportWidth < MIN_A4_PAGE_WIDTH ? MIN_A4_PAGE_WIDTH : reportWidth + REPORT_PADDING * 2);
		jd.setPageHeight(reportHeight < MIN_A4_PAGE_HEIGHT ? MIN_A4_PAGE_HEIGHT : reportHeight + REPORT_PADDING * 2); 
	}

	public ArrayList<ReportColumn> getColumns() {
		return columns;
	}

	public void setColumns(ArrayList<ReportColumn> columns) {
		this.columns = columns;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public int getColumnLevel() {
		return maxColumnLevel;
	}

	public void setColumnLevel(int columnLevel) {
		this.maxColumnLevel = columnLevel;
	}

	public int getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public int getColumnDetailHeight() {
		return columnDetailHeight;
	}

	public void setColumnDetailHeight(int columnDetailHeight) {
		this.columnDetailHeight = columnDetailHeight;
	}

	public Color getHeaderBackColor() {
		return headerBackColor;
	}

	public void setHeaderBackColor(Color headerBackColor) {
		this.headerBackColor = headerBackColor;
	}

	public Color getHeaderForeColor() {
		return headerForeColor;
	}

	public void setHeaderForeColor(Color headerForeColor) {
		this.headerForeColor = headerForeColor;
	}

	public boolean isNeedToColorDetailFieldsByValue() {
		return isNeedToColorDetailFieldsByValue;
	}

	public void setNeedToColorDetailFieldsByValue(boolean isNeedToColorDetailFieldsByValue) {
		this.isNeedToColorDetailFieldsByValue = isNeedToColorDetailFieldsByValue;
	}
}
