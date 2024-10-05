module paliplatform.dpd {
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires com.google.gson;
	requires paliplatform.base;
	opens paliplatform.dpd to javafx.base;
	uses paliplatform.base.SimpleService;
	provides javafx.css.Styleable with
		paliplatform.dpd.DpdMenu,
		paliplatform.dpd.DpdToolBarCom;
	provides paliplatform.base.DpdService with 
		paliplatform.dpd.DpdServiceImp;
}
