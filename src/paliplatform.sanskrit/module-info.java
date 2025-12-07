module paliplatform.sanskrit {
	requires javafx.base;
	requires javafx.controls;
	requires paliplatform.base;
	uses paliplatform.base.SimpleService;
	provides javafx.css.Styleable with
		paliplatform.sanskrit.SanskritMenu;
	provides paliplatform.base.SimpleService with 
		paliplatform.sanskrit.FontSetter;
}
