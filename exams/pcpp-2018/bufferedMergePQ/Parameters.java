
// This specifies how the recursive structure of the data structures 
// there is no need to change this
public class Parameters {
    final int [] input;
    final int left, right, depth, bufLen, cutoff,maxDepth;

    final int m;

    final boolean sortParallel;
    final PQPair pqPair;

    public final String indent;
    
    public Parameters(int[] inp, int l, int r, int d, int bufl, int cut, int maxD, boolean sp, PQPair pqp) {
        input = inp;
        left = l;
        right = r;
        depth = d;
        bufLen = bufl;
        cutoff = cut; 
        maxDepth = maxD;

        sortParallel = sp;
        pqPair = pqp;
        
        m = l + (r - l) / 2;

        // perhaps this could be on demand only, but OK
        indent = new String(new char[depth]).replace("\0", "   ");
    }

    public boolean splitMe() {
        return right-left >= cutoff && depth <= maxDepth;
    }

    public boolean splitChildren() {
        return right-left >= 2*cutoff && depth < maxDepth;
    }

    public Parameters left() {
        return new Parameters(input,left,m,depth+1,bufLen,cutoff,maxDepth, sortParallel,pqPair);
    }
    public Parameters right() {
        return new Parameters(input,m,right,depth+1,bufLen,cutoff,maxDepth, sortParallel,pqPair);
    }

    // Some auxiliary functions that turned out to be useful for debugging

    
    public String toString() {
        return String.format("%s: [%d:%d:%d] (%d/%d|%d) self:%b childr:%b parSort %b pqPair %s",indent,left,m,right,bufLen,cutoff,maxDepth,this.splitMe(),this.splitChildren(),sortParallel,pqPair.getClass());
    }
    public String treepos() {
        //        String indent = new String(new char[depth]).replace("\0", "   ");
        return String.format("[%02d:%02d] ",left,right);
    }
    public String treeposInd() {
        return String.format("%s: [%02d:%02d] ",indent,left,right);
    }
}
        
