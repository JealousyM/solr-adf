package by.perevertkin.solr.model;

import by.perevertkin.solr.model.common.AppModule;
import by.perevertkin.solr.model.view.ArticlesViewImpl;

import oracle.jbo.ApplicationModule;
import oracle.jbo.server.ApplicationModuleImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Tue Apr 26 10:15:12 MSK 2016
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class AppModuleImpl extends ApplicationModuleImpl implements AppModule {
    /**
     * This is the default constructor (do not remove).
     */
    public AppModuleImpl() {
    }
    
    public ApplicationModule getAm() {
           return this;
    }

    /**
     * Container's getter for ArticlesView1.
     * @return ArticlesView1
     */
    public ArticlesViewImpl getArticlesView1() {
        return (ArticlesViewImpl) findViewObject("ArticlesView1");
    }
}

