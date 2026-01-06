module paliplatform.sanskrit {
	requires java.sql;
	requires jdk.jsobject;
	requires jdk.xml.dom;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires paliplatform.base;
	opens paliplatform.sanskrit to javafx.web;
	uses paliplatform.base.SimpleService;
	provides javafx.css.Styleable with
		paliplatform.sanskrit.SanskritMenu,
		paliplatform.sanskrit.SktToolBarCom,
		paliplatform.sanskrit.DictSelectorBox;
	provides paliplatform.base.SimpleService with 
		paliplatform.sanskrit.FontSetter;
	provides paliplatform.base.SktService with 
		paliplatform.sanskrit.SktServiceImp;
}
