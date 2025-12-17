module paliplatform.dict {
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires com.google.gson;
	requires paliplatform.base;
	opens paliplatform.dict to javafx.web;
	uses paliplatform.base.SimpleService;
	exports paliplatform.dict to
		paliplatform.grammar,
		paliplatform.sentence,
		paliplatform.reader,
		javafx.graphics;
	provides paliplatform.base.SimpleService with 
		paliplatform.dict.DictWinLauncher,
		paliplatform.dict.FontSetter;
	provides paliplatform.base.DictService with 
		paliplatform.dict.DictServiceImp;
	provides javafx.css.Styleable with 
		paliplatform.dict.DictMenu,
		paliplatform.dict.DictToolBarCom,
		paliplatform.dict.DictSelectorBox;
}
