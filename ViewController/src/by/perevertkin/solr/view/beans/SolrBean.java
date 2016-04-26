package by.perevertkin.solr.view.beans;

import by.perevertkin.solr.model.AppModuleImpl;

import by.perevertkin.solr.model.view.ArticlesViewRowImpl;
import by.perevertkin.solr.view.utils.ADFUtils;

import java.net.URL;

import java.util.Date;
import java.util.logging.Level;

import javax.faces.event.ActionEvent;

import oracle.adf.share.perf.Timer;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.binding.OperationBinding;


public class SolrBean {
    
    private static final int RECORDS_COUNT = 100000;
    private RichOutputText tookTime;
    private RichInputText content;

    public SolrBean() {
    }
    
    protected AppModuleImpl getAm() {
        return (AppModuleImpl)ADFUtils.findOperation("getAm").execute();
    }


    public void generateData(ActionEvent actionEvent) {
        for (int i=0; i < RECORDS_COUNT; i++) {
            
           ArticlesViewRowImpl row = (ArticlesViewRowImpl) getAm().getArticlesView1().createRow();
           row.setAuthor("Author-"+i);
           row.setTitle("Title-"+i);
           row.setContent("Content-"+i);
           getAm().getArticlesView1().insertRow(row);
       }
        OperationBinding commit =
                     ADFUtils.findOperation("Commit");
        
        commit.execute();

    }

    public void setTookTime(RichOutputText tookTime) {
        this.tookTime = tookTime;
    }

    public RichOutputText getTookTime() {
        return tookTime;
    }

    public void standartSearch(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        System.out.println("content:"+content.getValue());
             OperationBinding byContentExecute =ADFUtils.findOperation("byContentExecute");       
             byContentExecute.getParamsMap().put("contentVar",content.getValue());
             byContentExecute.execute();
             System.out.println("getAm().getArticlesView1() count:"+getAm().getArticlesView1().getEstimatedRowCount());
        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime=dateEnd.getTime()-dateBegin.getTime();
        System.out.println("Search time:"+searchTime);
        tookTime.setValue("tookTime:"+searchTime);
    }

    public void solrSearch(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        System.out.println("content:"+content.getValue());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime=dateEnd.getTime()-dateBegin.getTime();
        System.out.println("Search time:"+searchTime);
        tookTime.setValue("tookTime:"+searchTime);
    }

    public void setContent(RichInputText content) {
        this.content = content;
    }

    public RichInputText getContent() {
        return content;
    }
}
