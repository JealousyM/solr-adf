package by.perevertkin.solr.view.beans;

import by.perevertkin.solr.model.AppModuleImpl;

import by.perevertkin.solr.model.view.ArticlesViewImpl;
import by.perevertkin.solr.model.view.ArticlesViewRowImpl;
import by.perevertkin.solr.view.utils.ADFUtils;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;
import java.util.logging.Level;

import javax.faces.event.ActionEvent;

import oracle.adf.share.perf.Timer;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.binding.OperationBinding;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class SolrBean {

    private static final int RECORDS_COUNT = 100000;
    private RichOutputText tookTime;
    private RichInputText content;

    public SolrBean() {
    }

    protected AppModuleImpl getAm() {
        return (AppModuleImpl) ADFUtils.findOperation("getAm").execute();
    }


    public void generateData(ActionEvent actionEvent) {
        for (int i = 0; i < RECORDS_COUNT; i++) {

            ArticlesViewRowImpl row = (ArticlesViewRowImpl) getAm().getArticlesView1().createRow();
            row.setAuthor("Author-" + i);
            row.setTitle("Title-" + i);
            row.setContent("Content-" + i);
            getAm().getArticlesView1().insertRow(row);
        }
        OperationBinding commit = ADFUtils.findOperation("Commit");

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
        System.out.println("content:" + content.getValue());
        OperationBinding byContentExecute = ADFUtils.findOperation("byContentExecute");
        byContentExecute.getParamsMap().put("contentVar", content.getValue());
        byContentExecute.execute();
        System.out.println("getAm().getArticlesView1() count:" + getAm().getArticlesView1().getEstimatedRowCount());
        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime = dateEnd.getTime() - dateBegin.getTime();
        System.out.println("Search time:" + searchTime);
        tookTime.setValue("tookTime:" + searchTime);
    }

    public void solrSearch(ActionEvent actionEvent) {
        Date dateBegin = new Date(System.currentTimeMillis());
        System.out.println("content:" + content.getValue());
        String queryString = (String) (content.getValue() != null ? content.getValue() : "*:*");
        try {
            SolrServer server = new CommonsHttpSolrServer("http://localhost:8093/solr");
            SolrQuery query = new SolrQuery();
            query.setQuery(queryString);
            query.addSortField("content", SolrQuery.ORDER.asc);
            QueryResponse rsp = server.query( query );
            SolrDocumentList docs = rsp.getResults();
            System.out.println("found documents:"+docs.size());
        } catch (MalformedURLException e) {
        } catch (SolrServerException e) {
        }

        Date dateEnd = new Date(System.currentTimeMillis());
        long searchTime = dateEnd.getTime() - dateBegin.getTime();
        System.out.println("Search time:" + searchTime);
        tookTime.setValue("tookTime:" + searchTime);
    }

    public void setContent(RichInputText content) {
        this.content = content;
    }

    public RichInputText getContent() {
        return content;
    }

    public void indexData(ActionEvent actionEvent) {
        try {
            SolrServer server = new CommonsHttpSolrServer("http://localhost:8093/solr");

            getAm().getArticlesView1().first();
            for (int i = 1; i < getAm().getArticlesView1().getEstimatedRowCount(); i++) {
                ArticlesViewRowImpl row = (ArticlesViewRowImpl) getAm().getArticlesView1().getCurrentRow();
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", row.getId());
                doc.addField("title", row.getTitle());
                doc.addField("category", row.getContent());
                System.out.println("Content Indexing Started for Id " + row.getId());
                server.add(doc);
                server.commit();
                System.out.println("Content Indexing Completed for Id " + row.getId());
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (SolrServerException e) {
        }
    }
}
