module paliplatform.base {
	requires jdk.jsobject;
	requires java.sql;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires org.apache.commons.io;
	requires org.apache.commons.csv;
	requires org.apache.commons.compress;
	requires org.tukaani.xz;
	requires com.h2database;
	exports paliplatform.base;
	uses paliplatform.base.SimpleService;
}
