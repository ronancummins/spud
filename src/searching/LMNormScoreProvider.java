package searching;

import java.io.IOException;
import java.util.logging.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.search.similarities.SPUDLMSimilarity;


/**
 *
 * This provides the correct normalisation for language models that use 
 * Bayesian smoothing (i.e. the SPUD model 
 * and the LM with Dirichlet priors smoothing)
 * 
 * @author ronanc
 */
public class LMNormScoreProvider extends CustomScoreProvider {

    private final static Logger logger = Logger.getLogger(LMNormScoreProvider.class.getName());
    
    private int queryLen;
    
    
    public LMNormScoreProvider(LeafReaderContext context, int _queryLen) {
        super(context);
        queryLen = _queryLen;
    }

    public float customScore(int doc,
            float subQueryScore,
            float[] valSrcScores)
            throws IOException {

        //get the document length
        float dl = this.context.reader().getNumericDocValues("TotalTerms").get(doc);
        float dvl = this.context.reader().getNumericDocValues("UniqueTerms").get(doc);
        
        //SPUD
        double spud_mu = SPUDLMSimilarity.b0*SPUDLMSimilarity.omega/(1-SPUDLMSimilarity.omega);
        float lmnorm = (float) (queryLen * Math.log(spud_mu / (dvl + spud_mu)));

        
        //LM Dirichlet 
        //float mu = 1000f;
        //float lmnorm = (float) (queryLen * Math.log(mu / (dl + mu)));
        
        return (subQueryScore + lmnorm );
        
    }

}
