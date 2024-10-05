module paliplatform.lucene {
	requires java.sql;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires org.apache.lucene.core;
 	requires org.apache.lucene.analysis.common;
	requires org.apache.lucene.queryparser;
	requires org.apache.lucene.highlighter;
	requires paliplatform.base;
	requires paliplatform.reader;
	opens paliplatform.lucene to javafx.base;
	uses paliplatform.base.SimpleService;
	provides paliplatform.base.LuceneService with 
		paliplatform.lucene.LuceneServiceImp;
	provides javafx.css.Styleable with
		paliplatform.lucene.LuceneMenu,
		paliplatform.lucene.LuceneToolBarCom;
}
