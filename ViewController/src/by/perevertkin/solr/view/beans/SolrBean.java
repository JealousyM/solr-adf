package by.perevertkin.solr.view.beans;

import by.perevertkin.solr.model.AppModuleImpl;

import by.perevertkin.solr.model.view.ArticlesViewRowImpl;
import by.perevertkin.solr.view.utils.ADFUtils;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.Date;

import javax.faces.event.ActionEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.binding.OperationBinding;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class SolrBean {

    private static final int RECORDS_COUNT = 100000;
    private RichOutputText tookTime;
    private RichInputText content;
    private static ADFLogger logger = ADFLogger.createADFLogger(SolrBean.class);

    public SolrBean() {
    }

    protected AppModuleImpl getAm() {
        return (AppModuleImpl) ADFUtils.findOperation("getAm").execute();
    }

    //generate data for indexing and searching
    public void generateData(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        for (int i = 0; i < RECORDS_COUNT; i++) {
            ArticlesViewRowImpl row = (ArticlesViewRowImpl) getAm().getArticlesView1().createRow();
            row.setAuthor("Author-" + i);
            row.setTitle("Title-" + i);
            row.setContent("Content-" + i);
            getAm().getArticlesView1().insertRow(row);
        }
        OperationBinding commit = ADFUtils.findOperation("Commit");
        commit.execute();
        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime = dateEnd.getTime() - dateBegin.getTime();
        tookTime.setValue("generateData tookTime:" + searchTime);
    }

    // standart search in mysql db, using ViewCriteria
    public void standartSearch(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        System.out.println("content:" + content.getValue());
        OperationBinding byContentExecute = ADFUtils.findOperation("byContentExecute");
        byContentExecute.getParamsMap().put("contentVar", content.getValue());
        byContentExecute.execute();
        logger.warning("getAm().getArticlesView1() count:" + getAm().getArticlesView1().getEstimatedRowCount());
        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime = dateEnd.getTime() - dateBegin.getTime();
        tookTime.setValue("standartSearch tookTime:" + searchTime + " ms");
    }

    //search data in solr server
    public void solrSearch(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        logger.warning("content:" + content.getValue());
        SolrClient solr;
        String queryString = (String) (content.getValue() != null ? content.getValue() : "*:*");
        logger.warning("queryString:" + queryString);
        try {
            solr = new HttpSolrClient("http://localhost:8983/solr/article_core"); //connect to Solr

            SolrQuery query = new SolrQuery(); // create Query 
            query.setQuery("content:" + queryString);
            query.setRows(RECORDS_COUNT);
            QueryResponse rsp = solr.query(query); //get Resulr
            SolrDocumentList docs = rsp.getResults();

            Date dateEnd = new Date(System.currentTimeMillis());
            long searchTime = dateEnd.getTime() - dateBegin.getTime();
            tookTime.setValue("solrSearch tookTime:" + searchTime + " found docs:" + docs.size());
            for (int i = 0; i < docs.size(); i++) {
                System.out.println(docs.get(i));
            }
        } catch (MalformedURLException e) {
            logger.severe("MalformedURLException:" + e);
        } catch (IOException e) {
            logger.severe("IOException:" + e);
        } catch (SolrServerException e) {
            logger.severe("SolrServerException:" + e);
        }
    }

    //index data to Solr
    public void indexData(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        SolrClient solr;
        try {
            solr = new HttpSolrClient("http://localhost:8983/solr/article_core"); //connect to Solr
            getAm().getArticlesView1().first();
            for (int i = 0; i <= getAm().getArticlesView1().getEstimatedRowCount(); i++) {
                ArticlesViewRowImpl row = (ArticlesViewRowImpl) getAm().getArticlesView1().getCurrentRow();
                SolrInputDocument doc = new SolrInputDocument(); //create SolrInputDocument 
                doc.addField("id", row.getId()); // add values in solr fiels
                doc.addField("title", row.getTitle());
                doc.addField("content", row.getContent());
                solr.add(doc); //add and commint SolrInputDocument
                solr.commit();
                getAm().getArticlesView1().next();
            }
        } catch (MalformedURLException e) {
            logger.severe("MalformedURLException:" + e);
        } catch (IOException e) {
            logger.severe("IOException:" + e);
        } catch (SolrServerException e) {
            logger.severe("SolrServerException:" + e);
        }
        Date dateEnd = new Date(System.currentTimeMillis());
        long indexTime = dateEnd.getTime() - dateBegin.getTime();
        tookTime.setValue("index data tookTime:" + indexTime + " ms");
    }
   
   // setters and getters for binding values
        
    public void setTookTime(RichOutputText tookTime) {
        this.tookTime = tookTime;
    }

    public RichOutputText getTookTime() {
        return tookTime;
    }

    public void setContent(RichInputText content) {
        this.content = content;
    }

    public RichInputText getContent() {
        return content;
    }
}
