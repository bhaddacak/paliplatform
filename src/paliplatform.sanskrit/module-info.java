module paliplatform.sanskrit {
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires paliplatform.base;
	opens paliplatform.sanskrit to javafx.web;
	uses paliplatform.base.SimpleService;
	provides javafx.css.Styleable with
		paliplatform.sanskrit.SanskritMenu,
		paliplatform.sanskrit.DictSelectorBox;
	provides paliplatform.base.SimpleService with 
		paliplatform.sanskrit.FontSetter;
}
