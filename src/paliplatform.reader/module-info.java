module paliplatform.reader {
	requires jdk.jsobject;
	requires jdk.xml.dom;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires com.google.gson;
	requires paliplatform.base;
	requires paliplatform.grammar;
	requires paliplatform.dict;
	opens paliplatform.reader to javafx.web, javafx.base;
	exports paliplatform.reader to 
		javafx.graphics,
		paliplatform.lucene;
	uses paliplatform.base.SimpleService;
	provides paliplatform.base.SimpleService with 
		paliplatform.reader.FontSetter;
	provides paliplatform.base.ReaderService with 
		paliplatform.reader.ReaderServiceImp;
	provides javafx.css.Styleable with
		paliplatform.reader.ReaderMenu,
		paliplatform.reader.ReaderToolBarCom,
		paliplatform.reader.RootFinderMenuItem,
		paliplatform.reader.GramSutFinderMenuItem;
}
