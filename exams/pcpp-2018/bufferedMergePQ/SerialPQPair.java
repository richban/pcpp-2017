import java.util.function.Function;  

public class SerialPQPair implements PQPair {
    PQ left,right;
    public PQPair clone() {
        return new SerialPQPair();
    }
    public void createPairParam(Parameters param, Function<Parameters,PQ> instanceCreator){
        left  = instanceCreator.apply( param.left() );
        right = instanceCreator.apply( param.right());
    };
    public PQ getLeft() { return left;}
    public PQ getRight(){ return right;}
}
