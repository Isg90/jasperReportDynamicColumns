package dynamiccolumns.reportBuilder.model;

import java.awt.Color;
import java.util.ArrayList;

public class ReportColumn {
	
	private String name;
	private Color headerBackColor = new Color(20, 123, 196, 255);
	private Color headerForeColor = new Color(255, 255, 255, 255);
	private Color detailBackColor = new Color(255, 255, 255, 255);
	private Color detailForeColor = new Color(0, 0, 0, 255);
	private boolean isHeaderRendered = false; //for header rendering process
	private int horizontalOrderNumber;
	private int level;
	private float fontSize = 12f;
	private int columnWidth; //works only for column without children
	private boolean isDetailVariable = false;
	private String columnDetailName = "";
	private Class<?> columnDetailClass;

	private ArrayList<ReportColumn> children = new ArrayList<ReportColumn>(0);
	
	public ReportColumn(String name, int level, int horizontalOrderNumber){
		checkInputParam(name, level, horizontalOrderNumber);
		this.name = name;
		this.horizontalOrderNumber = horizontalOrderNumber;
		this.level = level;
	}
	
	public ReportColumn(String name, int level, int horizontalOrderNumber, float fontSize){
		checkInputParam(name, level, horizontalOrderNumber);
		this.name = name;
		this.horizontalOrderNumber = horizontalOrderNumber;
		this.level = level;
		this.fontSize = fontSize;
	}

	private void checkInputParam(String name, int level, int horizontalOrderNumber) {
		if (horizontalOrderNumber < 1)
			throw new RuntimeException("Order can not be less than 1");
		if (level < 1)
			throw new RuntimeException("Level can not be less than 1");
		if (name == null)
			throw new RuntimeException("Name can not be null");
	}

	public ArrayList<ReportColumn> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<ReportColumn> children) {
		this.children = children;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	public Color getDetailBackColor() {
		return detailBackColor;
	}

	public void setDetailBackColor(Color detailBackColor) {
		this.detailBackColor = detailBackColor;
	}

	public Color getDetailForeColor() {
		return detailForeColor;
	}

	public void setDetailForeColor(Color detailForeColor) {
		this.detailForeColor = detailForeColor;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getHorizontalOrderNumber() {
		return horizontalOrderNumber;
	}

	public void setHorizontalOrderNumber(int horizontalOrderNumber) {
		this.horizontalOrderNumber = horizontalOrderNumber;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public int getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public String getColumnDetailName() {
		return columnDetailName;
	}

	public void setColumnDetailNameAndClass(String columnDetailName, Class<?> classValue) {
		this.columnDetailName = columnDetailName;
		this.columnDetailClass = classValue;
	}

	public boolean isHeaderRendered() {
		return isHeaderRendered;
	}

	public void setHeaderRendered(boolean isHeaderRendered) {
		this.isHeaderRendered = isHeaderRendered;
	}

	public boolean isDetailVariable() {
		return isDetailVariable;
	}

	public void setDetailVariable(boolean isDetailVariable) {
		this.isDetailVariable = isDetailVariable;
	}

	public Class<?> getColumnDetailClass() {
		return columnDetailClass;
	}
}
