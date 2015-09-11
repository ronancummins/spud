package org.apache.lucene.search.similarities;



import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.util.BytesRef;

/*
 * Implements the SPUDdir method in "A Polya Urn Document Language Model for Improved Information Retrieval
 * Ronan Cummins, Jiaul Hoque Paik, Yuanhua Lv"
 *
 * A subQuery must be used with this similarity measure to do the document-level normalisation. 
 * It is provided in searching.LMNormQuery
 */
public class SPUDLMSimilarity extends SPUDLMBaseSimilarity {

    private final static Logger logger = Logger.getLogger( SPUDLMSimilarity.class.getName());
    
    //background DCM mass to be estimated (or default)
    public static boolean b0est;
    
    //this is the background mass parameter
    public static double b0;
    
    public static double omega;
    
    
    
    
    public static int c =0;

    public SPUDLMSimilarity(CollectionModel collectionModel, float _omega) {
        super(collectionModel);
        this.omega = _omega;
    }

    public SPUDLMSimilarity(float _omega) {
        this.omega = _omega;
    }

    public SPUDLMSimilarity() {
        omega = 0.8f;
    }    
    
    public SPUDLMSimilarity(CollectionModel collectionModel) {
        this(collectionModel, (float)0.8);
    }
    
    
    

    
  
    
    /** 
     * simScorer.score is where the main scoring formula
     * @param stats
     * @param context
     * @return
     * @throws IOException 
     */
    
    @Override
    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        SPUDLMBaseSimilarity.LMStats SPUDstats = (SPUDLMBaseSimilarity.LMStats) stats;
        return new SPUDLMSimilarity.SPUDLMSimScorer(SPUDstats, context.reader());
    }    
    
    
    @Override
    public long computeNorm(FieldInvertState state){
        return state.getLength();
    }    
    
   
    /** 
     * This does not get called as simScorer is overridden
     * @param stats
     * @param freq
     * @param docLen
     * @return 
     */
    @Override
    protected float score(BasicStats stats, float freq, float docLen) {
        //logger.info("Should Not Get Called");
        return 0;
        
    }
    
   

    @Override
    protected void explain(Explanation expl, BasicStats stats, int doc,
            float freq, float docLen) {
        if (stats.getTotalBoost() != 1.0f) {
            expl.addDetail(new Explanation(stats.getTotalBoost(), "boost"));
        }

        expl.addDetail(new Explanation((float) omega, "omega"));
        Explanation weightExpl = new Explanation();
        weightExpl.setValue((float) Math.log(1 + freq
                / (omega * ((LMStats) stats).getPolyaCollectionProbability())));
        weightExpl.setDescription("term weight");
        expl.addDetail(weightExpl);
        expl.addDetail(new Explanation(
                (float) Math.log(omega / (docLen + omega)), "document norm"));
        super.explain(expl, stats, doc, freq, docLen);
    }

    public double getOmega() {
        return omega;
    }
    
    

    
    

    

    @Override
    public String getName() {
        return String.format(Locale.ROOT, "SPUD (%f)", getOmega());
    }


    
    private class SPUDLMSimScorer extends SimScorer {
        
        private final SPUDLMBaseSimilarity.LMStats stats;
        //private final float weightValue; // boost * idf * (k1 + 1)
        private final LeafReader reader;
        //private final float[] cache;

        SPUDLMSimScorer(SPUDLMBaseSimilarity.LMStats _stats, LeafReader _reader) throws IOException {
            this.stats = _stats;
            //this.weightValue = stats;
            //this.cache = stats.;
            reader = _reader;
        }

        @Override
        public float score(int doc, float freq) {
            
            double score = 0.0f;
            try {
                
                //resolve full doc_id
                //doc = reader.getContext().docBase + doc;
                
                double ptc = stats.getPolyaCollectionProbability();
                double ptmc = stats.getMultCollectionProbability();
                double dl, dvl;
                dl = reader.getNumericDocValues("TotalTerms").get(doc);
                dvl = reader.getNumericDocValues("UniqueTerms").get(doc);
                
                //Zhai and Lafferty dirichlet method (the lucene version does not seem to be correct 
                //so this version can be used here)
                //double mu = 1000;
                //score = (float) ((Math.log(1 + (freq/ (mu * ptmc)))));
            

                //SPUD LM function
                double spud_mu = b0*omega/(1-omega);
                score =  (Math.log(1 + ((freq*dvl)/ (dl*spud_mu * ptc)))); 
                        
                
            } catch (IOException ex) {
                
                Logger.getLogger(SPUDLMSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            return (float)score;
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {
            return null;
        }

        @Override
        public float computeSlopFactor(int distance) {
            return 1.0f;
        }

        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return 1.0f;
        }
        
        
    }
    
    
}
