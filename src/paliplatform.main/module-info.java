module paliplatform.main {
	requires java.sql;
	requires java.desktop;
	requires javafx.base;
	requires javafx.controls;
	requires paliplatform.base;
	exports paliplatform.main;
	uses javafx.css.Styleable;
	uses paliplatform.base.SimpleService;
	uses paliplatform.base.DictService;
	uses paliplatform.base.ReaderService;
	uses paliplatform.base.LuceneService;
	provides paliplatform.base.SimpleService with 
		paliplatform.main.EditorLauncher,
		paliplatform.main.DictSearch,
		paliplatform.main.DocFinderSearch,
		paliplatform.main.LuceneFinderSearch;
}
