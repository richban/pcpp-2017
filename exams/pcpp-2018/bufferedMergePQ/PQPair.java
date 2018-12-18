import java.util.function.Function;  

interface PQPair {
    public void createPairParam(Parameters param, Function<Parameters,PQ> creator);
    public PQPair clone();
    public PQ getLeft();
    public PQ getRight();
}
