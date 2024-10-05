module paliplatform.sentence {
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires com.google.gson;
	requires paliplatform.base;
	requires paliplatform.grammar;
	requires paliplatform.dict;
	exports paliplatform.sentence to javafx.graphics;
	uses paliplatform.base.SimpleService;
	provides paliplatform.base.SimpleService with 
		paliplatform.sentence.ReaderLauncher;
	provides javafx.css.Styleable with
		paliplatform.sentence.SentenceMenu;
	opens paliplatform.sentence to javafx.base;
}
