module paliplatform.grammar {
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires paliplatform.base;
	requires paliplatform.dict;
	exports paliplatform.grammar to 
		paliplatform.sentence,
		paliplatform.reader,
		javafx.graphics;
	opens paliplatform.grammar to javafx.base;
	uses paliplatform.base.ReaderService; 
	uses paliplatform.base.LuceneService;
	provides javafx.css.Styleable with 
		paliplatform.grammar.GrammarMenu;
	provides paliplatform.base.SimpleService with 
		paliplatform.grammar.FontSetter,
		paliplatform.grammar.DeclWinLauncher,
		paliplatform.grammar.ProsodyLauncher;
}
