module paliplatform.base {
	requires java.sql;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires org.apache.commons.io;
	requires org.apache.commons.csv;
	requires org.apache.commons.compress;
	requires org.tukaani.xz;
	exports paliplatform.base;
	uses paliplatform.base.SimpleService;
}
